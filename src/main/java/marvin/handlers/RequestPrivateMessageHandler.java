package marvin.handlers;

import marvin.UserManager;
import marvin.irc.IrcBot;
import marvin.irc.PrivateMessageHandler;
import marvin.irc.QueueManager;

public class RequestPrivateMessageHandler implements PrivateMessageHandler {

    private IrcBot ircBot;
    private QueueManager queueManager;
    private UserManager userManager;

    public RequestPrivateMessageHandler(IrcBot ircBot, QueueManager queueManager, UserManager userManager) {
        this.ircBot = ircBot;
        this.queueManager = queueManager;
        this.userManager = userManager;
    }

    @Override
    public void onMessage(String nick, String message) {
        if (!userManager.isAuthorized(nick)) {
            return;
        }
        if (message.startsWith("!")) {
            enqueue(nick, message);
        }
    }

    private void enqueue(String sender, String message) {
        // nick is assumed to be the text between the ! and the first space
        String nick = message.split(" ")[0].substring(1);
        queueManager.enqueue(nick, message);
        ircBot.sendPrivateMessage(sender, "Enqueued '" + message + "'");
    }
}
