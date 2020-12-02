package marvin.irc;

import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Singleton
public class ReceiveQueueManager extends AbstractQueueManager
        implements QueueManager {

    private static final Logger LOG = LoggerFactory.getLogger(ReceiveQueueManager.class);
    private final Map<String, Queue<String>> inProgress = new HashMap<>();

    public ReceiveQueueManager() {
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

    public Map<String, Queue<String>> getInProgress() {
        return inProgress;
    }

    public boolean markCompleted(String nick, String filename) {
        Queue<String> queue = inProgress.get(nick);
        String toRemove = null;
        for (String s : queue) {
            if (s.contains(filename)) {
                toRemove = s;
                break;
            }
        }
        if (toRemove != null) {
            return queue.remove(toRemove);
        }
        return false;
    }
}
