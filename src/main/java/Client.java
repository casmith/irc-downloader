import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import marvin.irc.IrcBot;
import marvin.irc.IrcBotImpl;

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
                ircConfig.getString("channel"));
        try {
            bot.start();
        } catch (Exception ex) {
            bot.shutdown();
        }
    }
}
