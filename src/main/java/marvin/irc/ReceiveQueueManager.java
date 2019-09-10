package marvin.irc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ReceiveQueueManager implements QueueManager {

    private static final Logger LOG = LoggerFactory.getLogger(ReceiveQueueManager.class);

    private Map<String, Queue<String>> queues = new HashMap<>();
    private Map<String, Queue<String>> inProgress = new HashMap<>();
    private Map<String, Integer> limits = new HashMap<>();
    private Map<String, Integer> queued = new HashMap<>();

    @Override
    public void update(String nick, int current) {
        synchronized (this) {
            queued.put(nick, current);
            printStats(nick);
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

    @Override
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
    public void addInProgress(String nick, String message) {
        inProgress.computeIfAbsent(nick, s -> new LinkedList<>()).offer(message);
    }

    @Override
    public void retry(String nick, String filename) {
        Queue<String> messages = inProgress.computeIfAbsent(nick, s -> new LinkedList<>());
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
}
