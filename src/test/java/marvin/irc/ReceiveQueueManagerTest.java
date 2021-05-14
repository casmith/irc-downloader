package marvin.irc;

import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

public class ReceiveQueueManagerTest {

    private ReceiveQueueManager receiveQueueManager = new ReceiveQueueManager();

    @Test
    public void testMarkInProgress() {
        receiveQueueManager.enqueue("server", "!server 4 EVER - Spune-Mi Cine (Smekerit).mp3  ::INFO:: 5.80Mb");
        receiveQueueManager.addInProgress("server", "!server 4 EVER - Spune-Mi Cine (Smekerit).mp3  ::INFO:: 5.80Mb");
        assertTrue(receiveQueueManager.markCompleted("server", "4 EVER - Spune-Mi Cine (Smekerit).mp3"));
        assertNull(receiveQueueManager.getInProgress().get("server"));
        assertFalse(receiveQueueManager.markCompleted("server", "4 EVER - Spune-Mi Cine (Smekerit).mp3"));
        assertFalse(receiveQueueManager.markCompleted("server2", "4 EVER - Spune-Mi Cine (Smekerit).mp3"));

        receiveQueueManager.enqueue("server", "!server 4 EVER - Spune-Mi Cine (Smekerit).mp3  ::INFO:: 5.80Mb");
        assertTrue(receiveQueueManager.markCompleted("server", "4_EVER_-_Spune-Mi_Cine_(Smekerit).mp3"));
        assertNull(receiveQueueManager.getInProgress().get("server"));
    }

    @Test
    public void testAddInProgress() {
        receiveQueueManager.enqueue("server", "filename.mp3");

        receiveQueueManager.addInProgress("server", "filename.mp3");
        assertEquals(1, receiveQueueManager.getInProgress().get("server").size());
    }

    @Test
    public void testPoll_emptyQueue() {
        assertFalse(receiveQueueManager.poll("server").isPresent());
    }


    @Test
    public void testPoll_nonEmptyQueue() {
        receiveQueueManager.enqueue("server", "filename.mp3");
        Optional<String> message = receiveQueueManager.poll("server");
        assertTrue(message.isPresent());
    }

    @Test
    public void testRetry() {

        receiveQueueManager.enqueue("server", "filename.mp3");
        receiveQueueManager.enqueue("server", "filename2.mp3");
        receiveQueueManager.enqueue("server", "filename3.mp3");
        receiveQueueManager.enqueue("server", "filename4.mp3");
        receiveQueueManager.enqueue("server", "filename5.mp3");

        receiveQueueManager.addInProgress("server", "filename.mp3");
        receiveQueueManager.addInProgress("server", "filename2.mp3");
        receiveQueueManager.addInProgress("server", "filename3.mp3");
        receiveQueueManager.addInProgress("server", "filename4.mp3");
        receiveQueueManager.addInProgress("server", "filename5.mp3");
        assertEquals(1, receiveQueueManager.getInProgress().size());
        assertEquals(5, receiveQueueManager.getQueue("server").size());

        receiveQueueManager.retry("server", "filename.mp3");

        assertEquals(4, receiveQueueManager.getInProgress().get("server").size());
        assertEquals("filename.mp3", receiveQueueManager.getQueue("server").getItems().get(0).getFilename());
    }
}
