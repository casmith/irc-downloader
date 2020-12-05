package marvin.web.status;

import com.google.inject.Inject;
import marvin.data.CompletedXferDao;
import marvin.irc.IrcBot;
import marvin.model.CompletedXferSummary;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/status")
public class StatusResource {

    private final CompletedXferDao completedXferDao;
    private final IrcBot ircBot;

    @Inject
    public StatusResource(CompletedXferDao completedXferDao, IrcBot ircBot) {
        this.completedXferDao = completedXferDao;
        this.ircBot = ircBot;
    }
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStatus() {
        CompletedXferSummary completedXferSummary = this.completedXferDao.summarize();
        StatusModel statusModel = new StatusModel(ircBot.getServerName(),
            ircBot.getRequestChannel(),
            ircBot.getNick(),
            ircBot.getControlChannel(),
            ircBot.isOnline(),
            completedXferSummary.getCount(),
            completedXferSummary.getTotalBytes());
        return Response.status(200)
            .entity(statusModel)
            .build();
    }
}
