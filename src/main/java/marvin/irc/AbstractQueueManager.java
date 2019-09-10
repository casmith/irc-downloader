package marvin.irc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public abstract class AbstractQueueManager implements QueueManager {

    public static final Logger LOG = LoggerFactory.getLogger(AbstractQueueManager.class);

    private Map<String, Queue<String>> queues = new HashMap<>();
    private Map<String, Integer> queued = new HashMap<>();
    private Map<String, Integer> limits = new HashMap<>();

    @Override
    public void enqueue(String nick, String message) {
        getQueue(nick).offer(message);
    }

    @Override
    public Queue<String> getQueue(String nick) {
        return queues.computeIfAbsent(nick, s -> new LinkedList<>());
    }

    @Override
    public Map<String, Queue<String>> getQueues() {
        return queues;
    }

    @Override
    public Integer getLimit(String nick) {
        return limits.getOrDefault(nick, 10);
    }

    @Override
    public void updateLimit(String nick, int limit) {
        // artificially cap at 10 for testing
        if (limit > 10) {
            limit = 10;
        }
        synchronized (this) {
            limits.put(nick, limit);
        }
    }

    @Override
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

    @Override
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

    @Override
    public void update(String nick, int current) {
        synchronized (this) {
            queued.put(nick, current);
            printStats(nick);
        }
    }

    private void printStats(String nick) {
        Integer current = getCurrent(nick);
        Integer limit = getLimit(nick);
        LOG.info("Queue for {}: {}/{}", nick, current, limit);
    }

    private Integer getCurrent(String nick) {
        return queued.getOrDefault(nick, 0);
    }
}
