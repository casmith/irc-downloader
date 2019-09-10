package marvin.handlers;

import marvin.irc.IrcBot;
import marvin.irc.QueueManager;
import marvin.irc.SendQueueManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.*;

public class FileRequestMessageHandlerTest {

    @Mock
    private IrcBot bot;
    private File downloadFile;
    private FileRequestMessageHandler handler;
    private QueueManager sendQueueManager;

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        downloadFile = File.createTempFile("download", "txt");
        downloadFile.deleteOnExit();
        sendQueueManager = new SendQueueManager();
        handler = new FileRequestMessageHandler(bot, sendQueueManager, "#coolguys", new File(downloadFile.getParent()));
        when(bot.getNick()).thenReturn("coolbot");
    }

    @Test
    public void testDownloadExistingFile() {
        String path = downloadFile.getAbsolutePath();
        handler.onMessage("#coolguys", "somedude", "!coolbot " + path);
        verify(bot).sendToChannel("#coolguys", "Accepted request from somedude for file " + path);
//        verify(bot).sendFile(any(), any());
        assertEquals(1, sendQueueManager.getQueue("somedude").size());

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
        handler = new FileRequestMessageHandler(bot, sendQueueManager, "#coolguys", new File("c:\\files"));
        String path = downloadFile.getAbsolutePath();
        handler.onMessage("#coolguys", "somedude", "!coolbot " + path);
        verify(bot).sendPrivateMessage("somedude", path + " was not found");
        verify(bot, never()).sendFile(any(), any());
    }
}