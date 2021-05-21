package marvin.web.history;

import com.google.inject.Inject;
import marvin.service.HistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/history")
public class HistoryResource {

    private final HistoryService historyService;

    @Inject
    public HistoryResource(HistoryService historyService) {
        this.historyService = historyService;
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get() {
        return Response.status(200)
            .entity(historyService.getHistory()).build();
    }
}
