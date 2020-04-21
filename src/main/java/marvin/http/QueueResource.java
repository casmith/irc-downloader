package marvin.http;

import marvin.irc.QueueManager;

import javax.ws.rs.*;
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

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response enqueue(QueueModel model) {
        for (QueueModel.QueueServerModel server : model.getServers()) {
            final Queue<String> queue = queueManager.getQueue(server.getNick());
            queue.addAll(server.getRequests());
        }
        return Response.status(200).build();
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
