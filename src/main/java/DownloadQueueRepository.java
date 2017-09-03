import java.util.Queue;

/**
 * Created by clay on 9/3/17.
 */
public interface DownloadQueueRepository {
    Queue<FileRequest> list();
    void save(Queue<FileRequest> downloadQueue);
}
