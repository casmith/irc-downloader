package marvin.handlers;

import marvin.irc.IrcBot;
import marvin.irc.PrivateMessageHandler;

public class ShutdownPrivateMessageHandler implements PrivateMessageHandler {

    private IrcBot ircBot;

    public ShutdownPrivateMessageHandler(IrcBot ircBot) {
        this.ircBot = ircBot;
    }

    @Override
    public void onMessage(String nick, String message) {
        if (!nick.equals(ircBot.getAuthorizedUser())) {
            return;
        }
        if (message.equals("die!")) {
            ircBot.shutdown();
        }
    }
}
