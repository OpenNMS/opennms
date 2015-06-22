package org.opennms.netmgt.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An abstract path used to represent a resource or its parent.
 *
 * The resource path may be may be mapped to a file-system path
 * or some other form by the {@link org.opennms.netmgt.dao.api.ResourceStorageDao}
 * implementation.
 *
 * @author jwhite
 */
public class ResourcePath implements Iterable<String>, Comparable<ResourcePath> {

    private final List<String> m_elements = new ArrayList<String>();

    public ResourcePath(String... path) {
        for (String el : path) {
            m_elements.add(el);
        }
    }

    public ResourcePath(ResourcePath parent, String... path) {
        m_elements.addAll(parent.m_elements);
        for (String el : path) {
            m_elements.add(el);
        }
    }

    /**
     * Convenience method.
     */
    public static ResourcePath get(String... path) {
        return new ResourcePath(path);
    }

    /**
     * Convenience method.
     */
    public static ResourcePath get(ResourcePath parent, String... path) {
        return new ResourcePath(parent, path);
    }

    public String getName() {
        final int k = m_elements.size() - 1;
        return k < 0 ? null : m_elements.get(k);
    }

    public String[] elements() {
        return m_elements.toArray(new String[m_elements.size()]);
    }

    @Override
    public Iterator<String> iterator() {
        return m_elements.iterator();
    }

    public String toString() {
        return m_elements.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((m_elements == null) ? 0 : m_elements.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof ResourcePath))
            return false;
        ResourcePath other = (ResourcePath) obj;
        if (m_elements == null) {
            if (other.m_elements != null)
                return false;
        } else if (!m_elements.equals(other.m_elements))
            return false;
        return true;
    }

    @Override
    public int compareTo(ResourcePath other) {
        return this.toString().compareTo(other.toString());
    }

}
