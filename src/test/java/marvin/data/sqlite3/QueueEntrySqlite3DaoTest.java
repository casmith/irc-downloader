package marvin.data.sqlite3;

import marvin.data.JdbcTemplate;
import marvin.model.QueueEntry;
import marvin.queue.QueueStatus;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
        assertEquals("!someguy blah.mp3", all.get(0).getRequestString());
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
        dao.updateStatus(queueEntry, QueueStatus.REQUESTED);
        QueueEntry found = dao.find("someguy", "!someguy blah.mp3");
        assertNotNull("missing entry record", found);
        assertEquals(QueueStatus.REQUESTED.toString(), found.getStatus());
    }

    @Test
    public void testFindByNickAndStatus() {
        assertEquals(0, dao.findByNickAndStatus("someguy", QueueStatus.PENDING).size());

        QueueEntry queueEntry = new QueueEntry("someguy", "batch1", "!someguy blah.mp3", QueueStatus.PENDING.toString(), "#mp3passion", LocalDateTime.now());
        dao.insert(queueEntry);
        List<QueueEntry> found = dao.findByNickAndStatus("someguy", QueueStatus.PENDING);
        assertEquals(1, found.size());
    }

}
