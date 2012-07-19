package org.opennms.features.topology.app.internal;

import java.io.Serializable;
import java.util.Hashtable;

public class KeyMapper implements Serializable {

    private int lastKey = 0;

    private final Hashtable<Object, String> objectKeyMap = new Hashtable<Object, String>();

    private final Hashtable<String, Object> keyObjectMap = new Hashtable<String, Object>();

    private String m_prefix;
    
    public KeyMapper(String prefix) {
        m_prefix = prefix;
    }
    
    /**
     * Gets key for an object.
     * 
     * @param o
     *            the object.
     */
    public String key(Object o) {

        if (o == null) {
            return "null";
        }

        // If the object is already mapped, use existing key
        String key = objectKeyMap.get(o);
        if (key != null) {
            return key;
        }

        // If the object is not yet mapped, map it
        key = getNextKey();
        objectKeyMap.put(o, key);
        keyObjectMap.put(key, o);

        return key;
    }

    private String getNextKey() {
        return m_prefix + String.valueOf(++lastKey);
    }

    /**
     * Retrieves object with the key.
     * 
     * @param key
     *            the name with the desired value.
     * @return the object with the key.
     */
    public Object get(String key) {

        return keyObjectMap.get(key);
    }

    /**
     * Removes object from the mapper.
     * 
     * @param removeobj
     *            the object to be removed.
     */
    public void remove(Object removeobj) {
        final String key = objectKeyMap.get(removeobj);

        if (key != null) {
            objectKeyMap.remove(removeobj);
            keyObjectMap.remove(key);
        }
    }

    /**
     * Removes all objects from the mapper.
     */
    public void removeAll() {
        objectKeyMap.clear();
        keyObjectMap.clear();
    }
}

