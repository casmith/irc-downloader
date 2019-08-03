import java.util.List;

/**
 * Created by clay on 9/3/17.
 */
public interface DownloadQueueRepository {
    List<FileRequest> list();
    void save(List<FileRequest> downloadQueue);
}
