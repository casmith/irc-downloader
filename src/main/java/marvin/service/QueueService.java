package marvin.service;

import marvin.queue.ReceiveQueue;
import marvin.web.queue.QueueModel;

public interface QueueService {

    QueueModel getQueue();

    ReceiveQueue getQueueForNick(String server);
}
