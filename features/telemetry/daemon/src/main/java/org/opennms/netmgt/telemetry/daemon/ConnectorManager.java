/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

import org.opennms.core.rpc.utils.mate.EntityScopeProvider;
import org.opennms.core.rpc.utils.mate.FallbackScope;
import org.opennms.core.rpc.utils.mate.Interpolator;
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

    private final Map<ConnectorKey, Connector> connectorsByKey = new LinkedHashMap<>();

    private final List<Closeable> serviceTrackerSessions = new LinkedList<>();

    private void startStreamingFor(ConnectorConfig connectorConfig, PackageConfig packageConfig, ServiceRef serviceRef) {
        synchronized (connectorsByKey) {
            final ConnectorKey key = toKey(connectorConfig, packageConfig, serviceRef);
            if (connectorsByKey.containsKey(key)) {
                LOG.debug("Connector already exists. Ignoring.");
            }
            List<Map<String, String>> interpolatedMapList = getGroupedParams(packageConfig, serviceRef);
            // Create a new connector
            LOG.debug("Starting connector for: {}", key);
            final Connector connector = telemetryRegistry.getConnector(connectorConfig);
            connectorsByKey.put(key, connector);
            connector.stream(serviceRef.getNodeId(), serviceRef.getIpAddress(), interpolatedMapList);
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
            final Connector connector = connectorsByKey.remove(key);
            if (connector != null) {
                try {
                    LOG.debug("Closing connector for: {}", key);
                    connector.close();
                } catch (IOException e) {
                    LOG.warn("Error closing connector: {}", key, e);
                }
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

        // Close the connectors
        synchronized (connectorsByKey) {
            connectorsByKey.forEach((key,connector) -> {
                try {
                    connector.close();
                } catch (IOException e) {
                    LOG.warn("Error closing connector: {}. Resources may not be properly recovered.", key, e);
                }
            });
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
    }

    @VisibleForTesting
    public void setEntityScopeProvider(EntityScopeProvider entityScopeProvider) {
        this.entityScopeProvider = entityScopeProvider;
    }
}
