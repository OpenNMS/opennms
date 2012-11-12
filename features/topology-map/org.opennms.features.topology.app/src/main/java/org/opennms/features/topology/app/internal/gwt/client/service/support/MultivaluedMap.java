package org.opennms.features.topology.app.internal.gwt.client.service.support;

import java.util.Map;
import java.util.Set;

public interface MultivaluedMap<K, V> extends Map<K, Set<V>> {

    /**
     * <p>add</p>
     *
     * @param key a K object.
     * @param value a V object.
     * @param <K> a K object.
     * @param <V> a V object.
     */
    public void add(K key, V value);
    
    /**
     * <p>remove</p>
     *
     * @param key a K object.
     * @param value a V object.
     * @return a boolean.
     */
    public boolean remove(K key, V value);
    
    /**
     * <p>getCopy</p>
     *
     * @param key a K object.
     * @return a {@link java.util.Set} object.
     */
    public Set<V> getCopy(K key);
}
