package marvin.web.history;

public class HistoryModel {

    private String nick;
    private String filename;
    private long size;
    private long timestamp;
    private boolean success;

    public HistoryModel(String nick, String filename, long size, long timestamp, boolean success) {
        this.nick = nick;
        this.filename = filename;
        this.size = size;
        this.timestamp = timestamp;
        this.success = success;
    }

    public String getNick() {
        return nick;
    }

    public String getFilename() {
        return filename;
    }

    public long getSize() {
        return size;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isSuccess() { return success; }
}
