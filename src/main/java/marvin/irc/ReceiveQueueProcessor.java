package marvin.irc;

import marvin.config.BotConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReceiveQueueProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(ReceiveQueueProcessor.class);
    private final IrcBot bot;
    private final BotConfig config;

    private ReceiveQueueManager queueManager;

    public ReceiveQueueProcessor(IrcBot bot, BotConfig config, ReceiveQueueManager queueManager) {
        this.bot = bot;
        this.config = config;
        this.queueManager = queueManager;
    }

    public void process() {
        LOG.debug("Processing {} queues...", queueManager.getQueues().size());
        queueManager.getQueues().keySet().forEach((nick) -> {
            if (bot.isNickOnline(nick)) {
                LOG.debug("[{}] is online", nick);
                queueManager.poll(nick)
                    .ifPresent(this::requestFile);
            } else {
                LOG.debug("[{}] is OFFLINE", nick);
            }
        });
    }

    private void requestFile(String message) {
        LOG.info("Requesting: {}", message);
        bot.sendToChannel(this.config.getRequestChannel(), message);
    }
}
