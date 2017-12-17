import java.net.URI;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.UriBuilder;
import models.RobotPart;

public class LocalTest {
    public static void main(final String[] args) throws RobotsClient.RobotsClientException {
        final URI serverUri = UriBuilder.fromUri("http://localhost:8080").build();
        final Client client = ClientBuilder.newClient();
        final RobotsClient robotsClient = new RobotsClient(client, serverUri);

        // Test: DB should be empty at the beginning of this test
        RobotPart[] robotParts = robotsClient.listAll();
        if (robotParts.length != 0) {
            System.out.println("FAIL 1");
            return;
        }

        // Test: Add a robot's part and verify DB
            robotsClient.add(
                "BasicHead",
                "1",
                "Manu",
                1000,
                new String[] {"2", "3"});
        robotParts = robotsClient.listAll();
        if (robotParts.length != 1) {
            System.out.println("FAIL 2");
            return;
        }
        RobotPart robotPart = robotParts[0];
        if (!"BasicHead".equals(robotPart.getName())
                || !"1".equals(robotPart.getSerialNumber())
                || !"Manu".equals(robotPart.getManufacturer())
                || robotPart.getWeight() != 1000
                || robotPart.getCompatibilities().length != 2) {
                //|| !robotPart.getCompatibilities().contains(ImmutableList.of("2", "3"))) {
            System.out.println("FAIL 3");
            return;
        }

        // Test: Delete previous robot's part and verify DB
        robotsClient.delete("1");
        robotParts = robotsClient.listAll();
        if (robotParts.length != 0) {
            System.out.println("FAIL 4");
            return;
        }

        // Test: Add several parts and verify DB
        robotsClient.add(
                "BasicHead",
                "1",
                "Manu",
                1000,
                new String[] {"2", "3"});
        robotsClient.add(
                "BasicBody",
                "2",
                "Manu",
                5000,
                new String[] {"1", "3"});
        robotsClient.add(
                "BasicArm",
                "3",
                "Manu",
                1500,
                new String[] {"1", "2"});
        robotsClient.add(
                "FancyHead",
                "4",
                "Manu",
                800,
                new String[] {});
        robotParts = robotsClient.listAll();
        if (robotParts.length != 4) {
            System.out.println("FAIL 5");
            return;
        }

        // Test: Read a part
        robotPart = robotsClient.read("3");
        if (!"BasicArm".equals(robotPart.getName())
                || !"3".equals(robotPart.getSerialNumber())
                || !"Manu".equals(robotPart.getManufacturer())
                || robotPart.getWeight() != 1500
                || robotPart.getCompatibilities().length != 2) {
                //|| !robotPart.getCompatibilities().contains(ImmutableList.of("1", "2"))) {
            System.out.println("FAIL 6");
            return;
        }

        // Test: Update part, verify it
        robotsClient.updateName("4", "SuperFancyHead");
        robotsClient.updateSerialNumber("4", "5");
        robotsClient.updateManufacturer("5", "SuperFancyCo");
        robotsClient.updateWeight("5", 600);
        robotsClient.updateCompatibilities("5", new String[] {"6"});
        robotPart = robotsClient.read("5");
        if (!"SuperFancyHead".equals(robotPart.getName())
                || !"5".equals(robotPart.getSerialNumber())
                || !"SuperFancyCo".equals(robotPart.getManufacturer())
                || robotPart.getWeight() != 600
                || robotPart.getCompatibilities().length != 1) {
                //|| !robotPart.getCompatibilities().contains(ImmutableList.of("1", "2"))) {
            System.out.println("FAIL 7");
            return;
        }

        // Test: List compatibles
        robotParts = robotsClient.listCompatible("1", 3);
        if (robotParts.length != 2) {
            System.out.println("FAIL 8");
        }

        // Cleanup DB
        robotsClient.delete("1");
        robotsClient.delete("2");
        robotsClient.delete("3");
        robotsClient.delete("5");
        robotParts = robotsClient.listAll();
        if (robotParts.length != 0) {
            System.out.println("FAIL 9");
            return;
        }

        System.out.println("All tests passed.");
    }
}
