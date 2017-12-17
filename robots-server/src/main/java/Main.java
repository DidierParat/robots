import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(final String[] args) {
        final DbService dbService;
        try {
            dbService = new DbService(
                    "jdbc:mysql://localhost:3306/robots?useSSL=false",
                    "root",
                    "root");
        } catch (final SQLException e) {
            LOGGER.log(Level.SEVERE, "Could not connect to DB.", e);
            return;
        }
        final RobotsResource robotsResource
                = new RobotsResource(dbService, new ObjectMapper());
        final ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.register(robotsResource);
        final ServletHolder servlet
                = new ServletHolder(new ServletContainer(resourceConfig));

        final Server server = new Server(8080);
        final ServletContextHandler context
                = new ServletContextHandler(server, "/*");
        context.addServlet(servlet, "/*");
        try {
            server.start();
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "Could not start server.", e);
            return;
        }
    }
}
