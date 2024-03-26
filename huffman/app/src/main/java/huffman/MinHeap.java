package huffman;

/**
 * A generic heap data structure.
 */
public class MinHeap<T extends Comparable<T>> {
    private static final int DEFAULT_CAPACITY = 11;
    private Object[] heap;
    private int size;

    public MinHeap() {
        size = 0;
        heap = new Object[DEFAULT_CAPACITY];
    }

    /**
     * Add a new element onto the heap.
     * 
     * @param data - the element to add
     */
    @SuppressWarnings("unchecked")
    public void add(T data) {
        if (data == null) // if the data given is null throw exception
            throw new NullPointerException();

        int i = size; // always insert at the end

        if (i >= heap.length) // out of space, grow by 1
            grow(i + 1);

        // Maintains the correct order after insertion
        while (i > 0) {
            // Get the parent of new data
            int parent = (i - 1) / 2;
            Object parentElem = heap[parent];

            // Completed if the new data is bigger than its parent
            if (((Comparable<? super T>) data).compareTo((T) parentElem) >= 0)
                break;

            // Swap the position of new data and parent
            heap[i] = parentElem;
            i = parent;
        }
        heap[i] = data;

        size++; // increment the size
    }

    /**
     * Removes the front element of the heap and return it.
     * 
     * @return the removed front element
     */
    @SuppressWarnings("unchecked")
    public T poll() {
        T min = (T) heap[0];

        if (min != null) { // has remaining elements, reorder heap
            // Get the last element and clear it (delete)
            int last = --size;
            Object lastElem = heap[last];
            heap[last] = null;

            // Re-order the remaining elements
            int half = last / 2;
            int i = 0;
            while (i < half) {
                // Get the left and right elements
                int left = (i * 2) + 1;
                Object leftElem = heap[left];
                int right = left + 1;
                Object rightElem = heap[right];

                // If left > right then swap them
                if (right < last && ((Comparable<? super T>) leftElem).compareTo((T) rightElem) > 0) {
                    leftElem = heap[right];
                    left = right;
                }

                // Complete when last element is <= the left element
                if (((Comparable<? super T>) lastElem).compareTo((T) leftElem) <= 0)
                    break;

                heap[i] = leftElem;
                i = left;
            }
            heap[i] = lastElem;
        }

        return min;
    }

    /**
     * The current size of the heap.
     * 
     * @return an integer denoting the size of the heap
     */
    public int size() {
        return size;
    }

    @Override
    public String toString() {
        String result = "[";
        for (int i = 0; i < size; i++) {
            result += heap[i].toString();
            if (i != size - 1)
                result += ", ";
        }
        result += "]";
        return result;
    }

    private void grow(int capacity) {
        Object[] newHeap = new Object[capacity];
        System.arraycopy(heap, 0, newHeap, 0, size);
        heap = newHeap;
    }
}
