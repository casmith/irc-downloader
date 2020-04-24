package marvin;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import marvin.config.BotConfig;
import marvin.config.ModuleFactory;
import marvin.data.CompletedXferDao;
import marvin.data.DatabaseException;
import marvin.handlers.*;
import marvin.http.JettyServer;
import marvin.irc.IrcBot;
import marvin.irc.QueueManager;
import marvin.irc.SendQueueManager;
import marvin.irc.events.DownloadCompleteEvent;
import marvin.model.CompletedXfer;
import marvin.util.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.google.common.base.Preconditions.checkArgument;

public class Client {

    private static final Logger LOG = LoggerFactory.getLogger(Client.class);

    private final IrcBot bot;
    private final ListServer listServer;
    private final ListGrabber listGrabber;
    private final QueueManager sendQueueManager;
    private final QueueManager queueManager;
    private final CompletedXferDao completedXferDao;
    private final BotConfig config;
    private UserManager userManager;
    private boolean isRunning;
    private File listRoot;
    private ListGenerator listGenerator;

    public Client() {
        this(null, null, null);
    }

    @Inject
    public Client(BotConfig config,
                  QueueManager queueManager, CompletedXferDao completedXferDao) {
        this.config = config;
        this.queueManager = queueManager;
        this.sendQueueManager = new SendQueueManager();
        this.bot = IrcBotFactory.fromConfig(config, queueManager);
        this.listServer = new ListServer(bot, config.getRequestChannel(), config.getList());
        this.listGrabber = new ListGrabber(bot, "list-manager.dat");
        this.userManager = new UserManager(config.getAdminPassword());
        this.listRoot = new File(config.getListRoot());
        this.listGenerator = new ListGenerator(config.getNick());
        this.completedXferDao = completedXferDao;
    }

    public static void main(String[] args) {

        final BotConfig config = BotConfig.from(getConfigFile());
        final MarvinModule marvinModule = new MarvinModule(config);
        Injector injector = Guice.createInjector(marvinModule);
        ModuleFactory.init(marvinModule);
        injector.getInstance(Client.class).run();
    }

    public void run() {
        // init DAOs
        this.completedXferDao.createTable();
        registerHandlers();
        start();
    }

    private static File getConfigDir() {
        final String configDir = System.getenv("CONFIG_DIR");
        if (configDir != null) {
            try {
                return new File(configDir);
            } catch (Exception e) {
                LOG.warn("CONFIG_DIR was specified but the directory {} could not be found", configDir);
            }
        }
        return getConfigDirFromUserHome();
    }

    public static File getConfigDirFromUserHome() {
        return new File(System.getProperty("user.home") + File.separator + ".marvinbot");
    }

    private static File getConfigFile() {
        return new File(getConfigDir().getAbsolutePath() + File.separator + "application.conf");
    }


    private void registerHandlers() {
        if (config.isFeatureEnabled("listGrab")) {
            LOG.info("List grabbing is enabled");
            bot.registerMessageHandler(new ListGrabberMessageHandler(listGrabber, bot));
        } else {
            LOG.info("List grabbing is disabled");
        }

        if (config.isFeatureEnabled("serve")) {
            LOG.info("File serving is enabled");
            bot.registerMessageHandler(new ListServerMessageHandler(listServer));
            bot.registerMessageHandler(new FileRequestMessageHandler(bot, sendQueueManager, this.config.getRequestChannel(), this.listRoot));
        } else {
            LOG.info("File serving is disabled");
        }

        this.listGenerator.generateList(new File(this.config.getListRoot()));
        this.listGenerator.printStatistics();

        bot.registerPrivateMessageHandler(new AuthPrivateMessageHandler(bot, userManager));
        bot.registerPrivateMessageHandler(new DirectPrivateMessageHandler(bot, userManager));
        bot.registerPrivateMessageHandler(new RequestPrivateMessageHandler(bot, queueManager, userManager));
        bot.registerPrivateMessageHandler(new ShutdownPrivateMessageHandler(bot, userManager));
        bot.registerNoticeHandler(new QueueLimitNoticeHandler(queueManager));

        bot.on(DownloadCompleteEvent.class, event -> {
            DownloadCompleteEvent dce = (DownloadCompleteEvent) event;
            try {
                completedXferDao.insert(
                        new CompletedXfer(dce.getNick(), "", dce.getFileName(), dce.getBytes(), LocalDateTime.now()));
            } catch (DatabaseException e) {
                LOG.error("Error recording completed xfer", e);
            }
        });
    }

