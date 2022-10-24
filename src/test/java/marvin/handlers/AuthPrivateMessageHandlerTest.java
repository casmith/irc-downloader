package marvin.handlers;

import marvin.UserManager;
import marvin.irc.IrcBot;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class AuthPrivateMessageHandlerTest {

    @Mock private IrcBot mockIrcBot;
    @Mock private UserManager mockUserManager;
    private AuthPrivateMessageHandler authPrivateMessageHandler;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.authPrivateMessageHandler = new AuthPrivateMessageHandler(mockIrcBot, mockUserManager);
    }

    @Test
    public void testNoAuth() {
        this.authPrivateMessageHandler.onMessage("user", "not auth");
        verify(mockUserManager, never()).authenticate(anyString(), anyString());
    }

    @Test
    public void testAuthWithSuccess() {
        when(mockUserManager.authenticate("user", "password")).thenReturn(true);
        this.authPrivateMessageHandler.onMessage("user", "auth password");
        verify(mockIrcBot, times(1)).sendPrivateMessage("user", "Authorized!");
    }

    @Test
    public void testAuthWithFailure() {
        when(mockUserManager.authenticate("user", "password")).thenReturn(false);
        this.authPrivateMessageHandler.onMessage("user", "auth password");
        verify(mockIrcBot, times(1)).sendPrivateMessage("user", "Sorry, try again?");
    }
}
