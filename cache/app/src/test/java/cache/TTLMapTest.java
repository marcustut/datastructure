package cache;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

public class TTLMapTest {
    Map<String, Long> map;

    // Set up an empty map before each test
    @BeforeEach
    public void setUp() {
        map = new TTLMap<>();
    }

    // Check that a new map returns 'true' for isEmpty
    @Test
    public void testIsEmptyForNewMap() {
        assertTrue(map.isEmpty());
    }

    // Test adding an element makes isEmpty return 'false'
    @Test
    public void testAddMakesIsEmptyFalse() {
        map.put("Hello", 5L);
        assertFalse(map.isEmpty());
    }

    // Check that size returns 0 for new map
    @Test
    public void testSizeForNewMap() {
        assertEquals(0, map.size());
    }

    // Test size increases as elements are added
    @Test
    public void testSizeIncrementsWhenAddingElements() {
        map.put("Hello", 5L);
        assertEquals(1, map.size());

        map.put("Goodbye", 5L);
        assertEquals(2, map.size());
    }

    // Make sure get returns the values added under keys
    @Test
    public void testGetReturnsCorrectValue() {
        map.put("Hello", 5L);
        map.put("Goodbye", 6L);
        assertEquals(5, map.get("Hello"));
        assertEquals(6, map.get("Goodbye"));
    }

    // Test that null is returned if a key does not exist
    @Test
    public void testReturnsNullIfKeyDoesNotExist() {
        assertEquals(map.get("Hello"), null);
    }

    // Test thats an added element replaces another with the same key
    @Test
    public void testReplacesValueWithSameKey() {
        map.put("Hello", 5L);
        map.put("Hello", 6L);

        assertEquals(6, map.get("Hello"));
    }

    // Make sure that two (non-equal) keys with the same hash do not overwrite each
    // other
    @Test
    public void testDoesNotOverwriteSeperateKeysWithSameHash() {
        map.put("Ea", 5L);
        map.put("FB", 6L);

        assertEquals(5, map.get("Ea"));
        assertEquals(6, map.get("FB"));
    }

    // Make sure size doesn't decrement below 0
    @Test
    public void testRemoveDoesNotEffectNewMap() {
        map.remove("Hello");

        assertEquals(0, map.size());
    }

    // Make sure that size decrements as elements are used
    @Test
    public void testRemoveDecrementsSize() {
        map.put("Hello", 5L);
        map.put("Goodbye", 6L);

        map.remove("Hello");

        assertEquals(1, map.size());

        map.remove("Goodbye");

        assertEquals(0, map.size());
    }

    // Test elements are actually removed when remove is called
    @Test
    public void testRemoveDeletesElement() {
        map.put("Hello", 5L);
        map.remove("Hello");

        assertEquals(map.get("Hello"), null);
    }

    // Test that contains is 'false' for new maps
    @Test
    public void testContainsKeyForNewMap() {
        assertFalse(map.containsKey("Hello"));
    }

    // Test that contains returns 'false' when key doesn't exist
    @Test
    public void testContainsKeyForNonExistingKey() {
        map.put("Hello", 5L);
        assertFalse(map.containsKey("Goodbye"));
    }

    // Make sure that contains returns 'true' when the key does exist
    @Test
    public void testContainsKeyForExistingKey() {
        map.put("Hello", 5L);
        assertTrue(map.containsKey("Hello"));
    }

    // Check that contains is not fooled by equivalent hash codes
    @Test
    public void testContainsKeyForKeyWithEquivalentHash() {
        map.put("Ea", 5L);

        assertFalse(map.containsKey("FB"));
    }
}
