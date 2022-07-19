package marvin.web.server;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import marvin.irc.IrcBot;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

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

    @GET
    @Path("/logging")
    @Produces(MediaType.APPLICATION_JSON)
    public Response setLogLevel() {

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        Logger root = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

        System.out.println("Current root logger level: " + root.getLevel());

        root.setLevel(Level.DEBUG);

        System.out.println("Current root logger level: " + root.getLevel());

        return Response.status(200).build();
    }
}
