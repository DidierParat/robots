import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.mockito.Mockito.mock;

public class RobotsResourceTest {
    private final ObjectMapper mapper;
    private final DbServiceTest dbService;

    public RobotsResourceTest() {
        this.mapper = mock(ObjectMapper.class);
        this.dbService = mock(DbServiceTest.class);
    }

    @Test
    public void testAdd() {
        // TODO
    }

    @Test
    public void testRead() {
        // TODO
    }

    @Test
    public void testUpdate() {
        // TODO
    }

    @Test
    public void testDelete() {
        // TODO
    }

    @Test
    public void testListAll() {
        // TODO
    }

    @Test
    public void testListCompatible() {
        // TODO
    }
}
