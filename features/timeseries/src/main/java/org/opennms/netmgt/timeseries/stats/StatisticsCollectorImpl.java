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
package org.opennms.netmgt.timeseries.stats;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.opennms.integration.api.v1.timeseries.Metric;
import org.opennms.integration.api.v1.timeseries.Sample;
import org.opennms.integration.api.v1.timeseries.Tag;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.util.concurrent.Striped;

import net.agkn.hll.HLL;

public class StatisticsCollectorImpl implements StatisticsCollector {

    private final static int MAX_TOP_N_METRIC = 10;
    private final static int MAX_TOP_N_TAG_KEYS = 10000;
    private final static int STRIPE_MULTIPLIER = 4;

    private final AtomicInteger lowestTagCount = new AtomicInteger();
    private final ConcurrentSkipListSet<Metric> topNMetrics = new ConcurrentSkipListSet<>(
            Comparator.comparingInt(m -> ((Metric)m).getMetaTags().size() + ((Metric)m).getExternalTags().size())
                    .reversed()
                    .thenComparing(m -> ((Metric)m).getKey())); // write should not happen very often since we just push to the highest limit
    private final ConcurrentHashMap<String, HLL> topNTags = new ConcurrentHashMap<>();
    @SuppressWarnings("UnstableApiUsage")
    private final HashFunction hllHashFunction = Hashing.murmur3_128();
    @SuppressWarnings("UnstableApiUsage")
    private final Striped<Lock> stripedLock;

    @Inject
    @SuppressWarnings("UnstableApiUsage")
    public StatisticsCollectorImpl(@Named("timeseries.writer_threads") Integer numWriterThreads) {
        stripedLock = Striped.lazyWeakLock(numWriterThreads * STRIPE_MULTIPLIER);
    }

    public void record(Collection<Sample> samples) {
        for (Sample sample : samples) {
            Metric metric = sample.getMetric();
            int count = countNoOfTags(metric);
            // we don't synchronize this method for performance reasons. Therefore the lowestTagCount might be changed while we run
            // through the method by another thread. But this should be inconsequential for statistic purposes.
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
        while(topNMetrics.size() > MAX_TOP_N_METRIC) {
            topNMetrics.pollLast();
        }
        this.lowestTagCount.set(countNoOfTags(topNMetrics.last()));
    }

    @SuppressWarnings("UnstableApiUsage")
    void putInTopTags(final Tag tag) {
        @SuppressWarnings("UnstableApiUsage")
        long hash = hllHashFunction.hashString(tag.getValue(), StandardCharsets.UTF_8).asLong();
        Lock lock = null;
        try {
            lock = stripedLock.get(tag.getKey());
            lock.lock();
            topNTags.computeIfAbsent( tag.getKey(), k -> new HLL(14, 5))
                    .addRaw(hash);
        } finally {
            if (lock != null) {
                lock.unlock();
            }
        }

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
                .limit(100)
                .map(e -> e.getKey() + ": " + e.getValue().cardinality())
                .collect(Collectors.toUnmodifiableList());
    }
}
