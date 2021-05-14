package marvin.model;

import java.time.LocalDateTime;

public class QueueEntry {
    private final String name;
    private final String requestString;
    private final String status;
    private final String channel;
    private final String batch;
    private final LocalDateTime timestamp;

    public QueueEntry(String name, String requestString, String batch, String status, String channel, LocalDateTime timestamp) {
        this.name = name;
        this.requestString = requestString;
        this.status = status;
        this.channel = channel;
        this.batch = batch;
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public String getBatch() {
        return batch;
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
