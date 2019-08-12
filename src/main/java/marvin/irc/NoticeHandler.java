package marvin.irc;

public interface NoticeHandler {
    void onNotice(String nick, String message);
}
