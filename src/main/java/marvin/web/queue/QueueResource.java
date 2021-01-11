package marvin.web.queue;

import com.google.inject.Inject;
import marvin.irc.ReceiveQueueManager;
import marvin.queue.ReceiveQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Queue;
import java.util.stream.Collectors;

@Path("/queue")
public class QueueResource {

    private ReceiveQueueManager queueManager;
    private Logger LOG = LoggerFactory.getLogger(QueueResource.class);

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
        LOG.info("In enqueue method with " + model.getServers().size() + " servers");
        LOG.info("QueueManager is " + queueManager.hashCode());
        for (QueueModel.QueueServerModel server : model.getServers()) {
            LOG.info("Enqueueing requests for " + server.getNick());
            for (QueueRequest request : server.getRequests()) {
                LOG.info("Enqueuing " + request);
                // TODO: enqueue the filename rather than the message
                queueManager.enqueue(server.getNick(), request.getRequest());
            }
        }
        return Response.status(200).build();
    }

    public QueueModel buildQueueModel() {
        QueueModel queueModel = new QueueModel();
        queueManager.getQueues()
                .forEach((nick, queue) -> queueModel.getServers().add(buildModel(nick, queue)));
        queueManager.getInProgress()
            .forEach((nick, queue) -> queueModel.getServers().add(buildModel(nick, queue, "REQUESTED")));
        return queueModel;
    }

    public QueueModel.QueueServerModel buildModel(String nick, Queue<String> queue, String status) {
        QueueModel.QueueServerModel qsm = new QueueModel.QueueServerModel(nick);
        qsm.getRequests().addAll(queue.stream().map(r -> new QueueRequest(r, status)).collect(Collectors.toList()));
        return qsm;
    }

    public QueueModel.QueueServerModel buildModel(String nick, ReceiveQueue queue) {
        QueueModel.QueueServerModel qsm = new QueueModel.QueueServerModel(nick);
        qsm.getRequests().addAll(queue.getItems().stream()
            .map(i -> new QueueRequest(i.getStatus().toString(), i.getFilename()))
            .collect(Collectors.toList()));
        return qsm;
    }
}
