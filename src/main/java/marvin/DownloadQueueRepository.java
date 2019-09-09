package marvin;

import java.util.Queue;

public interface DownloadQueueRepository {
    Queue<FileRequest> list();
    void save(Queue<FileRequest> downloadQueue);
}
