package marvin.irc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Singleton;
import marvin.data.QueueEntryDao;
import marvin.messaging.Producer;
import marvin.model.QueueEntry;
import marvin.queue.QueueStatus;
import marvin.queue.ReceiveQueue;
import marvin.web.queue.QueueModel;
import marvin.web.queue.QueueRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static marvin.queue.QueueStatus.PENDING;
import static marvin.queue.QueueStatus.REQUESTED;

@Singleton
public class ReceiveQueueManager {

    private static final Logger LOG = LoggerFactory.getLogger(ReceiveQueueManager.class);
    private final Map<String, Integer> limits = new HashMap<>();
    private final Map<String, Integer> queued = new HashMap<>();
    private final QueueEntryDao queueEntryDao;
    private final Producer producer;

    @Inject
    public ReceiveQueueManager(QueueEntryDao queueEntryDao,
                               Producer producer) {
        this.queueEntryDao = queueEntryDao;
        this.producer = producer;
    }

    public void addInProgress(String nick, String message) {
        synchronized (this) {
            QueueEntry queueEntry = queueEntryDao.find(nick, message);
            if (queueEntry != null) {
                LOG.info("Marking {} as REQUESTED", message);
                queueEntryDao.updateStatus(queueEntry, REQUESTED);
                publishQueueStatus();
            } else {
                LOG.info("Nothing to set in progress for {} / {}", nick, message);
            }
        }
    }

    public void retry(String nick, String filename) {
        synchronized (this) {
            QueueEntry queueEntry = queueEntryDao.find(nick, filename);
            if (queueEntry != null) {
                LOG.info("Retrying !" + nick + " " + filename);
                queueEntryDao.updateStatus(queueEntry, PENDING);
                publishQueueStatus();
            } else {
                LOG.info("Nothing to retry for {} / {}", nick, filename);
            }
        }
    }

    // TODO: remove
    public Map<String, Queue<String>> getInProgress() {
        synchronized (this) {
            List<QueueEntry> queueEntries = queueEntryDao.findByStatus(REQUESTED);
            return entriesToMap(queueEntries);
        }
    }

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

    public ReceiveQueue getQueue(String nick) {
        return getQueues().get(nick);
    }

    private Map<String, Queue<String>> entriesToMap(List<QueueEntry> queueEntries) {
        LinkedHashMap<String, Queue<String>> map = new LinkedHashMap<>();
        for (QueueEntry queueEntry : queueEntries) {
            Queue<String> queue = map.computeIfAbsent(queueEntry.getName(), n -> new LinkedList<>());
            queue.offer(queueEntry.getRequestString());
        }
        return map;
    }

    /** replaces spaces with underscores to make comparisons easier */
    private String normalizeFilename(String filename) {
        return filename.replace(" ", "_");
    }

    public boolean markCompleted(String nick, String filename) {
        synchronized (this) {
            LOG.info("Marking {} - {} completed", nick, filename);
            QueueEntry queueEntry = queueEntryDao.find(nick, filename);
            if (queueEntry == null) {
                LOG.error("Failed to remove {} from queue", filename);
                return false;
            } else {
                queueEntryDao.delete(queueEntry);
                publishQueueStatus();
                return true;
            }
        }
    }

    public Optional<String> poll(String nick) {
        synchronized (this) {
            List<QueueEntry> queueEntries = queueEntryDao.findByNickAndStatus(nick, PENDING);
            if (!queueEntries.isEmpty() && this.inc(nick)) {
                QueueEntry queueEntry = queueEntries.get(0);
                queueEntryDao.updateStatus(queueEntry, REQUESTED);
                publishQueueStatus();
                return Optional.of(queueEntry.getRequestString());
            } else {
                return Optional.empty();
            }
        }
    }

    // TODO: remove
    public boolean dec(String nick) {
        synchronized (this) {
            nick = nick.toLowerCase();
            boolean success = false;
            Integer current = getCurrent(nick);
            if (current > 0) {
                queued.put(nick, --current);
                success = true;
                printStats(nick);
            }
            return success;
        }
    }

    // TODO: remove
    public boolean inc(String nick) {
        synchronized (this) {
            nick = nick.toLowerCase();
            boolean success = false;
            Integer limit = getLimit(nick);
            Integer current = getCurrent(nick);
            if (current < limit) {
                queued.put(nick, ++current);
                success = true;
                printStats(nick);
            }
            return success;
        }
    }
    public void updateLimit(String nick, int limit) {
        // artificially cap at 10 for testing
        if (limit > 10) {
            limit = 10;
        }
        synchronized (this) {
            nick = nick.toLowerCase();
            limits.put(nick, limit);
        }
    }

    public void enqueue(String nick, String message, String batch) {
        synchronized (this) {
            LOG.info("Enqueueing [{}] - [{}]", nick, message);
            QueueEntry queueEntry = new QueueEntry(nick, batch, message, PENDING.toString(), "", LocalDateTime.now());
            queueEntryDao.insert(queueEntry);
            publishQueueStatus();
        }
    }

    private Integer getCurrent(String nick) {
        return queued.getOrDefault(nick, 0);
    }

    // TODO: remove, move to ReceiveQueue
    public Integer getLimit(String nick) {
        nick = nick.toLowerCase();
        return limits.getOrDefault(nick, 10);
    }

    private void printStats(String nick) {
        nick = nick.toLowerCase();
        Integer current = getCurrent(nick);
        Integer limit = getLimit(nick);
        LOG.info("Queue for {}: {}/{}", nick, current, limit);
    }

    private void publishQueueStatus() {
        LOG.info("Publishing queue status");
        ObjectMapper mapper = new ObjectMapper();
        String message = null;
        try {
            message = mapper.writeValueAsString(buildQueueModel());
        } catch (JsonProcessingException e) {
            LOG.error("Failed to convert queue to a json payload", e);
        }
        this.producer.publishTopic("queue-updated", message);
    }


    // TODO: below shamelessly copied from QueueResource
    private QueueModel buildQueueModel() {
        QueueModel queueModel = new QueueModel();
        this.getQueues()
            .forEach((nick, queue) -> queueModel.getServers().add(buildModel(nick, queue)));
        return queueModel;
    }

    private QueueModel.QueueServerModel buildModel(String nick, ReceiveQueue queue) {
        QueueModel.QueueServerModel qsm = new QueueModel.QueueServerModel(nick);
        qsm.getRequests().addAll(queue.getItems().stream()
            .map(i -> new QueueRequest(i.getFilename(), i.getStatus().toString()))
            .collect(Collectors.toList()));
        return qsm;
    }
}
