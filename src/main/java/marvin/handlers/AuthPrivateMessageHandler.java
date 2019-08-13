package marvin.handlers;

import marvin.irc.IrcBot;
import marvin.irc.PrivateMessageHandler;

public class AuthPrivateMessageHandler implements PrivateMessageHandler {

    private String adminPassword;
    private IrcBot bot;

    public AuthPrivateMessageHandler(String adminPassword, IrcBot bot) {
        this.adminPassword = adminPassword;
        this.bot = bot;
    }

    @Override
    public void onMessage(String nick, String message) {
        if (message.startsWith("auth")) {
            String passwordAttempt = message.split(" ")[1];
            if (passwordAttempt.equals(adminPassword)) {
                bot.setAuthorizedUser(nick);
                bot.sendPrivateMessage(nick, "Authorized!");
            } else {
                bot.sendPrivateMessage(nick, "Sorry, try again?");
            }
        }
    }
}
