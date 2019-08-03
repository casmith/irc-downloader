package marvin.irc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class QueueManager {

    private static final Logger LOG = LoggerFactory.getLogger(QueueManager.class);

    private Map<String, Queue<String>> queues = new HashMap<>();
    private Map<String, Integer> limits = new HashMap<>();
    private Map<String, Integer> queued = new HashMap<>();

    public void update(String nick, int current) {
        synchronized (this) {
            queued.put(nick, current);
            printStats(nick);
        }
    }

    public boolean inc(String nick) {
        synchronized (this) {
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

    public boolean dec(String nick) {
        synchronized (this) {
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

    private void printStats(String nick) {
        Integer current = getCurrent(nick);
        Integer limit = getLimit(nick);
        LOG.info("Queue for {}: {}/{}", nick, current, limit);
    }

    private Integer getLimit(String nick) {
        return limits.getOrDefault(nick, 10);
    }

    private Integer getCurrent(String nick) {
        return queued.getOrDefault(nick, 0);
    }

    public void updateLimit(String nick, int limit) {
        // artificially cap at 10 for testing
        if (limit > 10) {
            limit = 10;
        }
        synchronized (this) {
            limits.put(nick, limit);
            printStats(nick);
        }
    }

    public void enqueue(String nick, String message) {
        queues.computeIfAbsent(nick, s -> new LinkedList<>()).offer(message);
    }

    public Map<String, Queue<String>> getQueues() {
        return queues;
    }

}
