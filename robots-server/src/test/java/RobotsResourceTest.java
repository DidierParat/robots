import com.fasterxml.jackson.databind.ObjectMapper;
import javax.ws.rs.core.Response;
import models.RobotPart;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RobotsResourceTest {
    private final ObjectMapper mapper;
    private final DbService dbService;
    private RobotsResource robotsResource;

    public RobotsResourceTest() {
        this.mapper = new ObjectMapper();
        this.dbService = mock(DbService.class);
        this.robotsResource = new RobotsResource(dbService, mapper);
    }

    @Test
    public void testAdd() throws Exception {
        final String addRequest = "{\"name\":\"BasicHead\",\"serialNumber\":\"1\",\"manufacturer\":\"Manu\",\"weight\":1000,\"compatibilities\":[\"2\",\"3\"]}";
        final RobotPart robotPart = new RobotPart(
                "BasicHead",
                "1",
                "Manu",
                1000,
                new String[] {"2", "3"});
        final Response response = robotsResource.add(addRequest);
        verify(dbService)
                .add(argThat(new ObjectEqualityArgumentMatcher<>(robotPart)));
        assertEquals(response.getStatus(), 200);
    }

    @Test
    public void testRead() throws Exception {
        final String readRequest = "{\"serialNumber\":\"1\"}";
        final String serialNumber = "1";
        final RobotPart robotPart = new RobotPart(
                "BasicHead",
                "1",
                "Manu",
                1000,
                new String[] {"2", "3"});
        when(dbService.read(eq(serialNumber))).thenReturn(robotPart);
        final Response response = robotsResource.read(readRequest);
        verify(dbService).read(serialNumber);
        assertEquals(response.getStatus(), 200);
    }

    @Test
    public void testUpdate() throws Exception {
        final String updateRequest = "{\"originalSerialNumber\":\"4\",\"fieldToUpdate\":\"name\",\"valueOfField\":\"SuperFancyHead\"}";
        final String serialNumber = "4";
        final String fieldToUpdate = "name";
        final Object valueToUpdate = "SuperFancyHead";
        final Response response = robotsResource.update(updateRequest);
        verify(dbService).update(serialNumber, fieldToUpdate, valueToUpdate);
        assertEquals(response.getStatus(), 200);
    }

    @Test
    public void testDelete() throws Exception {
        final String deleteRequest = "{\"serialNumber\":\"1\"}";
        final String serialNumber = "1";
        final Response response = robotsResource.delete(deleteRequest);
        verify(dbService).delete(serialNumber);
        assertEquals(response.getStatus(), 200);
    }

    @Test
    public void testListAll() throws Exception {
        final RobotPart robotPart1 = new RobotPart(
                "BasicHead",
                "1",
                "Manu",
                1000,
                new String[] {"2", "3"});
        final RobotPart robotPart2 = new RobotPart(
                "BasicBody",
                "2",
                "Manu",
                5000,
                new String[] {"1", "3"});
        final RobotPart robotPart3 = new RobotPart(
                "BasicArm",
                "3",
                "Manu",
                1500,
                new String[] {"1", "2"});
        when(dbService.listAll())
                .thenReturn(
                        new RobotPart[] {robotPart1, robotPart2, robotPart3});
        final Response response = robotsResource.listAll();
        verify(dbService).listAll();
        assertEquals(response.getStatus(), 200);
    }

    @Test
    public void testListCompatible() throws Exception {
        final String listCompatibleRequest = "{\"serialNumber\":\"1\",\"number\":3}";
        final String serialNumber = "1";
        final Integer number = 3;
        final RobotPart robotPart2 = new RobotPart(
                "BasicBody",
                "2",
                "Manu",
                5000,
                new String[] {"1", "3"});
        final RobotPart robotPart3 = new RobotPart(
                "BasicArm",
                "3",
                "Manu",
                1500,
                new String[] {"1", "2"});
        when(dbService.listCompatible(serialNumber, number))
                .thenReturn(
                        new RobotPart[] {robotPart2, robotPart3});
        final Response response
                = robotsResource.listCompatible(listCompatibleRequest);
        verify(dbService).listCompatible(serialNumber, number);
        assertEquals(response.getStatus(), 200);
    }

    private class ObjectEqualityArgumentMatcher<T> implements ArgumentMatcher<T> {
        T thisObject;

        public ObjectEqualityArgumentMatcher(T thisObject) {
            this.thisObject = thisObject;
        }

        @Override
        public boolean matches(Object argument) {
            return thisObject.equals(argument);
        }
    }
}
