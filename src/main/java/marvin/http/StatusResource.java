package marvin.http;

import com.google.inject.Inject;
import marvin.data.CompletedXferDao;
import marvin.model.CompletedXferSummary;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/status")
public class StatusResource {

    private final CompletedXferDao completedXferDao;

    @Inject
    public StatusResource(CompletedXferDao completedXferDao) {
        this.completedXferDao = completedXferDao;
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStatus() {

        CompletedXferSummary summary = this.completedXferDao.summarize();

        return Response.status(200).entity(summary).build();
    }
}
