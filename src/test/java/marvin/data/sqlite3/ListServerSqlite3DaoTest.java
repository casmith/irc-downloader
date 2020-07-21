package marvin.data.sqlite3;

import marvin.model.ListServer;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ListServerSqlite3DaoTest {

    private ListServerSqlite3Dao dao;

    @Before
    public void setUp() {
        dao = new ListServerSqlite3Dao("./marvin.db");
        dao.createTable();
        dao.truncate(); // always start with a clean slate
    }

    @Test
    public void testInsert() {
        int initialCount = dao.selectAll().size();
        dao.insert(new ListServer("someguy", "someguy", "someguy@example.com", LocalDateTime.now()));
        assertEquals(initialCount + 1, dao.selectAll().size());
    }

    @Test
    public void testSelectAll() {
        dao.insert(new ListServer("someguy", "someguy", "someguy@example.com", LocalDateTime.now()));
        dao.insert(new ListServer("someguy", "someguy2", "someguy2@example.com", LocalDateTime.now().minusDays(1)));
        List<ListServer> all = dao.selectAll();
        assertEquals(2, all.size());
        assertEquals("someguy@example.com", all.get(0).getHost());
    }
}
