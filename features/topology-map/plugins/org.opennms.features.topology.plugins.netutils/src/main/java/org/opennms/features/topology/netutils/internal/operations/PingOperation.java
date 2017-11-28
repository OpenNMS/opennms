/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.netutils.internal.operations;

import java.net.InetAddress;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.features.topology.api.AbstractOperation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.netutils.internal.ping.PingWindow;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.icmp.proxy.LocationAwarePingClient;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class PingOperation extends AbstractOperation {

    private LocationAwarePingClient pingClient;

    private MonitoringLocationDao monitoringLocationDao;

    private NodeDao nodeDao;

    public PingOperation(LocationAwarePingClient pingClient, MonitoringLocationDao monitoringLocationDao, NodeDao nodeDao) {
        this.pingClient = Objects.requireNonNull(pingClient);
        this.monitoringLocationDao = Objects.requireNonNull(monitoringLocationDao);
        this.nodeDao = Objects.requireNonNull(nodeDao);
    }

    @Override
    public void execute(final List<VertexRef> targets, final OperationContext operationContext) {
        final VertexRef target = targets.get(0);
        final Vertex vertex = getVertexItem(operationContext, target);
        final Optional<OnmsNode> node = getNodeIfAvailable(vertex);

        final List<String> locations = monitoringLocationDao.findAll().stream().map(OnmsMonitoringLocation::getLocationName).collect(Collectors.toList());
        final String defaultLocation = node.isPresent()
                ? node.get().getLocation().getLocationName()
                : MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID;

        final List<InetAddress> ipAddresses = node.isPresent()
                ? Lists.newArrayList(node.get().getIpInterfaces()).stream().map(OnmsIpInterface::getIpAddress).collect(Collectors.toList())
                : Lists.newArrayList(InetAddressUtils.addr(vertex.getIpAddress()));
        final InetAddress defaultIp = getDefaultIp(vertex, node);

        final String caption = String.format("Ping - %s (%s)", vertex.getLabel(), vertex.getIpAddress());
        new PingWindow(pingClient,
                locations, ipAddresses,
                defaultLocation, defaultIp,
                caption)
                .open();
    }

    private InetAddress getDefaultIp(Vertex vertex, Optional<OnmsNode> node) {
        if (hasValidIpAddress(vertex)) {
            return InetAddressUtils.addr(vertex.getIpAddress());
        }
        if (node.isPresent() && node.get().getPrimaryInterface() != null) {
            return node.get().getPrimaryInterface().getIpAddress();
        }
        if (node.isPresent()) {
            return node.get().getIpInterfaces().iterator().next().getIpAddress();
        }
        throw new IllegalStateException("The vertex does not have a ip address or a node assigned.");
    }

    @Override
    public boolean enabled(List<VertexRef> targets, OperationContext operationContext) {
        if (targets.size() == 1) {
            final Vertex vertexItem = getVertexItem(operationContext, targets.get(0));
            if (vertexItem != null) {
                return hasValidIpAddress(vertexItem) || hasValidNodeId(vertexItem);
            }
        }
        return false;
    }

    @Override
    public boolean display(final List<VertexRef> targets, final OperationContext operationContext) {
        return targets != null && targets.size() > 0;
    }

    @Override
    public String getId() {
        return "ping";
    }


    /**
     * Verifies that the provided vertex has a valid node assigned and the node has at least one ip address.
     *
     * @param vertex The vertex to check
     * @return True if a node with at least one ip address is assigned, false otherwise.
     */
    private boolean hasValidNodeId(Vertex vertex) {
        return vertex.getNodeID() != null && getNodeIfAvailable(vertex).isPresent();
    }

    private boolean hasValidIpAddress(Vertex vertexItem) {
        // Only enable if we actually have something to ping
        final String ipAddress = vertexItem.getIpAddress();
        if (!Strings.isNullOrEmpty(ipAddress)) {
            try {
                InetAddressUtils.getInetAddress(ipAddress);
                return true;
            } catch (IllegalArgumentException ex) {
                return false;
            }
        }
        return false;
    }

    /**
     * Returns an Optional containing a node id {@link Vertex#getNodeID()} exists in the OpenNMS Database and that the node
     * has at least one ip interface.
     *
     * @param   vertex The vertex to verify.
     * @return  A non empty optional if a node with node id {@link Vertex#getNodeID()} exists and
     *          that node has at least one ip interface defined.
     */
    private Optional<OnmsNode> getNodeIfAvailable(Vertex vertex) {
        Objects.requireNonNull(vertex);
        Objects.requireNonNull(vertex.getNodeID());

        final OnmsNode node = nodeDao.get(vertex.getNodeID());
        if (node != null && !node.getIpInterfaces().isEmpty()) {
            return Optional.of(node);
        }
        return Optional.empty();
    }
}
