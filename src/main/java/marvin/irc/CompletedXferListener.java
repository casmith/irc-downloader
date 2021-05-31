package marvin.irc;

import marvin.data.CompletedXferDao;
import marvin.data.DatabaseException;
import marvin.irc.events.DownloadCompleteEvent;
import marvin.irc.events.Event;
import marvin.irc.events.Listener;
import marvin.messaging.Producer;
import marvin.model.CompletedXfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.LocalDateTime;

public class CompletedXferListener implements Listener {

    private static final Logger LOG = LoggerFactory.getLogger(CompletedXferListener.class);

    private final CompletedXferDao completedXferDao;
    private final Producer producer;

    @Inject
    public CompletedXferListener(CompletedXferDao completedXferDao, Producer producer) {
        this.completedXferDao = completedXferDao;
        this.producer = producer;
    }

    @Override
    public void notify(Event event) {
        DownloadCompleteEvent dce = (DownloadCompleteEvent) event;
        try {
            long bytes = dce.isSuccess() ? dce.getBytes() : -1; // indicate failed transfer by setting bytes to -1
            completedXferDao.insert(
                new CompletedXfer(dce.getNick(), "", dce.getFileName(), bytes, LocalDateTime.now()));

            if (dce.isSuccess()) {
                this.producer.enqueue("download-complete", dce.getFileName());
            }

        } catch (DatabaseException e) {
            LOG.error("Error recording completed xfer", e);
        }
    }
}
