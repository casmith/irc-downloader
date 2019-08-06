package marvin.irc.events;

public class DownloadCompleteEvent implements Event {
    private final String fileName;
    private final String nick;

    public DownloadCompleteEvent(String nick, String fileName) {
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
