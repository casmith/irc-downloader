package marvin.data.sqlite3;

import marvin.data.JdbcTemplate;
import marvin.model.QueueEntry;
import marvin.queue.QueueStatus;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;

import static marvin.queue.QueueStatus.REQUESTED;
import static org.junit.Assert.*;

public class QueueEntrySqlite3DaoTest {
    private QueueEntrySqlite3Dao dao;

    @Before
    public void setUp() {
        dao = new QueueEntrySqlite3Dao(new JdbcTemplate("jdbc:sqlite:./marvin.db"));
        dao.createTable();
        dao.truncate(); // always start with a clean slate
    }

    @Test
    public void testInsert() {
        int initialCount = dao.selectAll().size();
        dao.insert(new QueueEntry("someguy", "batch1", "!someguy blah.mp3", "REQUESTED", "#mp3passion", LocalDateTime.now()));
        assertEquals(initialCount + 1, dao.selectAll().size());
    }

    @Test
    public void testSelectAll() {
        dao.insert(new QueueEntry("someguy", "batch1", "!someguy blah.mp3", "REQUESTED", "#mp3passion", LocalDateTime.now()));
        dao.insert(new QueueEntry("someguy", "batch1", "!someguy blah2.mp3", "REQUESTED", "#mp3passion", LocalDateTime.now().minusDays(1)));
        List<QueueEntry> all = dao.selectAll();
        assertEquals(2, all.size());
        assertEquals("!someguy blah2.mp3", all.get(0).getRequestString());
    }

    @Test
    public void testFind() {
        dao.insert(new QueueEntry("someguy", "batch1", "!someguy blah.mp3", "REQUESTED", "#mp3passion", LocalDateTime.now()));
        dao.insert(new QueueEntry("someguy", "batch1", "!someguy blah2.mp3", "REQUESTED", "#mp3passion", LocalDateTime.now().minusDays(1)));
        QueueEntry entry = dao.find("someguy", "blah.mp3");
        assertNotNull("entry is null", entry);
        assertEquals(entry.getRequestString(), "!someguy blah.mp3");
    }

    @Test
    public void testUpdate() {
        QueueEntry queueEntry = new QueueEntry("someguy", "batch1", "!someguy blah.mp3", QueueStatus.PENDING.toString(), "#mp3passion", LocalDateTime.now());
        dao.insert(queueEntry);
        dao.updateStatus(queueEntry, REQUESTED);
        QueueEntry found = dao.find("someguy", "!someguy blah.mp3");
        assertNotNull("missing entry record", found);
        assertEquals(REQUESTED.toString(), found.getStatus());
    }

    @Test
    public void testFindByNickAndStatus() {
        assertEquals(0, dao.findByNickAndStatus("someguy", QueueStatus.PENDING).size());

        QueueEntry queueEntry = new QueueEntry("someguy", "batch1", "!someguy blah.mp3", QueueStatus.PENDING.toString(), "#mp3passion", LocalDateTime.now());
        dao.insert(queueEntry);
        List<QueueEntry> found = dao.findByNickAndStatus("someguy", QueueStatus.PENDING);
        assertEquals(1, found.size());
    }

    @Test
    public void testDequeue() {
        for (int i = 0; i < 20; i++) {
            QueueEntry queueEntry = new QueueEntry("someguy", "batch1", "!someguy blah[" + i + "].mp3", QueueStatus.PENDING.toString(), "#mp3passion", LocalDateTime.now().plusSeconds(i));
            dao.insert(queueEntry);
        }
        QueueEntry firstQueuedEntry = dao.dequeue("someguy", 1);
        dao.updateStatus(firstQueuedEntry, REQUESTED);

        assertNotNull(firstQueuedEntry);
        assertNull(dao.dequeue("someguy", 1));
        // dequeue 9 more with a queue limit of 10
        for (int i = 0; i < 9; i++) {
            QueueEntry dequeued = dao.dequeue("someguy", 10);
            assertNotNull(dequeued);
            dao.updateStatus(dequeued, REQUESTED);
        }
        // the 11th dequeued should be null
        assertNull(dao.dequeue("someguy", 10));

    }
}
