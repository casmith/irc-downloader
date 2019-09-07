package marvin.handlers;

import marvin.irc.IrcBot;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;

import static org.mockito.Mockito.*;

public class FileRequestMessageHandlerTest {

    @Mock
    private IrcBot bot;
    private File downloadFile;
    private FileRequestMessageHandler handler;

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        downloadFile = File.createTempFile("download", "txt");
        downloadFile.deleteOnExit();
        handler = new FileRequestMessageHandler(bot, "#coolguys", new File(downloadFile.getParent()));
        when(bot.getNick()).thenReturn("coolbot");
    }

    @Test
    public void testDownloadExistingFile() {
        String path = downloadFile.getAbsolutePath();
        handler.onMessage("#coolguys", "somedude", "!coolbot " + path);
        verify(bot).sendToChannel("#coolguys", "Sending " + path + " to somedude");
        verify(bot).sendFile(any(), any());
    }

    @Test
    public void testDownloadNonExistingFile() {
        File nonExistentFile = new File(downloadFile.getParent() + "\\notafile.txt");
        String path = nonExistentFile.getAbsolutePath();
        handler.onMessage("#coolguys", "somedude", "!coolbot " + path);
        verify(bot).sendPrivateMessage("somedude", path + " was not found");
        verify(bot, never()).sendFile(any(), any());
    }

    @Test
    public void testDownloadFileFromWrongDirectory() {
        handler = new FileRequestMessageHandler(bot, "#coolguys", new File("c:\\files"));
        String path = downloadFile.getAbsolutePath();
        handler.onMessage("#coolguys", "somedude", "!coolbot " + path);
        verify(bot).sendPrivateMessage("somedude", path + " was not found");
        verify(bot, never()).sendFile(any(), any());
    }
}