package marvin.web;

public class HistoryModel {

    private String nick;
    private String filename;
    private long size;
    private long timestamp;

    public HistoryModel(String nick, String filename, long size, long timestamp) {
        this.nick = nick;
        this.filename = filename;
        this.size = size;
        this.timestamp = timestamp;
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
}
