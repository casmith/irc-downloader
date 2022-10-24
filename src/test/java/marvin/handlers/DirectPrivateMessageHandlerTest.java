package marvin.handlers;

import marvin.UserManager;
import marvin.irc.IrcBot;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class DirectPrivateMessageHandlerTest {

    @Mock private IrcBot ircBot;
    @Mock private UserManager userManager;
    private DirectPrivateMessageHandler handler;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        handler = new DirectPrivateMessageHandler(ircBot, userManager);
    }

    @Test
    public void testUnauthorized() {
        when(userManager.isAuthorized(anyString())).thenReturn(false);
        handler.onMessage("user", "message to channel");
        verify(ircBot, never()).messageChannel(anyString());
    }

    @Test
    public void testAuthorizedButNotDirectMessage() {
        when(userManager.isAuthorized(anyString())).thenReturn(true);
        handler.onMessage("user", "message to channel");
        verify(ircBot, never()).messageChannel(anyString());
    }

    @Test
    public void testAuthorizedAndDirectMessage() {
        when(userManager.isAuthorized(anyString())).thenReturn(true);
        handler.onMessage("user", "direct message to channel");
        verify(ircBot, times(1)).messageChannel("message to channel");
    }
}
