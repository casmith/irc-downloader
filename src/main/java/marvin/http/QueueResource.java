package marvin.http;

import marvin.irc.QueueManager;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Queue;

@Path("/queue")
@Produces(MediaType.TEXT_PLAIN)
public class QueueResource {

    private QueueManager queueManager;

    public QueueResource(QueueManager queueManager) {
        this.queueManager = queueManager;
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response list() {
        return Response.status(200)
                .entity(buildQueueModel()).build();
    }

    public QueueModel buildQueueModel() {
        QueueModel queueModel = new QueueModel();
        queueManager.getQueues()
                .forEach((nick, queue) -> queueModel.getServers().add(buildModel(nick, queue)));
        return queueModel;
    }

    public QueueModel.QueueServerModel buildModel(String nick, Queue<String> queue) {
        QueueModel.QueueServerModel qsm = new QueueModel.QueueServerModel(nick);
        qsm.getRequests().addAll(queue);
        return qsm;
    }
}
