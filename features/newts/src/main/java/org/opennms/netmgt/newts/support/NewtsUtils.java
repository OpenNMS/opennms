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
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.opennms.netmgt.model.ResourcePath;
import org.opennms.newts.api.Context;
import org.opennms.newts.api.MetricType;
import org.opennms.newts.api.Resource;
import org.opennms.newts.api.Sample;
import org.opennms.newts.api.Timestamp;
import org.opennms.newts.api.ValueType;
import org.opennms.newts.api.search.BooleanQuery;
import org.opennms.newts.api.search.Operator;
import org.opennms.newts.api.search.Query;
import org.opennms.newts.api.search.Term;
import org.opennms.newts.api.search.TermQuery;
import org.opennms.newts.cassandra.search.CassandraIndexingOptions;
import org.opennms.newts.cassandra.search.EscapableResourceIdSplitter;
import org.opennms.newts.cassandra.search.ResourceIdSplitter;


/**
 * Utility functions and constants.
 *
 * @author jwhite
 */
public abstract class NewtsUtils {

    public static final boolean DISABLE_INDEXING = Boolean.getBoolean("org.opennms.newts.disable.indexing");

    public static final int MAX_BATCH_SIZE = Integer.getInteger("org.opennms.newts.config.max_batch_size", 16);

    public static final int TTL = Integer.getInteger("org.opennms.newts.config.ttl", 31536000);

    public static final String HOSTNAME_PROPERTY = "org.opennms.newts.config.hostname";

    public static final String KEYSPACE_PROPERTY = "org.opennms.newts.config.keyspace";

    public static final String PORT_PROPERTY = "org.opennms.newts.config.port";

    public static final String TTL_PROPERTY = "org.opennms.newts.config.ttl";

    public static final String DEFAULT_HOSTNAME = "localhost";

    public static final String DEFAULT_KEYSPACE = "newts";

    public static final String DEFAULT_PORT = "9043";

    public static final String DEFAULT_TTL = "" + 86400 * 365;

    public static final CassandraIndexingOptions INDEXING_OPTIONS = new CassandraIndexingOptions.Builder()
            .withHierarchicalIndexing(false)
            .withIndexResourceTerms(false)
            .withIndexUsingDefaultTerm(false)
            .withMaxBatchSize(MAX_BATCH_SIZE)
            .build();

    private static final ResourceIdSplitter s_splitter = new EscapableResourceIdSplitter();

    // Constants used when building mock samples in createSampleForIndexingStrings()
    private static final Timestamp EPOCH = Timestamp.fromEpochMillis(0);
    private static final ValueType<?> ZERO = ValueType.compose(0, MetricType.GAUGE);

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
        final List<String> els = Arrays.asList(path.elements());
        final int N = els.size();
        for (int i = 0; i < N; i++) {
            final String id = s_splitter.joinElementsToId(els.subList(0, i+1));
            attributes.put("_idx" + i, String.format("(%s,%d)", id, N));
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
        return s_splitter.joinElementsToId(Arrays.asList(path.elements()));
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

        List<String> els = s_splitter.splitIdIntoElements(resourceId);
        return ResourcePath.get(els.subList(0, els.size() - 1));
    }

    /**
     * Extracts the metric name from the resource id.
     *
     * @param resourceId Newts resource id
     * @return metric name
     */
    public static String toMetricName(String resourceId) {
        if (resourceId == null) {
            return null;
        }

        List<String> els = s_splitter.splitIdIntoElements(resourceId);
        return els.get(els.size() - 1);
    }

    /**
     * Creates a sample used to index string attributes.
     *
     * These should only be index and not be persisted.
     */
    public static Sample createSampleForIndexingStrings(Context context, Resource resource) {
        return new Sample(EPOCH, context, resource, "strings", MetricType.GAUGE, ZERO);
    }

}
