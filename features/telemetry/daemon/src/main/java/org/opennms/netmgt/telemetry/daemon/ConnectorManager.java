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

import java.io.IOException;
import java.net.InetAddress;
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
import org.opennms.netmgt.dao.api.FilterService;
import org.opennms.netmgt.telemetry.api.receiver.Connector;
import org.opennms.netmgt.telemetry.api.registry.TelemetryRegistry;
import org.opennms.netmgt.telemetry.config.model.ConnectorConfig;
import org.opennms.netmgt.telemetry.config.model.PackageConfig;
import org.opennms.netmgt.telemetry.config.model.Parameter;
import org.opennms.netmgt.telemetry.config.model.TelemetrydConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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
    private FilterService filterService;

    private final Map<ConnectorKey, Connector> connectorsByKey = new LinkedHashMap<>();

    private final List<FilterService.Session> filterWatchSessions = new LinkedList<>();

    private void startStreamingFor(ConnectorConfig connectorConfig, PackageConfig packageConfig, FilterService.NodeInterface iff) {
        synchronized (connectorsByKey) {
            final ConnectorKey key = toKey(connectorConfig, packageConfig, iff);
            if (connectorsByKey.containsKey(key)) {
                LOG.debug("Connector already exists. Ignoring.");
            }

            // Flatten the parameters to a map
            Map<String,String> parmMap = packageConfig.getParameters().stream()
                    .collect(Collectors.toMap(
                            Parameter::getKey,
                            Parameter::getValue
                    ));
            // Interpolate meta-data in parameter values
            parmMap = Interpolator.interpolateStrings(parmMap, new FallbackScope(
                    entityScopeProvider.getScopeForNode(iff.getNodeId()),
                    entityScopeProvider.getScopeForInterface(iff.getNodeId(), InetAddressUtils.toIpAddrString(iff.getInterfaceAddress())),
                    entityScopeProvider.getScopeForService(iff.getNodeId(), iff.getInterfaceAddress(), connectorConfig.getServiceName())
            ));

            // Create a new connector
            LOG.debug("Starting connector for: {}", key);
            final Connector connector = telemetryRegistry.getConnector(connectorConfig);
            connector.stream(iff.getNodeId(), iff.getInterfaceAddress(), parmMap);
        }
    }

    private void stopStreamingFor(ConnectorConfig connectorConfig, PackageConfig packageConfig, FilterService.NodeInterface iff) {
        synchronized (connectorsByKey) {
            final ConnectorKey key = toKey(connectorConfig, packageConfig, iff);
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
                    FilterService.Session session = filterService.watchServicesMatchingFilter(
                            connectorConfig.getServiceName(), packageConfig.getFilterRule(),
                            new FilterService.NodeInterfaceUpdateListener() {
                        @Override
                        public void onInterfaceMatchedFilter(FilterService.NodeInterface iff) {
                            startStreamingFor(connectorConfig, packageConfig, iff);
                        }

                        @Override
                        public void onInterfaceStoppedMatchingFilter(FilterService.NodeInterface iff) {
                            stopStreamingFor(connectorConfig, packageConfig, iff);
                        }
                    });
                    filterWatchSessions.add(session);
                }
            }
        }
    }

    public void stop() {
        // Close the filter watches
        filterWatchSessions.forEach(s -> {
            try {
                s.close();
            } catch (Exception e) {
                LOG.warn("Failed to close filter watch session. Resources may not be properly recovered.", e);
            }
        });
        filterWatchSessions.clear();

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

    private static ConnectorKey toKey(ConnectorConfig connectorConfig, PackageConfig packageConfig, FilterService.NodeInterface iff) {
        return new ConnectorKey(connectorConfig.getName(), packageConfig.getName(), iff.getNodeId(), iff.getInterfaceAddress());
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

}
