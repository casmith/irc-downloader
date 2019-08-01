import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FileRequestTests {

    private static String EXAMPLE_REQUEST = "!Metalhead Metallica - Master of Puppets.mp3  ::INFO:: 13.43Mb";

    @Test
    public void testConstructor_shouldParseNick() {
        FileRequest fileRequest = FileRequest.parse(EXAMPLE_REQUEST);
        assertEquals("Metalhead", fileRequest.getNick());
    }

    @Test
    public void testConstructor_shouldParseFilename() {
        FileRequest fileRequest = FileRequest.parse(EXAMPLE_REQUEST);
        String expected = "Metallica - Master of Puppets.mp3";
        assertEquals(expected, fileRequest.getFileName());
    }

    @Test(expected = ParseException.class)
    public void testConstructorGivenInvalid_shouldThrow() {
        FileRequest.parse("blah blah blah");
    }
}
