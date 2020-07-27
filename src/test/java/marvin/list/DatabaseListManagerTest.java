package marvin.list;

import marvin.data.ListFileDao;
import marvin.data.sqlite3.ListFileSqlite3Dao;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DatabaseListManagerTest {

    private ListFileDao dao;

    @Before
    public void setUp() {
        dao = new ListFileSqlite3Dao("jdbc:sqlite:./marvin.db");
    }

    @Test
    public void testSave() {
        ListManager listManager = new DatabaseListManager(dao);
        listManager.add("butthead");
        listManager.add("beavis");


        assertFalse("list manager was able to add a value which should already be present",
            listManager.add("butthead"));
    }

    @Test
    public void testDuplicates() {
        ListManager listManager = new DatabaseListManager(dao);
        assertTrue(listManager.add("!testguy"));
        assertFalse(listManager.add("!testguy"));
    }
}
