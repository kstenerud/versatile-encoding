package org.stenerud.remotefs.utility;

import org.stenerud.remotefs.NotFoundException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Map proxy that throws an exception rather than return null.
 * @param <K> Key type
 * @param <V> Value type
 */
public class StrictMap<K, V> implements Map<K, V> {
    private final Map<K, V> map;
    private final String errorMessageFormat;

    public static @Nonnull <K, V> StrictMap<K, V> with(@Nonnull Supplier<? extends Map<K, V>> supplier) {
        return new StrictMap<K, V>(supplier.get(), "Key [%s] not found");
    }

    public static @Nonnull <K, V> StrictMap<K, V> cloning(@Nonnull Map<K, V> map) {
        return with((Supplier<Map<K, V>>) () -> {
            try {
                return map.getClass().getConstructor(Map.class).newInstance(map);
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        });
    }

    private StrictMap(@Nonnull Map<K, V> map, @Nonnull String errorMessageFormat) {
        this.map = map;
        this.errorMessageFormat = errorMessageFormat;
    }

    public @Nonnull StrictMap withErrorFormat(String errorMessageFormat) {
        return new StrictMap(this.map, errorMessageFormat);
    }

    @Override
    public @Nonnull
    V get(@CheckForNull Object key) {
        if(key == null) {
            throw new NotFoundException(String.format(errorMessageFormat, key));
        }
        V value = map.get(key);
        if(value == null) {
            throw new NotFoundException(String.format(errorMessageFormat, key));
        }
        return value;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object o) {
        return map.containsKey(o);
    }

    @Override
    public boolean containsValue(Object o) {
        return map.containsValue(o);
    }

    @Override
    public V put(K k, V v) {
        return map.put(k, v);
    }

    @Override
    public V remove(Object o) {
        return map.remove(o);
    }

    @Override
    public void putAll(@Nonnull Map<? extends K, ? extends V> map) {
        this.map.putAll(map);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public @Nonnull Set<K> keySet() {
        return map.keySet();
    }

    @Override
    public @Nonnull Collection<V> values() {
        return map.values();
    }

    @Override
    public @Nonnull Set<Entry<K, V>> entrySet() {
        return map.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        return map.equals(o);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    @Override
    public V getOrDefault(Object o, V v) {
        return map.getOrDefault(o, v);
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> biConsumer) {
        map.forEach(biConsumer);
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> biFunction) {
        map.replaceAll(biFunction);
    }

    @Override
    public V putIfAbsent(K k, V v) {
        return map.putIfAbsent(k, v);
    }

    @Override
    public boolean remove(Object o, Object o1) {
        return map.remove(o, o1);
    }

    @Override
    public boolean replace(K k, V v, V v1) {
        return map.replace(k, v, v1);
    }

    @Override
    public V replace(K k, V v) {
        return map.replace(k, v);
    }

    @Override
    public V computeIfAbsent(K k, Function<? super K, ? extends V> function) {
        return map.computeIfAbsent(k, function);
    }

    @Override
    public V computeIfPresent(K k, BiFunction<? super K, ? super V, ? extends V> biFunction) {
        return map.computeIfPresent(k, biFunction);
    }

    @Override
    public V compute(K k, BiFunction<? super K, ? super V, ? extends V> biFunction) {
        return map.compute(k, biFunction);
    }

    @Override
    public V merge(K k, V v, BiFunction<? super V, ? super V, ? extends V> biFunction) {
        return map.merge(k, v, biFunction);
    }
}
