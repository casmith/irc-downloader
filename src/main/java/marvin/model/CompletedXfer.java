package marvin.model;

import java.time.LocalDateTime;

public class CompletedXfer {
    private String nick;
    private String channel;
    private String file;
    private Long filesize;
    private LocalDateTime timestamp;

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
