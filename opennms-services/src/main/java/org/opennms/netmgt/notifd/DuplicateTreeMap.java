package org.opennms.netmgt.notifd;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

public class DuplicateTreeMap<K, V> extends TreeMap<K, List<V>> {
    /**
     * 
     */
    private static final long serialVersionUID = 8020472612288161254L;

    public V putItem(K key, V value) {
        List<V> l;
        if (super.containsKey(key)) {
            l = super.get(key);
        } else {
            l = new LinkedList<V>();
            put(key, l);
        }
        
        if (l.contains(value)) {
            return value;
        } else {
            l.add(value);
            return null;
        }
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();

        for (List<V> list : values()) {
            for (V item : list) {
                buffer.append(item.toString() + System.getProperty("line.separator"));
            }
        }

        return buffer.toString();
    }
}

