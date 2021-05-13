package marvin.irc;

import com.google.inject.Singleton;
import marvin.queue.QueueStatus;
import marvin.queue.ReceiveQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class ReceiveQueueManager {

    private static final Logger LOG = LoggerFactory.getLogger(ReceiveQueueManager.class);
    private final Map<String, Integer> limits = new HashMap<>();
    private final Map<String, Integer> queued = new HashMap<>();
    private final Map<String, ReceiveQueue> receiveQueues = new HashMap<>();

    public void addInProgress(String nick, String message) {
        synchronized (this) {
            getQueue(nick).find(message).ifPresent(r -> r.setStatus(QueueStatus.REQUESTED));
        }
    }

    public void retry(String nick, String filename) {
        synchronized (this) {
            List<ReceiveQueue.ReceiveQueueItem> foundItems = getQueue(nick)
                .getItems().stream()
                .filter(i -> i.getFilename().toLowerCase().contains(filename.toLowerCase()))
                .collect(Collectors.toList());

            if (foundItems.size() > 1) {
                LOG.warn("Found " + foundItems.size() + " items to retry for " + nick + ":" + filename);
            } else {
                LOG.info("Retrying !" + nick + " " + filename);
            }

            foundItems.forEach(i -> i.setStatus(QueueStatus.PENDING));
        }
    }

    // TODO: remove
    public Map<String, Queue<String>> getInProgress() {
        synchronized (this) {
            LinkedHashMap<String, Queue<String>> map = new LinkedHashMap<>();
            for (Map.Entry<String, ReceiveQueue> entry : receiveQueues.entrySet()) {
                Queue<String> queue = new LinkedList<>();
                for (ReceiveQueue.ReceiveQueueItem item : entry.getValue().getItems()) {
                    if (item.getStatus() == QueueStatus.REQUESTED) {
                        queue.offer(item.getFilename());
                    }
                }
                map.put(entry.getKey(), queue);
            }

            return map;
        }
    }

    public boolean markCompleted(String nick, String filename) {
        synchronized (this) {
            LOG.info("Marking {} - {} completed", nick, filename);
            ReceiveQueue queue = getQueue(nick);
            Optional<ReceiveQueue.ReceiveQueueItem> found = queue.getItems().stream()
                .filter(i -> i.getFilename().contains(filename))
                .findFirst();

            Boolean wasDeleted = found.map(queue::removeItem)
                .orElse(false);

            if (!wasDeleted) {
                LOG.error("Failed to remove {} from [{}]", filename,
                    queue.getItems().stream().map(ReceiveQueue.ReceiveQueueItem::getFilename).collect(Collectors.joining(", ")));
            }

            if (queue.isEmpty()) {
                LOG.info("Removing empty queue [{}]", nick);
                receiveQueues.remove(nick);
            }
            return wasDeleted;
        }
    }

    public Optional<String> poll(String nick) {
        synchronized (this) {
            ReceiveQueue queue = getQueue(nick);

            // dump queue to log
            if (LOG.isDebugEnabled()) {
                LOG.debug("Queue for [{}]", nick);
                queue.getItems().stream()
                    .map(ReceiveQueue.ReceiveQueueItem::toString)
                    .forEach(s -> LOG.debug("- {}", s));
            }

            if (!queue.isEmpty() && this.inc(nick)) {
                return queue.poll().map(ReceiveQueue.ReceiveQueueItem::getFilename);
            } else {
                return Optional.empty();
            }
        }
    }

    // TODO: remove
    public boolean dec(String nick) {
        synchronized (this) {
            nick = nick.toLowerCase();
            boolean success = false;
            Integer current = getCurrent(nick);
            if (current > 0) {
                queued.put(nick, --current);
                success = true;
                printStats(nick);
            }
            return success;
        }
    }

    // TODO: remove
    public boolean inc(String nick) {
        synchronized (this) {
            nick = nick.toLowerCase();
            boolean success = false;
            Integer limit = getLimit(nick);
            Integer current = getCurrent(nick);
            if (current < limit) {
                queued.put(nick, ++current);
                success = true;
                printStats(nick);
            }
            return success;
        }
    }

    public ReceiveQueue getQueue(String nick) {
        return receiveQueues.computeIfAbsent(nick, s -> new ReceiveQueue(nick));
    }

    public void updateLimit(String nick, int limit) {
        // artificially cap at 10 for testing
        if (limit > 10) {
            limit = 10;
        }
        synchronized (this) {
            nick = nick.toLowerCase();
            limits.put(nick, limit);
        }
    }

    public void enqueue(String nick, String message) {
        synchronized (this) {
            ReceiveQueue queue = getQueue(nick);
            LOG.info("Enqueueing [{}] - [{}], count={}", nick, message, queue.size());
            queue.enqueue(message);
        }
    }

    public Map<String, ReceiveQueue> getQueues() {
        return receiveQueues;
    }

    private Integer getCurrent(String nick) {
        return queued.getOrDefault(nick, 0);
    }

    // TODO: remove, move to ReceiveQueue
    public Integer getLimit(String nick) {
        nick = nick.toLowerCase();
        return limits.getOrDefault(nick, 10);
    }

    private void printStats(String nick) {
        nick = nick.toLowerCase();
        Integer current = getCurrent(nick);
        Integer limit = getLimit(nick);
        LOG.info("Queue for {}: {}/{}", nick, current, limit);
    }
}
