package marvin.irc;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IrcBotImplTest {
    private IrcBotImpl ircBot = new IrcBotImpl("server",
            6667,
            "testbot",
            "password",
            "adminPassword",
            "#control",
            "#requestChannel",
            "downloadDir",
            new QueueManager());

    @Test
    public void formatMessage() {
        assertEquals("Just a plain message",
                ircBot.formatMessage("Just a plain message"));
        assertEquals("A message with three arguments",
                ircBot.formatMessage("A message {0} {1} {2}", "with", "three", "arguments"));
    }

}