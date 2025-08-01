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
package org.opennms.netmgt.timeseries.samplewrite;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.opennms.core.cache.Cache;
import org.opennms.core.cache.CacheBuilder;
import org.opennms.core.cache.CacheConfig;
import org.opennms.integration.api.v1.timeseries.Tag;
import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.api.PersisterFactory;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.rrd.RrdRepository;

import com.codahale.metrics.MetricRegistry;
import org.opennms.netmgt.timeseries.TimeseriesStorageManager;
import org.opennms.netmgt.timeseries.stats.StatisticsCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Factory for {@link TimeseriesPersister}.
 *
 * @author jwhite
 */
public class TimeseriesPersisterFactory implements PersisterFactory {
    private static final Logger LOG = LoggerFactory.getLogger(TimeseriesPersisterFactory.class);

    private final TimeseriesWriter timeseriesWriter;
    private final MetaTagDataLoader metaTagDataLoader;
    private final Cache<ResourcePath, Set<Tag>> configuredAdditionalMetaTagCache;
    private final MetricRegistry registry;

    @Inject
    public TimeseriesPersisterFactory(final MetaTagDataLoader metaTagDataLoader,
                                      final StatisticsCollector stats,
                                      @Named("timeseriesStorageManager") final TimeseriesStorageManager timeseriesStorageManager,
                                      @Named("timeseriesPersisterMetaTagCache") final CacheConfig cacheConfig,
                                      @Named("timeseriesMetricRegistry") MetricRegistry registry,
                                      @Named("timeseriesWriterConfig") TimeseriesWriterConfig timeseriesWriterConfig) {
        if (timeseriesWriterConfig.getBufferType() == TimeseriesWriterConfig.BufferType.OFFHEAP) {
            this.timeseriesWriter = new OffheapTimeSeriesWriter(timeseriesStorageManager,timeseriesWriterConfig, registry);
        } else {
            this.timeseriesWriter = new RingBufferTimeseriesWriter(timeseriesStorageManager, stats, timeseriesWriterConfig.getBufferSize(),
                    timeseriesWriterConfig.getNumWriterThreads(), registry);
        }
        LOG.info("Writer: {}", this.timeseriesWriter);

        this.metaTagDataLoader = metaTagDataLoader;
        this.configuredAdditionalMetaTagCache = new CacheBuilder<>()
                .withConfig(cacheConfig)
                .withCacheLoader(metaTagDataLoader)
                .build();
        this.registry = registry;
    }

    @Override
    public Persister createPersister(ServiceParameters params, RrdRepository repository) {
        return createPersister(params, repository, false, false, false);
    }

    @Override
    public Persister createPersister(ServiceParameters params, RrdRepository repository, boolean dontPersistCounters,
                                     boolean forceStoreByGroup, boolean dontReorderAttributes) {
        // We ignore the forceStoreByGroup flag since we always store by group, and we ignore
        // the dontReorderAttributes flag since attribute order does not matter
        TimeseriesPersister persister = new TimeseriesPersister(params, repository, timeseriesWriter, metaTagDataLoader, configuredAdditionalMetaTagCache, registry);
        persister.setIgnorePersist(dontPersistCounters);
        return persister;
    }

    public void destroy() {
        if (timeseriesWriter != null) {
            timeseriesWriter.destroy();
        }
    }
}
