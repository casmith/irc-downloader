package marvin.data;

import marvin.model.ListFile;
import marvin.model.QueueEntry;

import java.util.List;

public interface ListFileDao {
    void createTable();
    void insert(ListFile queueEntry);
    List<ListFile> selectAll();
    void truncate();
}
