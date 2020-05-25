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

package org.opennms.netmgt.timeseries.integration.persistence;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.api.PersisterFactory;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.timeseries.integration.TimeseriesWriter;
import org.opennms.newts.api.Context;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Factory for {@link TimeseriesPersister}.
 *
 * @author jwhite
 */
public class TimeseriesPersisterFactory implements PersisterFactory {

    private final TimeseriesWriter timeseriesWriter;
    private final Context context;
    private final MetaTagDataLoader metaTagDataLoader;
    private final Map<ResourcePath, Map<String, String>> metaCache;

    @Inject
    public TimeseriesPersisterFactory(final Context context, final TimeseriesWriter timeseriesWriter, final MetaTagDataLoader metaTagDataLoader) {
        this.context = Objects.requireNonNull(context);
        this.timeseriesWriter = timeseriesWriter;
        this.metaTagDataLoader = metaTagDataLoader;
        Cache<ResourcePath, Map<String, String>> cache = CacheBuilder
                .newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES) // TODO Patrick: make configurable
                .build();
        this.metaCache = cache.asMap();
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
        TimeseriesPersister persister =  new TimeseriesPersister(params, repository, timeseriesWriter, context, metaTagDataLoader, metaCache);
        persister.setIgnorePersist(dontPersistCounters);
        return persister;
    }
}
