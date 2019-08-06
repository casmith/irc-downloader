package marvin.irc;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class IncomingFileTransferListenerTest {

    @Test
    public void testGetDownloadFile() {
        IncomingFileTransferListener incomingFileTransferListener = new IncomingFileTransferListener(null, "c:/users/john/downloads");
        File downloadFile = incomingFileTransferListener.getDownloadFile("song.mp3");
        Assert.assertEquals("c:\\users\\john\\downloads\\song.mp3", downloadFile.getAbsolutePath());
    }
}