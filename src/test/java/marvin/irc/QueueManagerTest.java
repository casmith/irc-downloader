package marvin.irc;

import org.junit.Assert;
import org.junit.Test;

import java.util.Queue;

public class QueueManagerTest {
    @Test
    public void testRetry() {
        QueueManager queueManager = new QueueManager();
        queueManager.addInProgress("SomeGuy", "!SomeGuy Dokken (Erase The Slate) - 06 - One.mp3  ::INFO:: 5.02Mb  VBR/44.10/JS");
        queueManager.addInProgress("SomeGuy","!Moros Dokken (Erase The Slate) - 07 - Who Believes.mp3  ::INFO:: 7.34Mb  VBR/44.10/JS");
        queueManager.addInProgress("SomeGuy","!Moros Dokken (Erase The Slate) - 08 - Voice Of The Soul.mp3  ::INFO:: 6.88Mb  VBR/44.10/JS");
        queueManager.addInProgress("SomeGuy","!Moros Dokken (Erase The Slate) - 09 - Crazy Mary Goes Round.mp3  ::INFO:: 4.62Mb  VBR/44.10/JS");
        queueManager.addInProgress("SomeGuy","!Moros Dokken (Erase The Slate) - 10 - Haunted Lullabye.mp3  ::INFO:: 7.43Mb  VBR/44.10/JS");
        queueManager.addInProgress("SomeGuy","!Moros Dokken (Erase The Slate) - 11 - In Your Honor.mp3  ::INFO:: 6.37Mb  VBR/44.10/JS");
        queueManager.addInProgress("SomeGuy","!Moros Dokken (Erase The Slate) - 12 - Little Brown Pill (Bonus Track).mp3  ::INFO:: 1.98Mb  VBR/44.10/JS");
        queueManager.retry("SomeGuy", "Dokken (Erase The Slate) - 06 - One.mp3");
        Queue<String> someGuyQueue = queueManager.getQueue("SomeGuy");
        Assert.assertEquals(1, someGuyQueue.size());
        Assert.assertEquals("!SomeGuy Dokken (Erase The Slate) - 06 - One.mp3  ::INFO:: 5.02Mb  VBR/44.10/JS", someGuyQueue.peek());
    }
}