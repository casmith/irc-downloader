package marvin.http;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JettyServer implements HttpServer {

    private static Logger LOG = LoggerFactory.getLogger(JettyServer.class);
    private Server server;

    public JettyServer(int port) {
        server  = new Server(8000);
    }

    @Override
    public void start() {
        server.setHandler(new AbstractHandler() {
            @Override
            public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
                response.setContentType("text/plain; charset=utf-8");
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().println(baseRequest.toString());
                LOG.info(request.getReader().readLine());
                // TODO: send request
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
}