    private void start() {

        new Thread(() -> {
            try {
                isRunning = true;
                startReceiveQueueProcessor();
                if (config.isFeatureEnabled("serve")) {
                    startSendQueueProcessor();
                    startAdvertiserProcessor();
                }
                bot.start();

            } catch (Exception ex) {
                bot.shutdown();
                isRunning = false;
            }
        }).start();

        new Thread(() -> {
            JettyServer jettyServer = new JettyServer(8081);
            jettyServer.start();
        }).start();

    }

    private void startAdvertiserProcessor() {
        LOG.info("Starting advertiser");
        new Thread(() -> {
            while (isRunning) {
                if (bot != null) {
                    try {
                        sleep(60);
                        bot.sendToChannel(this.config.getRequestChannel(), getAdvert(bot.getNick(), listGenerator));
                    } catch (Exception e) {
                        LOG.warn("Bot is not fully initialized yet");
                        sleep(5);
                    }
                }
            }
        }).start();
    }


    String getDayOfMonthSuffix(final int n) {
        checkArgument(n >= 1 && n <= 31, "illegal day of month: " + n);
        if (n >= 11 && n <= 13) {
            return "th";
        }
        switch (n % 10) {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
            default:
                return "th";
        }
    }

    public String getAdvert(String nick, ListGenerator listGenerator) {
        return MessageFormat.format("Type: {0} for my list of {1} files ({2} GiB) "
                        + "Updated: {3} == "
                        //                                "Free Slots: 0/10 == " +
                        //                                "Files in Que: 0 == " +
                        //                                "Total Speed: 0cps == " +
                        //                                "Next Open Slot: NOW == " +
                        //                                "Files served: 0 == " +
                        + "Using MarvinBot v0.01",
                nick,
                listGenerator.getCount(),
                NumberUtils.format(((double) listGenerator.getBytes()) / NumberUtils.GIGABYTE),
                formatDate(listGenerator.getGeneratedDateTime()));
    }


    private String formatDate(LocalDateTime localDate) {
        final String strDay = DateTimeFormatter.ofPattern("d").format(localDate);
        final int day = Integer.parseInt(strDay);
        final String suffix = getDayOfMonthSuffix(day);
        return DateTimeFormatter.ofPattern("MMM").format(localDate) + " " + strDay + suffix;
    }

    private void startReceiveQueueProcessor() {
        LOG.info("Starting receive queue processor");
        new Thread(() -> {
            while (isRunning) {
                queueManager.getQueues().forEach((nick, queue) -> {
                    if (!queue.isEmpty()) {
                        if (queueManager.inc(nick)) {
                            String message = queue.poll();
                            LOG.info("Requesting: {}", message);
                            bot.sendToChannel(this.config.getRequestChannel(), message);
                            queueManager.addInProgress(nick, message);
                        }
                    }
                });
                sleep(1);
            }
        }).start();
    }

    private void startSendQueueProcessor() {
        LOG.info("Starting send queue processor");
        new Thread(() -> {
            while (isRunning) {
                sendQueueManager.getQueues().forEach((nick, queue) -> {
                    if (!queue.isEmpty()) {
                        if (sendQueueManager.inc(nick)) {
                            String file = queue.poll();
                            if (file != null) {
                                try {
                                    LOG.info("Sending {} to {}", file, nick);
                                    bot.sendFile(nick, new File(file));
                                } catch (Exception e) {
                                    LOG.error("Error sending file {} to {}: {}", file, nick, e.getMessage());
                                } finally {
                                    sendQueueManager.dec(nick);
                                }
                            }
                        }
                    }
                });
                sleep(1);
            }
        }).start();
    }

    private void sleep(int seconds) {
        try {
            Thread.sleep(1000 * seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
