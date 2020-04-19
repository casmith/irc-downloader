package marvin.http;

import marvin.irc.QueueManager;
import marvin.irc.ReceiveQueueManager;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class QueueResponderTest {

    QueueManager queueManager;
    QueueResponder queueResponder;

    @Before
    public void setup() {
        queueManager = new ReceiveQueueManager();
        queueResponder = new QueueResponder(queueManager);
    }

    @Test
    public void testEmptyQueue() {
        assertEquals("{}", queueResponder.respond());
    }

    @Test
    public void testSingleItemQueue() {
        queueManager.enqueue("someguy", "!someguy send me the song.mp3");
        assertEquals("{someguy=[!someguy send me the song.mp3]}", queueResponder.respond());
    }

    @Test
    public void testDoubleItemQueue() {
        queueManager.enqueue("someguy", "!someguy send me the song.mp3");
        queueManager.enqueue("someguy", "!someguy send me the other song.mp3");
        assertEquals("{someguy=[!someguy send me the song.mp3, !someguy send me the other song.mp3]}", queueResponder.respond());
    }


}