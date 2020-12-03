package marvin.web.queue;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "queue")
public class QueueModel {
    private final List<QueueServerModel> servers = new ArrayList<>();

    public List<QueueServerModel> getServers() {
        return servers;
    }

    public static class QueueServerModel {
        private final String nick;
        private final List<QueueRequest> requests;

        public QueueServerModel() {
            this(null);
        }

        public QueueServerModel(String nick) {
            this.nick = nick;
            this.requests = new ArrayList<>();
        }

        public String getNick() {
            return nick;
        }

        public List<QueueRequest> getRequests() {
            return requests;
        }

        public void addRequest(QueueRequest request) {
            this.getRequests().add(request);
        }
    }
}
