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

package org.opennms.netmgt.timeseries.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.opennms.integration.api.v1.timeseries.TimeSeriesStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for retrieving the TimeseriesStorage that was exposed via osgi.
 */
public class TimeseriesStorageManager {

    private static final Logger LOG = LoggerFactory.getLogger(TimeseriesStorageManager.class);

    private List<TimeSeriesStorage> stackOfStorages;

    private TimeSeriesStorage currentStorage;

    public TimeseriesStorageManager() {
        this.currentStorage = new NoOpsTimeseriesStorage(); // make sure we have at least a storage
        stackOfStorages = new ArrayList<>();
        this.stackOfStorages.add(currentStorage);
    }

    public TimeSeriesStorage get() {
        return this.currentStorage;
    }

    @SuppressWarnings("rawtypes")
    public synchronized void onBind(final TimeSeriesStorage storage, final Map properties) {
        LOG.debug("Bind called with {}: {}", storage, properties);
        if (storage != null && !this.stackOfStorages.contains(storage)) {
            LOG.info("Found new TimeSeriesStorage {}, it will replace the existing one: {}", storage, this.currentStorage);
            this.stackOfStorages.add(storage);
            this.currentStorage = storage;
        }
    }

    @SuppressWarnings("rawtypes")
    public synchronized void onUnbind(final TimeSeriesStorage storage, Map properties) {
        LOG.debug("Unbind called with {}: {}", storage, properties);
        if (storage != null) {
            this.stackOfStorages.remove(storage);
            this.currentStorage = this.stackOfStorages.get(this.stackOfStorages.size()-1);
            LOG.info("Remove TimeSeriesStorage {}, it will replaced by: {}", storage, this.currentStorage);
        }
    }
}
