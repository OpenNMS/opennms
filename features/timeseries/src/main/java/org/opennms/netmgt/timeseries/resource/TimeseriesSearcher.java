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
package org.opennms.netmgt.timeseries.resource;

import static org.opennms.netmgt.model.ResourceTypeUtils.getNumPathElementsToNodeLevel;
import static org.opennms.netmgt.timeseries.util.TimeseriesUtils.toResourceId;
import static org.opennms.netmgt.timeseries.util.TimeseriesUtils.toSearchRegex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.inject.Named;

import org.opennms.core.cache.Cache;
import org.opennms.core.cache.CacheConfig;
import org.opennms.integration.api.v1.timeseries.IntrinsicTagNames;
import org.opennms.integration.api.v1.timeseries.Metric;
import org.opennms.integration.api.v1.timeseries.StorageException;
import org.opennms.integration.api.v1.timeseries.Tag;
import org.opennms.integration.api.v1.timeseries.TagMatcher;
import org.opennms.integration.api.v1.timeseries.immutables.ImmutableTagMatcher;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.timeseries.TimeseriesStorageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.cache.CacheLoader;
import com.google.common.collect.Sets;

/**
 * Finds metrics by resource path.
 *
 * Searches at or below node level are served from a single cached storage query
 * per node ("everything below snmp/&lt;node&gt;"). Searches above node level are
 * cached per exact query. Either way the cached value is a {@link MetricIndex}:
 * the query result pre-grouped by every (parent path, depth) combination that
 * {@link #search(ResourcePath, int)} can ask for, so a search is a map lookup.
 *
 * Every cache entry is built completely by the cache loader and is immutable once
 * visible. Nothing is derived from an entry after it has been published, so a
 * concurrent reader can never observe a partially built or placeholder result.
 *
 * Entries expire a fixed time after they were written (see the
 * {@code cache.timeseriesSearcherCache.expireAfterWrite} property), so resources that
 * were added to storage become visible within that time even on a node that is read
 * constantly. Metrics deleted through {@link #removeFromCache(ResourcePath, Collection)}
 * disappear from the cached entry immediately.
 */
public class TimeseriesSearcher {

    private static final Logger LOG = LoggerFactory.getLogger(TimeseriesSearcher.class);

    private final Cache<Query, MetricIndex> indexByQuery;

    @Autowired
    public TimeseriesSearcher(TimeseriesStorageManager timeseriesStorageManager,
                              @Named("timeseriesSearcherCache") final CacheConfig cacheConfig) {
        Objects.requireNonNull(timeseriesStorageManager, "timeseriesStorageManager must not be null");
        indexByQuery = new org.opennms.core.cache.CacheBuilder<Query, MetricIndex>()
                .withConfig(cacheConfig)
                .withCacheLoader(new MetricIndexLoader(timeseriesStorageManager))
                .build();
    }

    /**
     * Returns all metrics whose resource id is exactly {@code depth + 1} elements longer than
     * {@code path}. A depth of {@code -1} returns the metrics whose resource id equals the path.
     * The returned set is immutable.
     */
    public Set<Metric> search(ResourcePath path, int depth) throws StorageException {
        return load(queryFor(path, depth)).get(toResourceId(path), depth + 1);
    }

    /**
     * Hides metrics that were deleted from storage from the cached entry that
     * {@link #search(ResourcePath, int)} would consult for {@code path}, without querying
     * storage again. Does nothing if that entry is not cached. The entry is replaced as a
     * whole, so a reload that completes concurrently may be overwritten by the previous
     * content minus the deleted metrics; the next write expiry reconciles that.
     */
    public void removeFromCache(final ResourcePath path, final Collection<Metric> deletedMetrics) {
        if (deletedMetrics.isEmpty()) {
            return;
        }
        // depth 0: the entry delete() searched, and the node entry for everything at or below the node
        final Query query = queryFor(path, 0);
        final MetricIndex cached = indexByQuery.getIfCached(query);
        if (cached != null) {
            indexByQuery.put(query, cached.without(deletedMetrics));
        }
    }

    private Query queryFor(final ResourcePath path, final int depth) {
        final int numPathElementsToNodeLevel = getNumPathElementsToNodeLevel(path);
        if (numPathElementsToNodeLevel <= 0) {
            // Above node level: ask storage for exactly what was requested. Only the
            // requested parent is ever looked up in the resulting index.
            return new Query(toSearchRegex(path, depth + 1), path.elements().length);
        }
        // At or below node level: fetch (or reuse) everything under the node. Only
        // parents at or below the node are ever looked up in the resulting index.
        final String nodeResourceId = String.join("/", Arrays.asList(path.elements()).subList(0, numPathElementsToNodeLevel));
        return new Query(nodeResourceId + "/.*$", numPathElementsToNodeLevel);
    }

    private MetricIndex load(final Query query) throws StorageException {
        try {
            return indexByQuery.get(query);
        } catch (ExecutionException | RuntimeException e) {
            throw new StorageException(e);
        }
    }

    /**
     * A storage query (a regex on the resource id) together with the number of elements of the
     * shortest parent path that will be looked up in its result.
     */
    private static final class Query {

