package marvin.irc;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public abstract class AbstractQueueManager implements QueueManager {

    private Map<String, Queue<String>> queues = new HashMap<>();
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
}
