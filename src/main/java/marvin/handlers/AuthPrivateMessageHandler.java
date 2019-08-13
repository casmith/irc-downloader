package marvin.handlers;

import marvin.UserManager;
import marvin.irc.IrcBot;
import marvin.irc.PrivateMessageHandler;

public class AuthPrivateMessageHandler implements PrivateMessageHandler {

    private IrcBot bot;
    private UserManager userManager;

    public AuthPrivateMessageHandler(IrcBot bot, UserManager userManager) {
        this.bot = bot;
        this.userManager = userManager;
    }

    @Override
    public void onMessage(String nick, String message) {
        if (message.startsWith("auth")) {
            String passwordAttempt = message.split(" ")[1];
            String response = userManager.authenticate(nick, passwordAttempt) ? "Authorized!" : "Sorry, try again?";
            bot.sendPrivateMessage(nick, response);
        }
    }
}
