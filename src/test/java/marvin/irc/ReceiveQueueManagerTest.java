package marvin.irc;

import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

public class ReceiveQueueManagerTest {

    private ReceiveQueueManager receiveQueueManager = new ReceiveQueueManager();

    @Test
    public void testMarkInProgress() {
        receiveQueueManager.addInProgress("server", "!server 4 EVER - Spune-Mi Cine (Smekerit).mp3  ::INFO:: 5.80Mb");
        assertTrue(receiveQueueManager.markCompleted("server", "4 EVER - Spune-Mi Cine (Smekerit).mp3"));
        assertTrue(receiveQueueManager.getInProgress().get("server").isEmpty());
        assertFalse(receiveQueueManager.markCompleted("server", "4 EVER - Spune-Mi Cine (Smekerit).mp3"));
        assertFalse(receiveQueueManager.markCompleted("server2", "4 EVER - Spune-Mi Cine (Smekerit).mp3"));
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
        receiveQueueManager.addInProgress("server", "filename.mp3");
        receiveQueueManager.addInProgress("server", "filename2.mp3");
        receiveQueueManager.addInProgress("server", "filename3.mp3");
        receiveQueueManager.addInProgress("server", "filename4.mp3");
        receiveQueueManager.addInProgress("server", "filename5.mp3");
        assertEquals(1, receiveQueueManager.getInProgress().size());
        assertEquals(0, receiveQueueManager.getQueue("server").size());

        receiveQueueManager.retry("server", "filename.mp3");

        assertEquals(4, receiveQueueManager.getInProgress().get("server").size());
        assertEquals("filename.mp3", receiveQueueManager.getQueue("server").peek());
    }
}
