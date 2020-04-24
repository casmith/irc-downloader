package marvin;

import marvin.config.BotConfig;
import marvin.irc.IrcBot;
import marvin.irc.IrcBotImpl;
import marvin.irc.QueueManager;

public class IrcBotFactory {
    public static IrcBot fromConfig(BotConfig config, QueueManager queueManager) {
        return new IrcBotImpl(config.getServer(),
                config.getPort(),
                config.getNick(),
                config.getPassword(),
                config.getAdminPassword(),
                config.getControlChannel(),
                config.getRequestChannel(),
                config.getDownloadDirectory(),
                queueManager);
    }
}
