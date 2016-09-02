package ro.mv.krol.util;

/**
 * Created by mihai on 9/2/16.
 */
public interface Cache<K, V> {

    V get(K key);

    void put(K key, V value);

    void clear();

}
