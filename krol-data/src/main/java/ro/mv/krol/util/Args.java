package ro.mv.krol.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by mihai.vaduva on 07/08/2016.
 */
public class Args {

    public static <S> S notNull(S obj) throws IllegalArgumentException {
        return notNull(obj, null);
    }

    public static <S> S notNull(S obj, String name) throws IllegalArgumentException {
        if (obj == null) {
            String message = name == null ? "object cannot be null" : name + " cannot be null!";
            throw new IllegalArgumentException(message);
        }
        return obj;
    }

    public static String notEmtpy(String s) throws IllegalArgumentException {
        return notEmpty(s, null);
    }

    public static String notEmpty(String s, String name) throws IllegalArgumentException {
        if (s == null || s.isEmpty()) {
            String message = name == null ? "string cannot be empty" : (name + " cannot be empty string!");
            throw new IllegalArgumentException(message);
        }
        return s;
    }

    public static <T> T[] notEmpty(T[] array) throws IllegalArgumentException {
        return notEmpty(array, null);
    }

    public static <T> T[] notEmpty(T[] array, String name) throws IllegalArgumentException {
        if (array == null || array.length == 0) {
            String message = name == null ? "array cannot be empty" : name + " cannot be empty array!";
            throw new IllegalArgumentException(message);
        }
        return array;
    }

    public static <E> List<E> notEmpty(List<E> list, String name) throws IllegalArgumentException {
        if (list == null || list.isEmpty()) {
            String message = name == null ? "list cannot be empty" : (name + " list be empty collection!");
            throw new IllegalArgumentException(message);
        }
        return list;
    }

    public static <E> List<E> notEmpty(List<E> list) throws IllegalArgumentException {
        return notEmpty(list, null);
    }

    public static <E> Collection<E> notEmpty(Collection<E> collection, String name) throws IllegalArgumentException {
        if (collection == null || collection.isEmpty()) {
            String message = name == null ? "collection cannot be empty" : (name + " cannot be empty collection!");
            throw new IllegalArgumentException(message);
        }
        return collection;
    }

    public static <E> Collection<E> notEmpty(Collection<E> collection) throws IllegalArgumentException {
        return notEmpty(collection, null);
    }

    public static <K, V> Map<K, V> notEmpty(Map<K, V> map, String name) throws IllegalArgumentException {
        if (map == null || map.isEmpty()) {
            String message = name == null ? "map cannot be empty" : (name + " cannot be empty map!");
            throw new IllegalArgumentException(message);
        }
        return map;
    }

    public static <K, V> Map<K, V> notEmpty(Map<K, V> map) throws IllegalArgumentException {
        return notEmpty(map, null);
    }

    public static <N extends Number> N greaterThanZero(N nr, String name) {
        if (nr.intValue() <= 0) {
            String message = "argument " + (name == null ? "number" : name) + " is not greater then 0";
            throw new IllegalArgumentException(message);
        }
        return nr;
    }

}
