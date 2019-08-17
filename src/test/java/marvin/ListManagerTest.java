package marvin;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

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
    public void test() {


    }

}