package marvin.handlers;

import marvin.UserManager;
import marvin.irc.IrcBot;
import marvin.irc.PrivateMessageHandler;

/**
 * Allows an authorized user to send direct message to the request channel
 */
public class DirectPrivateMessageHandler implements PrivateMessageHandler {

    private IrcBot ircBot;
    private UserManager userManager;

    public DirectPrivateMessageHandler(IrcBot ircBot, UserManager userManager) {
        this.ircBot = ircBot;
        this.userManager = userManager;
    }

    @Override
    public void onMessage(String nick, String message) {
        if (!userManager.isAuthorized(nick)) {
            return;
        }
        if (message.startsWith("direct")) {
            ircBot.messageChannel(message.substring("direct ".length()));
        }
    }
}
