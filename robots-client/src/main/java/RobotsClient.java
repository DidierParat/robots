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

    /**
     * Add a new robot part to the database
     * @param name Name of the robot part
     * @param serialNumber Serial Number of the robot part
     * @param manufacturer Manufacturer of the robot part
     * @param weight Weight of the robot part
     * @param compatibilities Array of Strings, each of them being
     *                       a serial number of a compatible robot part
     * @throws RobotsClientException If the robot part has not been added
     */
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

    /**
     * Retrieve the RobotPart corresponding to the serial number
     * @param serialNumber Serial number of the robot part
     * @return RobotPart corresponding to the serial number
     * @throws RobotsClientException If the robot part was not found
     * or could not be read
     */
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

    /**
     * Update the name of the robot part
     * @param serialNumber Serial number of the robot part
     * @param name Updated name of the robot part
     * @throws RobotsClientException If the robot part did not get updated
     */
    public void updateName(
            final String serialNumber,
            final String name)
            throws RobotsClientException {
        update(serialNumber, Constants.NAME, name);
    }

    /**
     * Update the serial number of the robot part
     * @param formerSerialNumber Former serial number of the robot part
     * @param updatedSerialNumber Updated serial number of the robot part
     * @throws RobotsClientException If the robot part did not get updated
     */
    public void updateSerialNumber(
            final String formerSerialNumber,
            final String updatedSerialNumber)
            throws RobotsClientException {
        update(formerSerialNumber, Constants.SERIAL_NUMBER, updatedSerialNumber);
    }

    /**
     * Update the manufacturer of the robot part
     * @param serialNumber Former serial number of the robot part
     * @param manufacturer Updated serial number of the robot part
     * @throws RobotsClientException If the robot part did not get updated
     */
    public void updateManufacturer(
            final String serialNumber,
            final String manufacturer)
            throws RobotsClientException {
        update(serialNumber, Constants.MANUFACTURER, manufacturer);
    }

    /**
     * Update the weight of the robot part
     * @param serialNumber Serial number of the robot part
     * @param weight Weight of the robot part
     * @throws RobotsClientException If the robot part did not get updated
     */
    public void updateWeight(
            final String serialNumber,
            final Integer weight)
            throws RobotsClientException {
        update(serialNumber, Constants.WEIGHT, weight);
    }

    /**
     * Update the compatibilities of the robot part
     * @param serialNumber Serial number of the robot part
     * @param compatibilities Array of Strings, each of them being
     *                        a serial number of a compatible robot part
     * @throws RobotsClientException If the robot part did not get updated
     */
    public void updateCompatibilities(
            final String serialNumber,
            final String[] compatibilities)
            throws RobotsClientException {
        update(serialNumber, Constants.COMPATIBILITIES, compatibilities);
    }

    /**
     * Delete the robot part
     * @param serialNumber Serial number of the robot part to delete
     * @throws RobotsClientException If the robot part did not get updated
     */
    public void delete(final String serialNumber) throws RobotsClientException {
        final DeleteRequest deleteRequest = new DeleteRequest(serialNumber);
        final Response response
                = sendPostRequest(PATH_DELETE, deleteRequest);
        checkHttpResponse(response);
    }

    /**
     * List all the robot parts in the DB
     * @return An array of all RobotPart in the DB
     * @throws RobotsClientException If the list could not be retrieved
     */
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

    /**
     * List all robot parts compatible with the robot part
     * corresponding to the given serial number
     * @param serialNumber Serial number of the robot part
     * @param number Max number of results
     * @return
     * @throws RobotsClientException
     */
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
