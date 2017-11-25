/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model.topology;

import java.util.HashSet;
import java.util.Set;


public class BridgeForwardingTable implements BridgeTopology {

    public static BridgeForwardingTable create(Bridge bridge, Set<BridgeForwardingTableEntry> entries) throws BridgeTopologyException {
        if (bridge == null) {
            throw new BridgeTopologyException(" bridge must not be null");
        }
        if (entries == null) {
            throw new BridgeTopologyException(" must not be null");
        }
        BridgeForwardingTable bft = new BridgeForwardingTable(bridge);
        bft.setBFTEntries(entries);
        return bft;
    }
    
    private final Bridge m_bridge;
    private Set<BridgeForwardingTableEntry> m_entries = new HashSet<BridgeForwardingTableEntry>();

    private BridgeForwardingTable(Bridge bridge) {
        m_bridge = bridge;
    }

    public Integer getNodeId() {
        return m_bridge.getNodeId();
    }

    public Set<String> getIdentifiers() {
        return m_bridge.getIdentifiers();
    }

    public Bridge getBridge() {
        return m_bridge;
    }
    
    public Set<BridgeForwardingTableEntry> getBFTEntries() {
        return m_entries;
    }

    public void setBFTEntries(Set<BridgeForwardingTableEntry> entries) {
        m_entries = entries;
    }

    public void setRootPort(Integer rootPort) {
        m_bridge.setRootPort(rootPort);
    }

    public String printTopology() {
    	StringBuffer strbfr = new StringBuffer();
        strbfr.append(m_bridge.printTopology());
        strbfr.append("\n");
        boolean rn = false;
        for (BridgeForwardingTableEntry bftentry: m_entries) {
            if (rn) {
                strbfr.append("\n");
            } else {
                rn = true;
            }
            strbfr.append(bftentry.printTopology());
        }
        return strbfr.toString();

    }

}
