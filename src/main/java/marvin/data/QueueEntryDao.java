package marvin.data;

import marvin.model.QueueEntry;

import java.util.List;

public interface QueueEntryDao {
    void createTable();
    void insert(QueueEntry queueEntry);
    List<QueueEntry> selectAll();
    void truncate();
    QueueEntry find(String nick, String requestLike);
//    List<QueueEntry> findBatch(String nick, String batch);
}
