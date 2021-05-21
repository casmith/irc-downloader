package marvin.irc;

import marvin.irc.events.EventSource;
import marvin.messaging.NoopProducer;
import marvin.messaging.Producer;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class IrcBotImplTest {
    private IrcBotImpl ircBot;

    {
        NoopProducer producer = new NoopProducer();
        ircBot = new IrcBotImpl("server",
            6667,
            "testbot",
            "password",
            "adminPassword",
            "#control",
            "#requestChannel",
            "downloadDir",
            Collections.emptyMap(),
            new ReceiveQueueManager(null, producer),
            null,
            new EventSource()
        );
    }

    @Test
    public void formatMessage() {
        assertEquals("Just a plain message",
                ircBot.formatMessage("Just a plain message"));
        assertEquals("A message with three arguments",
                ircBot.formatMessage("A message {0} {1} {2}", "with", "three", "arguments"));
    }
}
