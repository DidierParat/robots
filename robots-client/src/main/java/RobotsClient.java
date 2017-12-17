import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import models.Constants;
import models.DeleteRequest;
import models.JsonMessage;
import models.ListCompatibleRequest;
import models.ReadRequest;
import models.RobotPart;
import models.UpdateRequest;

public class RobotsClient {
    private static final String PATH_ADD = "add";
    private static final String PATH_READ = "read";
    private static final String PATH_UPDATE = "update";
    private static final String PATH_DELETE = "delete";
    private static final String PATH_LIST_ALL = "list-all";
    private static final String PATH_LIST_COMPATIBLE = "list-compatible";

    private final Client client;
    private final URI robotsServerUri;
    private final ObjectMapper mapper;

    public RobotsClient(final Client client, final URI robotsServerUri) {
        this.client = client;
        this.robotsServerUri = robotsServerUri;
        this.mapper = new ObjectMapper();
    }

    // TODO(didier) javadoc
    public void add(
            final String name,
            final String serialNumber,
            final String manufacturer,
            final Integer weight,
            final String[] compatibilities)
            throws RobotsClientException {
        final RobotPart robotPart
                = new RobotPart(
                        name,
                        serialNumber,
                        manufacturer,
                        weight,
                        compatibilities);
        final Response response
                = sendPostRequest(PATH_ADD, robotPart);
        checkHttpResponse(response);
    }

    public RobotPart read(
            final String serialNumber) throws RobotsClientException {
        final ReadRequest readRequest
                = new ReadRequest(serialNumber);
        final Response response
                = sendPostRequest(PATH_READ, readRequest);
        checkHttpResponse(response);
        try {
            return response.readEntity(RobotPart.class);
        } catch (ProcessingException | IllegalStateException e) {
            throw new RobotsClientException(
                    "Malformed response from server on \"read\" request.", e);
        }
    }

    public void updateName(
            final String oldSerialNumber,
            final String value)
            throws RobotsClientException {
        update(oldSerialNumber, Constants.NAME, value);
    }

    public void updateSerialNumber(
            final String oldSerialNumber,
            final String value)
            throws RobotsClientException {
        update(oldSerialNumber, Constants.SERIAL_NUMBER, value);
    }

    public void updateManufacturer(
            final String oldSerialNumber,
            final String value)
            throws RobotsClientException {
        update(oldSerialNumber, Constants.MANUFACTURER, value);
    }

    public void updateWeight(
            final String oldSerialNumber,
            final Integer value)
            throws RobotsClientException {
        update(oldSerialNumber, Constants.WEIGHT, value);
    }

    public void updateCompatibilities(
            final String oldSerialNumber,
            final String[] value)
            throws RobotsClientException {
        update(oldSerialNumber, Constants.COMPATIBILITIES, value);
    }

    private void update(
            final String oldSerialNumber,
            final String fieldName,
            final Object value)
            throws RobotsClientException {
        final UpdateRequest updateRequest
                = new UpdateRequest(
                        oldSerialNumber,
                        fieldName,
                        value);
        final Response response
                = sendPostRequest(PATH_UPDATE, updateRequest);
        checkHttpResponse(response);
    }

    public void delete(final String serialNumber) throws RobotsClientException {
        final DeleteRequest deleteRequest = new DeleteRequest(serialNumber);
        final Response response
                = sendPostRequest(PATH_DELETE, deleteRequest);
        checkHttpResponse(response);
    }

    public RobotPart[] listAll() throws RobotsClientException {
        final Response response
                = client.target(robotsServerUri)
                .path(PATH_LIST_ALL)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        checkHttpResponse(response);
        try {
            return response.readEntity(RobotPart[].class);
        } catch (ProcessingException | IllegalStateException e) {
            throw new RobotsClientException(
                    "Malformed response from server on \"listAll\" request.",
                    e);
        }
    }

    public RobotPart[] listCompatible(
            final String serialNumber,
            final Integer number)
            throws RobotsClientException {
        final ListCompatibleRequest listCompatibleRequest
                = new ListCompatibleRequest(serialNumber, number);
        final Response response
                = sendPostRequest(PATH_LIST_COMPATIBLE, listCompatibleRequest);
        checkHttpResponse(response);
        try {
            return response.readEntity(RobotPart[].class);
        } catch (ProcessingException | IllegalStateException e) {
            throw new RobotsClientException(
                    "Malformed response from server on"
                            + " \"listCompatible\" request.",
                    e);
        }
    }

    private Response sendPostRequest(
            final String path, final Object requestEntity) {
        return client.target(robotsServerUri)
                .path(path)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(
                        requestEntity, MediaType.APPLICATION_JSON_TYPE));
    }

    private void checkHttpResponse(
            final Response response) throws RobotsClientException {
        if (!Response.Status.Family.SUCCESSFUL
                .equals(response.getStatusInfo().getFamily())) {
            final JsonMessage jsonMessage;
            try {
                jsonMessage = response.readEntity(JsonMessage.class);
            } catch (ProcessingException | IllegalStateException e) {
                throw new RobotsClientException(
                        "Unexpected response from server.", e);
            }
            throw new RobotsClientException(jsonMessage.getMessage());
        }
    }

    public class RobotsClientException extends Exception {
        public RobotsClientException(String message) {
            super(message);
        }
        public RobotsClientException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
