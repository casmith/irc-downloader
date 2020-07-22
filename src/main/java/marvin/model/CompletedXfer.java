package marvin.model;

import java.time.LocalDateTime;

public class CompletedXfer {
    private final String nick;
    private final String channel;
    private final String file;
    private final Long filesize;
    private final LocalDateTime timestamp;

    public CompletedXfer(String nick, String channel, String file, Long filesize, LocalDateTime timestamp) {
        this.nick = nick;
        this.channel = channel;
        this.file = file;
        this.filesize = filesize;
        this.timestamp = timestamp;
    }

    public String getNick() {
        return nick;
    }

    public String getChannel() {
        return channel;
    }

    public String getFile() {
        return file;
    }

    public Long getFilesize() {
        return filesize;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
