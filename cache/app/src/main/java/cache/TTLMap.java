package cache;

import java.util.*;

/**
 * A specific type of map that helps to store the time-to-live (TTL) with an
 * associated key.
 */
public class TTLMap<K> extends AbstractMap<K, Long> {
    private ArrayList<Entry<K>> table = new ArrayList<>();
    private LinearProbeHashMap<K, Long> map = new LinearProbeHashMap<>();

    private class Entry<NK> implements Map.Entry<NK, Long> {
        NK key;
        long ttl;

        Entry(NK key, long ttl) {
            this.key = key;
            this.ttl = ttl;
        }

        @Override
        public NK getKey() {
            return key;
        }

        @Override
        public Long getValue() {
            return ttl;
        }

        @Override
        public Long setValue(Long value) {
            Long previous = this.ttl;
            this.ttl = value;
            return previous;
        }
    }

    public TTLMap() {
    }

    @Override
    public Long put(K key, Long ttl) {
        Long previous = map.put(key, ttl);
        table.add(findIndex(key), new Entry<>(key, ttl));
        return previous;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Long get(Object key) {
        int i = findIndex((K) key);
        if (i == -1)
            return null;
        return table.get(i).ttl;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Long remove(Object key) {
        int i = findIndex((K) key);
        if (i == -1)
            return null;
        map.remove(key);
        return table.remove(i).ttl;
    }

    public Set<Map.Entry<K, Long>> entrySet() {
        Set<Map.Entry<K, Long>> set = new HashSet<>();
        for (int i = 0; i < table.size(); i++)
            set.add(table.get(i));
        return set;
    }

    /**
     * Find the index of a given key in the table.
     * 
     * @param key
     * @return index otherwise -1
     */
    private int findIndex(K key) {
        Long ttl = map.get(key);
        if (ttl == null)
            return -1;
        return findIndex(ttl, 0, table.size() - 1);
    }

    private int findIndex(long ttl, int low, int high) {
        if (high < low) // not found
            return high + 1;

        int mid = (low + high) / 2;
        long midTTL = table.get(mid).ttl;

        if (ttl == midTTL) // found
            return mid;
        if (ttl < midTTL) // smaller than
            return findIndex(ttl, low, mid - 1);
        else // larger than
            return findIndex(ttl, mid + 1, high);
    }
}
