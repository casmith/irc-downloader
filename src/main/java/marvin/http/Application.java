package marvin.http;

import marvin.Client;

import java.util.HashSet;
import java.util.Set;

public class Application extends javax.ws.rs.core.Application {
    @Override
    public Set<Object> getSingletons() {
        Set<Object> set = new HashSet<>();
        // TODO: how to properly inject dependencies?
        set.add(new QueueResource(Client.getStaticQueueManager()));
        return set;
    }
}


