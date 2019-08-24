package marvin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class ListManager implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(ListManager.class);

    private Map<String, LocalDateTime> lists = new HashMap<>();

    public boolean add(String key) {
        if (lists.containsKey(key)) {
            LocalDateTime localDateTime = lists.get(key);
            LOG.info(key + " / " + localDateTime.format(DateTimeFormatter.ISO_DATE_TIME));
            return false;
        }
        lists.put(key, LocalDateTime.now());
        return true;
    }

    public static void save(ListManager listManager, String fileName) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName))) {
            out.writeObject(listManager);
        } catch (Exception e) {
            // TODO: throw exception
            System.out.println("Failed to write listManager");
        }
    }

    public static ListManager load(String fileName) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName))) {
            return (ListManager)in.readObject();
        } catch (Exception e) {
            // TODO: throw exception
            System.out.println("Failed to load listManager" + e);
        }
        return null;
    }

    @Override
    public String toString() {
        return String.join(",", lists.keySet());
    }
}
