package marvin.irc;

import org.junit.Test;

import static org.junit.Assert.*;

public class ReceiveQueueManagerTest {

    @Test
    public void markCompleted() {
        ReceiveQueueManager receiveQueueManager = new ReceiveQueueManager();
        receiveQueueManager.addInProgress("server", "!server 4 EVER - Spune-Mi Cine (Smekerit).mp3  ::INFO:: 5.80Mb");
        assertTrue(receiveQueueManager.markCompleted("server", "4 EVER - Spune-Mi Cine (Smekerit).mp3"));
        assertTrue(receiveQueueManager.getInProgress().get("server").isEmpty());
        assertFalse(receiveQueueManager.markCompleted("server", "4 EVER - Spune-Mi Cine (Smekerit).mp3"));
    }
}
