package marvin.irc;

import marvin.config.BotConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReceiveQueueProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(ReceiveQueueProcessor.class);
    private final IrcBot bot;
    private final BotConfig config;

    private QueueManager queueManager;

    public ReceiveQueueProcessor(IrcBot bot, BotConfig config, QueueManager queueManager) {
        this.bot = bot;
        this.config = config;
        this.queueManager = queueManager;
    }

    public void process() {
        LOG.info("QueueManager is " + queueManager.hashCode());
        queueManager.getQueues().forEach((nick, queue) -> {
            if (!queue.isEmpty()) {
                if (queueManager.inc(nick)) {
                    String message = queue.poll();
                    LOG.info("Requesting: {}", message);
                    bot.sendToChannel(this.config.getRequestChannel(), message);
                    queueManager.addInProgress(nick, message);
                }
            }
        });
    }
}
