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
