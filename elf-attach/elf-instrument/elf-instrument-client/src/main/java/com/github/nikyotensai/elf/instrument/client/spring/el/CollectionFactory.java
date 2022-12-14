/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.nikyotensai.elf.instrument.client.spring.el;

import java.util.*;

/**
 * Factory for collections, being aware of Java 5 and Java 6 collections.
 * Mainly for internal use within the framework.
 *
 * <p>The goal of this class is to avoid runtime dependencies on a specific
 * Java version, while nevertheless using the best collection implementation
 * that is available at runtime.
 *
 * @author Juergen Hoeller
 * @author Arjen Poutsma
 * @since 1.1.1
 */
abstract class CollectionFactory {

    private static Class navigableSetClass = null;

    private static Class navigableMapClass = null;

    private static final Set<Class> approximableCollectionTypes = new HashSet<Class>(10);

    private static final Set<Class> approximableMapTypes = new HashSet<Class>(6);


    static {
        // Standard collection interfaces
        approximableCollectionTypes.add(Collection.class);
        approximableCollectionTypes.add(List.class);
        approximableCollectionTypes.add(Set.class);
        approximableCollectionTypes.add(SortedSet.class);
        approximableMapTypes.add(Map.class);
        approximableMapTypes.add(SortedMap.class);

        // New Java 6 collection interfaces
        ClassLoader cl = CollectionFactory.class.getClassLoader();
        try {
            navigableSetClass = cl.loadClass("java.util.NavigableSet");
            navigableMapClass = cl.loadClass("java.util.NavigableMap");
            approximableCollectionTypes.add(navigableSetClass);
            approximableMapTypes.add(navigableMapClass);
        } catch (ClassNotFoundException ex) {
            // not running on Java 6 or above...
        }

        // Common concrete collection classes
        approximableCollectionTypes.add(ArrayList.class);
        approximableCollectionTypes.add(LinkedList.class);
        approximableCollectionTypes.add(HashSet.class);
        approximableCollectionTypes.add(LinkedHashSet.class);
        approximableCollectionTypes.add(TreeSet.class);
        approximableMapTypes.add(HashMap.class);
        approximableMapTypes.add(LinkedHashMap.class);
        approximableMapTypes.add(TreeMap.class);
    }

    /**
     * Determine whether the given collection type is an approximable type,
     * i.e. a type that {@link #createApproximateCollection} can approximate.
     *
     * @param collectionType the collection type to check
     * @return {@code true} if the type is approximable,
     * {@code false} if it is not
     */
    public static boolean isApproximableCollectionType(Class<?> collectionType) {
        return (collectionType != null && approximableCollectionTypes.contains(collectionType));
    }

    /**
     * Create the most approximate collection for the given collection.
     * <p>Creates an ArrayList, TreeSet or linked Set for a List, SortedSet
     * or Set, respectively.
     *
     * @param collection      the original Collection object
     * @param initialCapacity the initial capacity
     * @return the new Collection instance
     * @see ArrayList
     * @see TreeSet
     * @see LinkedHashSet
     */
    @SuppressWarnings("unchecked")
    public static Collection createApproximateCollection(Object collection, int initialCapacity) {
        if (collection instanceof LinkedList) {
            return new LinkedList();
        } else if (collection instanceof List) {
            return new ArrayList(initialCapacity);
        } else if (collection instanceof SortedSet) {
            return new TreeSet(((SortedSet) collection).comparator());
        } else {
            return new LinkedHashSet(initialCapacity);
        }
    }

    /**
     * Create the most appropriate collection for the given collection type.
     * <p>Creates an ArrayList, TreeSet or linked Set for a List, SortedSet
     * or Set, respectively.
     *
     * @param collectionType  the desired type of the target Collection
     * @param initialCapacity the initial capacity
     * @return the new Collection instance
     * @see ArrayList
     * @see TreeSet
     * @see LinkedHashSet
     */
    public static Collection createCollection(Class<?> collectionType, int initialCapacity) {
        if (collectionType.isInterface()) {
            if (List.class.equals(collectionType)) {
                return new ArrayList(initialCapacity);
            } else if (SortedSet.class.equals(collectionType) || collectionType.equals(navigableSetClass)) {
                return new TreeSet();
            } else if (Set.class.equals(collectionType) || Collection.class.equals(collectionType)) {
                return new LinkedHashSet(initialCapacity);
            } else {
                throw new IllegalArgumentException("Unsupported Collection interface: " + collectionType.getName());
            }
        } else {
            if (!Collection.class.isAssignableFrom(collectionType)) {
                throw new IllegalArgumentException("Unsupported Collection type: " + collectionType.getName());
            }
            try {
                return (Collection) collectionType.newInstance();
            } catch (Exception ex) {
                throw new IllegalArgumentException("Could not instantiate Collection type: " +
                        collectionType.getName(), ex);
            }
        }
    }

    /**
     * Determine whether the given map type is an approximable type,
     * i.e. a type that {@link #createApproximateMap} can approximate.
     *
     * @param mapType the map type to check
     * @return {@code true} if the type is approximable,
     * {@code false} if it is not
     */
    public static boolean isApproximableMapType(Class<?> mapType) {
        return (mapType != null && approximableMapTypes.contains(mapType));
    }

    /**
     * Create the most approximate map for the given map.
     * <p>Creates a TreeMap or linked Map for a SortedMap or Map, respectively.
     *
     * @param map             the original Map object
     * @param initialCapacity the initial capacity
     * @return the new Map instance
     * @see TreeMap
     * @see LinkedHashMap
     */
    @SuppressWarnings("unchecked")
    public static Map createApproximateMap(Object map, int initialCapacity) {
        if (map instanceof SortedMap) {
            return new TreeMap(((SortedMap) map).comparator());
        } else {
            return new LinkedHashMap(initialCapacity);
        }
    }

    /**
     * Create the most approximate map for the given map.
     * <p>Creates a TreeMap or linked Map for a SortedMap or Map, respectively.
     *
     * @param mapType         the desired type of the target Map
     * @param initialCapacity the initial capacity
     * @return the new Map instance
     * @see TreeMap
     * @see LinkedHashMap
     */
    public static Map createMap(Class<?> mapType, int initialCapacity) {
        if (mapType.isInterface()) {
            if (Map.class.equals(mapType)) {
                return new LinkedHashMap(initialCapacity);
            } else if (SortedMap.class.equals(mapType) || mapType.equals(navigableMapClass)) {
                return new TreeMap();
            } else if (MultiValueMap.class.equals(mapType)) {
                return new LinkedMultiValueMap();
            } else {
                throw new IllegalArgumentException("Unsupported Map interface: " + mapType.getName());
            }
        } else {
            if (!Map.class.isAssignableFrom(mapType)) {
                throw new IllegalArgumentException("Unsupported Map type: " + mapType.getName());
            }
            try {
                return (Map) mapType.newInstance();
            } catch (Exception ex) {
                throw new IllegalArgumentException("Could not instantiate Map type: " +
                        mapType.getName(), ex);
            }
        }
    }


}
