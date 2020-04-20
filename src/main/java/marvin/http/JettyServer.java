package marvin.http;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
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

    static final String APPLICATION_PATH = "/api";
    static final String CONTEXT_ROOT = "/";

    public JettyServer(int port) {
        server = new Server(port);


        // Setup the basic Application "context" at "/".
        // This is also known as the handler tree (in Jetty speak).
        final ServletContextHandler context = new ServletContextHandler(
                server, CONTEXT_ROOT);

        // Setup RESTEasy's HttpServletDispatcher at "/api/*".
        final ServletHolder restEasyServlet = new ServletHolder(
                new HttpServletDispatcher());
        restEasyServlet.setInitParameter("resteasy.servlet.mapping.prefix",
                APPLICATION_PATH);
        restEasyServlet.setInitParameter("javax.ws.rs.Application",
                Application.class.getName());
        context.addServlet(restEasyServlet, APPLICATION_PATH + "/*");

        restEasyServlet.setInitParameter("resteasy.scan.providers", "true");

        // Setup the DefaultServlet at "/".
        final ServletHolder defaultServlet = new ServletHolder(
                new DefaultServlet());
        context.addServlet(defaultServlet, CONTEXT_ROOT);


    }

    private Map<String, Responder> responderMap = new HashMap<>();

    @Override
    public void start() {
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
