import marvin.irc.IrcBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sends list to requesting clients
 */
public class ListServer {

    private static Logger LOG = LoggerFactory.getLogger(ListServer.class);

    private IrcBot bot;

    public ListServer(IrcBot bot) {
        this.bot = bot;
    }

    public boolean check(String message) {
        return message.startsWith("@" + bot.getNick());
    }

    public void send(String nick) {
        LOG.warn("List serving is not implemented yet");
    }
}
