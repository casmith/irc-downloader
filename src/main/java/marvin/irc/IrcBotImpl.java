package marvin.irc;

import org.pircbotx.Configuration;
import org.pircbotx.IdentServer;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;

import java.io.IOException;

public class IrcBotImpl implements IrcBot {

    private final boolean useIdent = true;
    private PircBotX bot;

    private Configuration configuration;

    public IrcBotImpl(String server, int port, String nick, String password, String autoJoinChannel) {
        configuration = new Configuration.Builder()
                .addServer(server, port)
                .setName(nick)
                .setRealName(nick)
                .setLogin(nick)
                .setServerPassword(password)
                .addAutoJoinChannel(autoJoinChannel)
                .setIdentServerEnabled(useIdent)
                .buildConfiguration();
    }

    @Override
    public void start() {
        bot = new PircBotX(configuration);
        startIdentServer();
        startIrcBot();
    }

    private void startIrcBot() {
        try {
            bot.startBot();
        } catch (IOException | IrcException e) {
            throw new IrcBotException("Failed to start IRC bot", e);
        }
    }

    private void startIdentServer() {
        try {
            IdentServer.startServer();
        } catch (Exception e) {
            throw new IrcBotException("Failed to start ident server", e);
        }
    }

    @Override
    public void shutdown() {
        bot.send().quitServer();
        try {
            IdentServer.stopServer();
        } catch (IOException e) {
            throw new IrcBotException("Failed to stop ident server", e);
        }
    }
}
