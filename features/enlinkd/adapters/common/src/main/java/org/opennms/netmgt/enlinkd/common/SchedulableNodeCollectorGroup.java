/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.enlinkd.common;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.enlinkd.service.api.Node;
import org.opennms.netmgt.enlinkd.service.api.NodeTopologyService;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.opennms.netmgt.scheduler.LegacyPriorityExecutor;
import org.opennms.netmgt.scheduler.SchedulableExecutableGroup;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;

public abstract class SchedulableNodeCollectorGroup extends SchedulableExecutableGroup {

    private final ProtocolSupported m_protocolSupported;
    private final NodeTopologyService m_nodeTopologyService;
    private final LocationAwareSnmpClient m_locationAwareSnmpClient;
    private final int m_priority;
    private final Set<Integer> m_suspended = new HashSet<>();

    public SchedulableNodeCollectorGroup(
            long interval,
            long initial,
            LegacyPriorityExecutor executor,
            int priority,
            ProtocolSupported protocolSupported,
            NodeTopologyService nodeTopologyService,
            LocationAwareSnmpClient locationAwareSnmpClient) {
        super(interval, initial, executor, protocolSupported.name());
        m_protocolSupported = protocolSupported;
        m_nodeTopologyService = nodeTopologyService;
        m_locationAwareSnmpClient = locationAwareSnmpClient;
        m_priority=priority;
    }

    public void suspend(final Integer nodeid) {
        m_suspended.add(nodeid);
    }

    public void wakeUp(final Integer nodeid) {
        m_suspended.remove(nodeid);
    }

    public Map<Integer,Integer> getPriorityMap() {
        return m_nodeTopologyService.getNodeidPriorityMap(m_protocolSupported);
    }

    public ProtocolSupported getProtocolSupported() {
        return m_protocolSupported;
    }

    public NodeTopologyService getNodeTopologyService() {
        return m_nodeTopologyService;
    }

    public abstract NodeCollector getNodeCollector(final Node node, final int priority);

    private int getMaxPriority(Collection<Integer> priorities) {
        if (priorities.size() == 0) {
            return 0;
        }
        return Collections.max(priorities);
    }

    @Override
    public void runSchedulable() {
        final Map<Integer, Integer> priorityMap = getPriorityMap();
        final Integer maxPriority = getMaxPriority(priorityMap.values());
        m_nodeTopologyService.findAllSnmpNode()
                .stream()
                .filter(node -> !m_suspended.contains(node.getNodeId()))
                .forEach(node -> this.add(getNodeCollector(node, (priorityMap.getOrDefault(node.getNodeId(), maxPriority))+ m_priority)));
        super.runSchedulable();
    }

    public LocationAwareSnmpClient getLocationAwareSnmpClient() {
        return m_locationAwareSnmpClient;
    }

}
