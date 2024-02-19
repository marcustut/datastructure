package lob.ds;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

public class StackTest {
    @Test
    public void testIsEmpty() {
        Stack<Integer> list = new Stack<>();

        assertTrue(list.isEmpty());

        list.add(5);

        assertFalse(list.isEmpty());
    }

    @Test
    public void testSize() {
        Stack<Integer> list = new Stack<>();

        assertEquals(0, list.size());

        list.add(5);

        assertEquals(1, list.size());

        list.add(8);

        assertEquals(2, list.size());
    }

    @Test
    public void testAdd() {
        Stack<Integer> list = new Stack<>();

        // Test adding same element
        assertTrue(list.add(1));
        assertTrue(list.add(1));

        // Test adding different element
        assertTrue(list.add(3));
    }

    @Test
    public void testPop() {
        Stack<Integer> list = new Stack<>();

        // Test when list is empty
        assertNull(list.pop());

        list.add(1);

        assertEquals(1, list.pop());
        assertEquals(0, list.size());

        assertTrue(list.add(1));
        assertTrue(list.add(2));
        assertTrue(list.add(3));

        assertEquals(3, list.pop());
        assertEquals(2, list.size());
        assertEquals(2, list.peek());
    }

    @Test
    public void testPeek() {
        Stack<Integer> list = new Stack<>();

        // When list is empty
        assertNull(list.peek());

        list.add(1);

        assertEquals(1, list.peek());
        assertEquals(1, list.size());
        assertEquals(1, list.peek());
    }

    @Test
    public void testRemove() {
        Stack<Integer> list = new Stack<>();

        assertTrue(list.add(1));
        assertTrue(list.add(2));
        assertTrue(list.add(3));

        list.remove(0);

        assertEquals("2 3 ", list.toString());
        assertEquals(2, list.size());

        list.remove(1);

        assertEquals("2 ", list.toString());
        assertEquals(1, list.size());

        assertTrue(list.add(4));
        assertTrue(list.add(5));

        list.remove(2);

        assertEquals("2 4 ", list.toString());
        assertEquals(2, list.size());
        assertEquals(4, list.peek());
    }

    @Test
    public void testReplace() {
        Stack<Integer> list = new Stack<>();

        assertTrue(list.add(1));
        assertTrue(list.add(2));
        assertTrue(list.add(3));

        list.replace(0, 3);
        list.replace(2, 1);

        assertEquals(list.get(0), 3);
        assertEquals(list.get(2), 1);
    }

    @Test
    public void testGet() {
        Stack<Integer> list = new Stack<>();

        assertTrue(list.add(1));
        assertTrue(list.add(2));
        assertTrue(list.add(3));

        assertEquals(1, list.get(0));
        assertEquals(2, list.get(1));
        assertEquals(3, list.get(2));
    }

    @Test
    public void testContains() {
        Stack<Integer> list = new Stack<>();

        assertTrue(list.add(2));
        assertTrue(list.add(3));
        assertTrue(list.add(4));

        assertTrue(list.contains(2));
        assertTrue(list.contains(3));
        assertTrue(list.contains(4));

        assertFalse(list.contains(1));
        assertFalse(list.contains(5));
    }

    @Test
    public void testFindIndex() {
        Stack<Integer> list = new Stack<>();

        assertTrue(list.add(1));
        assertTrue(list.add(2));
        assertTrue(list.add(3));

        assertEquals(0, list.findIndex(1));
        assertEquals(1, list.findIndex(2));
        assertEquals(2, list.findIndex(3));
    }

    @Test
    public void testIterator() {
        Stack<Integer> list = new Stack<>();
        StringBuilder sb = new StringBuilder();

        list.add(100);
        list.add(20);
        list.add(10);
        list.add(30);
        list.add(200);
        list.add(150);
        list.add(300);

        Iterator<Integer> itr = list.iterator();

        while (itr.hasNext()) {
            Integer elem = itr.next();
            sb.append(elem + ", ");
        }

        assertEquals("100, 20, 10, 30, 200, 150, 300, ", sb.toString());
    }
}