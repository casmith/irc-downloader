import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import marvin.IrcBotFactory;
import marvin.ListGrabber;
import marvin.ListServer;
import marvin.handlers.ListGrabberMessageHandler;
import marvin.handlers.ListServerMessageHandler;
import marvin.irc.IrcBot;

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
        registerHandlers();
        start();
    }

    private void registerHandlers() {
        if (isFeatureEnabled("enableListGrab")) {
            bot.registerMessageHandler(new ListGrabberMessageHandler(listGrabber));
        }
        bot.registerMessageHandler(new ListServerMessageHandler(listServer));
    }

    private void start() {
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
