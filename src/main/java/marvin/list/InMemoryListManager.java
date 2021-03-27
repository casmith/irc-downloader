package marvin.list;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class InMemoryListManager implements ListManager {

    private final Map<String, LocalDateTime> map = new HashMap<>();


    public InMemoryListManager() {
    }

    InMemoryListManager(Map<String, LocalDateTime> initialState) {
        map.putAll(initialState);
    }

    @Override
    public boolean add(String key) {
        if (map.containsKey(key)) {
            return updateListIfStale(key);
        } else {
            return addOrUpdateList(key);
        }
    }

    private boolean updateListIfStale(String key) {
        if (isListStale(map.get(key))) {
            System.out.println("List is stale" + map.get(key));
            return addOrUpdateList(key);
        } else {
            return false;
        }
    }

    private boolean addOrUpdateList(String key) {
        map.put(key, LocalDateTime.now());
        return true;
    }

    private boolean isListStale(LocalDateTime lastUpdated) {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1L);
        return lastUpdated.isBefore(yesterday);
    }
}
