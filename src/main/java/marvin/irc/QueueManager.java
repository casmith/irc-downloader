package marvin.irc;

import java.util.Map;
import java.util.Queue;

public interface QueueManager {

    boolean inc(String nick);

    boolean dec(String nick);

    void enqueue(String nick, String message);

    Queue<String> getQueue(String nick);

    Map<String, Queue<String>> getQueues();
}
