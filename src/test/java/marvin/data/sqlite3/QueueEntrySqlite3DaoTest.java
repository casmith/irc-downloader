package marvin.data.sqlite3;

import marvin.data.JdbcTemplate;
import marvin.model.QueueEntry;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.assertEquals;

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

}
