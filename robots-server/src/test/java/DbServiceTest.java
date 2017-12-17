import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import models.RobotPart;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DbServiceTest {
    private static final String HSQLDB_URL
            = "jdbc:hsqldb:mem:myunittests;sql.syntax_mys=true";
    private final DbService dbService;
    private final Connection connection;

    public DbServiceTest() throws SQLException {
        this.connection = DriverManager.getConnection(HSQLDB_URL);
        this.dbService = new DbService(HSQLDB_URL);
    }

    @Test
    public void testAdd() throws Exception {
        final String name = "BasicHead";
        final String serialNumber = "1";
        final String manufacturer = "Manu";
        final int weight = 1000;
        final String[] compatibilities = new String[] {"2", "3"};
        final RobotPart robotPart = new RobotPart(
                name,
                serialNumber,
                manufacturer,
                weight,
                compatibilities);
        dbService.add(robotPart);
        final Statement statement = connection.createStatement();
        final ResultSet resultSet = statement.executeQuery(
                "SELECT * FROM "
                + DbService.PARTS_TABLE_NAME
                + " WHERE "
                + DbService.SERIAL_NUMBER_COLUMN_NAME
                + "='1';");
        assertTrue(resultSet.next());
        assertEquals(resultSet.getString(DbService.NAME_COLUMN_NAME), name);
        assertEquals(
                resultSet.getString(
                        DbService.SERIAL_NUMBER_COLUMN_NAME), serialNumber);
        assertEquals(
                resultSet.getString(
                        DbService.MANUFACTURER_COLUMN_NAME), manufacturer);
        assertEquals(
                resultSet.getInt(DbService.WEIGHT_COLUMN_NAME), weight);
        assertEquals(
                resultSet.getString(DbService.COMPATIBILITIES_COLUMN_NAME),
                compatibilities[0]
                + DbService.COMPATIBILITIES_SEPARATOR
                + compatibilities[1]);
    }
/*
    @Test
    public void testRead() throws Exception {
        dbService.read();
        // TODO
    }

    @Test
    public void testUpdate() throws Exception {
        dbService.update();
        // TODO
    }

    @Test
    public void testDelete() throws Exception {
        dbService.delete();
        // TODO
    }

    @Test
    public void testListAll() throws Exception {
        dbService.listAll();
        // TODO
    }

    @Test
    public void testListCompatible() throws Exception {
        dbService.listCompatible();
        // TODO
    }
    */
}
