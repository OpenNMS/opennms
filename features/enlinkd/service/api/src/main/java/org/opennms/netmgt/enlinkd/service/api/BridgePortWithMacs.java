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
package org.opennms.netmgt.enlinkd.service.api;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.opennms.netmgt.enlinkd.model.BridgeMacLink;
import org.opennms.netmgt.model.OnmsNode;
import org.springframework.util.Assert;

public class BridgePortWithMacs implements Topology {

    private final BridgePort m_port;
    private final Set<String> m_macs;

    public BridgePortWithMacs(BridgePort port, Set<String> macs) {
        Assert.notNull(port);
        Assert.notNull(macs);
        m_port=port;
        m_macs=macs;
    }

    public BridgePort getPort() {
        return m_port;
    }

    public Set<String> getMacs() {
        return m_macs;
    }

    public List<BridgeMacLink> getBridgeMacLinks() {
        final List<BridgeMacLink> links = new ArrayList<>();
        m_macs.forEach(mac -> {
            BridgeMacLink maclink = new BridgeMacLink();
            OnmsNode node = new OnmsNode();
            node.setId(m_port.getNodeId());
            maclink.setNode(node);
            maclink.setBridgePort(m_port.getBridgePort());
            maclink.setBridgePortIfIndex(m_port.getBridgePortIfIndex());
            maclink.setMacAddress(mac);
            maclink.setVlan(m_port.getVlan());
            maclink.setLinkType(BridgeMacLink.BridgeMacLinkType.BRIDGE_FORWARDER);
            links.add(maclink);
        });
        return links;
    }

    public Set<BridgeForwardingTableEntry> getBridgeForwardingTableEntrySet() {
        Set<BridgeForwardingTableEntry> bftentries = new HashSet<>();
        m_macs.forEach(mac -> {
            BridgeForwardingTableEntry bftentry = new BridgeForwardingTableEntry();
            bftentry.setNodeId(m_port.getNodeId());
            bftentry.setBridgePort(m_port.getBridgePort());
            bftentry.setBridgePortIfIndex(m_port.getBridgePortIfIndex());
            bftentry.setVlan(m_port.getVlan());
            bftentry.setMacAddress(mac);
            bftentry.setBridgeDot1qTpFdbStatus(BridgeForwardingTableEntry.BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED);
            bftentries.add(bftentry);
        });
        return bftentries;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BridgePortWithMacs that = (BridgePortWithMacs) o;
        return Objects.equals(m_port, that.m_port);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_port);
    }

    @Override
    public String printTopology() {
        return m_port.printTopology() +
                " macs:" +
                m_macs;
    }


    
}
