package marvin.data;

import marvin.model.QueueEntry;
import marvin.queue.QueueStatus;

import java.util.List;

public interface QueueEntryDao {
    void createTable();
    void insert(QueueEntry queueEntry);
    List<QueueEntry> selectAll();
    void truncate();
    QueueEntry find(String nick, String requestLike);
    List<QueueEntry> findByNickAndStatus(String nick, QueueStatus status);
    void updateStatus(QueueEntry queueEntry, QueueStatus status);
    void delete(QueueEntry queueEntry);
    List<QueueEntry> findByStatus(QueueStatus status);
    void resetAll();
    List<QueueEntry> findByBatch(String batch);
    QueueEntry dequeue(String nick, int queueLimit);
}
