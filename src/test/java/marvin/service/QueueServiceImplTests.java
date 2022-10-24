package marvin.service;

import marvin.data.QueueEntryDao;
import marvin.model.QueueEntry;
import marvin.queue.QueueStatus;
import marvin.queue.ReceiveQueue;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import static org.mockito.Mockito.when;

public class QueueServiceImplTests {

    @Mock private QueueEntryDao mockQueueEntryDao;
    private QueueServiceImpl queueService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        queueService = new QueueServiceImpl(mockQueueEntryDao);
    }

    @Test
    public void testGetQueues_emptyQueues() {
        when(mockQueueEntryDao.selectAll()).thenReturn(Collections.emptyList());
        assert(queueService.getQueues().isEmpty());
    }

    @Test
    public void testGetQueues_oneQueueEntry() {
        when(mockQueueEntryDao.selectAll()).thenReturn(Collections.singletonList(new QueueEntry("", "", "", QueueStatus.PENDING.toString(), "", LocalDateTime.now())));
        Map<String, ReceiveQueue> queues = queueService.getQueues();
        assert(!queues.isEmpty());
    }
}
