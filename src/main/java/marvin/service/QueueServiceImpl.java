package marvin.service;

import marvin.data.QueueEntryDao;
import marvin.model.QueueEntry;
import marvin.queue.QueueStatus;
import marvin.queue.ReceiveQueue;
import marvin.web.queue.QueueModel;
import marvin.web.queue.QueueRequest;

import javax.inject.Inject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class QueueServiceImpl
    implements QueueService {

    private final QueueEntryDao queueEntryDao;

    @Inject
    public QueueServiceImpl(QueueEntryDao queueEntryDao) {
        this.queueEntryDao = queueEntryDao;
    }

    @Override
    public QueueModel getQueue() {
        QueueModel queueModel = new QueueModel();
        this.getQueues()
            .forEach((nick, queue) -> queueModel.getServers().add(buildModel(nick, queue)));
        return queueModel;

    }

    public ReceiveQueue getQueueForNick(String nick) {
        return getQueues().get(nick);
    }

    // TODO: eliminate use of ReceiveQueue and rewrite this
    public Map<String, ReceiveQueue> getQueues() {
        List<QueueEntry> queueEntries = queueEntryDao.selectAll();
        LinkedHashMap<String, ReceiveQueue> map = new LinkedHashMap<>();
        for (QueueEntry queueEntry : queueEntries) {
            ReceiveQueue queue = map.computeIfAbsent(queueEntry.getName(), ReceiveQueue::new);
            ReceiveQueue.ReceiveQueueItem enqueue = queue.enqueue(queueEntry.getRequestString());
            enqueue.setStatus(QueueStatus.valueOf(queueEntry.getStatus()));
        }
        return map;
    }

    // TODO: eliminate use of ReceiveQueue and rewrite this
    private QueueModel.QueueServerModel buildModel(String nick, ReceiveQueue queue) {
        QueueModel.QueueServerModel qsm = new QueueModel.QueueServerModel(nick);
        qsm.getRequests().addAll(queue.getItems().stream()
            .map(i -> new QueueRequest(i.getFilename(), i.getStatus().toString()))
            .collect(Collectors.toList()));
        return qsm;
    }
}
