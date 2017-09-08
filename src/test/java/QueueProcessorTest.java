import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class QueueProcessorTest {

    private QueueProcessor queueProcessor;
    private DownloadQueue downloadQueue;

    @Before
    public void setUp() {
        downloadQueue = new DownloadQueue();
        downloadQueue.enqueue("!someguy Metallica - Am I Evil.mp3");
        downloadQueue.enqueue("!someguy Metallica - Ride the Lightning.mp3");
        downloadQueue.enqueue("!someguy Metallica - Master of Puppets.mp3");

        queueProcessor = new QueueProcessor(downloadQueue, null, 0, "#somechannel");
    }

    @Test
    public void testSomething() {
        queueProcessor.processQueue();
        assertEquals(1, downloadQueue.getRequested("someguy").size());
    }

}