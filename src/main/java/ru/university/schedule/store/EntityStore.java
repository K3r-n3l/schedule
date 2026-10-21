package ru.university.schedule.store;

import java.util.List;
import java.util.Optional;

public interface EntityStore<K, T> {
    void add(K key, T entity);
    boolean removeByKey(K key);
    Optional<T> findByKey(K key);
    List<T> snapshot();
    void clear();
    int size();
}