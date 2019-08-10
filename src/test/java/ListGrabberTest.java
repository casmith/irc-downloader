import marvin.ListGrabber;
import marvin.irc.IrcBot;
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
        listGrabber = new ListGrabber(ircBot);
    }

    @Test
    public void check() {
        assertFalse(listGrabber.check("I have a list blah blah @me"));
        assertTrue(listGrabber.check("Type: @marvin for my list"));
    }

    @Test
    public void grab() {
        listGrabber.grab("#somechannel", "Type: @marvin for my list");
        listGrabber.grab("#somechannel", "Type: @marvin for my list");
        Mockito.verify(ircBot, atMostOnce()).sendToChannel("#somechannel", "@marvin");
    }
}
