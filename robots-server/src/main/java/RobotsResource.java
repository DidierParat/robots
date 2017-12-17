import com.google.gson.Gson;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.core.Response;
import models.DeleteRequest;
import models.JsonMessage;
import models.ListCompatibleRequest;
import models.ReadRequest;
import models.RobotPart;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import models.UpdateRequest;

@Path("/")
public class RobotsResource {
    private static final Logger LOGGER
            = Logger.getLogger(RobotsResource.class.getName());
    private static final String PATH_ADD = "add";
    private static final String PATH_READ = "read";
    private static final String PATH_UPDATE = "update";
    private static final String PATH_DELETE = "delete";
    private static final String PATH_LIST_ALL = "list-all";
    private static final String PATH_LIST_COMPATIBLE = "list-compatible";

    private final DbService dbService;
    private final ObjectMapper mapper;
    private final Gson gson;

    public RobotsResource(
            final DbService dbService, final ObjectMapper mapper) {
        this.dbService = dbService;
        this.mapper = mapper;
        this.gson = new Gson();
    }

    @POST
    @Path(PATH_ADD)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response add(final String request) {
        final RobotPart robotPart;
        try {
            robotPart = mapper.readValue(request, RobotPart.class);
        } catch (final IOException e) {
            LOGGER.log(
                    Level.WARNING,
                    "Could not read \"add\" request:" + request,
                    e);
            return formatJsonResponse(422, "Unprocessable Entity");
        }
        try {
            dbService.add(robotPart);
        } catch (DbService.DbServiceException e) {
            LOGGER.log(
                    Level.WARNING, "Exception thrown while adding to DB.", e);
            return formatJsonResponse(500, "Internal Server error");
        } catch (DbService.RessourceAlreadyExistsException e) {
            LOGGER.log(
                    Level.WARNING, "Exception thrown while adding to DB.", e);
            return formatJsonResponse(
                    400, "Bad request. Item already exists.");
        }
        return formatJsonResponse(200, "OK");
    }

    @POST
    @Path(PATH_READ)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response read(final String request) {
        final ReadRequest readRequest;
        try {
            readRequest
                    = mapper.readValue(request, ReadRequest.class);
        } catch (final IOException e) {
            LOGGER.log(
                    Level.WARNING,
                    "Could not read \"read\" request:" + request,
                    e);
            return formatJsonResponse(422, "Unprocessable Entity");
        }
        final RobotPart robotPart;
        try {
            robotPart = dbService.read(readRequest.getSerialNumber());
        } catch (DbService.DbServiceException e) {
            LOGGER.log(
                    Level.WARNING, "Exception thrown while adding to DB.", e);
            return formatJsonResponse(500, "Internal Server error");
        } catch (DbService.RessourceNotFoundException e) {
            LOGGER.log(
                    Level.WARNING, "Exception thrown while adding to DB.", e);
            return formatJsonResponse(404, "Item not found.");
        }
        return Response.ok(robotPart, MediaType.APPLICATION_JSON_TYPE).build();
    }

    @POST
    @Path(PATH_UPDATE)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(final String request) {
        final UpdateRequest updateRequest;
        try {
            updateRequest = mapper.readValue(request, UpdateRequest.class);
        } catch (final IOException e) {
            LOGGER.log(
                    Level.WARNING,
                    "Could not read \"add\" request:" + request,
                    e);
            return formatJsonResponse(422, "Unprocessable Entity");
        }
        try {
            dbService.update(
                    updateRequest.getOriginalSerialNumber(),
                    updateRequest.getFieldToUpdate(),
                    updateRequest.getValueOfField());
        } catch (DbService.DbServiceException e) {
            LOGGER.log(
                    Level.WARNING, "Exception thrown while adding to DB.", e);
            return formatJsonResponse(500, "Internal Server error");
        } catch (DbService.RessourceNotFoundException e) {
            LOGGER.log(
                    Level.WARNING, "Exception thrown while adding to DB.", e);
            return formatJsonResponse(404, "Item not found.");
        } catch (DbService.UpdateDbException e) {
            LOGGER.log(
                    Level.WARNING, "Exception thrown while adding to DB.", e);
            return formatJsonResponse(
                    400,
                    "Bad request. New serial number already exists.");
        } catch (DbService.IllegalArgumentException e) {
            return formatJsonResponse(
                    400,
                    "Bad request. Unrecognized field.");
        }
        return formatJsonResponse(200, "OK");
    }

    @POST
    @Path(PATH_DELETE)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(final String request) {
        final DeleteRequest deleteRequest;
        try {
            deleteRequest
                    = mapper.readValue(request, DeleteRequest.class);
        } catch (final IOException e) {
            LOGGER.log(
                    Level.WARNING,
                    "Could not read \"read\" request:" + request,
                    e);
            return formatJsonResponse(422, "Unprocessable Entity");
        }
        try {
            dbService.delete(deleteRequest.getSerialNumber());
        } catch (DbService.DbServiceException e) {
            LOGGER.log(
                    Level.WARNING, "Exception thrown while adding to DB.", e);
            return formatJsonResponse(500, "Internal Server error");
        }
        return formatJsonResponse(200, "OK");
    }

    @GET
    @Path(PATH_LIST_ALL)
    @Produces(MediaType.APPLICATION_JSON)
    public Response listAll() {
        final RobotPart[] robotParts;
        try {
            robotParts = dbService.listAll();
        } catch (DbService.DbServiceException e) {
            LOGGER.log(
                    Level.WARNING, "Exception thrown while adding to DB.", e);
            return formatJsonResponse(500, "Internal Server error");
        }
        return Response
                .ok(gson.toJson(robotParts), MediaType.APPLICATION_JSON_TYPE)
                .build();
    }

    @POST
    @Path(PATH_LIST_COMPATIBLE)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response listCompatible(final String request) {
        final ListCompatibleRequest listCompatibleRequest;
        try {
            listCompatibleRequest = mapper.readValue(request, ListCompatibleRequest.class);
        } catch (final IOException e) {
            LOGGER.log(
                    Level.WARNING,
                    "Could not read \"listCompatible\" request:" + request,
                    e);
            return formatJsonResponse(422, "Unprocessable Entity");
        }
        final RobotPart[] robotParts;
        try {
            robotParts = dbService.listCompatible(
                    listCompatibleRequest.getSerialNumber(),
                    listCompatibleRequest.getNumber());
        } catch (DbService.DbServiceException e) {
            LOGGER.log(
                    Level.WARNING, "Exception thrown while adding to DB.", e);
            return formatJsonResponse(500, "Internal Server error");
        } catch (DbService.RessourceNotFoundException e) {
            LOGGER.log(
                    Level.WARNING, "Exception thrown while adding to DB.", e);
            return formatJsonResponse(404, "Item not found.");
        }

        return Response
                .ok(robotParts, MediaType.APPLICATION_JSON_TYPE)
                .build();
    }

    private Response formatJsonResponse(
            final Integer httpCode, final String message) {
        final JsonMessage jsonMessage = new JsonMessage(message);
        return Response
                .status(httpCode)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(jsonMessage).build();
    }
}
