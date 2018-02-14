/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.adapters.collection;

import java.io.File;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.api.PersisterFactory;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.telemetry.adapters.api.Adapter;
import org.opennms.netmgt.telemetry.adapters.api.TelemetryMessage;
import org.opennms.netmgt.telemetry.adapters.api.TelemetryMessageLog;
import org.opennms.netmgt.telemetry.config.api.Package;
import org.opennms.netmgt.telemetry.config.api.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public abstract class AbstractPersistingAdapter implements Adapter {
    private final Logger LOG = LoggerFactory.getLogger(AbstractPersistingAdapter.class);

    private static final ServiceParameters EMPTY_SERVICE_PARAMETERS = new ServiceParameters(Collections.emptyMap());

    @Autowired
    private FilterDao filterDao;

    @Autowired
    private PersisterFactory persisterFactory;

    private Protocol protocol;

    private final LoadingCache<CacheKey, Optional<Package>> cache = CacheBuilder.newBuilder()
            .maximumSize(Long.getLong("org.opennms.features.telemetry.cache.ipAddressFilter.maximumSize", 1000))
            .expireAfterWrite(Long.getLong("org.opennms.features.telemetry.cache.ipAddressFilter.expireAfterWrite", 120), TimeUnit.SECONDS)
            .build(new CacheLoader<CacheKey, Optional<Package>>() {
                @Override
                public Optional<Package> load(CacheKey key) {
                    for (Package pkg : protocol.getPackages()) {
                        final String filterRule = pkg.getFilterRule();
                        if (filterRule == null) {
                            // No filter specified, always match
                            return Optional.of(pkg);
                        }
                        // NOTE: The location of the host address is not taken into account.
                        if (filterDao.isValid(key.getHostAddress(), pkg.getFilterRule())) {
                            return Optional.of(pkg);
                        }
                    }
                    return Optional.empty();
                }
            });

    /**
     * Build a collection set from the given message.
     *
     * The message log is also provided in case the log contains additional meta-data
     * required.
     *
     * IMPORTANT: Implementations of this method must be thread-safe.
     *
     * @param message message to be converted into a collection set
     * @param messageLog message log to which the message belongs
     * @return a {@link CollectionSetWithAgent} or an empty value if nothing should be persisted
     * @throws Exception if an error occured while generating the collection set
     */
    public abstract Optional<CollectionSetWithAgent> handleMessage(TelemetryMessage message, TelemetryMessageLog messageLog) throws Exception;

    @Override
    public void handleMessageLog(TelemetryMessageLog messageLog) {
        for (TelemetryMessage message : messageLog.getMessageList()) {
            final Optional<CollectionSetWithAgent> result;
            try {
                result = handleMessage(message, messageLog);
            } catch (Exception e) {
                LOG.warn("Failed to build a collection set from message: {}. Dropping.", message, e);
                return;
            }

            if (!result.isPresent()) {
                LOG.debug("No collection set was returned when processing message: {}. Dropping.", message);
                return;
            }

            // Locate the matching package definition
            final Package pkg = getPackageFor(protocol, result.get().getAgent());
            if (pkg == null) {
                LOG.warn("No matching package found for message: {}. Dropping.", message);
                return;
            }

            // Build the repository from the package definition
            final RrdRepository repository = new RrdRepository();
            repository.setStep(pkg.getRrd().getStep());
            repository.setHeartBeat(repository.getStep() * 2);
            repository.setRraList(pkg.getRrd().getRras());
            repository.setRrdBaseDir(new File(pkg.getRrd().getBaseDir()));

            // Persist!
            final CollectionSet collectionSet = result.get().getCollectionSet();
            LOG.trace("Persisting collection set: {} for message: {}", collectionSet, message);
            final Persister persister = persisterFactory.createPersister(EMPTY_SERVICE_PARAMETERS, repository);
            collectionSet.visit(persister);
        }
    }

    @Override
    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    private Package getPackageFor(Protocol protocol, CollectionAgent agent) {
        try {
            Optional<Package> value = cache.get(new CacheKey(protocol.getName(), agent.getHostAddress()));
            return value.orElse(null);
        } catch (ExecutionException e) {
            LOG.error("Error while retrieving package from Cache: {}.", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

	public void setFilterDao(FilterDao filterDao) {
		this.filterDao = filterDao;
	}

	public void setPersisterFactory(PersisterFactory persisterFactory) {
		this.persisterFactory = persisterFactory;
	}

    private static class CacheKey {
        private String protocol;
        private String hostAddress;

        public CacheKey(String protocol, String hostAddress) {
            this.protocol = Objects.requireNonNull(protocol);
            this.hostAddress = Objects.requireNonNull(hostAddress);
        }

        public String getProtocol() {
            return protocol;
        }

        public String getHostAddress() {
            return hostAddress;
        }

        @Override
        public int hashCode() {
            return Objects.hash(hostAddress, protocol);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final CacheKey cacheKey = (CacheKey) o;
            final boolean equals = Objects.equals(hostAddress, cacheKey.hostAddress)
                    && Objects.equals(protocol, cacheKey.protocol);
            return equals;
        }
    }
}
