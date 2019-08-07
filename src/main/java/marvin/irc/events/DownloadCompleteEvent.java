package marvin.irc.events;

public class DownloadCompleteEvent implements Event {
    private final String fileName;
    private final String nick;
    private final boolean success;

    public DownloadCompleteEvent(String nick, String fileName, boolean success) {
        this.nick = nick;
        this.fileName = fileName;
        this.success = success;
    }

    public String getFileName() {
        return fileName;
    }

    public String getNick() {
        return nick;
    }

    public boolean isSuccess() {
        return success;
    }
}
