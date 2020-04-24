package marvin.irc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;

public class SendQueueProcessor {
    private static Logger LOG = LoggerFactory.getLogger(SendQueueProcessor.class);

    private QueueManager sendQueueManager;
    private IrcBot bot;

    @Inject
    public SendQueueProcessor(IrcBot bot, QueueManager sendQueueManager) {
        this.bot = bot;
        this.sendQueueManager = sendQueueManager;
    }

    public void process() {
        sendQueueManager.getQueues().forEach((nick, queue) -> {
            if (!queue.isEmpty()) {
                if (sendQueueManager.inc(nick)) {
                    String file = queue.poll();
                    if (file != null) {
                        try {
                            LOG.info("Sending {} to {}", file, nick);
                            bot.sendFile(nick, new File(file));
                        } catch (Exception e) {
                            LOG.error("Error sending file {} to {}: {}", file, nick, e.getMessage());
                        } finally {
                            sendQueueManager.dec(nick);
                        }
                    }
                }
            }
        });
    }
}
