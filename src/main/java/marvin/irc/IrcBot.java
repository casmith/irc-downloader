package marvin.irc;

public interface IrcBot {
    void start();
    void shutdown();

    String getNick();

    void sendToChannel(String channel, String message);

    void registerMessageHandler(MessageHandler handler);
}
