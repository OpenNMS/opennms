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

package org.opennms.netmgt.enlinkd.service.api;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.enlinkd.model.BridgeElement;

public class Bridge implements Topology {

    public static Set<String> getIdentifier(List<BridgeElement> elems) {
        Set<String> identifiers = new HashSet<String>();
        for (BridgeElement element: elems) {
            if (InetAddressUtils.isValidBridgeAddress(element.getBaseBridgeAddress())) {
                identifiers.add(element.getBaseBridgeAddress());
            }

        }
        return identifiers;
    }

    public static String getDesignated(List<BridgeElement> elems) {
        for (BridgeElement element: elems) {
            if (InetAddressUtils.
                    isValidStpBridgeId(element.getStpDesignatedRoot()) 
                    && !element.getBaseBridgeAddress().
                    equals(InetAddressUtils.getBridgeAddressFromStpBridgeId(element.getStpDesignatedRoot()))) {
                String designated=InetAddressUtils.
                               getBridgeAddressFromStpBridgeId(element.getStpDesignatedRoot());
                if (InetAddressUtils.isValidBridgeAddress(designated)) {
                    return designated;
                }
            }
        }
        return null;
    }

    public static Bridge create(BroadcastDomain domain, Integer nodeid) {
        Bridge bridge = new Bridge(nodeid);
        domain.getBridges().add(bridge);
        return bridge;
    }

    public static Bridge createRootBridge(BroadcastDomain domain, Integer nodeid) {
        Bridge bridge = new Bridge(nodeid);
        bridge.setRootBridge();
        domain.getBridges().add(bridge);
        return bridge;
    }
    
    public static Bridge create(BroadcastDomain domain, Integer nodeid, Integer rootport) {
        Bridge bridge = new Bridge(nodeid);
        bridge.setRootPort(rootport);
        domain.getBridges().add(bridge);
        return bridge;
    }

    private final Integer m_nodeId;
    private Integer m_rootPort;
    private boolean m_isRootBridge;
    private Set<String> m_identifiers = new HashSet<String>();
    private String m_designated;

    private Bridge(Integer id) {
        super();
        m_nodeId = id;
    }

    public Integer getRootPort() {
        return m_rootPort;
    }

    public boolean isNewTopology() {
        if (m_rootPort != null) {
            return false;
        }
        return !m_isRootBridge;
    }
    public void setRootPort(Integer rootPort) {
        m_rootPort = rootPort;
        m_isRootBridge = false;
    }

    public boolean isRootBridge() {
        return m_isRootBridge;
    }

    public void setRootBridge() {
        m_isRootBridge = true;
        m_rootPort = null;
    }

    public Integer getNodeId() {
        return m_nodeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bridge bridge = (Bridge) o;
        return Objects.equals(m_nodeId, bridge.m_nodeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_nodeId);
    }

    @Override
    public String printTopology() {
    	StringBuffer strbfr = new StringBuffer();
        strbfr.append("bridge: nodeid[");
        strbfr.append(m_nodeId);
        strbfr.append("],");
        if (m_isRootBridge) {
            strbfr.append(" isRootBridge,");
        } else {
            strbfr.append(" designated port:[");
            strbfr.append(m_rootPort);
            strbfr.append("],");
        }
        strbfr.append(" designated:[");
        strbfr.append(m_designated);
        strbfr.append("], identifiers:");
        strbfr.append(m_identifiers);
        return strbfr.toString();
    }

    public Set<String> getIdentifiers() {
        return m_identifiers;
    }

    public void setIdentifiers(Set<String> identifiers) {
        m_identifiers = identifiers;
    }

    public String getDesignated() {
        return m_designated;
    }

    public void setDesignated(String designated) {
        m_designated = designated;
    }

    public void clear() {
        m_identifiers.clear();
        m_designated = null;
    }

}
