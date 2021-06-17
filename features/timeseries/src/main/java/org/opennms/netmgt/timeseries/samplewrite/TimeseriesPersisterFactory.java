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

/**
 * Factory for {@link TimeseriesPersister}.
 *
 * @author jwhite
 */
public class TimeseriesPersisterFactory implements PersisterFactory {

    private final TimeseriesWriter timeseriesWriter;
    private final MetaTagDataLoader metaTagDataLoader;
    private final Cache<ResourcePath, Set<Tag>> configuredAdditionalMetaTagCache;
    private final MetricRegistry registry;

    @Inject
    public TimeseriesPersisterFactory(final TimeseriesWriter timeseriesWriter,
                                      final MetaTagDataLoader metaTagDataLoader,
                                      @Named("timeseriesPersisterMetaTagCache") final CacheConfig cacheConfig,
                                      @Named("timeseriesMetricRegistry") MetricRegistry registry) {
        this.timeseriesWriter = timeseriesWriter;
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
        TimeseriesPersister persister =  new TimeseriesPersister(params, repository, timeseriesWriter, metaTagDataLoader, configuredAdditionalMetaTagCache, registry);
        persister.setIgnorePersist(dontPersistCounters);
        return persister;
    }
}
