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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResourceId implements Comparable<ResourceId> {
    private final static Pattern PATTERN = Pattern.compile("(?:^|\\.)" + // Start with line beginning or a literal dot
                                                           "(?<type>\\w+)" + // Match the type identifier
                                                           "\\[" + // The opening bracket
                                                           "(?<name>(?:" +
                                                           "    [^\\[\\]\\\\]" + // Avoid the escaped characters
                                                           "    |" +
                                                           "    \\\\." + // Any escaped character
                                                           ")*)" + // The name group
                                                           "\\]" +  // The closing bracket
                                                           "", Pattern.COMMENTS);

    public final ResourceId parent;
    public final String type;
    public final String name;

    private ResourceId(final ResourceId parent,
                       final String type,
                       final String name) {
        if (type == null) {
            throw new IllegalArgumentException("Type must be non-null");
        }
        if (!type.matches("\\w+")) {
            throw new IllegalArgumentException("Type must be a valid identifier: "+ type);
        }

        this.parent = parent;
        this.type = type;
        this.name = name == null ? "" : name;
    }

    public ResourceId getParent() {
        return this.parent;
    }

    public ResourceId resolve(final String type,
                              final String name) {
        return new ResourceId(this, type, name);
    }

    public static ResourceId get(final ResourceId parent,
                                 final String type,
                                 final String name) {
        return new ResourceId(parent, type, name);
    }

    public static ResourceId get(final String type,
                                 final String name) {
        return new ResourceId(null, type, name);
    }

    @Override
    public String toString() {
        final StringBuilder s = new StringBuilder();

        if (this.parent != null) {
            s.append(this.parent.toString());
            s.append('.');
        }

        s.append(escape(this.type));
        s.append('[');
        s.append(escape(this.name));
        s.append(']');

        return s.toString();
    }

    public static ResourceId fromString(final String s) {
        if (s == null) {
            return null;
        }

        final Matcher m = PATTERN.matcher(s);

        final StringBuffer sb = new StringBuffer();

        ResourceId id = null;

        while (m.find()) {
            final String type = unescape(m.group("type"));
            final String name = unescape(m.group("name"));

            id = new ResourceId(id, type, name);

            m.appendReplacement(sb, "");
        }

        m.appendTail(sb);

        if (sb.length() > 0) {
            throw new IllegalArgumentException("Ill-formed resource ID: " + sb.toString());
        }

        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResourceId that = (ResourceId) o;

        if (parent != null ? !parent.equals(that.parent) : that.parent != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        return name != null ? name.equals(that.name) : that.name == null;
    }

    @Override
    public int hashCode() {
        int result = parent != null ? parent.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(ResourceId resourceId) {
        return toString().compareToIgnoreCase(resourceId.toString());
    }

    private static String escape(final String raw) {
        return raw.replaceAll("[\\[\\]\\\\]", "\\\\$0");
    }

    private static String unescape(final String escaped) {
        return escaped.replaceAll("\\\\(.)", "$1");
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
