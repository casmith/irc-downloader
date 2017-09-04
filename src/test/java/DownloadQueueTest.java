import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;

public class DownloadQueueTest {

    private DownloadQueue downloadQueue;

    @Before
    public void setUp() {
        downloadQueue = new DownloadQueue();
    }

    @Test
    public void testEnqueueOneFile_shouldAddFileRequestForUser() {
        downloadQueue.enqueue("!someguy Metallica - Am I Evil.mp3");
        assertEquals(1, downloadQueue.getQueue("someguy").size());
    }

    @Test
    public void testRequestOneFile_shouldRemoveFromQueue() {
        downloadQueue.enqueue("!someguy Metallica - Am I Evil.mp3");
        FileRequest request = downloadQueue.request("someguy");
        assertEquals(0, downloadQueue.getQueue("someguy").size());
    }

    @Test
    public void testRequestOneFile_shouldAddToRequested() {
        downloadQueue.enqueue("!someguy Metallica - Am I Evil.mp3");
        FileRequest request = downloadQueue.request("someguy");
        assertEquals(1, downloadQueue.getRequested("someguy").size());
        assertEquals(request, downloadQueue.getRequested("someguy").peek());
    }

    @Test
    public void testRequestOneFineWithoutNick_shouldFindAQueuedFile() {
        downloadQueue.enqueue("!someguy Metallica - Am I Evil.mp3");
        downloadQueue.request();
        assertEquals(1, downloadQueue.getRequested("someguy").size());
    }

    @Test
    public void testRequestOneFileWithoutNickEmptyQueue_shouldReturnNull() {
        assertNull(downloadQueue.request());
    }

    @Test
    public void testComplete_shouldRemoveFromRequested() {
        downloadQueue.enqueue("!someguy Metallica - Am I Evil.mp3");
        FileRequest request = downloadQueue.request("someguy");
        downloadQueue.complete(request);
        assertEquals(0, downloadQueue.getRequested("someguy").size());
    }
}