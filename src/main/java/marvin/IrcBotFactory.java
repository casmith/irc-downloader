package marvin;

import com.typesafe.config.Config;
import marvin.irc.IrcBot;
import marvin.irc.IrcBotImpl;

public class IrcBotFactory {
    public static IrcBot fromConfig(Config config) {
        String password = config.hasPath("password") ?
                config.getString("password") : null;
        return new IrcBotImpl(config.getString("server"),
                config.getInt("port"),
                config.getString("nick"),
                password,
                config.getString("adminpw"),
                config.getString("controlChannel"),
                config.getString("requestChannel"),
                config.getString("downloadDirectory"));
    }
}
