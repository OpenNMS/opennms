package org.opennms.netmgt.newts.support;

import java.util.Map;

import org.opennms.netmgt.model.ResourcePath;

/**
 * Utility functions and constants.
 *
 * @author jwhite
 */
public abstract class NewtsUtils {

    public static final boolean ENABLE_HIERARCHICAL_INDEXING = false;

    public static final int TTL = Integer.getInteger("org.opennms.newts.config.ttl", 31536000);

    public static final String HOSTNAME_PROPERTY = "org.opennms.newts.config.hostname";

    public static final String KEYSPACE_PROPERTY = "org.opennms.newts.config.keyspace";

    public static final String PORT_PROPERTY = "org.opennms.newts.config.port";

    public static final String TTL_PROPERTY = "org.opennms.newts.config.ttl";

    public static final String DEFAULT_HOSTNAME = "localhost";

    public static final String DEFAULT_KEYSPACE = "newts";

    public static final String DEFAULT_PORT = "9043";

    public static final String DEFAULT_TTL = "" + 86400 * 365;

    public static void addParentPathAttributes(ResourcePath path, Map<String, String> attributes) {
        // Add all of the parent paths as attributes
        StringBuffer sb = new StringBuffer();
        int i = 0;
        for (String el : path) {
            if (sb.length() > 0) {
                sb.append(":");
            }
            sb.append(el);
            attributes.put("_parent" + i++, sb.toString());
        }
    }

    public static String toResourceId(ResourcePath path) {
        StringBuilder sb = new StringBuilder();
        for (final String entry : path) {
            if (sb.length() != 0) {
                sb.append(":");
            }
            sb.append(entry);
        }
        return sb.toString();
    }
}
