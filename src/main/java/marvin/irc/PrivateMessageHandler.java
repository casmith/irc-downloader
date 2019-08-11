package marvin.irc;

public interface PrivateMessageHandler {
    void onMessage(String nick, String message);
}
