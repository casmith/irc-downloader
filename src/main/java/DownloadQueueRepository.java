import java.util.Queue;

public interface DownloadQueueRepository {
    Queue<FileRequest> get();
    void save(Queue<FileRequest> downloadQueue);
}
