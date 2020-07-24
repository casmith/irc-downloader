package marvin.irc.events;

public class DownloadCompleteEvent implements Event {
    private final String fileName;
    private final String nick;
    private final boolean success;
    private final long duration;
    private final long bytes;

    public DownloadCompleteEvent(String nick, String fileName, long bytes, long duration, boolean success) {
        this.nick = nick;
        this.fileName = fileName;
        this.success = success;
        this.duration = duration;
        this.bytes = bytes;
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

    public long getDuration() {
        return duration;
    }

    public long getBytes() {
        return bytes;
    }

    @Override
    public String toString() {
        return "DownloadCompleteEvent{" +
            "fileName='" + fileName + '\'' +
            ", nick='" + nick + '\'' +
            ", success=" + success +
            ", duration=" + duration +
            ", bytes=" + bytes +
            '}';
    }
}
