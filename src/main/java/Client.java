import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import marvin.IrcBotFactory;
import marvin.irc.IrcBot;
import marvin.irc.IrcBotImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Client {
    private final Config config;
    private final Config ircConfig;
    private final IrcBot bot;
    private final ListServer listServer;
    private final ListGrabber listGrabber;

    public static void main(String[] args) {
        new Client().run();
    }

    public Client() {
        this.config = ConfigFactory.load();
        this.ircConfig = config.getConfig("irc");
        this.bot = IrcBotFactory.fromConfig(ircConfig);
        this.listServer = new ListServer(bot);
        this.listGrabber = new ListGrabber(bot);
    }

    public void run() {
        if (isFeatureEnabled("enableListGrab")) {
            bot.registerMessageHandler((channelName, nick, message) -> {
                if (listGrabber.check(message)) {
                    listGrabber.grab(channelName, message);
                }
            });
        }

        bot.registerMessageHandler((channelName, nick, message) -> {
            if (listServer.check(message)) {
                listServer.send(nick);
            }
        });

        try {
            bot.start();
        } catch (Exception ex) {
            bot.shutdown();
        }
    }

    private boolean isFeatureEnabled(String feature) {
        return ircConfig.hasPath(feature)
                && ircConfig.getBoolean(feature);
    }
}
