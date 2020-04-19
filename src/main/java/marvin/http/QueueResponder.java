package marvin.http;

import marvin.irc.QueueManager;

public class QueueResponder implements Responder {

    private final QueueManager queueManager;

    public QueueResponder(QueueManager queueManager) {
        this.queueManager = queueManager;
    }

    public String respond() {
        return queueManager.getQueues().toString();
    }
}
