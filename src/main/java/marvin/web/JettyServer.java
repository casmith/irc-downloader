package marvin.web;

import com.google.inject.Injector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;

public class JettyServer implements HttpServer {

    private static final Logger LOG = LoggerFactory.getLogger(JettyServer.class);
    private Server server;

    static final String APPLICATION_PATH = "/api";
    static final String CONTEXT_ROOT = "/";

    private final int port;

    public JettyServer(int port, Injector injector) {
        this.port = port;
        server = new Server(port);

        // Setup the basic Application "context" at "/".
        // This is also known as the handler tree (in Jetty speak).
        final ServletContextHandler context = new ServletContextHandler(server, CONTEXT_ROOT);
        context.setContextPath("/");
        try {
            String webRootPath = System.getenv("WEBROOT");
            if (webRootPath == null) {
                webRootPath = "dist/marvin";
            }
            final File webroot = new File(webRootPath);
            URI webRootUri = webroot.toURI().resolve("./").normalize();
            LOG.info("webRootUri [{}]", webRootUri);
            context.setBaseResource(Resource.newResource(webRootUri));
            context.setWelcomeFiles(new String[]{"index.html"});
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        configureResteasyServlet(context);

        // Setup the DefaultServlet at "/".
        final ServletHolder defaultServlet = new ServletHolder(new DefaultServlet());
        context.addServlet(defaultServlet, CONTEXT_ROOT);

        context.addEventListener(injector.getInstance(MarvinServletContextListener.class));
    }

    private void configureResteasyServlet(ServletContextHandler context) {
        final ServletHolder restEasyServlet = new ServletHolder(new HttpServletDispatcher());
        restEasyServlet.setInitParameter("resteasy.servlet.mapping.prefix", APPLICATION_PATH);
        restEasyServlet.setInitParameter("javax.ws.rs.Application", Application.class.getName());
        context.addServlet(restEasyServlet, APPLICATION_PATH + "/*");
        restEasyServlet.setInitParameter("resteasy.scan.providers", "true");
    }

    @Override
    public void start() {
        try {
            server.start();
            server.join();
            LOG.info("HTTP server listening on port {}", this.port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
