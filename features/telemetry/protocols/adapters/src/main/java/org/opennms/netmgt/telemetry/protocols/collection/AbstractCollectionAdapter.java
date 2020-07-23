/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.collection;

import static com.codahale.metrics.MetricRegistry.name;

import java.io.File;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import org.opennms.core.sysprops.SystemProperties;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.api.PersisterFactory;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLog;
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLogEntry;
import org.opennms.netmgt.telemetry.config.api.AdapterDefinition;
import org.opennms.netmgt.telemetry.config.api.PackageDefinition;
import org.opennms.netmgt.threshd.api.ThresholdInitializationException;
import org.opennms.netmgt.threshd.api.ThresholdingService;
import org.opennms.netmgt.threshd.api.ThresholdingSession;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;

import com.codahale.metrics.MetricRegistry;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public abstract class AbstractCollectionAdapter extends AbstractAdapter {
    private static final ServiceParameters EMPTY_SERVICE_PARAMETERS = new ServiceParameters(Collections.emptyMap());

    private final LoadingCache<CacheKey, Optional<PackageDefinition>> cache = CacheBuilder.newBuilder()
            .maximumSize(SystemProperties.getLong("org.opennms.features.telemetry.cache.ipAddressFilter.maximumSize", 1000))
            .expireAfterWrite(
                    SystemProperties.getLong("org.opennms.features.telemetry.cache.ipAddressFilter.expireAfterWrite", 120),
                    TimeUnit.SECONDS)
                                                                                          .build(new CacheLoader<CacheKey, Optional<PackageDefinition>>() {
                @Override
                public Optional<PackageDefinition> load(CacheKey key) {
                    for (PackageDefinition pkg : adapterConfig.getPackages()) {
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
    protected BundleContext bundleContext;

    @Autowired
    private FilterDao filterDao;

    @Autowired
    private PersisterFactory persisterFactory;

    @Autowired
    private ThresholdingService thresholdingService;

    // Changed to False if no ThresholdingService has been wired.
    private AtomicBoolean isThresholdingEnabled = new AtomicBoolean(true);

    // Default TTL for ThresholdingSessions is one day.
    private Integer thresholdingSessionTtlMinutes = SystemProperties.getInteger("org.opennms.netmgt.telemetry.protocols.collection.thresholdingSessionTtlMinutes", 1440);

    private Cache<String, ThresholdingSession> agentThresholdingSessions = CacheBuilder.newBuilder().expireAfterAccess(thresholdingSessionTtlMinutes, TimeUnit.MINUTES).build();

    public AbstractCollectionAdapter(final AdapterDefinition adapterConfig,
                                     final MetricRegistry metricRegistry) {
        super(adapterConfig, metricRegistry);
    }

    /**
     * Build a collection set from the given message.
     *
     * The message log is also provided in case the log contains additional
     * meta-data required.
     *
     * IMPORTANT: Implementations of this method must be thread-safe.
     *
     * @param message
     *            message to be converted into a collection set
     * @param messageLog
     *            message log to which the message belongs
     * @return a {@link CollectionSetWithAgent} or an empty value if nothing
     *         should be persisted
     * @throws Exception
     *             if an error occured while generating the collection set
     */
    public abstract Stream<CollectionSetWithAgent> handleCollectionMessage(TelemetryMessageLogEntry message, TelemetryMessageLog messageLog);

    public final void handleMessage(TelemetryMessageLogEntry message, TelemetryMessageLog messageLog) {
        handleCollectionMessage(message, messageLog).forEach(result -> {
            // Locate the matching package definition
            final PackageDefinition pkg = getPackageFor(adapterConfig, result.getAgent());
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
            final CollectionSet collectionSet = result.getCollectionSet();
            LOG.trace("Persisting collection set: {} for message: {}", collectionSet, message);
            final Persister persister = persisterFactory.createPersister(EMPTY_SERVICE_PARAMETERS, repository);
            collectionSet.visit(persister);

            // Thresholding
            try {
                if (isThresholdingEnabled.get()) {
                    ThresholdingSession session = getSessionForAgent(result.getAgent(), repository);
                    session.accept(collectionSet);
                }
            } catch (ThresholdInitializationException e) {
                LOG.warn("Failed Thresholding of CollectionSet : {} for agent: {}", e.getMessage(), result.getAgent());
            }
        });
    }

    private ThresholdingSession getSessionForAgent(CollectionAgent agent, RrdRepository repository) throws ThresholdInitializationException {
        if (thresholdingService == null) {
            // If we don't have a ThresholdingService,
            // we are running in the OSGi container (i.e. on a Sentinal) with no Thresholding Service Configured
            // Disable Thresholding.
            isThresholdingEnabled.set(false);
            throw new ThresholdInitializationException("No ThresholdingService available. No future Threshholding will be done");
        }
        // Map of sessions keyed by agent
        int nodeId = agent.getNodeId();
        String hostAddress = agent.getHostAddress();
        String serviceName = adapterConfig.getName();
        String sessionKey = getSessionKey(nodeId, hostAddress, serviceName);

        ThresholdingSession session = agentThresholdingSessions.getIfPresent(sessionKey);
        if (session == null) {
            session = thresholdingService.createSession(nodeId, hostAddress, serviceName, repository, EMPTY_SERVICE_PARAMETERS);
            agentThresholdingSessions.put(sessionKey, session);
        }
        return session;
    }

    private String getSessionKey(int nodeId, String hostAddress, String serviceName) {
        return new StringBuilder(String.valueOf(nodeId)).append(hostAddress).append(serviceName).toString();
    }

    private PackageDefinition getPackageFor(AdapterDefinition protocol, CollectionAgent agent) {
        try {
            Optional<PackageDefinition> value = cache.get(new CacheKey(protocol.getName(), agent.getHostAddress()));
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

    public ThresholdingService getThresholdingService() {
        return thresholdingService;
    }

    public void setThresholdingService(ThresholdingService thresholdingService) {
        this.thresholdingService = thresholdingService;
    }

    public void setBundleContext(final BundleContext bundleContext) {
        this.bundleContext = bundleContext;
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
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            final CacheKey cacheKey = (CacheKey) o;
            final boolean equals = Objects.equals(hostAddress, cacheKey.hostAddress)
                    && Objects.equals(protocol, cacheKey.protocol);
            return equals;
        }
    }

}
