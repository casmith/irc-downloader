package marvin.list;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class InMemoryListManager implements ListManager {

    private Map<String, LocalDateTime> map = new HashMap<>();

    @Override
    public boolean add(String key) {

        if (map.containsKey(key)) {
            map.put(key, LocalDateTime.now());
            return false;
        } else {
            map.put(key, LocalDateTime.now());
            return true;
        }
    }
}
