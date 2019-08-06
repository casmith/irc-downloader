import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import static org.junit.Assert.assertEquals;

public class FlatFileDownloadQueueRepositoryTests {

    private FlatFileDownloadQueueRepository repository;

    @Before
    public void setUp() {
        this.repository = new FlatFileDownloadQueueRepository("test-queue.dat");
    }

    @Test
    public void testSaveAndList() {
        // save the queue
        Queue<FileRequest> queue = new LinkedList<>(Arrays.asList(
                new FileRequest("filename1.mp3", "username"),
                new FileRequest("filename2.mp3", "username"),
                new FileRequest("filename3.mp3", "username")
        ));

        this.repository.save(queue);

        // load the queue again
        Queue<FileRequest> list = this.repository.list();

        // they should match
        assertEquals(queue, list);
    }
}
