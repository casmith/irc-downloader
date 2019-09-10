package marvin.irc;

import java.util.Map;
import java.util.Queue;

public interface QueueManager {
    void update(String nick, int current);

    boolean inc(String nick);

    boolean dec(String nick);

    void updateLimit(String nick, int limit);

    void enqueue(String nick, String message);

    Queue<String> getQueue(String nick);

    Map<String, Queue<String>> getQueues();

    void addInProgress(String nick, String message);

    void retry(String nick, String filename);

    Integer getLimit(String nick);
}
