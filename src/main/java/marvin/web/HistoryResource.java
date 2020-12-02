package marvin.web;

import com.google.inject.Inject;
import marvin.data.CompletedXferDao;
import marvin.model.CompletedXfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/history")
public class HistoryResource {

    private final CompletedXferDao completedXferDao;
    private Logger LOG = LoggerFactory.getLogger(HistoryResource.class);

    @Inject
    public HistoryResource(CompletedXferDao completedXferDao) {
        this.completedXferDao = completedXferDao;
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get() {
        List<CompletedXfer> completedXfers = completedXferDao.selectAll();
        return Response.status(200)
            .entity(completedXfers).build();
    }
}
