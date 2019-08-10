package marvin.irc;

public interface MessageHandler {
    boolean onMessage(String channelName, String nick, String message);
}
