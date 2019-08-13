import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import marvin.IrcBotFactory;
import marvin.ListGrabber;
import marvin.ListServer;
import marvin.handlers.*;
import marvin.irc.*;
import org.pircbotx.User;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;

public class Client {

    private static final Logger LOG = LoggerFactory.getLogger(Client.class);

    private final Config config;
    private final Config ircConfig;
    private final IrcBot bot;
    private final ListServer listServer;
    private final ListGrabber listGrabber;
    private final String adminPassword;
    private final String requestChannel;
    private QueueManager queueManager = new QueueManager();
    private boolean isRunning;

    public static void main(String[] args) {
        new Client().run();
    }

    public Client() {
        this.config = ConfigFactory.load();
        this.ircConfig = config.getConfig("irc");
        this.adminPassword = this.ircConfig.getString("adminpw");
        this.bot = IrcBotFactory.fromConfig(ircConfig, queueManager);
        this.listServer = new ListServer(bot);
        this.listGrabber = new ListGrabber(bot);
        this.requestChannel = ircConfig.getString("requestChannel");
    }

    public void run() {
        registerHandlers();
        start();
    }

    private void registerHandlers() {
        if (isFeatureEnabled("enableListGrab")) {
            bot.registerMessageHandler(new ListGrabberMessageHandler(listGrabber));
        }
        bot.registerMessageHandler(new ListServerMessageHandler(listServer));
        bot.registerPrivateMessageHandler(new AuthPrivateMessageHandler(adminPassword, bot));
        bot.registerPrivateMessageHandler(new DirectPrivateMessageHandler(bot));
        bot.registerPrivateMessageHandler(new RequestPrivateMessageHandler(bot, queueManager));
        bot.registerPrivateMessageHandler(new ShutdownPrivateMessageHandler(bot));
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
        return ircConfig.hasPath(feature)
                && ircConfig.getBoolean(feature);
    }
}
