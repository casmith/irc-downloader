package marvin.handlers;

import marvin.irc.NoticeHandler;
import marvin.irc.ReceiveQueueManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;

public class QueueLimitNoticeHandler implements NoticeHandler {

    private static final Logger LOG = LoggerFactory.getLogger(QueueLimitNoticeHandler.class);

    private ReceiveQueueManager queueManager;

    public QueueLimitNoticeHandler(ReceiveQueueManager queueManager) {
        this.queueManager = queueManager;
    }

    @Override
    public void onNotice(String nick, String message) {
        LOG.info("NOTICE {} - {}", nick, message);
        Pattern pattern = Pattern.compile(".*Allowed: ([0-9]+) of ([0-9]+).*");
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            queueManager.updateLimit(nick, parseInt(matcher.group(2)));
        }
    }
}
