/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.model;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
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
 * The elements of the path must not contain a forward slash (/).
 *
 * @author jwhite
 */
public class ResourcePath implements Iterable<String>, Comparable<ResourcePath> {

    private static final Pattern SANITIZE_PATH_PATTERN = Pattern.compile("[^a-zA-Z0-9.-]");
    private static final String SANITIZE_PATH_PLACEHOLDER = "_";

    private final List<String> elements = new ArrayList<>();

    public ResourcePath(String... path) {
        this(Arrays.asList(path));
    }

    public ResourcePath(Iterable<String> pathElements) {
        for (String el : pathElements) {
            if(el != null && el.contains("/")) {
                throw new IllegalArgumentException(String.format("path elements must not contain a forward slash. Offender: %s in %s", el, pathElements));
            }
            elements.add(el);
        }
    }

    public ResourcePath(ResourcePath parent, String... path) {
        this(parent, Arrays.asList(path));
    }

    public ResourcePath(ResourcePath parent, Iterable<String> pathElements) {
        elements.addAll(parent.elements);
        for (String el : pathElements) {
            if(el != null && el.contains("/")) {
                throw new IllegalArgumentException(String.format("path elements must not contain a forward slash. Offender: %s in %s", el, pathElements));
            }
            elements.add(el);
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
        final int k = elements.size() - 1;
        return k < 0 ? null : elements.get(k);
    }

    public String[] elements() {
        return elements.toArray(new String[elements.size()]);
    }

    /**
     * Determines the relative depth of a child path.
     *
     * @return the relative depth >= 0, or -1 if the given child is not actually a child
     */
    public int relativeDepth(ResourcePath child) {
        final List<String> childEls = child.elements;
        final int numChildEls = childEls.size();
        final int numParentEls = elements.size();

        if (numChildEls < numParentEls) {
            // Definitely not a child
            return -1;
        }

        // Verify the path elements up to the parents
        for (int i = 0; i < numParentEls; i++) {
            if (!elements.get(i).equals(childEls.get(i))) {
                return -1;
            }
        }

        return numChildEls - numParentEls;
    }

    public ResourcePath getParent() {
        if(!hasParent()){
            throw new UnsupportedOperationException("I am on the root level already");
        }
        return new ResourcePath(this.elements.subList(0, this.elements.size()-1));
    }

    public boolean hasParent() {
        return this.elements.size()>1;
    }

    @Override
    public Iterator<String> iterator() {
        return elements.iterator();
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
                + ((elements == null) ? 0 : elements.hashCode());
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
        if (elements == null) {
            if (other.elements != null)
                return false;
        } else if (!elements.equals(other.elements))
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
        return path.elements.stream().collect(Collectors.joining(File.separator));
    }
}
