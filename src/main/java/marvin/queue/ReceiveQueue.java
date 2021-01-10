package marvin.queue;

import java.util.*;

public class ReceiveQueue {
    private final String nick;
    private final Map<UUID, ReceiveQueueItem> items = new LinkedHashMap<>();

    public ReceiveQueue(String nick) {
        this.nick = nick;
    }

    public ReceiveQueueItem enqueue(String filename) {
        return this.find(filename)
            .orElseGet(() -> enqueueInternal(filename));
    }

    private ReceiveQueueItem enqueueInternal(String filename) {
        return this.put(new ReceiveQueueItem(UUID.randomUUID(), this.nick, filename));
    }

    private ReceiveQueueItem put(ReceiveQueueItem item) {
        this.items.put(item.getUuid(), item);
        return item;
    }

    public Optional<ReceiveQueueItem> find(String filename) {
        return this.items.values().stream()
            .filter(i -> i.getFilename().equals(filename)).findFirst();
    }

    public ReceiveQueueItem dequeue() {
        if (this.isEmpty()) throw new EmptyQueueException();
        return this.next();
    }

    private ReceiveQueueItem next() {
        return this.items.values().iterator().next();
    }

    public long size() {
        return this.items.size();
    }

    public boolean isEmpty() {
        return this.items.isEmpty();
    }

    public static class ReceiveQueueItem {
        private final UUID uuid;
        private final String filename;
        private final String nick;
        private final QueueStatus status;

        public ReceiveQueueItem(UUID uuid, String nick,  String filename) {
            this.uuid = uuid;
            this.nick = nick;
            this.filename = filename;
            this.status = QueueStatus.PENDING;
        }

        public UUID getUuid() {
            return uuid;
        }

        public String getFilename() {
            return filename;
        }

        public String getNick() {
            return nick;
        }

        public QueueStatus getStatus() {
            return status;
        }
    }
}
