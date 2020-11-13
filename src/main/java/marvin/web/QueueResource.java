package marvin.web;

import com.google.inject.Inject;
import marvin.irc.QueueManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Queue;

@Path("/queue")
public class QueueResource {

    private QueueManager queueManager;
    private Logger LOG = LoggerFactory.getLogger(QueueResource.class);

    @Inject
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
        LOG.info("In enqueue method with " + model.getServers().size() + " servers");
        LOG.info("QueueManager is " + queueManager.hashCode());
        for (QueueModel.QueueServerModel server : model.getServers()) {
            LOG.info("Enqueueing requests for " + server.getNick());
            for (String request : server.getRequests()) {
                LOG.info("Enqueuing " + request);
                queueManager.enqueue(server.getNick(), request);
            }
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
