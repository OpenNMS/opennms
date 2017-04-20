package org.opennms.netmgt.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResourceId {
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
        // TODO: Check for valid type identifier (limit to java identifier rules?)
        this.parent = parent;
        this.type = type;
        this.name = name;
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

    private static String escape(final String raw) {
        return raw.replaceAll("[\\[\\]\\\\]", "\\\\$0");
    }

    private static String unescape(final String escaped) {
        return escaped.replaceAll("\\\\(.)", "$1");
    }
}
