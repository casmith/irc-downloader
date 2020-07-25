package marvin.list;

import marvin.irc.IrcBot;
import marvin.list.ListGrabber;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atMostOnce;

public class ListGrabberTest {

    @Mock
    private IrcBot ircBot;
    private ListGrabber listGrabber;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        listGrabber = new ListGrabber(ircBot, null, new InMemoryListManager(), true);
    }

    @Test
    public void check() {
        assertFalse(listGrabber.check("I have a list blah blah @me"));
        assertTrue(listGrabber.check("Type: @marvin for my list"));
        assertTrue(listGrabber.check("� Type: @Mr_Mp3 For My List Of: 454,164 Files � Slots: 5/5 � Queued: 0 � Speed: 0cps � Next: NOW � Served: 139,868 � List: Aug 15th � Search: ON � Mode: Normal �"));
    }

    @Test
    public void grab() {
        listGrabber.grab("#somechannel", "Type: @marvin for my list");
        listGrabber.grab("#somechannel", "Type: @marvin for my list");
        Mockito.verify(ircBot, atMostOnce()).sendToChannel("#somechannel", "@marvin");
    }
}