import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import marvin.irc.IrcBot;
import marvin.irc.IrcBotImpl;

/*
 * Example list string:
 * Type: @Key-Lay for my list of 3189 files (16.2GB) Updated: Aug 2nd == Free Slots: 6/6 == Files in Que: 0 == Total Speed: 0cps == Next Open Slot: NOW == Files served: 471825 == Using SDFind v3.99e
 */

public class Client {
    public static void main(String[] args) {

        Config config = ConfigFactory.load();

        Config ircConfig = config.getConfig("irc");
        String password = ircConfig.hasPath("password") ?
                ircConfig.getString("password") : null;

        IrcBot bot = new IrcBotImpl(ircConfig.getString("server"),
                ircConfig.getInt("port"),
                ircConfig.getString("nick"),
                password,
                ircConfig.getString("channel"),
                ircConfig.getString("adminpw"),
                ircConfig.getString("requestChannel"),
                ircConfig.getString("downloadDirectory"));
        try {
            bot.start();
        } catch (Exception ex) {
            bot.shutdown();
        }
    }
}
