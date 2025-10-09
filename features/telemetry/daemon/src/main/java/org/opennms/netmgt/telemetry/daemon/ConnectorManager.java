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

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.core.mate.api.EntityScopeProvider;
import org.opennms.core.mate.api.FallbackScope;
import org.opennms.core.mate.api.Interpolator;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.ServiceRef;
import org.opennms.netmgt.dao.api.ServiceTracker;
import org.opennms.netmgt.telemetry.api.receiver.Connector;
import org.opennms.netmgt.telemetry.api.registry.TelemetryRegistry;
import org.opennms.netmgt.telemetry.config.model.ConnectorConfig;
import org.opennms.netmgt.telemetry.config.model.PackageConfig;
import org.opennms.netmgt.telemetry.config.model.Parameter;
import org.opennms.netmgt.telemetry.config.model.TelemetrydConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.annotations.VisibleForTesting;

/**
 * The ConnectorManager is responsible for starting/stopping connectors that connect to the target agents.
 *
 * Connectors are initially created with services that are currently populated in the database and new connectors
 * are started or stopped when services are added or removed.
 *
 * The {@link Connector} should make a best effort to maintain the connection with the target agent until it is stopped,
 * meaning it should retry connecting on failure, etc... No attempt will be made by this manager to restart the connector.
 *
 * @author jwhite
 */
