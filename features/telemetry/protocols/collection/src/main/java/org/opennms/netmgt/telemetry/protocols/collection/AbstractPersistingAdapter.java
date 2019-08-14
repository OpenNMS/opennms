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

package org.opennms.netmgt.telemetry.protocols.collection;

import static com.codahale.metrics.MetricRegistry.name;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import javax.script.ScriptException;

import org.opennms.core.fileutils.FileUpdateCallback;
import org.opennms.core.fileutils.FileUpdateWatcher;
import org.opennms.core.sysprops.SystemProperties;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.api.PersisterFactory;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.telemetry.api.adapter.Adapter;
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLog;
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLogEntry;
import org.opennms.netmgt.telemetry.config.api.AdapterDefinition;
import org.opennms.netmgt.telemetry.config.api.PackageDefinition;
import org.opennms.netmgt.threshd.api.ThresholdInitializationException;
import org.opennms.netmgt.threshd.api.ThresholdingService;
import org.opennms.netmgt.threshd.api.ThresholdingSession;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
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

    @Autowired
    private ThresholdingService thresholdingService;

    // Changed to False if no ThresholdingService has been wired.
    private AtomicBoolean isThresholdingEnabled = new AtomicBoolean(true);

    // Default TTL for ThresholdingSessions is one day.
    private Integer thresholdingSessionTtlMinutes = SystemProperties.getInteger("org.opennms.netmgt.telemetry.protocols.collection.thresholdingSessionTtlMinutes", 1440);

    private Cache<String, ThresholdingSession> agentThresholdingSessions = CacheBuilder.newBuilder().expireAfterAccess(thresholdingSessionTtlMinutes, TimeUnit.MINUTES).build();

    private AdapterDefinition adapterConfig;

    private FileUpdateWatcher scriptUpdateWatcher;

    private BundleContext bundleContext;

    private String script;

    /*
     * Since ScriptCollectionSetBuilder is not thread safe , loading of script
     * is handled in ThreadLocal.
     */
    private final ThreadLocal<ScriptedCollectionSetBuilder> scriptedCollectionSetBuilders = new ThreadLocal<ScriptedCollectionSetBuilder>() {
        @Override
        protected ScriptedCollectionSetBuilder initialValue() {
            try {
                return loadCollectionBuilder(bundleContext, script);
            } catch (Exception e) {
                LOG.error("Failed to create builder for script '{}'.", script, e);
                return null;
            }
        }
    };

    /*
     * Flag to reload script if script didn't compile in earlier invocation,
     * need to be ThreadLocal as script itself loads in ThreadLocal.
     */
    private ThreadLocal<Boolean> scriptCompiled = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return true;
        }

    };

    /*
     * This map is needed since loading of script happens in a ThreadLocal and
     * status of script update needs to be propagated to each thread. This map
     * collects ScriptedCollectionSetBuilder and set it's value as false
     * initially. Whenever script updates and callback reload() gets called, all
     * values will be set to true to trigger reload of script in corresponding
     * thread, see getCollectionBuilder().
     */
    private Map<ScriptedCollectionSetBuilder, Boolean> scriptUpdateMap = new ConcurrentHashMap<>();

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

    /**
     * Time taken to handle a log
     */
    private final Timer logParsingTimer;

    /**
     * Number of message per log
     */
    private final Histogram packetsPerLogHistogram;

    public AbstractPersistingAdapter(String name, MetricRegistry metricRegistry) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(metricRegistry);

        logParsingTimer = metricRegistry.timer(name("adapters", name, "logParsing"));
        packetsPerLogHistogram = metricRegistry.histogram(name("adapters", name, "packetsPerLog"));
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
    public abstract Stream<CollectionSetWithAgent> handleMessage(TelemetryMessageLogEntry message, TelemetryMessageLog messageLog);

    @Override
    public void handleMessageLog(TelemetryMessageLog messageLog) {
        try (Timer.Context ctx = logParsingTimer.time()) {
            for (TelemetryMessageLogEntry message : messageLog.getMessageList()) {
                handleMessage(message, messageLog).forEach(result -> {
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
            packetsPerLogHistogram.update(messageLog.getMessageList().size());
        }
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

    @Override
    public void setConfig(AdapterDefinition adapterConfig) {
        this.adapterConfig = adapterConfig;
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

    /*
     * This method checks and reloads script if there is an update else returns
     * existing builder
     */
    protected ScriptedCollectionSetBuilder getCollectionBuilder() {
        ScriptedCollectionSetBuilder builder = scriptedCollectionSetBuilders.get();
        // Reload script if reload() happened or earlier invocation of script didn't compile
        if ((builder != null && scriptUpdateMap.get(builder)) || !scriptCompiled.get()) {
            scriptedCollectionSetBuilders.remove();
            builder = scriptedCollectionSetBuilders.get();
        }
        if (builder == null) {
            // script didn't compile, set flag to false
            scriptCompiled.set(false);
            return null;
        } else if (!scriptCompiled.get()) {
            scriptCompiled.set(true);
        }
        return builder;
    }

    private ScriptedCollectionSetBuilder loadCollectionBuilder(BundleContext bundleContext, String script)
            throws IOException, ScriptException {
        ScriptedCollectionSetBuilder builder;
        if (bundleContext != null) {
            builder = new ScriptedCollectionSetBuilder(new File(script), bundleContext);
            scriptUpdateMap.put(builder, false);
            return builder;
        } else {
            builder = new ScriptedCollectionSetBuilder(new File(script));
            scriptUpdateMap.put(builder, false);
            return builder;
        }
    }

    private ScriptedCollectionSetBuilder checkScript(BundleContext bundleContext, String script)
            throws IOException, ScriptException {
        if (bundleContext != null) {
            return new ScriptedCollectionSetBuilder(new File(script), bundleContext);
        } else {
            return new ScriptedCollectionSetBuilder(new File(script));
        }
    }

    private void setFileUpdateCallback(String script) {
        if (!Strings.isNullOrEmpty(script)) {
            try {
                scriptUpdateWatcher = new FileUpdateWatcher(script, reloadScript());
            } catch (Exception e) {
                LOG.info("Script reload Utils is not registered", e);
            }
        }
    }

    private FileUpdateCallback reloadScript() {

        return new FileUpdateCallback() {
            /* Callback method for script update */
            @Override
            public void reload() {
                try {
                    checkScript(bundleContext, script);
                    LOG.debug("Updated script compiled");
                    // Set all the values in Map to true to trigger reload of script in all threads
                    scriptUpdateMap.replaceAll((builder, Boolean) -> true);
                } catch (Exception e) {
                    LOG.error("Updated script failed to build, using existing script'{}'.", script, e);
                }
            }

        };
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
        setFileUpdateCallback(script);
    }

    public void destroy() {
        if (scriptUpdateWatcher != null) {
            scriptUpdateWatcher.destroy();
        }
    }
}
