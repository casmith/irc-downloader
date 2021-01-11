package marvin.irc;

import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Singleton
public class ReceiveQueueManager {

    private static final Logger LOG = LoggerFactory.getLogger(ReceiveQueueManager.class);
    private final Map<String, Queue<String>> inProgress = new HashMap<>();
    private Map<String, Integer> limits = new HashMap<>();
    private Map<String, Integer> queued = new HashMap<>();
    private Map<String, Queue<String>> queues = new HashMap<>();

    public ReceiveQueueManager() {
    }

    public void addInProgress(String nick, String message) {
        getInProgress(nick).offer(message);
    }

    public void retry(String nick, String filename) {
        Queue<String> messages = getInProgress(nick);
        int count = 0;
        for(Iterator<String> iter = messages.iterator(); iter.hasNext();) {
            String message = iter.next();
            if (message.toLowerCase().contains(filename.toLowerCase())) {
                count++;
                iter.remove();
                LOG.info("Retrying !" + nick + " " + filename);
                enqueue(nick, message);
            }
        }
        if (count != 1) {
            LOG.warn("Found " + count + " items to retry for " + nick + ":" + filename);
        }
    }

    private Queue<String> getInProgress(String nick) {
        return inProgress.computeIfAbsent(nick, s -> new LinkedList<>());
    }

    public Map<String, Queue<String>> getInProgress() {
        return inProgress;
    }

    public boolean markCompleted(String nick, String filename) {
        Queue<String> queue = inProgress.get(nick);
        if (queue != null) {
            Optional<String> found = queue.stream()
                .filter(i -> i.contains(filename))
                .findFirst();
            if (found.isPresent()) {
                return queue.remove(found.get());
            }
        }
        return false;
    }

    public Optional<String> poll(String nick) {
        Queue<String> queue = getQueue(nick);
        if (!queue.isEmpty() && this.inc(nick)) {
            return Optional.ofNullable(queue.poll());
        } else {
            return Optional.empty();
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

    public Queue<String> getQueue(String nick) {
        return queues.computeIfAbsent(nick, s -> new LinkedList<>());
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
        final Queue<String> queue = getQueue(nick);
        if (!queue.contains(message)) {
            queue.offer(message);
        }
    }

    public Map<String, Queue<String>> getQueues() {
        return null;
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
