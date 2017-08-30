import org.pircbotx.dcc.ReceiveFileTransfer;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.IncomingFileTransferEvent;

import java.io.File;

public class IncomingFileTransferListener extends ListenerAdapter {
    @Override
    public void onIncomingFileTransfer(IncomingFileTransferEvent event) throws Exception {
        File file = new File(event.getSafeFilename());

        System.out.println("Receiving " + file.getName() + " from " + event.getUserHostmask());
        ReceiveFileTransfer xfer = event.accept(file);
        xfer.transfer();
        System.out.println("Done downloading " + file.getName());
    }
}
