package org.opennms.netmgt.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

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

    private static final Pattern SANITIZE_PATH_PATTERN = Pattern.compile("[^a-zA-Z0-9.-]");
    private static final String SANITIZE_PATH_PLACEHOLDER = "_";

    private final List<String> m_elements = new ArrayList<String>();

    public ResourcePath(String... path) {
        for (String el : path) {
            m_elements.add(el);
        }
    }

    public ResourcePath(Collection<String> pathElements) {
        for (String el : pathElements) {
            m_elements.add(el);
        }
    }

    public ResourcePath(ResourcePath parent, String... path) {
        m_elements.addAll(parent.m_elements);
        for (String el : path) {
            m_elements.add(el);
        }
    }

    public ResourcePath(ResourcePath parent, Iterable<String> path) {
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
    public static ResourcePath get(Collection<String> pathElements) {
        return new ResourcePath(pathElements);
    }

    /**
     * Convenience method.
     */
    public static ResourcePath get(ResourcePath parent, String... path) {
        return new ResourcePath(parent, path);
    }

    /**
     * Convenience method.
     */
    public static ResourcePath get(ResourcePath parent, Iterable<String> path) {
        return new ResourcePath(parent, path);
    }

    /**
     * Convenience method.
     */
    public static ResourcePath get(Path path) {
        List<String> elements = new LinkedList<String>();
        for (Path element : path) {
            elements.add(element.toString());
        }
        return new ResourcePath(elements.toArray(new String[elements.size()]));
    }

    /**
     * Convenience method.
     */
    public static ResourcePath get(String prefix, Path path) {
        List<String> elements = new LinkedList<String>();
        elements.add(prefix);
        for (Path element : path) {
            elements.add(element.toString());
        }
        return new ResourcePath(elements.toArray(new String[elements.size()]));
    }

    public String getName() {
        final int k = m_elements.size() - 1;
        return k < 0 ? null : m_elements.get(k);
    }

    public String[] elements() {
        return m_elements.toArray(new String[m_elements.size()]);
    }

    /**
     * Determines the relative depth of a child path.
     *
     * @return the relative depth >= 0, or -1 if the given child is not actually a child
     */
    public int relativeDepth(ResourcePath child) {
        final List<String> childEls = child.m_elements;
        final int numChildEls = childEls.size();
        final int numParentEls = m_elements.size();

        if (numChildEls < numParentEls) {
            // Definitely not a child
            return -1;
        }

        // Verify the path elements up to the parents
        for (int i = 0; i < numParentEls; i++) {
            if (!m_elements.get(i).equals(childEls.get(i))) {
                return -1;
            }
        }

        return numChildEls - numParentEls;
    }

    @Override
    public Iterator<String> iterator() {
        return m_elements.iterator();
    }

    @Override
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

    public static String sanitize(String path) {
        if (path == null) {
            return null;
        }
        return SANITIZE_PATH_PATTERN.matcher(path).replaceAll(SANITIZE_PATH_PLACEHOLDER);
    }
}
