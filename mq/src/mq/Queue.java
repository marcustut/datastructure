package mq;

import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

public class Queue<T> extends AbstractQueue<T> {
    private class Node {
        private T data;
        private Node next;

        Node(T data) {
            this.data = data;
        }
    }

    private Node head, tail;
    private int size = 0;

    /**
     * Add an element to the tail of the queue
     * <p>
     * We only add an element to the tail of the queue so it is O(1).
     * </p>
     * @param e - element to be added
     * @return true if element is added, false otherwise
     */
    @Override
    public boolean offer(T e) {
        if (head == null) {
            head = new Node(e);
            size++;
            return true;
        }

        if (tail == null) {
            tail = new Node(e);
            head.next = tail;
            size++;
            return true;
        }

        tail.next = new Node(e);
        tail = tail.next;
        size++;

        return true;
    }

    /**
     * Poll an element from the head of the queue
     * <p>
     * We only take the element from the head of the queue so it is O(1).
     * </p>
     * @return data if found, null otherwise
     */
    @Override
    public T poll() {
        if (head == null || size == 0)
            return null;

        Node cur = head;
        head = cur.next;
        size--;

        return cur.data;
    }

    /**
     * Take a look at the data of the head of the queue
     * <p>
     * O(1) since we only look at the head node.
     * </p>
     * @return data if found, null otherwise
     */
    @Override
    public T peek() {
        if (head == null)
            return null;
        return head.data;
    }

    /**
     * Returns an iterator object for the queue.
     * <p>
     * Constant time operation because we are just returning an iterator we are not
     * iterating through the queue, therefore O(1).
     * </p>
     * @return an iterator
     */
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Node trav = head;

            @Override
            public boolean hasNext() {
                return trav != null;
            }

            @Override
            public T next() {
                T data = trav.data;
                trav = trav.next;
                return data;
            }
        };
    }

    /**
     * Get the size of the queue
     * <p>
     * We are storing the queue size so accessing it is constant time, so O(1)
     * </p>
     * @return size of the queue
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * Check if queue is empty
     * <p>
     * We are storing the queue size so checking if it is empty is constant time, so
     * O(1)
     * </p>
     *
     * @return true if queue is empty, false otherwise
     */
    public boolean isEmpty() {
        return size == 0;
    }
}
