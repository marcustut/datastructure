package cache;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class CacheTest {
    Cache<String, String> cache;

    // Set up an empty map before each test
    @BeforeEach
    public void setUp() {
        cache = new Cache<>();
    }

    // Close the cache after all tests
    @AfterEach
    public void destroy() {
        cache.close();
    }

    // Check that the same value is returned after inserting
    @Test
    public void testPutThenGet() {
        cache.put("Hello", "World", 1);
        assertEquals(cache.get("Hello"), "World");
    }

    // Check that null is returned when getting non-existent value
    @Test
    public void testGetEmpty() {
        assertEquals(cache.get("Hello"), null);
    }

    // Check that eviction is working
    @Test
    public void testGetAfterEvicted() throws InterruptedException {
        cache.put("Hello", "World", 1);
        Thread.sleep(1500);
        assertEquals(cache.get("Hello"), null);
    }

    // Check that putting after eviction works
    @Test
    public void testPutAfterEvicted() throws InterruptedException {
        cache.put("Hello", "World", 1);
        assertEquals(cache.get("Hello"), "World");
        Thread.sleep(1500);
        cache.put("Hello", "World2", 1);
        assertEquals(cache.get("Hello"), "World2");
    }
}
