package mq;

import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class Queue<T> extends AbstractQueue<T> {
    private class Node {
        private T data;
        private Node next;

        Node(T data) {
            this.data = data;
        }

        Node(T data, Node next) {
            this.data = data;
            this.next = next;
        }
    }

    private Node head;
    private int size = 0;

    public Queue() {
    }

    @Override
    public boolean offer(T e) {
        if (this.head == null) {
            this.head = new Node(e);
            this.size++;
            return true;
        }

        Node cur = this.head;
        while (cur.next != null)
            cur = cur.next;

        cur.next = new Node(e);
        this.size++;
        return true;
    }

    @Override
    public T poll() {
        if (this.head == null)
            return null;

        Node cur = this.head;
        this.head = cur.next;
        this.size--;

        return cur.data;
    }

    @Override
    public T peek() {
        if (this.head == null)
            return null;
        return this.head.data;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            Node cur = new Node(null, head);

            @Override
            public boolean hasNext() {
                if (size == 0 && head == null)
                    return false;
                return cur.next != null;
            }

            @Override
            public T next() {
                if (hasNext()) {
                    cur = head.next;
                    return cur.data;
                }
                cur.next = null;
                throw new NoSuchElementException();
            }
        };
    }

    @Override
    public int size() {
        return this.size;
    }
}
