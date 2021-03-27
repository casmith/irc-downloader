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
        ListFile exisingList = dao.findByName(key);
        if (exisingList == null) {
            dao.insert(listFile);
            return true;
        } else {
            if (isListStale(exisingList.getLastUpdated())) {
                dao.update(listFile);
                return true;
            }
            return false;
        }
    }

    private boolean isListStale(LocalDateTime lastUpdated) {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1L);
        return lastUpdated.isBefore(yesterday);
    }
}
