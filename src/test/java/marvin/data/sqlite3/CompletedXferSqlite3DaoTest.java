package marvin.data.sqlite3;

import marvin.model.CompletedXfer;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.*;

public class CompletedXferSqlite3DaoTest {

    @Test
    public void testInsert() {
        CompletedXferSqlite3Dao dao = new CompletedXferSqlite3Dao("./marvin.db");
        dao.createTable();
        int initialCount = dao.selectAll().size();
        dao.insert(new CompletedXfer("someguy", "#marvinbot", "blah.mp3", 1024L, LocalDateTime.now()));
        assertEquals(initialCount + 1, dao.selectAll().size());
    }
}