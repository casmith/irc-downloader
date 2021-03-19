package marvin.web.history;

import com.google.inject.Inject;
import marvin.data.CompletedXferDao;
import marvin.model.CompletedXfer;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.stream.Collectors;

@Path("/history")
public class HistoryResource {

    private final CompletedXferDao completedXferDao;

    @Inject
    public HistoryResource(CompletedXferDao completedXferDao) {
        this.completedXferDao = completedXferDao;
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get() {
        return Response.status(200)
            .entity(completedXferDao.selectAll().stream()
                .map(this::toModel)
                .collect(Collectors.toList())).build();
    }

    private HistoryModel toModel(CompletedXfer xfer) {
        return new HistoryModel(xfer.getNick(), xfer.getFile(), xfer.getFilesize(), toEpochMillis(xfer.getTimestamp()), xfer.getFilesize() > -1);
    }

    private long toEpochMillis(LocalDateTime localDateTime) {
        return localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

}
