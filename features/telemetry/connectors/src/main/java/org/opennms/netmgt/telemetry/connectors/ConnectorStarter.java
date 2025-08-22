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

package org.opennms.netmgt.telemetry.connectors;


import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.core.ipc.sink.api.MessageDispatcherFactory;
import org.opennms.core.ipc.twin.api.TwinSubscriber;
import org.opennms.core.logging.Logging;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.telemetry.api.receiver.Connector;
import org.opennms.netmgt.telemetry.api.receiver.TelemetryMessage;
import org.opennms.netmgt.telemetry.api.registry.TelemetryRegistry;
import org.opennms.netmgt.telemetry.common.ipc.TelemetrySinkModule;
import org.opennms.netmgt.telemetry.daemon.ConnectorTwinConfig;
import org.opennms.netmgt.telemetry.daemon.Telemetryd;
import org.opennms.netmgt.telemetry.distributed.common.MapBasedConnectorDef;
import org.opennms.netmgt.telemetry.distributed.common.PropertyTree;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Dictionary;
import java.util.Set;
import java.util.Objects;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ConnectorStarter implements ManagedService {
    private static final Logger LOG = LoggerFactory.getLogger(ConnectorStarter.class);

    Map<String, String> configMap;

    private MessageDispatcherFactory messageDispatcherFactory;

    private DistPollerDao distPollerDao;

    private TelemetryRegistry telemetryRegistry;

    private BundleContext bundleContext;

    @Autowired
    private TwinSubscriber m_twinSubscriber;

    private Closeable m_twinSubscription;

    private Object configuredLock = new Object();

    private MapBasedConnectorDef baseDef;

    private Map<String, Entity> entities = new LinkedHashMap<>();

    // This is used just to  track dispatchers by queue name to avoid duplicate registration
    private Map<String, AsyncDispatcher<TelemetryMessage>> queueDispatchers = new ConcurrentHashMap<>();

    // We are using shared dispatcher so if there are multiple connector  with one dispatcher then on stop/disable node flow
    // we should not remove the shared dispatcher . This is used to track it
    private Map<String, AtomicInteger> queueReferenceCounts = new ConcurrentHashMap<>();


    public ConnectorStarter() {}

    public void start() {
        final PropertyTree definition = PropertyTree.from(configMap);
        baseDef = new MapBasedConnectorDef(definition);

    }

    public void stop() {
        LOG.info("ConnectorListener stopping…");
        try {
            m_twinSubscription.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        new ArrayList<>(this.entities.keySet()).forEach(this::delete);
    }

    public void delete(String key) {
        final Entity entity = this.entities.remove(key);
        if (entity != null) {
            LOG.info("Stopping connector for key: {}", key);
            try {
                if (entity.connector != null) {
                    entity.connector.close();
                }
            } catch (IOException e) {
                LOG.error("Failed to close connector for key: {}", key, e);
            }

            // Decrement reference count and cleanup if no more references of connectors exist or not using the shared dispatcher
            if (entity.queueName != null) {
                AtomicInteger refCount = queueReferenceCounts.get(entity.queueName);
                if (refCount != null && refCount.decrementAndGet() <= 0) {
                    AsyncDispatcher<TelemetryMessage> dispatcher = queueDispatchers.remove(entity.queueName);
                    queueReferenceCounts.remove(entity.queueName);
                    if (dispatcher != null) {
                        try {
                            dispatcher.close();
                        } catch (Exception ex) {
                            LOG.error("Failed to close dispatcher for queue: {}", entity.queueName, ex);
                        }
                    }
                    telemetryRegistry.removeDispatcher(entity.queueName);
                }
            }
        }
    }

    @Override
    public void updated(Dictionary<String, ?> props) throws ConfigurationException {

        System.out.println(props);
    }


    private void stopQueues(Set<String> queueNames) {
        Objects.requireNonNull(queueNames);
        for (String queueName : queueNames) {
            try {
                final AsyncDispatcher<TelemetryMessage> dispatcher = telemetryRegistry.getDispatcher(queueName);
                if (dispatcher != null) {
                    dispatcher.close();
                }
            } catch (Exception ex) {
                LOG.error("Failed to close dispatcher.", ex);
            } finally {
                telemetryRegistry.removeDispatcher(queueName);
            }
        }
    }

    public void subscribe() {
        m_twinSubscription = m_twinSubscriber.subscribe( ConnectorTwinConfig.CONNECTOR_KEY, ConnectorTwinConfig.class, this::onConfig);
    }

    private void onConfig(ConnectorTwinConfig request) {
        try (Logging.MDCCloseable mdc = Logging.withPrefixCloseable(Telemetryd.LOG_PREFIX)) {
            LOG.info("Got connectors config update - reloading");
            synchronized (configuredLock) {

                Set<String> newConfigKeys = request.getConfigurations().stream()
                        .map(ConnectorTwinConfig.ConnectorConfig::getNodeConnectorKey)
                        .collect(Collectors.toSet());

                // Remove connectors that are no longer present in the new configuration
                Set<String> currentKeys = new HashSet<>(entities.keySet());
                for (String existingKey : currentKeys) {
                    if (!newConfigKeys.contains(existingKey)) {
                        LOG.info("Removing connector for key: {}", existingKey);
                        delete(existingKey);
                    }
                }

                for (ConnectorTwinConfig.ConnectorConfig config : request.getConfigurations()) {
                    LOG.error("iterating the configs- reloading {} ",config.toString());
                    if (config.isEnabled()) {
                        Entity existingEntity = entities.get(config.getNodeConnectorKey());

                        if (existingEntity != null) {
                            // Check if configuration has changed and needs update
                            // stop the previous connector of node and start with updated configs
                            if (hasConfigChanged(existingEntity.config, config)) {
                                LOG.info("Configuration changed for key: {}, updating", config.getNodeConnectorKey());
                                delete(config.getNodeConnectorKey());
                                startConnector(config);
                            }
                        } else {
                            startConnector(config);
                        }
                    } else {
                        // Configuration is disabled remove if exists
                        if (entities.containsKey(config.getNodeConnectorKey())) {
                            LOG.info("Configuration disabled for key: {}", config.getNodeConnectorKey());
                            delete(config.getNodeConnectorKey());
                        }
                    }
                }
            }
        }
    }

    private void startConnector(ConnectorTwinConfig.ConnectorConfig config) {
        try {
            final Entity entity = new Entity();
            entity.config = config;

            final String queueName = Objects.requireNonNull(baseDef.getQueueName());

            // Get or create dispatcher for this our static queue "loaded from blueprint.xml:
            AsyncDispatcher<TelemetryMessage> dispatcher = queueDispatchers.computeIfAbsent(queueName, name -> {
                final TelemetrySinkModule sinkModule = new TelemetrySinkModule(baseDef);
                sinkModule.setDistPollerDao(distPollerDao);
                AsyncDispatcher<TelemetryMessage> newDispatcher = messageDispatcherFactory.createAsyncDispatcher(sinkModule);
                telemetryRegistry.registerDispatcher(queueName, newDispatcher);
                queueReferenceCounts.put(queueName, new AtomicInteger(0));
                return newDispatcher;
            });

            // Increment reference count for this queue (just to decide weather remove the dispatcher or not while delete/flow disable on a  node)
            queueReferenceCounts.get(queueName).incrementAndGet();

            entity.queueName = queueName;
            entity.connector = telemetryRegistry.getConnector(baseDef);

            InetAddress ip = InetAddress.getByName(config.getIpAddress());
            entity.connector.stream(config.getNodeId(), ip, config.getParameters());

            entities.put(config.getNodeConnectorKey(), entity);
            LOG.info("Started connector for key: {}", config.getNodeConnectorKey());
        } catch (Exception e) {
            LOG.error("Failed to start connector for key: {}", config.getNodeConnectorKey(), e);
        }
    }

    private boolean hasConfigChanged(ConnectorTwinConfig.ConnectorConfig oldConfig,
                                     ConnectorTwinConfig.ConnectorConfig newConfig) {
        return !Objects.equals(oldConfig.getNodeId(), newConfig.getNodeId()) ||
                !Objects.equals(oldConfig.getIpAddress(), newConfig.getIpAddress()) ||
                !Objects.equals(oldConfig.getParameters(), newConfig.getParameters());
    }



    public void bind(TwinSubscriber twinSubscriber) {
        m_twinSubscriber = twinSubscriber;
        subscribe();
    }

    public void unbind(TwinSubscriber twinSubscriber) {
        m_twinSubscriber = null;
    }

    private static class Entity {
        public ConnectorTwinConfig.ConnectorConfig config;
        private Connector connector;
        private String queueName;


    }

    public Map<String, String> getConfigMap() {
        return configMap;
    }

    public void setConfigMap(Map<String, String> configMap) {
        this.configMap = configMap;
    }

    public MessageDispatcherFactory getMessageDispatcherFactory() {
        return messageDispatcherFactory;
    }

    public void setMessageDispatcherFactory(MessageDispatcherFactory messageDispatcherFactory) {
        this.messageDispatcherFactory = messageDispatcherFactory;
    }

    public DistPollerDao getDistPollerDao() {
        return distPollerDao;
    }

    public void setDistPollerDao(DistPollerDao distPollerDao) {
        this.distPollerDao = distPollerDao;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void setTelemetryRegistry(TelemetryRegistry telemetryRegistry) {
        this.telemetryRegistry = telemetryRegistry;
    }

}