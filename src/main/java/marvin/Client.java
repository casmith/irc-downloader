package marvin;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import marvin.handlers.*;
import marvin.irc.IrcBot;
import marvin.irc.QueueManager;
import marvin.irc.ReceiveQueueManager;
import marvin.irc.SendQueueManager;
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

    private final Config ircConfig;
    private final IrcBot bot;
    private final ListServer listServer;
    private final ListGrabber listGrabber;
    private final String requestChannel;
    private final String list;
    private final Config config;
    private final QueueManager sendQueueManager;
    private final QueueManager queueManager;
    private UserManager userManager;
    private boolean isRunning;
    private File listRoot;
    private ListGenerator listGenerator;

    public static void main(String[] args) {
        new Client().run();
    }

    public Client() {
        Config config = ConfigFactory.load();
        this.queueManager = new ReceiveQueueManager();
        this.sendQueueManager = new SendQueueManager();
        this.config = config;
        this.ircConfig = config.getConfig("irc");
        this.bot = IrcBotFactory.fromConfig(ircConfig, queueManager);
        this.list = ircConfig.getString("list");
        this.requestChannel = ircConfig.getString("requestChannel");
        this.listServer = new ListServer(bot, this.requestChannel, this.list);
        this.listGrabber = new ListGrabber(bot, "list-manager.dat");
        this.userManager = new UserManager(this.ircConfig.getString("adminpw"));
        this.listRoot = new File(this.ircConfig.getString("listRoot"));
        this.listGenerator = new ListGenerator(this.ircConfig.getString("nick"));
    }

    public void run() {
        registerHandlers();
        start();
    }

    private void registerHandlers() {
        if (isFeatureEnabled("listGrab")) {
            LOG.info("List grabbing is enabled");
            bot.registerMessageHandler(new ListGrabberMessageHandler(listGrabber, bot));
        } else {
            LOG.info("List grabbing is disabled");
        }

        if (this.isFeatureEnabled("serve")) {
            LOG.info("File serving is enabled");
            bot.registerMessageHandler(new ListServerMessageHandler(listServer));
            bot.registerMessageHandler(new FileRequestMessageHandler(bot, sendQueueManager, requestChannel, this.listRoot));
        } else {
            LOG.info("File serving is disabled");
        }

        this.listGenerator.generateList(new File(ircConfig.getString("listRoot")));
        this.listGenerator.printStatistics();

        bot.registerPrivateMessageHandler(new AuthPrivateMessageHandler(bot, userManager));
        bot.registerPrivateMessageHandler(new DirectPrivateMessageHandler(bot, userManager));
        bot.registerPrivateMessageHandler(new RequestPrivateMessageHandler(bot, queueManager, userManager));
        bot.registerPrivateMessageHandler(new ShutdownPrivateMessageHandler(bot, userManager));
        bot.registerNoticeHandler(new QueueLimitNoticeHandler(queueManager));
    }

    private void start() {
        try {
            isRunning = true;
            startReceiveQueueProcessor();
            if (this.isFeatureEnabled("serve")) {
                startSendQueueProcessor();
                startAdvertiserProcessor();
            }
            bot.start();
        } catch (Exception ex) {
            bot.shutdown();
            isRunning = false;
        }
    }

    private void startAdvertiserProcessor() {
        LOG.info("Starting advertiser");
        new Thread(() -> {
            while (isRunning) {
                if (bot != null) {
                    try {
                        bot.sendToChannel(this.requestChannel, getAdvert(bot.getNick(), listGenerator));
                        sleep(60);
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


        final double GIGABYTE = 1024.0 * 1024 * 1024;
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
                NumberUtils.format(((double)listGenerator.getBytes()) / NumberUtils.GIGABYTE),
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
                            bot.sendToChannel(requestChannel, message);
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
                                    bot.sendToChannel(this.requestChannel, "Sending " + file + " to " + nick);
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

    private boolean isFeatureEnabled(String feature) {
        String key = "features." + feature;
        return config.hasPath(key) && config.getBoolean(key);
    }
}
