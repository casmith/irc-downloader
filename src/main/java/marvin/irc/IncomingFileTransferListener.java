package marvin.irc;

import org.pircbotx.User;
import org.pircbotx.dcc.ReceiveFileTransfer;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.IncomingFileTransferEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class IncomingFileTransferListener extends ListenerAdapter {

    private final EventSource eventSource;
    private Logger LOG = LoggerFactory.getLogger(IncomingFileTransferListener.class);

    public IncomingFileTransferListener(EventSource eventSource) {
        this.eventSource = eventSource;
    }

    @Override
    public void onIncomingFileTransfer(IncomingFileTransferEvent event) throws Exception {
        File file = new File(event.getSafeFilename());
        User user = event.getUser();
        String sender = null;
        String nick = null;
        if (user != null) {
            nick = user.getNick();
            sender = user.getNick() + "@" + user.getHostmask();
        }
        boolean success = false;
        LOG.info("Receiving {} from {}", file.getName(), sender);
        try {
            ReceiveFileTransfer accept = event.accept(file);
            accept.transfer();
            LOG.info("Done downloading " + file.getName());
            success = true;
        } catch (Exception e) {
            LOG.error("File transfer failed", e);
        } finally {
            this.eventSource.publish(new DownloadCompleteEvent(nick, file.getName(), success));
        }
    }
}
