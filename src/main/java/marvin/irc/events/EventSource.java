package marvin.irc.events;

import java.util.ArrayList;
import java.util.List;

public class EventSource {

    private List<Listener> listeners = new ArrayList<>();

    public void publish(Event event) {
        for (Listener listener : this.listeners) {
            listener.notify(event);
        }
    }

    public void subscribe(Listener listener) {
        this.listeners.add(listener);
    }
}
