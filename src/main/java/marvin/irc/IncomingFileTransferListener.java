package marvin.irc;

import marvin.irc.events.DownloadCompleteEvent;
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
        try {
            ReceiveFileTransfer accept = event.accept(file);
            accept.transfer();
            LOG.info("Done downloading " + file.getAbsolutePath());
            success = true;
        } catch (Exception e) {
            LOG.error("File transfer failed", e);
        } finally {
            this.eventSource.publish(new DownloadCompleteEvent(nick, file.getName(), success));
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
