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
package org.opennms.netmgt.collection.distributed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;

import org.codehaus.jackson.map.ObjectMapper;
import org.opennms.core.xml.JacksonUtils;
import org.opennms.features.distributed.kvstore.api.JsonStore;
import org.opennms.netmgt.config.AbstractResourceTypesDao;
import org.opennms.netmgt.config.dao.common.api.ConfigDaoConstants;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.ResourceTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The resource types of Sentinel: the ones of its own data collection configuration and the ones core stores in the
 * configuration manager and publishes to the {@link JsonStore}.
 *
 * <p>The JSON store is polled lazily, at most once per polling interval. Resource types are looked up while telemetry
 * is processed, so a missing or unreadable document never blocks or fails the lookup: the last resource types read
 * are kept (none before the first successful read) and the next attempt is made after the interval.</p>
 */
public class SentinelResourceTypesDao extends AbstractResourceTypesDao {
    private static final Logger LOG = LoggerFactory.getLogger(SentinelResourceTypesDao.class);

    public static final long DEFAULT_POLLING_INTERVAL_MS = TimeUnit.SECONDS.toMillis(30);

    private final ObjectMapper mapper = JacksonUtils.createDefaultObjectMapper();
    private final JsonStore jsonStore;
    private final long pollingIntervalMs;

    private volatile List<ResourceType> resourceTypes = Collections.emptyList();
    private volatile long lastUpdate = 0;
    private volatile long nextCheck = 0;
    // only accessed while synchronized
    private long lastPublished = -1;
    private boolean missingLogged = false;

    public SentinelResourceTypesDao(final JsonStore jsonStore) {
        this(jsonStore, DEFAULT_POLLING_INTERVAL_MS);
    }

    public SentinelResourceTypesDao(final JsonStore jsonStore, final long pollingIntervalMs) {
        this.jsonStore = Objects.requireNonNull(jsonStore);
        this.pollingIntervalMs = pollingIntervalMs;
    }

    @Override
    protected List<ResourceType> getStoredResourceTypes() {
        poll();
        return resourceTypes;
    }

    @Override
    protected long getStoredLastUpdate() {
        poll();
        return lastUpdate;
    }

    private void poll() {
        if (System.currentTimeMillis() < nextCheck) {
            return;
        }
        synchronized (this) {
            final long now = System.currentTimeMillis();
            if (now < nextCheck) {
                return;
            }
            nextCheck = now + pollingIntervalMs;
            try {
                final OptionalLong published = jsonStore.getLastUpdated(JSON_STORE_KEY, ConfigDaoConstants.JSON_KEY_STORE_CONTEXT);
                if (published.isEmpty()) {
                    if (!missingLogged) {
                        LOG.warn("OpenNMS hasn't published resource types to the JSON store yet, only the resource types of the data collection configuration are known.");
                        missingLogged = true;
                    }
                    return;
                }
                if (published.getAsLong() <= lastPublished) {
                    return;
                }
                final Optional<String> json = jsonStore.get(JSON_STORE_KEY, ConfigDaoConstants.JSON_KEY_STORE_CONTEXT);
                if (json.isEmpty()) {
                    return;
                }
                final ResourceTypes types = mapper.readValue(json.get(), ResourceTypes.class);
                resourceTypes = Collections.unmodifiableList(new ArrayList<>(types.getResourceTypes()));
                lastPublished = published.getAsLong();
                lastUpdate = Math.max(lastUpdate + 1, now);
                missingLogged = false;
                LOG.info("Loaded {} resource types from the JSON store.", resourceTypes.size());
            } catch (final Exception e) {
                LOG.warn("Failed to read the resource types from the JSON store, keeping the {} known ones. Retrying in {} ms: {}",
                        resourceTypes.size(), pollingIntervalMs, e.getMessage(), e);
            }
        }
    }
}
