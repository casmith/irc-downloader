package marvin.irc;

import marvin.irc.events.DownloadCompleteEvent;
import marvin.irc.events.DownloadStartedEvent;
import marvin.irc.events.EventSource;
import org.apache.commons.lang3.StringUtils;
import org.pircbotx.User;
import org.pircbotx.dcc.ReceiveFileTransfer;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.IncomingFileTransferEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class IncomingFileTransferListener extends ListenerAdapter {

    private final EventSource eventSource;
    private final String downloadDirectory;
    private Logger LOG = LoggerFactory.getLogger(IncomingFileTransferListener.class);

    public IncomingFileTransferListener(EventSource eventSource, String downloadDirectory) {
        this.eventSource = eventSource;
        this.downloadDirectory = downloadDirectory;
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
            ReceiveFileTransfer accept = event.accept(file);
            accept.transfer();
            long ms = System.currentTimeMillis() - start;
            long seconds = ms / 1000;
            if (seconds ==  0) {
                seconds = 1;
            }
            bytes = accept.getFileSize();
            long kbps = (bytes - 1024) / seconds;
            LOG.info("Done downloading {} in {}s ({} KiB/s)", file.getAbsolutePath(), seconds, kbps);
            success = true;
        } catch (Exception e) {
            LOG.error("File transfer failed", e);
        } finally {
            long duration = System.currentTimeMillis() - start;
            this.eventSource.publish(new DownloadCompleteEvent(nick, file.getName(), bytes, duration, success));
        }
    }

    public File getDownloadFile(String fileName) {
        String filePath = "";
        if (StringUtils.isNotEmpty(downloadDirectory)) {
            filePath = downloadDirectory + "/";
        }
        filePath += fileName;
        return new File(filePath);
    }
}
