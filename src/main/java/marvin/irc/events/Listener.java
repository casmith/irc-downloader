package marvin.irc.events;

import marvin.irc.events.Event;

public interface Listener {
    public void notify(Event event);
}
