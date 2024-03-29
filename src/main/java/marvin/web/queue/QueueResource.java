package marvin.web.queue;

import com.google.inject.Inject;
import marvin.irc.ReceiveQueueManager;
import marvin.queue.ReceiveQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.print.attribute.standard.Media;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("/queue")
public class QueueResource {

    private ReceiveQueueManager queueManager;
    private static final Logger LOG = LoggerFactory.getLogger(QueueResource.class);

    @Inject
    public QueueResource(ReceiveQueueManager queueManager) {
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
        String batch = UUID.randomUUID().toString();
        LOG.info("In enqueue method with " + model.getServers().size() + " servers");
        LOG.info("QueueManager is " + queueManager.hashCode());
        for (QueueModel.QueueServerModel server : model.getServers()) {
            LOG.info("Enqueueing requests for " + server.getNick());
            for (QueueRequest request : server.getRequests()) {
                LOG.info("Enqueuing " + request);
                // TODO: enqueue the filename rather than the message
                queueManager.enqueue(server.getNick(), request.getRequest(), batch);
            }
        }
        return Response.status(200).build();
    }

    @DELETE
    @Path("/{nick}/{requestString}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("nick") String nick,
                           @PathParam("requestString") String requestString) {
        try {
            if (queueManager.markCompleted(nick, requestString)) {
                return Response.status(200).build();
            } else {
                return Response.status(400).build();
            }
        } catch (Exception e) {
            return Response.status(500).build();
        }
    }

    public QueueModel buildQueueModel() {
        QueueModel queueModel = new QueueModel();
        queueManager.getQueues()
                .forEach((nick, queue) -> queueModel.getServers().add(buildModel(nick, queue)));
        return queueModel;
    }

    public QueueModel.QueueServerModel buildModel(String nick, ReceiveQueue queue) {
        QueueModel.QueueServerModel qsm = new QueueModel.QueueServerModel(nick);
        qsm.getRequests().addAll(queue.getItems().stream()
            .map(i -> new QueueRequest(i.getFilename(), i.getStatus().toString()))
            .collect(Collectors.toList()));
        return qsm;
    }
}
