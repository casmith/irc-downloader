package marvin;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import marvin.config.BotConfig;
import marvin.config.ModuleFactory;
import marvin.data.CompletedXferDao;
import marvin.data.KnownUserDao;
import marvin.handlers.*;
import marvin.irc.*;
import marvin.irc.events.DownloadCompleteEvent;
import marvin.web.JettyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Client {

    private static final Logger LOG = LoggerFactory.getLogger(Client.class);

    private final IrcBot bot;
    private final ListServer listServer;
    private final ListGrabber listGrabber;
    private final QueueManager sendQueueManager;
    private final QueueManager queueManager;
    private final CompletedXferDao completedXferDao;
    private final BotConfig config;
    private final Advertiser advertiser;
    private final ReceiveQueueProcessor receiveQueueProcessor;
    private final SendQueueProcessor sendQueueProcessor;
    private final KnownUserDao knownUserDao;
    private UserManager userManager;
    private boolean isRunning;
    private ListGenerator listGenerator;

    public Client() {
        this(null, null, null, null, null, null,null);
    }

    @Inject
    public Client(BotConfig config,
                  QueueManager queueManager,
                  CompletedXferDao completedXferDao,
                  KnownUserDao knownUserDao,
                  IrcBot bot,
                  ListGenerator listGenerator,
                  UserManager userManager) {
        this.config = config;
        this.queueManager = queueManager;
        this.sendQueueManager = new SendQueueManager();
        this.bot = bot;
        this.listServer = new ListServer(bot, config.getRequestChannel(), config.getList());
        final boolean isListGrabbingEnabled = config.isFeatureEnabled("listGrab");
        LOG.info("List grabbing is " + (isListGrabbingEnabled ? "enabled" : "disabled"));
        this.listGrabber = new ListGrabber(bot, knownUserDao, "list-manager.dat", isListGrabbingEnabled);
        this.userManager = userManager;
        this.listGenerator = listGenerator;
        this.completedXferDao = completedXferDao;
        this.knownUserDao = knownUserDao;
        this.advertiser = new Advertiser(bot, config, listGenerator);
        this.receiveQueueProcessor = new ReceiveQueueProcessor(bot, config, queueManager);
        this.sendQueueProcessor = new SendQueueProcessor(bot, sendQueueManager);
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
        this.knownUserDao.createTable();
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
        bot.registerMessageHandler(new ListGrabberMessageHandler(listGrabber, bot));
        if (config.isFeatureEnabled("serve")) {
            LOG.info("File serving is enabled");
            bot.registerMessageHandler(new ListServerMessageHandler(listServer));
            bot.registerMessageHandler(new FileRequestMessageHandler(bot, sendQueueManager, this.config.getRequestChannel(), new File(this.config.getListRoot())));
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
        bot.on(DownloadCompleteEvent.class, new CompletedXferListener(completedXferDao));
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
                        this.advertiser.advertise();
                    } catch (Exception e) {
                        LOG.warn("Bot is not fully initialized yet");
                        sleep(5);
                    }
                }
            }
        }).start();
    }

    private void startReceiveQueueProcessor() {
        LOG.info("Starting receive queue processor");
        new Thread(() -> {
            while (isRunning) {
                this.receiveQueueProcessor.process();
                sleep(1);
            }
        }).start();
    }

    private void startSendQueueProcessor() {
        LOG.info("Starting send queue processor");
        new Thread(() -> {
            while (isRunning) {
                this.sendQueueProcessor.process();
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
