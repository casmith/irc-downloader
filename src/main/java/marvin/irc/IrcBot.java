package marvin.irc;

import marvin.irc.events.Event;
import marvin.irc.events.Listener;

import java.io.File;

public interface IrcBot {
    void start();
    void shutdown();

    String getNick();

    void sendToChannel(String channel, String message);
    void sendPrivateMessage(String recipient, String message);

    void registerMessageHandler(MessageHandler handler);
    void registerPrivateMessageHandler(PrivateMessageHandler handler);
    void registerNoticeHandler(NoticeHandler handler);

    void messageChannel(String substring, Object... args);
    void messageControlChannel(String substring, Object... args);

    void sendFile(String nick, File file);

    void on(Class<? extends Event> eventClass, Listener listener);

    boolean isNickOnline(String nick);
}