public class ConnectorManager {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectorManager.class);

    @Autowired
    private TelemetryRegistry telemetryRegistry;

    @Autowired
    private EntityScopeProvider entityScopeProvider;

    @Autowired
    private ServiceTracker serviceTracker;

    @Autowired
    private OpenConfigTwinPublisher openConfigTwinPublisher;
    private final Map<ConnectorKey, Connector> connectorsByKey = new LinkedHashMap<>();

    private final List<Closeable> serviceTrackerSessions = new LinkedList<>();

    private void startStreamingFor(ConnectorConfig connectorConfig, PackageConfig packageConfig, ServiceRef serviceRef) {
        synchronized (connectorsByKey) {
            final ConnectorKey key = toKey(connectorConfig, packageConfig, serviceRef);
            if (connectorsByKey.containsKey(key)) {
                LOG.debug("Connector already exists. Ignoring.");
            }
            List<Map<String, String>> interpolatedMapList = getGroupedParams(packageConfig, serviceRef);

            try {
                openConfigTwinPublisher.publishConfig(serviceRef, interpolatedMapList, key.stringKey(), connectorConfig.getQueueName());
            } catch (IOException e) {
                LOG.error("Failed to publish config for connector: {}", key, e);
            }
        }
    }

    List<Map<String, String>> getGroupedParams(PackageConfig packageConfig, ServiceRef serviceRef) {

        List<Map<String, String>> interpolatedMapList = new ArrayList<>();
        // Convert parameters from the package into different groups grouped by parameter group.
        Map<String, Map<String, String>> parmMapByGroup = packageConfig.getParameters().stream()
                .peek(parameter -> {
                    if (parameter.getGroup() == null) {
                        parameter.setGroup("");
                    }
                })
                .collect(Collectors.groupingBy(Parameter::getGroup, Collectors.toMap(Parameter::getKey, Parameter::getValue)));

        // Interpolate meta-data and add grouped params to list.
        parmMapByGroup.forEach((group, parmeterMap) -> {
            interpolatedMapList.add(getInterpolated(parmeterMap, serviceRef));
        });
        return interpolatedMapList;
    }

    private Map<String, String> getInterpolated(Map<String, String> parameterMap, ServiceRef serviceRef) {
        return Interpolator.interpolateStrings(parameterMap, new FallbackScope(
                entityScopeProvider.getScopeForNode(serviceRef.getNodeId()),
                entityScopeProvider.getScopeForInterface(serviceRef.getNodeId(), InetAddressUtils.str(serviceRef.getIpAddress())),
                entityScopeProvider.getScopeForService(serviceRef.getNodeId(), serviceRef.getIpAddress(), serviceRef.getServiceName())
        ));
    }

    private void stopStreamingFor(ConnectorConfig connectorConfig, PackageConfig packageConfig, ServiceRef serviceRef) {
        synchronized (connectorsByKey) {
            final ConnectorKey key = toKey(connectorConfig, packageConfig, serviceRef);
            try {
                openConfigTwinPublisher.removeConfig(serviceRef, key.stringKey());
            } catch (IOException e) {
                LOG.warn("Error closing connector: {}.", key, e);
            }
        }
    }

    public void start(TelemetrydConfig config) {
        for (ConnectorConfig connectorConfig : config.getConnectors()) {
            if (connectorConfig.getPackages().isEmpty()) {
                // No packages defined
                LOG.warn("No packages defined for connector named: {}. No connections will be attempted.", connectorConfig.getName());
            } else {
                LOG.info("Watching for services named '{}' for connector: {}", connectorConfig.getServiceName(), connectorConfig.getName());
                // One or more packages defined
                for (PackageConfig packageConfig : connectorConfig.getPackages()) {
                    // Watch the services matching the filter rule
                    Closeable session = serviceTracker.trackServiceMatchingFilterRule(
                            connectorConfig.getServiceName(), packageConfig.getFilterRule(),
                            new ServiceTracker.ServiceListener() {
                                @Override
                                public void onServiceMatched(ServiceRef serviceRef) {
                                    startStreamingFor(connectorConfig, packageConfig, serviceRef);
                                }

                                @Override
                                public void onServiceStoppedMatching(ServiceRef serviceRef) {
                                    stopStreamingFor(connectorConfig, packageConfig, serviceRef);
                                }
                            });
                    serviceTrackerSessions.add(session);
                }
            }
        }
    }

    public void stop() {
        // Close the filter watches
        serviceTrackerSessions.forEach(s -> {
            try {
                s.close();
            } catch (Exception e) {
                LOG.warn("Failed to close filter watch session. Resources may not be properly recovered.", e);
            }
        });
        serviceTrackerSessions.clear();

        try {
            openConfigTwinPublisher.close();
        } catch (IOException e) {
            LOG.error("Stopping Twin Location publishers and configs :", e);
        }
    }

    private static ConnectorKey toKey(ConnectorConfig connectorConfig, PackageConfig packageConfig, ServiceRef serviceRef) {
        return new ConnectorKey(connectorConfig.getName(), packageConfig.getName(), serviceRef.getNodeId(), serviceRef.getIpAddress());
    }

    private static class ConnectorKey {
        private final String connectorName;
        private final String packageName;
        private final int nodeId;
        private final InetAddress interfaceAddress;

        public ConnectorKey(String connectorName, String packageName, int nodeId, InetAddress interfaceAddress) {
            this.connectorName = connectorName;
            this.packageName = packageName;
            this.nodeId = nodeId;
            this.interfaceAddress = interfaceAddress;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ConnectorKey)) return false;
            ConnectorKey that = (ConnectorKey) o;
            return nodeId == that.nodeId &&
                    Objects.equals(connectorName, that.connectorName) &&
                    Objects.equals(packageName, that.packageName) &&
                    Objects.equals(interfaceAddress, that.interfaceAddress);
        }

        @Override
        public int hashCode() {
            return Objects.hash(connectorName, packageName, nodeId, interfaceAddress);
        }

        @Override
        public String toString() {
            return "ConnectorKey{" +
                    "connectorName='" + connectorName + '\'' +
                    ", packageName='" + packageName + '\'' +
                    ", nodeId=" + nodeId +
                    ", interfaceAddress=" + interfaceAddress +
                    '}';
        }

        public String stringKey() {
            String sb;
            sb = connectorName +
                    packageName +
                    interfaceAddress.getHostAddress() + "_"+
                    nodeId;
            return sb;
        }
    }

    @VisibleForTesting
    public void setEntityScopeProvider(EntityScopeProvider entityScopeProvider) {
        this.entityScopeProvider = entityScopeProvider;
    }
}
