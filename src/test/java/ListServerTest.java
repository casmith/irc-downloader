import marvin.ListServer;
import marvin.irc.IrcBot;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class ListServerTest {
    @Mock
    private IrcBot ircBot;
    private ListServer listServer;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        listServer = new ListServer(ircBot, "#requestchannel", "c:\\temp\\list.txt");
    }

    @Test
    public void check() {
        when(ircBot.getNick()).thenReturn("marvinbot");
        assertTrue(listServer.check("#requestchannel", "@marvinbot"));
        assertFalse(listServer.check("#requestchannel","@notmarvin"));
    }

    @Test
    public void send() {
    }
}