        private final String resourceIdRegex;
        private final int minParentElements;

        Query(final String resourceIdRegex, final int minParentElements) {
            this.resourceIdRegex = Objects.requireNonNull(resourceIdRegex);
            this.minParentElements = minParentElements;
        }

        int minParentElements() {
            return minParentElements;
        }

        TagMatcher toTagMatcher() {
            return ImmutableTagMatcher.builder()
                    .type(TagMatcher.Type.EQUALS_REGEX)
                    .key(IntrinsicTagNames.resourceId)
                    .value(resourceIdRegex)
                    .build();
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Query)) {
                return false;
            }
            final Query other = (Query) o;
            return minParentElements == other.minParentElements && resourceIdRegex.equals(other.resourceIdRegex);
        }

        @Override
        public int hashCode() {
            return Objects.hash(resourceIdRegex, minParentElements);
        }

        @Override
        public String toString() {
            return "Query{resourceIdRegex='" + resourceIdRegex + "', minParentElements=" + minParentElements + "}";
        }
    }

    /**
     * The result of one storage query, grouped by (parent resource id, number of
     * elements below it). Immutable. Metrics deleted since the query was made are
     * kept in a separate set and excluded from every answer, which makes a delete
     * cost nothing but a set union, independent of the size of the index.
     */
    static final class MetricIndex {

        private static final class Key {

            private final String parentResourceId;
            private final int elementsBelow;

            Key(final String parentResourceId, final int elementsBelow) {
                this.parentResourceId = parentResourceId;
                this.elementsBelow = elementsBelow;
            }

            @Override
            public boolean equals(final Object o) {
                if (this == o) {
                    return true;
                }
                if (!(o instanceof Key)) {
                    return false;
                }
                final Key other = (Key) o;
                return elementsBelow == other.elementsBelow && parentResourceId.equals(other.parentResourceId);
            }

            @Override
            public int hashCode() {
                return 31 * parentResourceId.hashCode() + elementsBelow;
            }
        }

        private final Map<Key, Set<Metric>> metricsByKey;
        private final Set<Metric> deleted;

        private MetricIndex(final Map<Key, Set<Metric>> metricsByKey, final Set<Metric> deleted) {
            this.metricsByKey = metricsByKey;
            this.deleted = deleted;
        }

        /**
         * Groups the metrics by every parent that has at least {@code minParentElements} elements,
         * including the metric's own resource id (zero elements below). Shorter parents are never
         * looked up, so indexing them would only cost memory.
         */
        static MetricIndex of(final List<Metric> metrics, final int minParentElements) {
            final Map<Key, List<Metric>> index = new HashMap<>();
            for (final Metric metric : metrics) {
                final Tag resourceIdTag = metric.getFirstTagByKey(IntrinsicTagNames.resourceId);
                if (resourceIdTag == null || resourceIdTag.getValue() == null) {
                    LOG.warn("Ignoring metric without a resource id: {}", metric);
                    continue;
                }
                final String[] elements = resourceIdTag.getValue().split("/");
                final int firstParent = Math.max(1, minParentElements);
                if (elements.length < firstParent) {
                    continue;
                }
                final StringBuilder parent = new StringBuilder(String.join("/", Arrays.asList(elements).subList(0, firstParent)));
                for (int p = firstParent; p <= elements.length; p++) {
                    if (p > firstParent) {
                        parent.append('/').append(elements[p - 1]);
                    }
                    index.computeIfAbsent(new Key(parent.toString(), elements.length - p), k -> new ArrayList<>()).add(metric);
                }
            }
            final Map<Key, Set<Metric>> immutable = new HashMap<>(index.size() * 2);
            index.forEach((key, list) -> immutable.put(key, Set.copyOf(list)));
            return new MetricIndex(Collections.unmodifiableMap(immutable), Collections.emptySet());
        }

        /** Returns an index that answers like this one but without the given metrics. */
        MetricIndex without(final Collection<Metric> metrics) {
            final Set<Metric> union = new HashSet<>(deleted);
            union.addAll(metrics);
            return new MetricIndex(metricsByKey, Collections.unmodifiableSet(union));
        }

        Set<Metric> get(final String parentResourceId, final int elementsBelow) {
            final Set<Metric> metrics = metricsByKey.getOrDefault(new Key(parentResourceId, elementsBelow), Collections.emptySet());
            if (deleted.isEmpty() || metrics.isEmpty()) {
                return metrics;
            }
            // an unmodifiable live view over two immutable sets
            return Sets.difference(metrics, deleted);
        }
    }

    private static final class MetricIndexLoader extends CacheLoader<Query, MetricIndex> {

        private final TimeseriesStorageManager timeseriesStorageManager;

        MetricIndexLoader(final TimeseriesStorageManager timeseriesStorageManager) {
            this.timeseriesStorageManager = timeseriesStorageManager;
        }

        @Override
        public MetricIndex load(final Query query) throws Exception {
            LOG.debug("Loading metrics for {}", query);
            final List<Metric> metrics = timeseriesStorageManager.get().findMetrics(Collections.singletonList(query.toTagMatcher()));
            return MetricIndex.of(metrics, query.minParentElements());
        }
    }
}
