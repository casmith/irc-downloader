package marvin.list;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class FlatFileListManager implements ListManager, Serializable {
    private static final long serialVersionUID = 2L;
    private static final Logger LOG = LoggerFactory.getLogger(ListManager.class);

    private Map<String, LocalDateTime> lists = new HashMap<>();
    private File file;

    private FlatFileListManager(File file) {
        this.file = file;
    }

    public boolean add(String key) {
        if (lists.containsKey(key)) {
            LocalDateTime localDateTime = lists.get(key);
            LOG.info(key + " / " + localDateTime.format(DateTimeFormatter.ISO_DATE_TIME));
            this.save();
            return false;
        }
        lists.put(key, LocalDateTime.now());
        this.save();
        return true;
    }

    private void save() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(this);
        } catch (Exception e) {
            // TODO: throw exception
            System.out.println("Failed to write listManager");
        }
    }

    public static ListManager open(String fileName) {
        return FlatFileListManager.open(new File(fileName));
    }

    public static ListManager open(File file) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            return (ListManager)in.readObject();
        } catch (Exception e) {
            LOG.warn("Failed to load a list manager - creating a new one.");
            return new FlatFileListManager(file);
        }
    }

    @Override
    public String toString() {
        return String.join(",", lists.keySet());
    }
}
