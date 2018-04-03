/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.model;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    private final List<String> m_elements = new ArrayList<>();

    public ResourcePath(String... path) {
        for (String el : path) {
            m_elements.add(el);
        }
    }

    public ResourcePath(Iterable<String> pathElements) {
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
    public static ResourcePath get(Iterable<String> pathElements) {
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
        return ResourcePath.toString(this);
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

    /**
     * Converts the given resource path to a relative path on filesystem.
     * @param path the resource path to resolve
     * @return the relative path of the resource on disk
     */
    public static Path resourceToFilesystemPath(ResourcePath path) {
        // Replace colons on windows machines (see #NMS-8085)
        Path result = Paths.get("");
        for (String e : path) {
            if (File.separatorChar == '\\') {
                e = e.replace(':', '_');
            }

            result = result.resolve(e);
        }

        return result;
    }

    public static ResourcePath fromString(final String s) {
        if (s.isEmpty()) {
            return ResourcePath.get();
        }

        return ResourcePath.get(s.split("/"));
    }

    public static String toString(final ResourcePath path) {
        return path.m_elements.stream().collect(Collectors.joining(File.separator));
    }
}
