package marvin.irc;

import marvin.config.BotConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;

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
        queueManager.getQueues().forEach((nick, queue) -> {
            if (!queue.isEmpty()) {
                if (bot.isNickOnline(nick) && queueManager.inc(nick)) {
                    String message = queue.poll();
                    LOG.info("Requesting: {}", message);
                    bot.sendToChannel(this.config.getRequestChannel(), message);
                    queueManager.addInProgress(nick, message);
                }
            }
        });
    }
}
