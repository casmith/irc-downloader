package marvin.list;

import marvin.list.ListManager;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class FlatFileListManagerTest {

    @Test
    public void testSave() throws IOException {

        File tempFile = createTempFile("testSave");
        ListManager listManager = FlatFileListManager.open(tempFile);
        listManager.add("butthead");
        listManager.add("beavis");

        ListManager anotherListManager = FlatFileListManager.open(tempFile);

        assertNotNull("list manager failed to load", anotherListManager);
        assertFalse("list manager was able to add a value which should already be present",
            anotherListManager.add("butthead"));
    }

    @Test
    public void testDuplicates() throws IOException {
        ListManager listManager = FlatFileListManager.open(createTempFile("testDuplicates"));
        assertTrue(listManager.add("!testguy"));
        assertFalse(listManager.add("!testguy"));
    }

    private File createTempFile(String prefix) throws IOException {
        File tempFile = File.createTempFile(prefix, "dat");
        tempFile.deleteOnExit();
        return tempFile;
    }
}
