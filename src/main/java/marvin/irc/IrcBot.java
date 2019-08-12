package marvin.irc;

public interface IrcBot {
    void start();
    void shutdown();

    String getNick();

    void sendToChannel(String channel, String message);
    void sendPrivateMessage(String recipient, String message);

    void registerMessageHandler(MessageHandler handler);
    void registerPrivateMessageHandler(PrivateMessageHandler handler);
    void registerNoticeHandler(NoticeHandler handler);

    void messageChannel(String substring);
}
