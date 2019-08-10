import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import marvin.irc.IrcBot;
import marvin.irc.IrcBotImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client {
    private static final Logger LOG = LoggerFactory.getLogger(Client.class);

    public static void main(String[] args) {

        Config config = ConfigFactory.load();

        Config ircConfig = config.getConfig("irc");
        String password = ircConfig.hasPath("password") ?
                ircConfig.getString("password") : null;

        boolean enableListGrab = ircConfig.hasPath("enableListGrab")
                && ircConfig.getBoolean("enableListGrab");

        IrcBot bot = new IrcBotImpl(ircConfig.getString("server"),
                ircConfig.getInt("port"),
                ircConfig.getString("nick"),
                password,
                ircConfig.getString("adminpw"),
                ircConfig.getString("controlChannel"),
                ircConfig.getString("requestChannel"),
                ircConfig.getString("downloadDirectory"));

        if (enableListGrab) {
            ListGrabber listGrabber = new ListGrabber(bot);
            bot.registerMessageHandler((channelName, nick, message) -> {
                if (listGrabber.check(message)) {
                    listGrabber.grab(channelName, message);
                }
            });
        }

        bot.registerMessageHandler((channelName, nick, message) -> {
            if (message.startsWith("@" + ircConfig.getString("nick"))) {
                LOG.warn("List requests are not yet implemented");
            }
        });

        try {
            bot.start();
        } catch (Exception ex) {
            bot.shutdown();
        }
    }
}
