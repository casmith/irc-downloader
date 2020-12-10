package marvin.web.server;

import marvin.irc.IrcBot;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/server")
public class ServerResource {

    private final IrcBot ircBot;

    @Inject
    public ServerResource(IrcBot ircBot) {
        this.ircBot = ircBot;
    }

    @GET
    @Path("/users")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listUsers() {
        return Response.status(200)
            .entity(this.ircBot.listUsers()).build();
    }
}
