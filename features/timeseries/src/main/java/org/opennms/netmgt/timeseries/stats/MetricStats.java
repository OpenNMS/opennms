/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.timeseries.stats;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.opennms.integration.api.v1.timeseries.Metric;
import org.opennms.integration.api.v1.timeseries.Sample;
import org.opennms.integration.api.v1.timeseries.Tag;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import net.agkn.hll.HLL;

/**
 * We record statistics to answer the following questions:
 * <ul>
 *     <li>What metrics series have the highest tag cardinality?
 *         What does the set tags for the top 10 look like?</li>
 *     <li>Which string properties have the most unique values?</li>
 * </ul>
 *
 */
public class MetricStats {

    private final static int MAX_TOP_N_METRIC = 10;
    private final static int MAX_TOP_N_TAG_KEYS = 10000;

    private final AtomicInteger lowestTagCount = new AtomicInteger();
    private final ConcurrentSkipListSet<Metric> topNMetrics = new ConcurrentSkipListSet<>(
            Comparator.comparingInt(m -> ((Metric)m).getMetaTags().size() + ((Metric)m).getExternalTags().size())
                    .reversed()
                    .thenComparing(m -> ((Metric)m).getKey())); // write should not happen very often since we just push to the highest limit


    private final ConcurrentHashMap<String, HLL> topNTags = new ConcurrentHashMap<>();

    @SuppressWarnings("UnstableApiUsage")
    private final HashFunction hllHashFunction = Hashing.murmur3_128();

    public void record(Collection<Sample> samples) {
        // race conditions might happen but it shouldn't matter for statistical purposes.
        for (Sample sample : samples) {
            Metric metric = sample.getMetric();
            int count = countNoOfTags(metric);
            if (topNMetrics.size() < MAX_TOP_N_METRIC && count >= lowestTagCount.get() || count > lowestTagCount.get() ) {
                putInTopNMetrics(metric);
            }
            if (this.topNTags.size() < MAX_TOP_N_TAG_KEYS) { // we stop counting once we reached the max
                metric.getIntrinsicTags().forEach(this::putInTopTags);
                metric.getMetaTags().forEach(this::putInTopTags);
                metric.getExternalTags().forEach(this::putInTopTags);
            }
        }
    }

    void putInTopNMetrics(final Metric metric) {
        topNMetrics.add(metric);
        if(topNMetrics.size() > MAX_TOP_N_METRIC) {
            topNMetrics.pollLast();
        }
        this.lowestTagCount.set(countNoOfTags(topNMetrics.last()));
    }

    void putInTopTags(final Tag tag) {
        @SuppressWarnings("UnstableApiUsage")
        long hash = hllHashFunction.hashString(tag.getValue(), StandardCharsets.UTF_8).asLong();
        topNTags.computeIfAbsent( tag.getKey(), k -> new HLL(14, 5))
                .addRaw(hash);
    }

    private int countNoOfTags(final Metric metric) {
        return 2 + metric.getMetaTags().size() + metric.getExternalTags().size(); // intrinsic tag count is always 2
    }

    /**
     * List.get(0) => has most tags (top n)
     */
    public List<Metric> getTopNMetricsWithMostTags() {
        return topNMetrics.stream().collect(Collectors.toUnmodifiableList());
    }

    public List<String> getTopNTags() {
        Comparator<Map.Entry<String, HLL>> comp = Comparator.<Map.Entry<String, HLL>>comparingLong(e -> e.getValue().cardinality())
                .reversed()
                .thenComparing(Map.Entry::getKey);
        return topNTags.entrySet().stream()
                .sorted(comp)
                .map(e -> e.getKey() + ": " + e.getValue().cardinality())
                .collect(Collectors.toUnmodifiableList());
    }
}
