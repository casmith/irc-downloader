package marvin.irc;

import marvin.config.BotConfig;
import marvin.queue.ReceiveQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Map;

public class ReceiveQueueProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(ReceiveQueueProcessor.class);
    private final IrcBot bot;
    private final BotConfig config;
    private final ReceiveQueueManager queueManager;

    @Inject
    public ReceiveQueueProcessor(IrcBot bot,
                                 BotConfig config,
                                 ReceiveQueueManager queueManager) {
        this.bot = bot;
        this.config = config;
        this.queueManager = queueManager;
    }

    public void process() {
        try {
            Map<String, ReceiveQueue> queues = queueManager.getQueues();
            LOG.debug("Processing {} queues...", queues.size());
            queues.keySet().forEach((nick) -> {
                if (bot.isNickOnline(nick)) {
                    LOG.debug("[{}] is online", nick);
                    queueManager.poll(nick)
                        .ifPresent(this::requestFile);
                } else {
                    LOG.debug("[{}] is OFFLINE", nick);
                }
            });
        } catch (Exception e) {
            LOG.warn("Exception while trying to process queues", e);
        }
    }

    private void requestFile(String message) {
        LOG.info("Requesting: {}", message);
        bot.sendToChannel(this.config.getRequestChannel(), message);
    }
}
