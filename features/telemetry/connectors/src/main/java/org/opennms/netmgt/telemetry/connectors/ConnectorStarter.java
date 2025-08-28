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

import com.google.common.annotations.VisibleForTesting;
import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.core.ipc.sink.api.MessageDispatcherFactory;
import org.opennms.core.ipc.twin.api.TwinSubscriber;
import org.opennms.core.logging.Logging;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.telemetry.api.receiver.Connector;
import org.opennms.netmgt.telemetry.api.receiver.TelemetryMessage;
import org.opennms.netmgt.telemetry.api.registry.TelemetryRegistry;
import org.opennms.netmgt.telemetry.common.ipc.TelemetrySinkModule;
import org.opennms.netmgt.telemetry.distributed.common.MapBasedConnectorDef;
import org.opennms.netmgt.telemetry.distributed.common.PropertyTree;
import org.opennms.netmgt.telemetry.config.model.ConnectorTwinConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ConnectorStarter {
    private static final Logger LOG = LoggerFactory.getLogger(ConnectorStarter.class);

    Map<String, String> configMap = new HashMap<>();

    private MessageDispatcherFactory messageDispatcherFactory;

    private DistPollerDao distPollerDao;

    private TelemetryRegistry telemetryRegistry;

    @Autowired
    private TwinSubscriber m_twinSubscriber;

    private Closeable m_twinSubscription;

    private final Object configuredLock = new Object();

    private MapBasedConnectorDef baseDef;

    private final Map<String, Entity> entities = new LinkedHashMap<>();

    // This is used just to  track dispatchers by queue name to avoid duplicate registration
    private AsyncDispatcher<TelemetryMessage> sharedQueueDispatcher = null;

    // We are using shared dispatcher so if there are multiple connector  with one dispatcher then on stop/disable node flow
    // we should not remove the shared dispatcher . This is used to track it
    private final AtomicInteger queueReferenceCount = new AtomicInteger(0);


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
                if (queueReferenceCount.decrementAndGet() <= 0) {
                    if (sharedQueueDispatcher != null) {
                        try {
                            sharedQueueDispatcher.close();
                        } catch (Exception ex) {
                            LOG.error("Failed to close shared dispatcher for queue: {}", entity.queueName, ex);
                        } finally {
                            sharedQueueDispatcher = null;
                        }
                    }
                    // remove from telemetry registry for this queue name
                    telemetryRegistry.removeDispatcher(entity.queueName);
                    // reset counter to 0 just to be safe
                    queueReferenceCount.set(0);
                }
            }
        }
    }

    public void subscribe() {
        m_twinSubscription = m_twinSubscriber.subscribe(ConnectorTwinConfig.CONNECTOR_KEY, ConnectorTwinConfig.class, this::onConfig);
    }

    private void onConfig(ConnectorTwinConfig request) {
        try (Logging.MDCCloseable mdc = Logging.withPrefixCloseable("telemetryd")) {
            LOG.info("Got connectors config update - reloading");
            synchronized (configuredLock) {

                Set<String> newConfigKeys = request.getConfigurations().stream()
                        .map(ConnectorTwinConfig.ConnectorConfig::getNodeConnectorKey)
                        .collect(Collectors.toSet());
                // Remove connectors that are no longer present in the new configuration
                Set<String> currentKeys = new HashSet<>(entities.keySet());
                for (String existingKey : currentKeys) {
                    if (!newConfigKeys.contains(existingKey)) {
                        delete(existingKey);
                    }
                }

                for (ConnectorTwinConfig.ConnectorConfig config : request.getConfigurations()) {
                    LOG.debug("Processing connector config: {}", config.getNodeConnectorKey());
                    Entity existingEntity = entities.get(config.getNodeConnectorKey());

                    if (existingEntity != null && hasConfigChanged(existingEntity.config, config)) {
                        // if config changed -> restart connector
                            LOG.info("Configuration changed for key: {}, updating", config.getNodeConnectorKey());
                            delete(config.getNodeConnectorKey());
                            startConnector(config);
                    } else {
                        // new config found in list -> start connector
                        startConnector(config);
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
            AsyncDispatcher<TelemetryMessage> dispatcher = telemetryRegistry.getDispatcher(baseDef.getQueueName());
            if (dispatcher == null) {
                // No dispatcher in registry, check if we have a local instance
                if (sharedQueueDispatcher == null) {
                    // Create new dispatcher and register it
                    TelemetrySinkModule sinkModule = new TelemetrySinkModule(baseDef);
                    sinkModule.setDistPollerDao(distPollerDao);
                    sharedQueueDispatcher = messageDispatcherFactory.createAsyncDispatcher(sinkModule);
                    telemetryRegistry.registerDispatcher(queueName, sharedQueueDispatcher);
                }
                dispatcher = sharedQueueDispatcher;
            } else {
                // Dispatcher exists in registry
                sharedQueueDispatcher = dispatcher;
            }
            queueReferenceCount.incrementAndGet();
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

    public Map<String, Entity> getEntities() {
        return entities;
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

    public void setTelemetryRegistry(TelemetryRegistry telemetryRegistry) {
        this.telemetryRegistry = telemetryRegistry;
    }

    public TwinSubscriber getM_twinSubscriber() {
        return m_twinSubscriber;
    }

    @VisibleForTesting
    public void setM_twinSubscriber(TwinSubscriber m_twinSubscriber) {
        this.m_twinSubscriber = m_twinSubscriber;
        subscribe();
    }

}