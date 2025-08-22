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
package org.opennms.netmgt.telemetry.daemon;

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
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public class ConnectorStarter implements ManagedService, Connector {
    private static final Logger LOG = LoggerFactory.getLogger(ConnectorManager.class);

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

        new ArrayList<>(this.entities.keySet()).forEach(pid -> delete(pid));
    }

    public void  delete(String key){
        final Entity entity = this.entities.remove(key);
        if (entity.connector != null) {
            LOG.info("Stopping listener for key: {}", key);
            try {
                entity.connector.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (entity.queueNames != null) {
            stopQueues(entity.queueNames);
        }
    }
    Entity entity = new Entity();
    @Override
    public void updated(Dictionary<String, ?> props) throws ConfigurationException {
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
            LOG.error("Got listener config update - reloading");
            synchronized (configuredLock) {
                if (request.isEnabled()) {
                    if(entities.containsKey(request.getConnectionKey())) {
                        LOG.warn("A connector is already registered with node '{}' ",request.getNodeId());
                        return;
                    }
                    final Entity entity = new Entity();
                    final TelemetrySinkModule sinkModule = new TelemetrySinkModule(baseDef);
                    sinkModule.setDistPollerDao(distPollerDao);
                    final AsyncDispatcher<TelemetryMessage> dispatcher = messageDispatcherFactory.createAsyncDispatcher(sinkModule);
                    final String queueName = Objects.requireNonNull(baseDef.getQueueName());
                    if (telemetryRegistry.getDispatcher(queueName) == null) {
                        telemetryRegistry.registerDispatcher(queueName, dispatcher);
                    }
                    entity.queueNames.add(baseDef.getQueueName());
                    entity.connector = telemetryRegistry.getConnector(baseDef);
                    InetAddress ip = null;
                    try {
                        ip = InetAddress.getByName(request.getIpAddress());
                    } catch (UnknownHostException ignored) {
                        LOG.warn("Invalid ip address (hostname)  '{}' ",request.getIpAddress());
                    }
                    entity.connector.stream(request.getNodeId(), ip, request.getParameters());
                    entities.put(request.getConnectionKey(), entity);
                } else {
                    delete(request.getConnectionKey());
                }
            }
        }
    }

    @Override
    public void stream(int nodeId, InetAddress ipAddress, List<Map<String, String>> paramList) {
       // entity.connector.stream(nodeId, ipAddress, paramList);
    }

    @Override
    public void close() throws IOException { }

    public void bind(TwinSubscriber twinSubscriber) {
        m_twinSubscriber = twinSubscriber;
        subscribe();
    }

    public void unbind(TwinSubscriber twinSubscriber) {
        m_twinSubscriber = null;
    }

    private static class Entity {
        private Connector connector;
        private final Set<String> queueNames = new HashSet<>();
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

