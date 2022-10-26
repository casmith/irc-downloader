package marvin.web.queue;

import marvin.irc.ReceiveQueueManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.core.Response;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class QueueResourceTest {

    @Mock private ReceiveQueueManager queueManager;
    private QueueResource resource;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        resource = new QueueResource(queueManager);
    }

    @Test
    public void testDelete_success() {
        when(queueManager.markCompleted("user", "!user thing.mp3")).thenReturn(true);
        Response response = resource.delete("user", "!user thing.mp3");
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testDelete_notFound() {
        when(queueManager.markCompleted("user", "!user thing.mp3")).thenReturn(false);
        Response response = resource.delete("user", "!user thing.mp3");
        assertEquals(400, response.getStatus());
    }

    @Test
    public void testDelete_error() {
        when(queueManager.markCompleted("user", "!user thing.mp3")).thenThrow(new RuntimeException("wtf?!"));
        Response response = resource.delete("user", "!user thing.mp3");
        assertEquals(500, response.getStatus());
    }
}
