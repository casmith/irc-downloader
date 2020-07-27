package marvin.list;

import marvin.data.ListFileDao;
import marvin.model.ListFile;

import java.time.LocalDateTime;

public class DatabaseListManager implements ListManager {

    private final ListFileDao dao;

    public DatabaseListManager(ListFileDao dao) {
        this.dao = dao;
    }

    @Override
    public boolean add(String key) {
        ListFile listFile = new ListFile(key, LocalDateTime.now());
        if (dao.findByName(key) == null) {
            dao.insert(listFile);
            return true;
        } else {
            dao.update(listFile);
            return false;
        }
    }
}
