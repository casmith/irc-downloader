package marvin.http;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "queue")
public class QueueModel {
    private List<QueueServerModel> servers = new ArrayList<>();

    public List<QueueServerModel> getServers() {
        return servers;
    }

    public static class QueueServerModel {
        private String nick;
        private List<String> requests;

        public QueueServerModel(String nick) {
            this.nick = nick;
            this.requests = new ArrayList<>();
        }

        public QueueServerModel() {
        }

        public String getNick() {
            return nick;
        }

        public List<String> getRequests() {
            return requests;
        }
    }
}
