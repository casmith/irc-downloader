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

/*
 * Example list string:
 * Type: @Key-Lay for my list of 3189 files (16.2GB) Updated: Aug 2nd == Free Slots: 6/6 == Files in Que: 0 == Total Speed: 0cps == Next Open Slot: NOW == Files served: 471825 == Using SDFind v3.99e
 */

public class Client {
    private static final Logger LOG = LoggerFactory.getLogger(Client.class);

    public static void main(String[] args) {

        Config config = ConfigFactory.load();

        Config ircConfig = config.getConfig("irc");
        String password = ircConfig.hasPath("password") ?
                ircConfig.getString("password") : null;

        boolean enableListGrag = ircConfig.hasPath("enableListGrab")
                && ircConfig.getBoolean("enableListGrab");

        IrcBot bot = new IrcBotImpl(ircConfig.getString("server"),
                ircConfig.getInt("port"),
                ircConfig.getString("nick"),
                password,
                ircConfig.getString("channel"),
                ircConfig.getString("adminpw"),
                ircConfig.getString("requestChannel"),
                ircConfig.getString("downloadDirectory"));

        Set<String> lists = new HashSet<>();

        bot.registerMessageHandler((channelName, nick, message) -> {
            if (message.startsWith("@" + ircConfig.getString("nick"))) {
                LOG.warn("List requests are not yet implemented");
            }

            if (enableListGrag) {
                handleListGrab(bot, lists, channelName, message);
            }

            return true;
        });

        try {
            bot.start();
        } catch (Exception ex) {
            bot.shutdown();
        }
    }

    private static void handleListGrab(IrcBot bot, Set<String> lists, String channelName, String message) {
        Pattern pattern = Pattern.compile("Type: (@.*) for my list");
        Matcher matcher = pattern.matcher(message);
        if (matcher.matches()) {
            String listQuery = matcher.group(1);
            if (lists.add(listQuery)) {
                bot.sendToChannel(channelName, listQuery);
            }
        }
    }
}
