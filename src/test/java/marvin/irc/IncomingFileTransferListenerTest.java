package marvin.irc;

import marvin.data.JdbcTemplate;
import marvin.data.sqlite3.QueueEntrySqlite3Dao;
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
        QueueEntrySqlite3Dao dao = new QueueEntrySqlite3Dao(new JdbcTemplate("jdbc:sqlite:./marvin.db"));
        IncomingFileTransferListener incomingFileTransferListener = new IncomingFileTransferListener(null, new Configuration("/downloads"), null, new NoopProducer(), dao);
        File downloadFile = incomingFileTransferListener.getDownloadFile("song.mp3", "batch1");
        assertEquals("/downloads/batch1/song.mp3", downloadFile.getAbsolutePath());
    }

    @Test
    public void testConfiguration() {
        Configuration config = new Configuration("/downloads")
            .withMapping("txt", new File("/lists"));

        assertEquals(new File("/downloads"), config.getDirectory("blah.mp3"));
        assertEquals(new File("/lists"), config.getDirectory("blah.txt"));
    }
}
