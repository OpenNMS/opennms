/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.timeseries;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import org.opennms.core.soa.lookup.ServiceLookup;
import org.opennms.core.soa.lookup.ServiceLookupBuilder;
import org.opennms.core.soa.lookup.ServiceRegistryLookup;
import org.opennms.core.soa.support.DefaultServiceRegistry;
import org.opennms.integration.api.v1.timeseries.TimeSeriesStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for retrieving the TimeseriesStorage that was exposed via osgi.
 */
public class TimeseriesStorageManagerImpl implements TimeseriesStorageManager {

    private static final Logger LOG = LoggerFactory.getLogger(TimeseriesStorageManagerImpl.class);

    private CopyOnWriteArrayList<TimeSeriesStorage> stackOfStorages;

    private ServiceLookup<Class<?>, String> LOOKUP;

    public TimeseriesStorageManagerImpl() {
        this(new ServiceLookupBuilder(new ServiceRegistryLookup(DefaultServiceRegistry.INSTANCE))
                .blocking()
                .build());
    }

    public TimeseriesStorageManagerImpl(final ServiceLookup<Class<?>, String> lookup) {
        stackOfStorages = new CopyOnWriteArrayList<>();
        LOOKUP = Objects.requireNonNull(lookup);
    }

    public TimeSeriesStorage get() {
        if(this.stackOfStorages.isEmpty()) {
            TimeSeriesStorage storage = LOOKUP.lookup(TimeSeriesStorage.class, null);
            if(storage != null) {
                this.stackOfStorages.addIfAbsent(storage);
            } else {
                LOG.warn("Could not find a TimeSeriesStorage implementation. The collection of metrics won't work properly." +
                        " Please refer to the documentation: https://docs.opennms.org/opennms/releases/latest/guide-admin/guide-admin.html#ga-opennms-operation-timeseries");
            }
        }
        return getOrNull();
    }

    private TimeSeriesStorage getOrNull() {
        return this.stackOfStorages.isEmpty() ? null : this.stackOfStorages.get(this.stackOfStorages.size()-1);
    }

    @SuppressWarnings("rawtypes")
    public synchronized void onBind(final TimeSeriesStorage storage, final Map properties) {
        LOG.debug("Bind called with {}: {}", storage, properties);
        TimeSeriesStorage currentStorage = getOrNull();
        if (storage != null && this.stackOfStorages.addIfAbsent(storage)) {
            LOG.info("Found new TimeSeriesStorage {}, will replace the existing one: {}", storage, currentStorage);
        }
    }

    @SuppressWarnings("rawtypes")
    public synchronized void onUnbind(final TimeSeriesStorage storage, Map properties) {
        LOG.debug("Unbind called with {}: {}", storage, properties);
        if (storage != null && this.stackOfStorages.remove(storage)) {
            TimeSeriesStorage currentStorage = getOrNull();
            LOG.info("Remove TimeSeriesStorage {}, it will be replaced by: {}", storage, currentStorage);
        }
    }
}
