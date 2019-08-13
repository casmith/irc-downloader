package marvin.handlers;

import marvin.irc.IrcBot;
import marvin.irc.PrivateMessageHandler;

/**
 * Allows an authorized user to send direct message to the request channel
 */
public class DirectPrivateMessageHandler implements PrivateMessageHandler {

    private IrcBot ircBot;

    public DirectPrivateMessageHandler(IrcBot ircBot) {
        this.ircBot = ircBot;
    }

    @Override
    public void onMessage(String nick, String message) {
        if (!nick.equals(ircBot.getAuthorizedUser())) {
            return;
        }
        if (message.startsWith("direct")) {
            ircBot.messageChannel(message.substring("direct ".length()));
        }
    }
}
