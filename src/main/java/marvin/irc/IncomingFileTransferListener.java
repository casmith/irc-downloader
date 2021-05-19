package marvin.irc;

import marvin.config.Config;
import marvin.config.DownloadDirectoryMapper;
import marvin.data.QueueEntryDao;
import marvin.irc.events.DownloadCompleteEvent;
import marvin.irc.events.DownloadStartedEvent;
import marvin.irc.events.EventSource;
import marvin.messaging.Producer;
import marvin.model.QueueEntry;
import org.pircbotx.User;
import org.pircbotx.dcc.ReceiveFileTransfer;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.IncomingFileTransferEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static java.io.File.separator;

public class IncomingFileTransferListener extends ListenerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(IncomingFileTransferListener.class);

    private final EventSource eventSource;
    private final ReceiveQueueManager queueManager;
    private final Configuration configuration;
    private final Producer producer;
    private final QueueEntryDao queueEntryDao;

    public IncomingFileTransferListener(EventSource eventSource,
                                        Configuration configuration,
                                        ReceiveQueueManager queueManager,
                                        Producer producer, QueueEntryDao queueEntryDao) {
        this.eventSource = eventSource;
        this.configuration = configuration;
        this.queueManager = queueManager;
        this.producer = producer;
        this.queueEntryDao = queueEntryDao;
    }

    @Override
    public void onIncomingFileTransfer(IncomingFileTransferEvent event) {
        User user = event.getUser();
        String sender = null;
        String nick = null;
        if (user != null) {
            nick = user.getNick();
            sender = user.getNick() + "@" + user.getHostmask();
        }

        boolean success = false;
        String batch = null;
        QueueEntry queueEntry = queueEntryDao.find(nick, event.getSafeFilename());
        if (queueEntry != null) {
            batch = queueEntry.getBatch();
        }

        // it's not safe to assume there will always be a queue entry because lists are requested separately
//        if (queueEntry == null) {
//            LOG.warn("Discarding incoming file transfer {} {} because queue entry was not found", nick, event.getSafeFilename());
//            return;
//        }

        File file = getDownloadFile(event.getSafeFilename(), batch);
        LOG.info("Receiving {} from {}", file.getName(), sender);
        this.eventSource.publish(new DownloadStartedEvent(nick, file.getName()));
        long bytes = -1;
        long start = System.currentTimeMillis();
        try {
            ReceiveFileTransfer fileTransfer = acceptTransfer(event, file);
            fileTransfer.transfer();
            long ms = System.currentTimeMillis() - start;
            long seconds = ms / 1000;
            if (seconds == 0) {
                seconds = 1;
            }
            bytes = fileTransfer.getFileSize();
            long kbps = (bytes - 1024) / seconds;
            LOG.info("Done downloading {} in {}s ({} KiB/s)", file.getAbsolutePath(), seconds, kbps);
            if (!queueManager.markCompleted(nick, event.getSafeFilename())) {
                LOG.warn("Nothing to mark completed for {} filename {}", nick, event.getSafeFilename());
            }
            if (batch != null && queueEntryDao.findByBatch(batch).isEmpty()) {
                this.producer.publish("batch-download-complete", batch);
            }

            success = true;
        } catch (Throwable e) {
            LOG.error("File transfer failed", e);
        } finally {
            long duration = System.currentTimeMillis() - start;
            this.eventSource.publish(new DownloadCompleteEvent(nick, file.getName(), bytes, duration, success));
            this.producer.publish("download-complete", file.getName());
        }
    }

    public File getDownloadFile(String fileName, String batch) {
        File downloadDirectory = getDownloadDirectory(fileName, batch);
        return new File(downloadDirectory + separator + fileName);
    }

    public File getDownloadDirectory(String fileName, String batch) {
        File directory = configuration.getDirectory(fileName);
        if (batch != null && !batch.isEmpty()) {
            File batchDirectory = new File(directory + separator + batch);
            batchDirectory.mkdirs(); // ensure the directory exists
            return batchDirectory;
        } else {
            return directory;
        }
    }

    public ReceiveFileTransfer acceptTransfer(IncomingFileTransferEvent event, File file) throws IOException, InterruptedException {
        if (file.exists()) {
            // resume doesn't seem to work, so clear the existing file and start over
            boolean delete = file.delete();
            if (delete) {
                LOG.info("Existing file {} deleted", file.getName());
            }
        }
        return event.accept(file);
    }

    public static class Configuration {
        private final DownloadDirectoryMapper downloadDirectoryMapper;

        public Configuration(String defaultDirectory) {
            this(new DownloadDirectoryMapper(defaultDirectory));
        }

        public Configuration(DownloadDirectoryMapper downloadDirectoryMapper) {
            this.downloadDirectoryMapper = downloadDirectoryMapper;
        }

        public File getDirectory(String filename) {
            return downloadDirectoryMapper.map(filename);
        }

        public Configuration withMapping(String mapping, File destination) {
            downloadDirectoryMapper.add(mapping, destination);
            return this;
        }
    }
}
