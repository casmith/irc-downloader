package marvin.handlers;

import marvin.ListGrabber;
import marvin.irc.IrcBot;
import marvin.irc.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListGrabberMessageHandler implements MessageHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ListGrabberMessageHandler.class);
    private final IrcBot bot;
    private ListGrabber listGrabber;

    public ListGrabberMessageHandler(ListGrabber listGrabber, IrcBot bot) {
        this.listGrabber = listGrabber;
        this.bot = bot;
    }

    @Override
    public void onMessage(String channelName, String nick, String message, String hostmask) {
        LOG.debug("{} {}", channelName, message);

        if (listGrabber.check(message)) {
            listGrabber.updateLastSeen(nick, message, hostmask);
        }

        if (listGrabber.grab(channelName, message)) {
            bot.messageControlChannel("Requested list from {0}", nick);
        }
    }
}
