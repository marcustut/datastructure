package cache;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiConsumer;

/**
 * An in-memory cache that supports eviction based on time-to-live (TTL).
 */
public class Cache<K extends Comparable<K>, V> implements AutoCloseable {
    private LinearProbeHashMap<K, V> map = new LinearProbeHashMap<>();
    private TTLMap<K> ttls = new TTLMap<>();
    private ScheduledExecutorService executor;

    private BiConsumer<LinearProbeHashMap<K, V>.Entry<K, V>, Long> onEvict;
    private BiConsumer<LinearProbeHashMap<K, V>.Entry<K, V>, Long> onAdd;

    public Cache() {
        TimerTask repeatedTask = new TimerTask() {
            public void run() {
                long currentTime = System.currentTimeMillis();

                ArrayList<K> evicted = new ArrayList<>();

                for (var entry : ttls.entrySet())
                    if (currentTime >= entry.getValue()) {
                        V previous = map.remove(entry.getKey());
                        evicted.add(entry.getKey());

                        if (onEvict != null)
                            onEvict.accept(
                                    map.new Entry<K, V>(entry.getKey(), previous),
                                    entry.getValue());
                    }

                for (K key : evicted)
                    ttls.remove(key);
            }
        };

        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(repeatedTask, 0, 1000L, TimeUnit.MILLISECONDS);
    }

    public V put(K key, V value, long ttlSecs) {
        V previous = map.put(key, value);
        ttls.put(key, System.currentTimeMillis() + ttlSecs * 1000);
        if (onAdd != null)
            onAdd.accept(map.new Entry<K, V>(key, value), ttlSecs);
        return previous;
    }

    public V get(K key) {
        return map.get(key);
    }

    public V remove(K key) {
        V removed = map.remove(key);
        if (removed != null)
            ttls.remove(key);
        return removed;
    }

    public void setOnEvict(BiConsumer<LinearProbeHashMap<K, V>.Entry<K, V>, Long> onEvict) {
        this.onEvict = onEvict;
    }

    public void setOnAdd(BiConsumer<LinearProbeHashMap<K, V>.Entry<K, V>, Long> onAdd) {
        this.onAdd = onAdd;
    }

    @Override
    public void close() {
        executor.close();
    }
}
