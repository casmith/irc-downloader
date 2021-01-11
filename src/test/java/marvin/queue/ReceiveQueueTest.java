package marvin.queue;

import org.junit.Test;

import static org.junit.Assert.*;

public class ReceiveQueueTest {

    private ReceiveQueue receiveQueue = new ReceiveQueue("nick");

    @Test
    public void testEnqueue_forEmptyQueue() {
        ReceiveQueue.ReceiveQueueItem item = receiveQueue.enqueue("filename.mp3");
        assertEquals(QueueStatus.PENDING, receiveQueue.get(item.getUuid()).getStatus());
        ReceiveQueue.ReceiveQueueItem dequeuedItem = receiveQueue.dequeue();
        assertEquals(item.getUuid(), dequeuedItem.getUuid());
        assertEquals("nick", dequeuedItem.getNick());
        assertEquals(QueueStatus.REQUESTED, dequeuedItem.getStatus());
    }

    @Test
    public void testEnqueue_duplicateItem() {
        receiveQueue.enqueue("filename.mp3");
        receiveQueue.enqueue("filename.mp3");
        assertEquals(1, receiveQueue.size());
    }

    @Test(expected=EmptyQueueException.class)
    public void testDequeue_forEmptyQueue() {
        receiveQueue.dequeue();
    }

    @Test
    public void testPoll_forEmptyQueue() {
        assertFalse(receiveQueue.poll().isPresent());
    }

    @Test
    public void testPoll_forNonEmptyQueue() {
        receiveQueue.enqueue("filename.mp3");
        assertTrue(receiveQueue.poll().isPresent());
    }

    @Test
    public void testDequeue_setsStatusToRequested() {
        receiveQueue.enqueue("filename.mp3");
        ReceiveQueue.ReceiveQueueItem dequeued = receiveQueue.dequeue();
        assertEquals(QueueStatus.REQUESTED, dequeued.getStatus());
    }

    @Test
    public void testDequeue_skipsNonPendingItems() {
        receiveQueue.enqueue("filename.mp3");
        receiveQueue.enqueue("filename2.mp3");
        receiveQueue.dequeue();
        ReceiveQueue.ReceiveQueueItem secondDequeued = receiveQueue.dequeue();
        assertEquals("filename2.mp3", secondDequeued.getFilename());
    }

    @Test
    public void testFind_emptyQueue() {
        assertFalse(receiveQueue.find("filename.mp3").isPresent());
    }

    @Test
    public void testFind_nonEmptyQueue() {
        receiveQueue.enqueue("filename.mp3");
        assertTrue(receiveQueue.find("filename.mp3").isPresent());
    }

    @Test
    public void testFind_unmatchedEntry() {
        receiveQueue.enqueue("filename.mp3");
        assertFalse(receiveQueue.find("cool song.mp3").isPresent());
    }
}
