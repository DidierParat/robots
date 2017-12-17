import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import models.Constants;
import models.RobotPart;

public class DbService {
    private static final String PARTS_TABLE_NAME = "parts";
    private static final String NAME_COLUMN_NAME = "name";
    private static final String SERIAL_NUMBER_COLUMN_NAME = "serial_number";
    private static final String MANUFACTURER_COLUMN_NAME = "manufacturer";
    private static final String WEIGHT_COLUMN_NAME = "weight";
    private static final String COMPATIBILITIES_COLUMN_NAME = "compatibilities";
    private static final String COMPATIBILITIES_SEPARATOR = ":";

    private final Connection connection;
    public DbService() throws SQLException {
        connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/robots?useSSL=false",
                "root",
                "root");
    }

    public void add(final RobotPart robotPart)
            throws DbServiceException, RessourceAlreadyExistsException {
        if (robotPartExists(robotPart.getSerialNumber())) {
            throw new RessourceAlreadyExistsException(
                    "Could not add robot part. "
                    + "Serial number already exists in DB: "
                    + robotPart.getSerialNumber());
        }
        final String insertString =
                "INSERT INTO "
                + PARTS_TABLE_NAME
                + " VALUES (?, ?, ?, ?, ?);";
        final PreparedStatement insertStatement;
        try {

            insertStatement = connection.prepareStatement(insertString);
            insertStatement.setString(1, robotPart.getName());
            insertStatement.setString(2, robotPart.getSerialNumber());
            insertStatement.setString(3, robotPart.getManufacturer());
            insertStatement.setInt(4, robotPart.getWeight());
            insertStatement.setString(
                    5, formatCompatibilities(robotPart.getCompatibilities()));
            insertStatement.executeUpdate();
            insertStatement.close();
        } catch (final SQLException e) {
            throw new DbServiceException("Could not add robot part to DB.", e);
        }

    }

    public RobotPart read(
            final String robotPartSerialNumber)
            throws DbServiceException, RessourceNotFoundException {
        final String selectString =
                "SELECT * FROM "
                        + PARTS_TABLE_NAME
                        + " WHERE "
                        + SERIAL_NUMBER_COLUMN_NAME
                        + "= ?;";
        try {
            final PreparedStatement selectStatement
                    = connection.prepareStatement(selectString);
            selectStatement.setString(1, robotPartSerialNumber);
            final ResultSet resultSet = selectStatement.executeQuery();
            if (!resultSet.next()) {
                throw new RessourceNotFoundException(
                        "Could not read robot part. "
                                + "Serial number does not exist in DB: "
                                + robotPartSerialNumber);
            }
            final RobotPart robotPart = createRobotPartFromResultSet(resultSet);
            resultSet.close();
            selectStatement.close();
            return robotPart;
        } catch (final SQLException e) {
            throw new DbServiceException(
                    "Could not read robot part from DB.", e);
        }
    }

    public void update(
            final String originalSerialNumber,
            final String fieldToUpdate,
            final Object valueOfField)
            throws DbServiceException,
            RessourceNotFoundException,
            UpdateDbException,
            IllegalArgumentException {
        if (!robotPartExists(originalSerialNumber)) {
            throw new RessourceNotFoundException(
                    "Could not update robot parts. "
                    + "Serial number not found: "
                    + originalSerialNumber);
        }
        final StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("UPDATE " + PARTS_TABLE_NAME + " SET ");
        if (Constants.NAME.equals(fieldToUpdate)) {
            stringBuffer.append(NAME_COLUMN_NAME + "=?");
        } else if (Constants.SERIAL_NUMBER.equals(fieldToUpdate)) {
            stringBuffer.append(SERIAL_NUMBER_COLUMN_NAME + "=?");
        } else if (Constants.MANUFACTURER.equals(fieldToUpdate)) {
            stringBuffer.append(MANUFACTURER_COLUMN_NAME + "=?");
        } else if (Constants.WEIGHT.equals(fieldToUpdate)) {
            stringBuffer.append(WEIGHT_COLUMN_NAME + "=?");
        } else if (Constants.COMPATIBILITIES.equals(fieldToUpdate)) {
            stringBuffer.append(COMPATIBILITIES_COLUMN_NAME + "=?");
        } else if (Constants.SERIAL_NUMBER.equals(fieldToUpdate)) {
            if (robotPartExists((String) valueOfField)) {
                throw new UpdateDbException(
                        "Could not update robot parts. "
                        + "The new serial number already exists in the DB.");
            }
            stringBuffer.append(SERIAL_NUMBER_COLUMN_NAME + "=?");
        } else {
            throw new IllegalArgumentException("Unrecognized field");
        }
        stringBuffer.append(" WHERE " + SERIAL_NUMBER_COLUMN_NAME + "=?;");
        final String updateString = stringBuffer.toString();
        try {
            final PreparedStatement updateStatement
                    = connection.prepareStatement(updateString);
            updateStatement.setObject(1, valueOfField);
            updateStatement.setString(2, originalSerialNumber);
            updateStatement.executeUpdate();
            updateStatement.close();
        } catch (final SQLException e) {
            throw new DbServiceException(
                    "Could not update robot part in DB.", e);
        }
    }

    public void delete(
            final String robotPartSerialNumber) throws DbServiceException {
        final String deleteString =
                "DELETE FROM "
                + PARTS_TABLE_NAME
                + " WHERE "
                + SERIAL_NUMBER_COLUMN_NAME
                + "=?;";
        try {
            final PreparedStatement deleteStatement
                    = connection.prepareStatement(deleteString);
            deleteStatement.setString(1, robotPartSerialNumber);
            deleteStatement.executeUpdate();
            deleteStatement.close();
        } catch (final SQLException e) {
            throw new DbServiceException(
                    "Could not delete robot parts from DB: "
                            + robotPartSerialNumber,
                    e);
        }
    }

    public RobotPart[] listAll() throws DbServiceException {
        final RobotPart[] robotParts;
        try {
            final Statement countStatement = connection.createStatement();
            final ResultSet countResultSet
                    = countStatement.executeQuery(
                    "SELECT COUNT(*) FROM " + PARTS_TABLE_NAME + ";");
            if (!countResultSet.next()) {
                throw new DbServiceException(
                        "Could not count raw of table " + PARTS_TABLE_NAME);
            }
            int tableSize = countResultSet.getInt(1);
            countResultSet.close();
            countStatement.close();

            final Statement listStatement = connection.createStatement();
            final ResultSet resultSet
                    = listStatement.executeQuery(
                    "SELECT * FROM " + PARTS_TABLE_NAME + ";");
            robotParts = new RobotPart[tableSize];
            int arrayColumn = 0;
            while (resultSet.next()) {
                robotParts[arrayColumn] = createRobotPartFromResultSet(resultSet);
                arrayColumn++;
            }
            resultSet.close();
            listStatement.close();
        } catch (final SQLException e) {
            throw new DbServiceException(
                    "Could not list all robot parts from DB.", e);
        }

        return robotParts;
    }

    public RobotPart[] listCompatible(
            final String robotPartSerialNumber,
            final Integer number)
            throws DbServiceException, RessourceNotFoundException {
        final RobotPart robotPart = read(robotPartSerialNumber);
        final String[] compatibilities = robotPart.getCompatibilities();
        final int numberOfRobotPartsToRetreive
                = compatibilities.length < number
                ? compatibilities.length : number;

        final StringBuffer selectStringBuffer = new StringBuffer();
        selectStringBuffer.append(
                "SELECT * FROM "
                + PARTS_TABLE_NAME
                + " WHERE ");
        for (int count = 0; count < numberOfRobotPartsToRetreive; count++) {
            if (count != 0) {
                selectStringBuffer.append("OR ");
            }
            selectStringBuffer.append(SERIAL_NUMBER_COLUMN_NAME + "= ? ");
        }
        selectStringBuffer.append("LIMIT ?;");
        final String selectString = selectStringBuffer.toString();
        try {
            final PreparedStatement listStatement
                    = connection.prepareStatement(selectString);
            for (int count = 0; count < numberOfRobotPartsToRetreive; count++) {
                listStatement.setString(count + 1, compatibilities[count]);
            }
            listStatement.setInt(
                    numberOfRobotPartsToRetreive + 1,
                    numberOfRobotPartsToRetreive);
            final ResultSet resultSet = listStatement.executeQuery();
            final RobotPart[] robotParts = new RobotPart[numberOfRobotPartsToRetreive];
            int arrayColumn = 0;
            while (resultSet.next()) {
                robotParts[arrayColumn] = createRobotPartFromResultSet(resultSet);
                arrayColumn++;
            }
            resultSet.close();
            listStatement.close();
            return robotParts;
        } catch (final SQLException e) {
            throw new DbServiceException(
                    "Could not list compatible robot parts from DB,", e);
        }
    }

    private String formatCompatibilities(final String[] compatibilities) {
        final StringBuffer formatedCompatibilities = new StringBuffer();
        for (final String serialNumber : compatibilities) {
            formatedCompatibilities.append(serialNumber);
            formatedCompatibilities.append(COMPATIBILITIES_SEPARATOR);
        }
        return formatedCompatibilities.toString();
    }

    private String[] extractCompatibilities(final String compatibilities) {
        return compatibilities.split(COMPATIBILITIES_SEPARATOR);
    }

    private boolean robotPartExists(
            final String serialNumber) throws DbServiceException {
        final String selectString =
                "SELECT * FROM "
                + PARTS_TABLE_NAME
                + " WHERE "
                + SERIAL_NUMBER_COLUMN_NAME
                + "= ?;";
        try {
            final PreparedStatement selectStatement
                    = connection.prepareStatement(selectString);
            selectStatement.setString(1, serialNumber);
            final ResultSet resultSet = selectStatement.executeQuery();
            final boolean robotPartExists = resultSet.next();
            resultSet.close();
            selectStatement.close();
            return robotPartExists;
        } catch (final SQLException e) {
            throw new DbServiceException(
                    "Could not check if robot part exists in DB,", e);
        }
    }

    private RobotPart createRobotPartFromResultSet(
            final ResultSet resultSet) throws DbServiceException {
        try {
            final String name = resultSet.getString(NAME_COLUMN_NAME);
            final String serialNumber
                    = resultSet.getString(SERIAL_NUMBER_COLUMN_NAME);
            final String manufacturer
                    = resultSet.getString(MANUFACTURER_COLUMN_NAME);
            final Integer weight = resultSet.getInt(WEIGHT_COLUMN_NAME);
            final String[] compatibilities
                    = extractCompatibilities(
                    resultSet.getString(COMPATIBILITIES_COLUMN_NAME));
            return new RobotPart(
                    name,
                    serialNumber,
                    manufacturer,
                    weight,
                    compatibilities);
        } catch (final SQLException e) {
            throw new DbServiceException(
                    "Could not create robot parts from resultSet,", e);
        }
    }

    public class DbServiceException extends Exception {
        public DbServiceException(final String message) {
            super(message);
        }
        public DbServiceException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }

    public class RessourceNotFoundException extends Exception {
        public RessourceNotFoundException(
                final String message) {
            super(message);
        }
    }

    public class UpdateDbException extends Exception {
        public UpdateDbException(
                final String message) {
            super(message);
        }
    }

    public class RessourceAlreadyExistsException extends Exception {
        public RessourceAlreadyExistsException(
                final String message) {
            super(message);
        }
    }

    public class IllegalArgumentException extends Exception {
        public IllegalArgumentException(
                final String message) {
            super(message);
        }
    }
}
