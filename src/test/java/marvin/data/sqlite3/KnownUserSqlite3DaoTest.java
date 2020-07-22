package marvin.data.sqlite3;

import marvin.model.KnownUser;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class KnownUserSqlite3DaoTest {

    private KnownUserSqlite3Dao dao;

    @Before
    public void setUp() {
        dao = new KnownUserSqlite3Dao("jdbc:sqlite:./marvin.db");
        dao.createTable();
        dao.truncate(); // always start with a clean slate
    }

    @Test
    public void testInsert() {
        int initialCount = dao.selectAll().size();
        dao.insert(new KnownUser("someguy", "someguy", "someguy@example.com", LocalDateTime.now()));
        assertEquals(initialCount + 1, dao.selectAll().size());
    }

    @Test
    public void testSelectAll() {
        dao.insert(new KnownUser("someguy", "someguy", "someguy@example.com", LocalDateTime.now()));
        dao.insert(new KnownUser("someguy", "someguy2", "someguy2@example.com", LocalDateTime.now().minusDays(1)));
        List<KnownUser> all = dao.selectAll();
        assertEquals(2, all.size());
        assertEquals("someguy@example.com", all.get(0).getHost());
    }
}
