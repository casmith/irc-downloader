import java.io.Serializable;
import java.util.*;

public class DownloadQueue implements Serializable {

    private Map<String, Queue<FileRequest>> requested = new HashMap<>();
    private Map<String, Queue<FileRequest>> queued = new HashMap<>();

    /**
     * Mark the request as complete (the download finished)
     *
     * @param request
     */
    public void complete(FileRequest request) {
        Queue<FileRequest> requested = getRequested(request.getNick());
        requested.remove(request);
    }

    /**
     * Enqueue a file to be downloaded
     *
     * This is a convenience method that handles creation of a FileRequest
     *
     * @param line
     */
    public void enqueue(String line) {
        enqueue(new FileRequest(line));
    }

    /**
     * Enqueue a file to be downloaded
     * @param fileRequest
     */
    public void enqueue(FileRequest fileRequest) {
        Queue<FileRequest> fileRequests = getQueue(fileRequest.getNick());
        fileRequests.add(fileRequest);
    }

    /**
     * Get the queued files (not yet requested) for the user
     * @param nick
     * @return
     */
    public Queue<FileRequest> getQueue(String nick) {
        return queued.computeIfAbsent(nick, k -> new LinkedList<>());
    }

    /**
     * Get the requested files for the user
     * @param nick
     * @return
     */
    public Queue<FileRequest> getRequested(String nick) {
        return requested.computeIfAbsent(nick, k -> new LinkedList<>());
    }

    /**
     * Request the next file in the queue for the given user
     * @param nick
     * @return
     */
    public FileRequest request(String nick) {
        Queue<FileRequest> queue = getQueue(nick);
        FileRequest removed = queue.remove();
        getRequested(nick).add(removed);
        return removed;
    }

    public FileRequest request() {
        Set<String> nicks = queued.keySet();
        for (String nick : nicks) {
            Queue<FileRequest> queue = getQueue(nick);
            if (!queue.isEmpty()) {
                return request(nick);
            }
        }
        return null;
    }
}
