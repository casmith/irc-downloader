package marvin;

import org.junit.Test;

import static org.junit.Assert.*;

public class ListManagerTest {

    @Test
    public void testSave() {

        ListManager listManager = new ListManager();
        listManager.add("butthead");
        listManager.add("beavis");
        String fileName = "thing.txt";

        ListManager.save(listManager, fileName);

        listManager = ListManager.load(fileName);

        assertNotNull("list manager failed to load", listManager);
        assertFalse("list manager was able to add a value which should already be present",
                listManager.add("butthead"));
    }

    @Test
    public void testDuplicates() {
        ListManager listManager = new ListManager();
        assertTrue(listManager.add("!testguy"));
        assertFalse(listManager.add("!testguy"));
    }
}