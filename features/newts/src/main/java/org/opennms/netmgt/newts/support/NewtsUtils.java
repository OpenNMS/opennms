/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.newts.support;

import java.util.Arrays;
import java.util.Map;

import org.opennms.netmgt.model.ResourcePath;
import org.opennms.newts.api.search.BooleanQuery;
import org.opennms.newts.api.search.Operator;
import org.opennms.newts.api.search.Query;
import org.opennms.newts.api.search.Term;
import org.opennms.newts.api.search.TermQuery;

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

    /**
     * Extends the attribute map with indices used by the {@link org.opennms.netmgt.dao.support.NewtsResourceStorageDao}.
     *
     * A resource path of the form [a, b, c, d] will be indexed with:
     * <ul>
     * <li> _idx1: (a, 4)
     * <li> _idx2: (a:b, 4)
     * <li> _idx3: (a:b:c, 4)
     */
    public static void addIndicesToAttributes(ResourcePath path, Map<String, String> attributes) {
        StringBuffer sb = new StringBuffer();
        String[] els = path.elements();
        for (int i = 0; i < els.length-1; i++) {
            if (i > 0) { sb.append(":"); }
            sb.append(els[i]);
            attributes.put("_idx" + i, String.format("(%s,%d)", sb.toString(), els.length));
        }
    }

    /**
     * Constructs a query used to find all of the resources that have
     * one or more metrics at the given depth bellow the path.
     *
     * Requires resources to have been indexed using {@link #addIndicesToAttributes}.
     */
    public static Query findResourcesWithMetricsAtDepth(ResourcePath path, int depth) {
        // Numeric suffix for the index name, based on the length of parent path
        int idxSuffix = path.elements().length - 1;
        // The length of the resource ids we're interested in finding
        int targetLen = idxSuffix + depth + 2;
        TermQuery tq = new TermQuery(new Term(
                        "_idx"+idxSuffix, // key
                        String.format("(%s,%d)", toResourceId(path), targetLen) // value
                        ));
        BooleanQuery q = new BooleanQuery();
        q.add(tq, Operator.OR);
        return q;
    }

    /**
     * Converts a {@link org.opennms.netmgt.model.ResourcePath} to a Newts resource id.
     *
     * @param path path to convert
     * @return Newts resource id
     */
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

    /**
     * Converts a Newts resource id to a {@link org.opennms.netmgt.model.ResourcePath}.
     *
     * @param resourceId Newts resource id
     * @return path
     */
    public static ResourcePath toResourcePath(String resourceId) {
        if (resourceId == null) {
            return null;
        }

        String els[] = resourceId.split(":");
        if (els.length == 0) {
            return null;
        }

        return ResourcePath.get(Arrays.copyOf(els, els.length-1));
    }

}
