package org.stenerud.remotefs.utility;

import java.util.Map;

public class KeyValue<K, V> {
    public final K key;
    public final V value;

    public KeyValue(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public KeyValue(Map.Entry<K, V> entry) {
        this(entry.getKey(), entry.getValue());
    }
}
