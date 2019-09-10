package marvin.irc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ReceiveQueueManager extends AbstractQueueManager
        implements QueueManager {

    private static final Logger LOG = LoggerFactory.getLogger(ReceiveQueueManager.class);

    private Map<String, Queue<String>> inProgress = new HashMap<>();
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

    private Integer getCurrent(String nick) {
        return queued.getOrDefault(nick, 0);
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
