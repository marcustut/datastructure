package cache;

import java.util.*;

/**
 * A type of HashMap that uses linear probing to resolve hash conflicts.
 */
public class LinearProbeHashMap<K, V> extends AbstractMap<K, V> {
    private static int DEFAULT_PRIME_FACTOR = 109345121;
    private static int DEFAULT_CAPACITY = 17;

    // since linear probing works best when the load factor is low, we resize
    // the table once the load factor exceeds this specified limit.
    private final static double MAX_LOAD_FACTOR = 0.5;

    private int size = 0; // the number of entries in the map
    private int capacity; // the length of the hash table
    private int prime; // the prime factor for MAD compression
    private long scale, shift; // the scale and shift factor for MAD compression

    private Entry<K, V>[] table; // the hash table

    public class Entry<NK, NV> implements Map.Entry<NK, NV> {
        NK key;
        NV value;

        Entry(NK key, NV value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public NK getKey() {
            return key;
        }

        @Override
        public NV getValue() {
            return value;
        }

        @Override
        public NV setValue(NV value) {
            NV previous = this.value;
            this.value = value;
            return previous;
        }
    }

    @SuppressWarnings("unchecked")
    public LinearProbeHashMap() {
        prime = DEFAULT_PRIME_FACTOR;
        capacity = DEFAULT_CAPACITY;
        Random rand = new Random();
        // required to make the bound one less and +1 to make sure
        // scale % prime != 0
        scale = rand.nextInt(prime - 1) + 1;
        shift = rand.nextInt(prime);
        table = new Entry[DEFAULT_CAPACITY];
    }

    @Override
    public V put(K key, V value) {
        int i = probe(key);
        if (i >= 0) { // found existing, update
            V previousValue = table[i].value;
            table[i] = new Entry<K, V>(key, value);
            return previousValue;
        }
        table[-(i + 1)] = new Entry<K, V>(key, value);
        size++; // increment the current size

        // Since linear probing works best when the load factor is low, we resize
        // the table once the load factor exceeds this specified limit.
        if (loadFactor() > MAX_LOAD_FACTOR)
            resize(2 * capacity - 1);

        return null;
    }

    @Override
    public V get(Object key) {
        int i = probe(key);
        if (i < 0) // unable to find the key
            return null;
        return table[i].value;
    }

    @Override
    public V remove(Object key) {
        int i = probe(key);
        if (i < 0)
            return null; // not able to find the element to remove
        V removed = table[i].value;
        table[i] = null;
        size--;
        return removed;
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        Set<Map.Entry<K, V>> set = new HashSet<>();
        for (int i = 0; i < capacity; i++)
            if (table[i] != null)
                set.add(table[i]);
        return set;
    }

    /**
     * Hash function to get the index of the hash table given a key.
     * Note that this function uses a combination of the MAD
     * (Multiply-Add-and-Divide) and division compression method:
     * 
     * MAD: (ka + b) mod p
     * 
     * where:
     * k = key
     * a = scale
     * b = shift
     * p = a prime number
     * 
     * Division: k mod N
     * 
     * where:
     * k = key
     * N = capacity
     * 
     * The reason we combine them two because MAD requires that N to be a prime
     * number but our capacity is not necessarily a prime number. To summarize,
     * the function is described as:
     * 
     * hash: ((ka + b) mod p) mod N
     * 
     * @param key
     * @return the hash value.
     */
    private int hash(Object key) {
        // Note that we need `Math.abs` because scale and shift can be a negative value.
        return (int) ((Math.abs(key.hashCode() * scale + shift) % prime) % capacity);
    }

    /**
     * Calculate and return the current load factor of the map. For example,
     * when:
     * 
     * size = 4, capacity = 8
     * loadFactor = 4/8 = 0.5
     * 
     * @return the load factor.
     */
    private double loadFactor() {
        return size / (double) capacity;
    }

    /**
     * Resize the map to the given capacity, note that if the new capacity is a
     * number smaller than the current capacity then there might be data loss.
     * 
     * @param newCapacity the new capacity.
     */
    @SuppressWarnings("unchecked")
    private void resize(int newCapacity) {
        // Copy existing elements to a temporary buffer
        Map.Entry<K, V>[] temp = new Map.Entry[capacity];
        int i = 0;
        for (Map.Entry<K, V> entry : entrySet()) {
            temp[i] = entry;
            i++;
        }
        int oldCapacity = capacity;

        // Update the map to have the new capacity and extends the table
        capacity = newCapacity;
        table = new Entry[capacity];
        size = 0;

        // Copy over previous elements
        for (i = 0; i < oldCapacity; i++)
            put(temp[i].getKey(), temp[i].getValue());
    }

    /**
     * Search the table in a cyclic fashion to find if a key exist. The function
     * returns positive integer if a matching key is found, the returned value is
     * the index. Otherwise, it returns a negative integer indicating a free slot in
     * the table.
     * 
     * @param hash the hash of the key
     * @param key  the key to search for
     * @return an integer
     */
    private int probe(Object key) {
        int free = -1; // indicate the index of found position
        int hash = hash(key);
        int i = hash(key); // use the hash as an index

        // cycle through the table to find free position
        do {
            if (table[i] == null) {
                if (free == -1)
                    free = i;
                break;
            } else if (table[i].key.equals(key)) {
                return i;
            }
            i = (i + 1) % capacity;
        } while (i != hash);

        return -(free + 1);
    }
}
