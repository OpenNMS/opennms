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

package org.opennms.netmgt.telemetry.distributed.minion;

import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.core.ipc.sink.api.MessageDispatcherFactory;
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

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public class ConnectorManager implements ManagedService, Connector {
    private static final Logger LOG = LoggerFactory.getLogger(ConnectorManager.class);

    Map<String, String> configMap;

    private MessageDispatcherFactory messageDispatcherFactory;

    private DistPollerDao distPollerDao;

    private TelemetryRegistry telemetryRegistry;

    private BundleContext bundleContext;

    final Entity entity = new Entity();

    public ConnectorManager() {
    }

    public void start() {
        LOG.error(" connector listener is started");
    }

    public void stop() {
        LOG.info("ConnectorListener stopping…");

        if (entity.connector != null) {
            try {
                entity.connector.close();
            } catch (IOException e) {
                LOG.warn("Error stopping connector", e);
            }
        }

        stopQueues(entity.queueNames);

    }

    @Override
    public void updated(Dictionary<String, ?> props) throws ConfigurationException {
        String enabledProp = props != null
                ? (String) props.get("enabled")
                : "false";
        boolean enabled = Boolean.parseBoolean(enabledProp);


        if (!enabled) {
            stop();
            LOG.info("Connector disabled – cleaned up and exiting updated()");
            return;
        }

        stopQueues(entity.queueNames);

        final PropertyTree definition = PropertyTree.from(configMap);
        final MapBasedConnectorDef connectorDef = new MapBasedConnectorDef(definition);

        if (telemetryRegistry.getDispatcher(connectorDef.getQueueName()) != null) {
            throw new IllegalArgumentException("A queue with name " + connectorDef.getQueueName() + " is already defined. Bailing.");
        }
        final TelemetrySinkModule sinkModule = new TelemetrySinkModule(connectorDef);
        sinkModule.setDistPollerDao(distPollerDao);
        final AsyncDispatcher<TelemetryMessage> dispatcher = messageDispatcherFactory.createAsyncDispatcher(sinkModule);
        final String queueName = Objects.requireNonNull(connectorDef.getQueueName());
        telemetryRegistry.registerDispatcher(queueName, dispatcher);
        entity.queueNames.add(connectorDef.getQueueName());
        entity.connector = telemetryRegistry.getConnector(connectorDef);

        String nodeIdStr  = get(props, "nodeId",  "1");
        String host       = get(props, "hostname", "127.0.0.1");
        String portStr    = get(props, "port",     "0");
        String mode       = get(props, "mode",     "jti");
        String paths      = get(props, "paths",    "/interfaces/interface/state/counters");

        int nodeId;
        try {
            nodeId = Integer.parseInt(nodeIdStr);
        } catch (NumberFormatException e) {
            throw new ConfigurationException("nodeId", "Invalid integer: " + nodeIdStr);
        }

        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            throw new ConfigurationException("port", "Invalid integer: " + portStr);
        }

        InetAddress ip;
        try {
            ip = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            throw new ConfigurationException("hostname", "Invalid host: " + host);
        }

        Map<String,String> entry = new HashMap<>();
        entry.put("hostname", host);
        entry.put("port",     Integer.toString(port));
        entry.put("mode",     mode);
        entry.put("paths",    paths);

        List<Map<String,String>> paramList = List.of(entry);
        entity.connector.stream(nodeId, ip, paramList);

    }

    String get(Dictionary<String,?> d, String key, String def) {
        Object v = (d != null ? d.get(key) : null);
        return (v != null ? v.toString() : def);
    }


    private void stopQueues(Set<String> queueNames) {
        Objects.requireNonNull(queueNames);
        for (String queueName : queueNames) {
            try {
                final AsyncDispatcher<TelemetryMessage> dispatcher = telemetryRegistry.getDispatcher(queueName);
                if(dispatcher != null) {
                    dispatcher.close();
                }
            } catch (Exception ex) {
                LOG.error("Failed to close dispatcher.", ex);
            } finally {
                telemetryRegistry.removeDispatcher(queueName);
            }
        }
    }

    public void connect(int nodeId, InetAddress ipAddress, List<Map<String, String>> pathList) {
        entity.connector.stream(nodeId, ipAddress, pathList);
    }

    @Override
    public void stream(int nodeId, InetAddress ipAddress, List<Map<String, String>> paramList) {
        entity.connector.stream(nodeId, ipAddress, paramList);
    }

    @Override
    public void close() throws IOException {

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
