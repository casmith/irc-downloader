package marvin.data.sqlite3;

import marvin.model.ListFile;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.assertEquals;

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
}