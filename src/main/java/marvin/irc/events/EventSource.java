package marvin.irc.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class EventSource {
    private static final Logger LOG = LoggerFactory.getLogger(EventSource.class);

    private List<Listener> listeners = new ArrayList<>();

    public void publish(Event event) {
        LOG.debug("publishing {}", event);

        for (Listener listener : this.listeners) {
            if (listener != null) {
                listener.notify(event);
            }
        }
    }

    public void subscribe(Listener listener) {
        LOG.debug("Subscribing");
        this.listeners.add(listener);
    }
}
