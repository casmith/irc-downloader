package marvin.irc.events;

public class DownloadStartedEvent implements Event {
    private final String fileName;
    private final String nick;

    public DownloadStartedEvent(String nick, String fileName) {
        this.nick = nick;
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public String getNick() {
        return nick;
    }
}
