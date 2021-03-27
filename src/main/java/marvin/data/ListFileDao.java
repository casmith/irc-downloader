package marvin.data;

import marvin.model.ListFile;

import java.util.List;

public interface ListFileDao {
    void createTable();
    ListFile findByName(String name);
    void insert(ListFile queueEntry);
    List<ListFile> selectAll();
    void truncate();
    void update(ListFile queueEntry);
}
