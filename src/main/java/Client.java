import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import marvin.IrcBotFactory;
import marvin.ListGrabber;
import marvin.ListServer;
import marvin.handlers.ListGrabberMessageHandler;
import marvin.handlers.ListServerMessageHandler;
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
    private String authorizedUser;
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
        bot.registerPrivateMessageHandler((nick, message) -> {
            if (message.startsWith("auth")) {
                String passwordAttempt = message.split(" ")[1];
                if (passwordAttempt.equals(adminPassword)) {
                    authorizedUser = nick;
                    bot.sendPrivateMessage(nick, "Authorized!");
                } else {
                    bot.sendPrivateMessage(nick, "Sorry, try again?");
                }
            } else {
                processMessage(nick, message);
            }
        });

        bot.registerNoticeHandler((nick, message) -> {
            LOG.info("NOTICE {} - {}", nick, message);
            Pattern pattern = Pattern.compile(".*Allowed: ([0-9]+) of ([0-9]+).*");
            Matcher matcher = pattern.matcher(message);
            if (matcher.find()) {
                queueManager.updateLimit(nick, parseInt(matcher.group(2)));
            }
        });
    }

    private void processMessage(String nick, String message) {
        if (nick.equals(authorizedUser)) {
            if (message.startsWith("!")) {
                enqueue(nick, message);
            } else if (message.startsWith("direct")) {
                bot.messageChannel(message.substring("direct ".length()));
            } else if (message.equals("die!")) {
                bot.shutdown();
            }
        }
    }

    private void enqueue(String sender, String message) {
        // nick is assumed to be the text between the ! and the first space
        String nick = message.split(" ")[0].substring(1);
        queueManager.enqueue(nick, message);
        bot.sendPrivateMessage(sender, "Enqueued '" + message + "'");
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
