package marvin.irc;

public interface IrcBot {
    void start();
    void shutdown();

    void sendToChannel(String channel, String message);

    void registerMessageHandler(MessageHandler handler);
}
