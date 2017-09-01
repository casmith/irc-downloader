import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

public class ListSearcherTests {

    @Test
    public void testSearch() throws IOException {


        File listDirectory = getListsDirectory();
        ListSearcher lists = new ListSearcher(listDirectory);
        List<String> metallica = lists.search("^\\!.*Metallica.*");
        Assert.assertFalse(metallica.isEmpty());
    }

    private File getListsDirectory() {
        try {
            URL listsDirectoryUrl = ListSearcher.class.getResource("/lists");
            return new File(listsDirectoryUrl.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed to load lists directory", e);
        }
    }
}
