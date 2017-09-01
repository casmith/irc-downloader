import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ListSearcherTests {

    @Test
    public void testSearch() throws IOException {
        ListSearcher lists = new ListSearcher(new File("lists"));
        List<String> metallica = lists.search("^\\!.*Metallica.*");
        Assert.assertFalse(metallica.isEmpty());
    }
}
