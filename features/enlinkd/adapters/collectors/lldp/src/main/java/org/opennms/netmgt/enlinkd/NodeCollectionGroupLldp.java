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
package org.opennms.netmgt.enlinkd;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.enlinkd.common.NodeCollector;
import org.opennms.netmgt.enlinkd.common.SchedulableNodeCollectorGroup;
import org.opennms.netmgt.enlinkd.service.api.LldpTopologyService;
import org.opennms.netmgt.enlinkd.service.api.Node;
import org.opennms.netmgt.enlinkd.service.api.NodeTopologyService;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.opennms.netmgt.scheduler.Executable;
import org.opennms.netmgt.scheduler.LegacyPriorityExecutor;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;

public class NodeCollectionGroupLldp extends SchedulableNodeCollectorGroup {

    private final LldpTopologyService m_lldpTopologyService;


    public NodeCollectionGroupLldp(long interval, long initial, LegacyPriorityExecutor executor, int priority, NodeTopologyService nodeTopologyService, LocationAwareSnmpClient locationAwareSnmpClient, LldpTopologyService lldpTopologyService) {
        super(interval, initial, executor, priority, ProtocolSupported.LLDP, nodeTopologyService, locationAwareSnmpClient);
        m_lldpTopologyService = lldpTopologyService;
    }


    public LldpTopologyService getLldpTopologyService() {
        return m_lldpTopologyService;
    }

    @Override
    public NodeCollector getNodeCollector(final Node node, final int priority) {
        return new NodeDiscoveryLldp(this, node, priority);
    }
}
