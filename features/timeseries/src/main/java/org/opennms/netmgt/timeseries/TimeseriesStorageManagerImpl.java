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
package org.opennms.netmgt.timeseries;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import org.opennms.core.soa.lookup.ServiceLookup;
import org.opennms.core.soa.lookup.ServiceLookupBuilder;
import org.opennms.core.soa.lookup.ServiceRegistryLookup;
import org.opennms.core.soa.support.DefaultServiceRegistry;
import org.opennms.integration.api.v1.timeseries.StorageException;
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
                .blocking(ServiceLookupBuilder.GRACE_PERIOD_MS, ServiceLookupBuilder.LOOKUP_DELAY_MS, 0)
                .build());
    }

    public TimeseriesStorageManagerImpl(final ServiceLookup<Class<?>, String> lookup) {
        stackOfStorages = new CopyOnWriteArrayList<>();
        LOOKUP = Objects.requireNonNull(lookup);
    }

    public TimeSeriesStorage get() throws StorageException {
        if(this.stackOfStorages.isEmpty()) {
            TimeSeriesStorage storage = LOOKUP.lookup(TimeSeriesStorage.class, null);
            if(storage != null) {
                this.stackOfStorages.addIfAbsent(storage);
            } else {
                LOG.warn("Could not find a TimeSeriesStorage implementation. The collection of metrics won't work properly." +
                        " Please refer to the documentation: https://docs.opennms.org/opennms/releases/latest/guide-admin/guide-admin.html#ga-opennms-operation-timeseries");
            }
        }
        return Optional.ofNullable(getOrNull()).orElseThrow(() -> new StorageException("No timeseries storage implementation found"));
    }

    private TimeSeriesStorage getOrNull() {
        return stackOfStorages.isEmpty() ? null : this.stackOfStorages.get(this.stackOfStorages.size()-1);
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
