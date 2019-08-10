package marvin.irc;

public interface MessageHandler {
    void onMessage(String channelName, String nick, String message);
}
