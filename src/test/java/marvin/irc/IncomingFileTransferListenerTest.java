package marvin.irc;

import marvin.irc.IncomingFileTransferListener.Configuration;
import marvin.messaging.NoopProducer;
import marvin.messaging.Producer;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class IncomingFileTransferListenerTest {

    @Test
    public void testGetDownloadFile() {
        IncomingFileTransferListener incomingFileTransferListener = new IncomingFileTransferListener(null, new Configuration("/downloads"), null, new NoopProducer());
        File downloadFile = incomingFileTransferListener.getDownloadFile("song.mp3");
        assertEquals("/downloads/song.mp3", downloadFile.getAbsolutePath());
    }

    @Test
    public void testConfiguration() {
        Configuration config = new Configuration("/downloads")
            .withExtension("txt", new File("/lists"));

        assertEquals(new File("/downloads"), config.getDirectory("blah.mp3"));
        assertEquals(new File("/lists"), config.getDirectory("blah.txt"));
    }
}
