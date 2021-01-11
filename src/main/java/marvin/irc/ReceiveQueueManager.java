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
        getInProgress(nick).offer(message);
    }

    @Override
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
}
