package sylva;

import java.util.AbstractList;

public class Vector<T> extends AbstractList<T> {
    private static int INITIAL_CAPACITY = 10;
    private int capacity;
    private int size = 0;
    private Object buffer[];

    public Vector() {
        this.capacity = INITIAL_CAPACITY;
        this.buffer = new Object[this.capacity];
    }

    public Vector(int capacity) {
        this.capacity = capacity;
        this.buffer = new Object[capacity];
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean add(T t) {
        return addT(t);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get(int index) {
        return (T) buffer[index];
    }

    @Override
    public boolean remove(Object o) {
        for (int i = 0; i < size; i++) {
            if (!buffer[i].equals(o))
                continue;

            for (int j = i; j < size - 1; j++)
                buffer[j] = buffer[j + 1];
            size--;
            return true;
        }
        return false;
    }

    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }

    @Override
    public boolean contains(Object o) {
        for (var t : buffer)
            if (t == o)
                return true;
        return false;
    }

    @Override
    public Object[] toArray() {
        return this.buffer;
    }

    @Override
    public void clear() {
        this.buffer = new Object[capacity];
        this.size = 0;
    }

    public void quicksort() {

    }

    public void mergesort() {

    }

    // boolean equals(Object o);

    // int hashCode();

    // Positional Access Operations

    // E set(int index, E element);

    // void add(int index, E element);

    @Override
    public T remove(int index) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException();

        var t = get(index);

        for (int i = index; i < size - 1; i++)
            buffer[i] = buffer[i + 1];
        size--;

        return t;
    }

    @Override
    public int indexOf(Object o) {
        for (int i = 0; i < size; i++)
            if (buffer[i].equals(o))
                return i;
        return -1;
    }

    // int lastIndexOf(Object o);

    // ListIterator<E> listIterator();

    // ListIterator<E> listIterator(int index);

    // List<E> subList(int fromIndex, int toIndex);

    private boolean addT(T t) {
        if (size < capacity) {
            buffer[size++] = t;
            return true;
        }
        return false;
    }

}
