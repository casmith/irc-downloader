package marvin.model;

import java.time.LocalDateTime;

public class QueueEntry {
    private String name;
    private String requestString;
    private String status;
    private String channel;
    private LocalDateTime timestamp;

    public QueueEntry(String name, String requestString, String status, String channel, LocalDateTime timestamp) {
        this.name = name;
        this.requestString = requestString;
        this.status = status;
        this.channel = channel;
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public String getRequestString() {
        return requestString;
    }

    public String getStatus() {
        return status;
    }

    public String getChannel() {
        return channel;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
