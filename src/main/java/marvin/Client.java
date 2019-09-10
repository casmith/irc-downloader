package marvin;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import marvin.handlers.*;
import marvin.irc.IrcBot;
import marvin.irc.QueueManager;
import marvin.irc.ReceiveQueueManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Client {

    private static final Logger LOG = LoggerFactory.getLogger(Client.class);

    private final Config ircConfig;
    private final IrcBot bot;
    private final ListServer listServer;
    private final ListGrabber listGrabber;
    private final String requestChannel;
    private final String list;
    private final Config config;
    private QueueManager queueManager;
    private UserManager userManager;
    private boolean isRunning;
    private File listRoot;

    public static void main(String[] args) {
        new Client().run();
    }

    public Client() {
        Config config = ConfigFactory.load();
        this.queueManager = new ReceiveQueueManager();
        this.config = config;
        this.ircConfig = config.getConfig("irc");
        this.bot = IrcBotFactory.fromConfig(ircConfig, queueManager);
        this.list = ircConfig.getString("list");
        this.requestChannel = ircConfig.getString("requestChannel");
        this.listServer = new ListServer(bot, this.requestChannel, this.list);
        this.listGrabber = new ListGrabber(bot, "list-manager.dat");
        this.userManager = new UserManager(this.ircConfig.getString("adminpw"));
        this.listRoot = new File(this.ircConfig.getString("listRoot"));
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
            bot.registerMessageHandler(new FileRequestMessageHandler(bot, requestChannel, this.listRoot));
        } else {
            LOG.info("File serving is disabled");
        }

        bot.registerPrivateMessageHandler(new AuthPrivateMessageHandler(bot, userManager));
        bot.registerPrivateMessageHandler(new DirectPrivateMessageHandler(bot, userManager));
        bot.registerPrivateMessageHandler(new RequestPrivateMessageHandler(bot, queueManager, userManager));
        bot.registerPrivateMessageHandler(new ShutdownPrivateMessageHandler(bot, userManager));
        bot.registerNoticeHandler(new QueueLimitNoticeHandler(queueManager));
    }

    private void start() {
        try {
            isRunning = true;
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
                    sleep(10);
                }
            }).start();
            bot.start();
        } catch (Exception ex) {
            bot.shutdown();
            isRunning = false;
        }
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
