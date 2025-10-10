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
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.telemetry.api.receiver.Connector;
import org.opennms.netmgt.telemetry.api.receiver.TelemetryMessage;
import org.opennms.netmgt.telemetry.api.registry.TelemetryRegistry;
import org.opennms.netmgt.telemetry.common.ipc.TelemetrySinkModule;
import org.opennms.netmgt.telemetry.distributed.common.MapBasedConnectorDef;
import org.opennms.netmgt.telemetry.distributed.common.PropertyTree;
import org.opennms.netmgt.telemetry.config.model.ConnectorTwinConfig;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Dictionary;
import java.util.Objects;
import java.util.Collections;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ConnectorStarter implements ManagedService {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectorStarter.class);

    private Map<String, String> configMap = new HashMap<>();

    private MessageDispatcherFactory messageDispatcherFactory;

    private DistPollerDao distPollerDao;

    private TelemetryRegistry telemetryRegistry;

    @Autowired
    private TwinSubscriber twinSubscriber;

    private Closeable twinSubscription;

    private final Object configuredLock = new Object();

    private MapBasedConnectorDef baseDef;

    private final Map<String, Entity> entities = new LinkedHashMap<>();

    // This is used just to  track dispatchers by queue name to avoid duplicate registration
    private AsyncDispatcher<TelemetryMessage> sharedQueueDispatcher = null;

    private String currentQueueName;


    public void start() {
        final PropertyTree definition = PropertyTree.from(configMap);
        baseDef = new MapBasedConnectorDef(definition);
        currentQueueName = baseDef.getQueueName();
        // incase of calling "initializeDispatcherForQueue" here this star() is executed before the telemetryd which causing issue
    }

    public void stop() {
        LOG.info("ConnectorListener stopping…");
        try {
            twinSubscription.close();
        } catch (IOException e) {
            LOG.error("Failed to  stop twin subscription: error '{}' ", e.getMessage());
        }

        new ArrayList<>(this.entities.keySet()).forEach(this::delete);

        cleanupDispatcher();
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
        }
    }

    @Override
    public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
        if (properties == null) {
            LOG.info("No configuration received, using defaults");
            return;
        }

        Map<String, String> newConfigMap = new HashMap<>();
        for (String key : Collections.list(properties.keys())) {
            newConfigMap.put(key, properties.get(key).toString());
        }

        boolean needsRestart = hasQueueNameChanged(newConfigMap);

        baseDef = new MapBasedConnectorDef(PropertyTree.from(configMap));

        if (needsRestart) {
            LOG.info("Critical configuration changed, restarting all connectors with new dispatcher");
            restartAllConnectorsWithNewDispatcher();
        }

    }

    public void subscribe() {
        twinSubscription = twinSubscriber.subscribe(ConnectorTwinConfig.CONNECTOR_KEY, ConnectorTwinConfig.class, this::handleTwinUpdate);
    }

    private void handleTwinUpdate(ConnectorTwinConfig request) {
        try (Logging.MDCCloseable mdc = Logging.withPrefixCloseable("telemetryd")) {
            LOG.info("Got connectors config update - reloading");
            synchronized (configuredLock) {

                Set<String> newConfigKeys = request.getConfigurations().stream().map(ConnectorTwinConfig.ConnectorConfig::getConnectionKey).collect(Collectors.toSet());
                // Remove connectors that are no longer present in the new configuration
                Set<String> currentKeys = new HashSet<>(entities.keySet());
                for (String existingKey : currentKeys) {
                    if (!newConfigKeys.contains(existingKey)) {
                        delete(existingKey);
                    }
                }

                initializeOrGetDispatcherForQueue(currentQueueName);

                for (ConnectorTwinConfig.ConnectorConfig config : request.getConfigurations()) {
                    LOG.debug("Processing connector config: {}", config.getConnectionKey());
                    processConfig(config);
                }
            }
        }
    }

    private void processConfig(ConnectorTwinConfig.ConnectorConfig config) {
        String key = config.getConnectionKey();
        Entity existing = entities.get(key);
        // start new connector
        if (existing == null) {
            startConnector(config);
            return;
        }

        if (!hasConfigChanged(existing.config, config)) {
            LOG.debug("No change for key: {}, skipping start", key);
            return;
        }

        LOG.info("Configuration changed for key: {}, updating", key);
        delete(key);
        startConnector(config);
    }


    private void startConnector(ConnectorTwinConfig.ConnectorConfig config) {
        try {

            final Entity entity = new Entity();
            entity.config = config;
            entity.queueName = currentQueueName;
            entity.connector = telemetryRegistry.getConnector(baseDef);

            InetAddress ip = InetAddressUtils.addr(config.getIpAddress());
            entity.connector.stream(config.getNodeId(), ip, config.getParameters());

            entities.put(config.getConnectionKey(), entity);
            LOG.info("Started connector for key: {}", config.getConnectionKey());
        } catch (Exception e) {
            LOG.error("Failed to start connector for key: {}", config.getConnectionKey(), e);
        }
    }

    private synchronized void initializeOrGetDispatcherForQueue(String queueName) {

        sharedQueueDispatcher = telemetryRegistry.getDispatcher(queueName);

        if (sharedQueueDispatcher == null) {
            TelemetrySinkModule sinkModule = new TelemetrySinkModule(baseDef);
            sinkModule.setDistPollerDao(distPollerDao);
            sharedQueueDispatcher = messageDispatcherFactory.createAsyncDispatcher(sinkModule);
            telemetryRegistry.registerDispatcher(queueName, sharedQueueDispatcher);
        }

    }

    private boolean hasConfigChanged(ConnectorTwinConfig.ConnectorConfig oldConfig, ConnectorTwinConfig.ConnectorConfig newConfig) {
        return !Objects.equals(oldConfig.getNodeId(), newConfig.getNodeId()) || !Objects.equals(oldConfig.getIpAddress(), newConfig.getIpAddress()) || !Objects.equals(oldConfig.getParameters(), newConfig.getParameters());
    }

    private boolean hasQueueNameChanged(Map<String, String> newConfig) {

        String newQueueName = newConfig.get("queue");
        if (!Objects.equals(currentQueueName, newQueueName)) {
            LOG.info("Queue name changed from {} to {}", currentQueueName, newQueueName);
            configMap.put("queue",newQueueName);
            configMap.put("name",newQueueName);
            return true;
        }
        return false;
    }

    private void restartAllConnectorsWithNewDispatcher() {
        // Store current entities configuration
        Map<String, ConnectorTwinConfig.ConnectorConfig> savedConfigs = new HashMap<>();
        for (Map.Entry<String, Entity> entry : entities.entrySet()) {
            savedConfigs.put(entry.getKey(), entry.getValue().config);
        }

        new ArrayList<>(this.entities.keySet()).forEach(this::delete);

        cleanupDispatcher();

        currentQueueName = baseDef.getQueueName();

        initializeOrGetDispatcherForQueue(currentQueueName);

        for (Map.Entry<String, ConnectorTwinConfig.ConnectorConfig> entry : savedConfigs.entrySet()) {
            startConnector(entry.getValue());
        }

        LOG.info("All connectors restarted with new dispatcher for queue: {}", currentQueueName);
    }

    private void cleanupDispatcher() {
        if (sharedQueueDispatcher != null) {
            try {
                sharedQueueDispatcher.close();
            } catch (Exception ex) {
                LOG.error("Failed to close dispatcher.", ex);
            } finally {
                if (currentQueueName != null) {
                    telemetryRegistry.removeDispatcher(currentQueueName);
                }
            }
        }
    }

    public void bind(TwinSubscriber twinSubscriber) {
        this.twinSubscriber = twinSubscriber;
        subscribe();
    }


    public void unbind(TwinSubscriber twinSubscriber) {
        twinSubscriber = null;
    }

    public Map<String, Entity> getEntities() {
        return entities;
    }

    public static class Entity {
        private ConnectorTwinConfig.ConnectorConfig config;
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

    public TwinSubscriber getTwinSubscriber() {
        return twinSubscriber;
    }

    @VisibleForTesting
    public void setTwinSubscriber(TwinSubscriber twinSubscriber) {
        this.twinSubscriber = twinSubscriber;
        subscribe();
    }

    @VisibleForTesting
    public TelemetryRegistry getTelemetryRegistry() {
        return telemetryRegistry;
    }
}