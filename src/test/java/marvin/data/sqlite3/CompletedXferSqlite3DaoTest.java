package marvin.data.sqlite3;

import marvin.model.CompletedXfer;
import marvin.model.CompletedXferSummary;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;

public class CompletedXferSqlite3DaoTest {

    private CompletedXferSqlite3Dao dao;

    @Before
    public void setUp() {
        dao = new CompletedXferSqlite3Dao("./marvin.db");
        dao.createTable();
        dao.truncate(); // always start with a clean slate
    }

    @Test
    public void testInsert() {
        int initialCount = dao.selectAll().size();
        dao.insert(new CompletedXfer("someguy", "#marvinbot", "blah.mp3", 1024L, LocalDateTime.now()));
        assertEquals(initialCount + 1, dao.selectAll().size());
    }

    @Test
    public void testSummarize() {
        dao.insert(new CompletedXfer("someguy", "#marvinbot", "blah.mp3", 1024L, LocalDateTime.now()));
        dao.insert(new CompletedXfer("someguy", "#marvinbot", "blah2.mp3", 1024L, LocalDateTime.now()));
        CompletedXferSummary summary = dao.summarize();
        assertEquals(2, summary.getCount());
        assertEquals(2048, summary.getTotalBytes());
    }
}