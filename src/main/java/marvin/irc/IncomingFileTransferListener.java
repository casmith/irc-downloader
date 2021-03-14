package marvin.irc;

import marvin.irc.events.DownloadCompleteEvent;
import marvin.irc.events.DownloadStartedEvent;
import marvin.irc.events.EventSource;
import org.apache.commons.io.FilenameUtils;
import org.pircbotx.User;
import org.pircbotx.dcc.ReceiveFileTransfer;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.IncomingFileTransferEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class IncomingFileTransferListener extends ListenerAdapter {

    private final EventSource eventSource;
    private final ReceiveQueueManager queueManager;
    private final Configuration configuration;
    private Logger LOG = LoggerFactory.getLogger(IncomingFileTransferListener.class);

    public IncomingFileTransferListener(EventSource eventSource, Configuration configuration, ReceiveQueueManager queueManager) {
        this.eventSource = eventSource;
        this.configuration = configuration;
        this.queueManager = queueManager;
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
        File file = getDownloadFile(event.getSafeFilename());
        LOG.info("Receiving {} from {}", file.getName(), sender);
        this.eventSource.publish(new DownloadStartedEvent(nick, file.getName()));
        long bytes = -1;
        long start = System.currentTimeMillis();
        try {
            ReceiveFileTransfer fileTransfer = event.accept(file);
            fileTransfer.transfer();
            long ms = System.currentTimeMillis() - start;
            long seconds = ms / 1000;
            if (seconds ==  0) {
                seconds = 1;
            }
            bytes = fileTransfer.getFileSize();
            long kbps = (bytes - 1024) / seconds;
            LOG.info("Done downloading {} in {}s ({} KiB/s)", file.getAbsolutePath(), seconds, kbps);
            queueManager.markCompleted(nick, event.getSafeFilename());
            success = true;
        } catch (Exception e) {
            LOG.error("File transfer failed", e);
        } finally {
            long duration = System.currentTimeMillis() - start;
            this.eventSource.publish(new DownloadCompleteEvent(nick, file.getName(), bytes, duration, success));
        }
    }

    public File getDownloadFile(String fileName) {
        File directory = configuration.getDirectory(fileName);
        String filePath = directory + File.separator + fileName;
        return new File(filePath);
    }

    public static class Configuration {
        Map<String, File> mappings = new HashMap<>();
        File defaultDirectory;


        public Configuration(String defaultDirectory) {
            this(new File(defaultDirectory));
        }

        public Configuration(File defaultDirectory) {
            this.defaultDirectory = defaultDirectory;
        }

        public Configuration withExtension(String extension, File directory) {
            mappings.put(extension, directory);
            return this;
        }

        public File getDirectory(String filename) {
            File file = mappings.get(FilenameUtils.getExtension(filename));
            return (file != null) ? file: defaultDirectory;
        }
    }
}
