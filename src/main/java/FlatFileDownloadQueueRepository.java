import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Collections;
import java.util.List;

public class FlatFileDownloadQueueRepository implements DownloadQueueRepository {

    private String fileName;
    private static final Logger LOGGER = LoggerFactory.getLogger(FlatFileDownloadQueueRepository.class);

    public FlatFileDownloadQueueRepository(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public List<FileRequest> list() {
        try {
            ObjectInputStream queue = new ObjectInputStream(new FileInputStream(this.fileName));
            return (List<FileRequest>) queue.readObject();
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.warn("Failed to load queue. Returning an empty one.");
            return Collections.emptyList();
        }
    }

    @Override
    public void save(List<FileRequest> downloadQueue) {
        try {
            ObjectOutputStream queue = new ObjectOutputStream(new FileOutputStream(this.fileName));
            queue.writeObject(downloadQueue);
        } catch (IOException e) {
            LOGGER.error("Failed to save queue", e);
        }
    }
}
