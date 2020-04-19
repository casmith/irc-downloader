package marvin.http;

import marvin.irc.QueueManager;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JettyServer implements HttpServer {

    private static Logger LOG = LoggerFactory.getLogger(JettyServer.class);
    private Server server;

    public JettyServer(int port) {
        server  = new Server(port);
    }

    private Map<String, Responder> responderMap = new HashMap<>();

    @Override
    public void start() {
        server.setHandler(new AbstractHandler() {
            @Override
            public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
                response.setContentType("text/plain; charset=utf-8");
                response.setStatus(HttpServletResponse.SC_OK);
                if (responderMap.containsKey(target)) {
                    Responder responder = responderMap.get(target);
                    response.getWriter().println(responder.respond());
                } else {
                    response.getWriter().println("default...");;
                }
                baseRequest.setHandled(true);
            }
        });

        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            server.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void registerResponder(String path, Responder responder) {
        this.responderMap.put(path, responder);
    }
}
