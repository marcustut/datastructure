package huffman;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MinHeapTest {
    @Test
    void testAdd() {
        MinHeap<Integer> heap = new MinHeap<>();
        heap.add(5);
        assertEquals(heap.toString(), "[5]");
        assertEquals(heap.size(), 1);
        heap.add(3);
        assertEquals(heap.toString(), "[3, 5]");
        assertEquals(heap.size(), 2);
        heap.add(4);
        assertEquals(heap.toString(), "[3, 5, 4]");
        assertEquals(heap.size(), 3);
        heap.add(2);
        assertEquals(heap.toString(), "[2, 3, 4, 5]");
        assertEquals(heap.size(), 4);
        heap.add(2);
        assertEquals(heap.toString(), "[2, 2, 4, 5, 3]");
        assertEquals(heap.size(), 5);
        heap.add(2);
        assertEquals(heap.toString(), "[2, 2, 2, 5, 3, 4]");
        assertEquals(heap.size(), 6);
        heap.add(2);
        assertEquals(heap.toString(), "[2, 2, 2, 5, 3, 4, 2]");
        assertEquals(heap.size(), 7);
    }

    @Test
    void testPoll() {
        MinHeap<Integer> heap = new MinHeap<>();
        heap.add(5);
        heap.add(3);
        heap.add(4);
        heap.add(2);
        assertEquals(heap.toString(), "[2, 3, 4, 5]");
        assertEquals(heap.size(), 4);

        assertEquals(heap.poll(), 2);
        assertEquals(heap.size(), 3);

        assertEquals(heap.poll(), 3);
        assertEquals(heap.size(), 2);

        assertEquals(heap.poll(), 4);
        assertEquals(heap.size(), 1);

        assertEquals(heap.poll(), 5);
        assertEquals(heap.size(), 0);
    }
}
