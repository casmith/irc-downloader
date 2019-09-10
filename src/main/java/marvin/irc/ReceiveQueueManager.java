package marvin.irc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ReceiveQueueManager extends AbstractQueueManager
        implements QueueManager {

    private static final Logger LOG = LoggerFactory.getLogger(ReceiveQueueManager.class);

    private Map<String, Queue<String>> inProgress = new HashMap<>();

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
