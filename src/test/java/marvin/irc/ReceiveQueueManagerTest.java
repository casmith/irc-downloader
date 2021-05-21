package marvin.irc;

import marvin.data.JdbcTemplate;
import marvin.data.QueueEntryDao;
import marvin.data.sqlite3.QueueEntrySqlite3Dao;
import marvin.messaging.NoopProducer;
import marvin.service.QueueService;
import marvin.service.QueueServiceImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

public class ReceiveQueueManagerTest {

    private ReceiveQueueManager receiveQueueManager;
    private QueueService queueService;
    private QueueEntryDao dao;

    @Before
    public void setUp() {
        dao = new QueueEntrySqlite3Dao(new JdbcTemplate("jdbc:sqlite:./marvin.db"));
        queueService = new QueueServiceImpl(dao);
        receiveQueueManager = new ReceiveQueueManager(queueService, dao, new NoopProducer());
        dao.truncate();
    }

    @Test
    public void testMarkInProgress() {
        receiveQueueManager.enqueue("server", "!server 4 EVER - Spune-Mi Cine (Smekerit).mp3  ::INFO:: 5.80Mb", "batch1");
        receiveQueueManager.addInProgress("server", "!server 4 EVER - Spune-Mi Cine (Smekerit).mp3  ::INFO:: 5.80Mb");
        assertTrue(receiveQueueManager.markCompleted("server", "4 EVER - Spune-Mi Cine (Smekerit).mp3"));
        assertNull(receiveQueueManager.getInProgress().get("server"));
        assertFalse(receiveQueueManager.markCompleted("server", "4 EVER - Spune-Mi Cine (Smekerit).mp3"));
        assertFalse(receiveQueueManager.markCompleted("server2", "4 EVER - Spune-Mi Cine (Smekerit).mp3"));

        receiveQueueManager.enqueue("server", "!server 4 EVER - Spune-Mi Cine (Smekerit).mp3  ::INFO:: 5.80Mb", "batch1");
        assertTrue(receiveQueueManager.markCompleted("server", "4_EVER_-_Spune-Mi_Cine_(Smekerit).mp3"));
        assertNull(receiveQueueManager.getInProgress().get("server"));
    }

    @Test
    public void testAddInProgress() {
        receiveQueueManager.enqueue("server", "filename.mp3", "batch1");

        receiveQueueManager.addInProgress("server", "filename.mp3");
        assertEquals(1, receiveQueueManager.getInProgress().get("server").size());
    }

    @Test
    public void testPoll_emptyQueue() {
        assertFalse(receiveQueueManager.poll("server").isPresent());
    }


    @Test
    public void testPoll_nonEmptyQueue() {
        receiveQueueManager.enqueue("server", "filename.mp3", "batch1");
        Optional<String> message = receiveQueueManager.poll("server");
        assertTrue(message.isPresent());
    }

    @Test
    public void testRetry() {

        receiveQueueManager.enqueue("server", "filename.mp3", "batch1");
        receiveQueueManager.enqueue("server", "filename2.mp3", "batch1");
        receiveQueueManager.enqueue("server", "filename3.mp3", "batch1");
        receiveQueueManager.enqueue("server", "filename4.mp3", "batch1");
        receiveQueueManager.enqueue("server", "filename5.mp3", "batch1");

        receiveQueueManager.addInProgress("server", "filename.mp3");
        receiveQueueManager.addInProgress("server", "filename2.mp3");
        receiveQueueManager.addInProgress("server", "filename3.mp3");
        receiveQueueManager.addInProgress("server", "filename4.mp3");
        receiveQueueManager.addInProgress("server", "filename5.mp3");
        assertEquals(1, receiveQueueManager.getInProgress().size());
        assertEquals(5, queueService.getQueueForNick("server").size());

        receiveQueueManager.retry("server", "filename.mp3");

        assertEquals(4, receiveQueueManager.getInProgress().get("server").size());
        assertEquals("filename.mp3", queueService.getQueueForNick("server").getItems().get(0).getFilename());
    }
}
