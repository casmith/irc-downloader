package marvin.handlers;

import marvin.UserManager;
import marvin.irc.IrcBot;
import marvin.irc.PrivateMessageHandler;

public class ShutdownPrivateMessageHandler implements PrivateMessageHandler {

    private IrcBot ircBot;
    private UserManager userManager;

    public ShutdownPrivateMessageHandler(IrcBot ircBot, UserManager userManager) {
        this.ircBot = ircBot;
        this.userManager = userManager;
    }

    @Override
    public void onMessage(String nick, String message) {
        if (!userManager.isAuthorized(nick)) {
            return;
        }
        if (message.equals("die!")) {
            ircBot.shutdown();
        }
    }
}
