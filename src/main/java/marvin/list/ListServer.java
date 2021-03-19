package marvin.list;

import marvin.irc.IrcBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Sends list to requesting clients
 */
public class ListServer {

    private static final Logger LOG = LoggerFactory.getLogger(ListServer.class);
    private final String requestChannel;
    private final String list;

    private IrcBot bot;
    public ListServer(IrcBot bot, String requestChannel, String list) {
        this.bot = bot;
        this.requestChannel = requestChannel;
        this.list = list;
    }

    public boolean check(String channelName, String message) {
        String botNick = this.bot.getNick();
        String listKeyword = "@" + botNick;
        LOG.debug("{} {} {} {}", channelName, this.requestChannel, listKeyword, message);
        return channelName.equals(this.requestChannel) && message.trim().equals(listKeyword);
    }

    public void send(String nick) {
        LOG.info("Sending list to {}", nick);
        bot.sendFile(nick, new File(this.list));
    }
}
