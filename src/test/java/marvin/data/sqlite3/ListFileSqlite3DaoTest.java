package marvin.data.sqlite3;

import marvin.model.ListFile;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.time.temporal.ChronoUnit.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ListFileSqlite3DaoTest {
    private ListFileSqlite3Dao dao;

    @Before
    public void setUp() {
        dao = new ListFileSqlite3Dao("jdbc:sqlite:./marvin.db");
        dao.createTable();
        dao.truncate(); // always start with a clean slate
    }

    @Test
    public void testInsert() {
        int initialCount = dao.selectAll().size();
        dao.insert(new ListFile("someguy", LocalDateTime.now()));
        assertEquals(initialCount + 1, dao.selectAll().size());
    }

    @Test
    public void testSelectAll() {
        dao.insert(new ListFile("someguy", LocalDateTime.now()));
        dao.insert(new ListFile("someotherguy", LocalDateTime.now().minusDays(1)));
        List<ListFile> all = dao.selectAll();
        assertEquals(2, all.size());
        assertEquals("someguy", all.get(0).getName());
    }

    @Test
    public void testFindByName() {
        dao.insert(new ListFile("jlpicard", LocalDateTime.now()));
        assertNotNull(dao.findByName("jlpicard"));
    }

    @Test
    public void testUpdate() {
        ListFile listFile = new ListFile("wtriker", LocalDateTime.now().minusDays(1));
        dao.insert(listFile);
        LocalDateTime now = LocalDateTime.now();
        dao.update(new ListFile("wtriker", now));
        ListFile fetched = dao.findByName("wtriker");
        assertEquals(now.truncatedTo(SECONDS), fetched.getLastUpdated().truncatedTo(SECONDS));
    }
}
