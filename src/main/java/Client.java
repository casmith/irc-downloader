import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import marvin.IrcBotFactory;
import marvin.ListGrabber;
import marvin.ListServer;
import marvin.UserManager;
import marvin.handlers.*;
import marvin.irc.IrcBot;
import marvin.irc.QueueManager;
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

    public static void main(String[] args) {
        new Client().run();
    }

    public Client() {
        Config config = ConfigFactory.load();
        this.queueManager = new QueueManager();
        this.config = config;
        this.ircConfig = config.getConfig("irc");
        this.bot = IrcBotFactory.fromConfig(ircConfig, queueManager);
        this.list = ircConfig.getString("list");
        this.requestChannel = ircConfig.getString("requestChannel");
        this.listServer = new ListServer(bot, this.requestChannel, this.list);
        this.listGrabber = new ListGrabber(bot, "list-manager.dat");
        this.userManager = new UserManager(this.ircConfig.getString("adminpw"));
    }

    public void run() {
        registerHandlers();
        start();
    }

    private void registerHandlers() {
        if (isFeatureEnabled("listGrab")) {
            LOG.info("List grabbing is enabled");
            bot.registerMessageHandler(new ListGrabberMessageHandler(listGrabber, bot));
        }

        if (this.isFeatureEnabled("serve")) {
            LOG.info("List serving is enabled");
            bot.registerMessageHandler(new ListServerMessageHandler(listServer));
        } else {
            LOG.info("List serving is disabled");
        }

        if (this.isFeatureEnabled("serve")) {
            bot.registerMessageHandler((channelName, nick, message) -> {
                // request file
                String botNick = this.bot.getNick();
                String requestPrefix = "!" + botNick;
                if (channelName.equals(this.requestChannel) && message.startsWith(requestPrefix)) {
                    String fileName = message.replace(requestPrefix, "").trim();
                    bot.sendToChannel(this.requestChannel, "Sending " + fileName + " to " + nick);
                    File file = new File(fileName);
                    // TODO: ensure the file is w/in the music directory, or this could be super dangerous!
                    if (file.exists() && !file.isDirectory()) {
                        bot.sendFile(nick, file);
                    }
                }
            });
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
