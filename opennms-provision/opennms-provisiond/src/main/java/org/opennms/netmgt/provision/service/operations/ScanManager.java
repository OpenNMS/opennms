//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Jun 24: Use Java 5 generics. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//    
// For more information contact: 
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
// Tab Size = 8
package org.opennms.netmgt.provision.service.operations;

import java.net.InetAddress;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.PeerFactory;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.provision.service.snmp.IfTable;
import org.opennms.netmgt.provision.service.snmp.IfXTable;
import org.opennms.netmgt.provision.service.snmp.IpAddrTable;
import org.opennms.netmgt.provision.service.snmp.SystemGroup;
import org.opennms.netmgt.snmp.AggregateTracker;
import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpWalker;

public class ScanManager {
    
    private SystemGroup m_systemGroup;
    private IfTable m_ifTable;
    private IpAddrTable m_ipAddrTable;
    private IfXTable m_ifXTable;
    private InetAddress m_address;

    ScanManager(InetAddress address) {
        m_address = address;
        m_systemGroup = new SystemGroup(address);
        m_ifTable = new IfTable(address);
        m_ipAddrTable = new IpAddrTable(address);
        m_ifXTable = new IfXTable(address);
    }
    
    public SystemGroup getSystemGroup() {
        return m_systemGroup;
    }

    /**
     * @return the ifTable
     */
    public IfTable getIfTable() {
        return m_ifTable;
    }

    /**
     * @return the ipAddrTable
     */
    public IpAddrTable getIpAddrTable() {
        return m_ipAddrTable;
    }

    /**
     * @return the ifXTable
     */
    public IfXTable getIfXTable() {
        return m_ifXTable;
    }

    String getNetMask(int ifIndex) {
        InetAddress addr = getIpAddrTable().getNetMask(ifIndex);
        return (addr == null ? null : addr.getHostAddress());
    }

    public Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    boolean isSnmpDataForInterfacesUpToDate() {
        return !getIfTable().failed() && !getIpAddrTable().failed();
    }

    boolean isSnmpDataForNodeUpToDate() {
        return !getSystemGroup().failed();
    }

    void updateSnmpData(OnmsNode node) {
        run();
        
        getSystemGroup().updateSnmpDataForNode(node);
        
        Set<String> ipAddrs = getIpAddressesToUpdate(node);
        
        Set<Integer> ifIndices = getIfIndicesToUpdate(node);
        
        for(Integer ifIndex : ifIndices) {
            getIfTable().updateSnmpInterfaceData(node, ifIndex);
        }
        
        for(Integer ifIndex : ifIndices) {
            getIfXTable().updateSnmpInterfaceData(node, ifIndex);
        }
                
        for(String ipAddr : ipAddrs) {   
            getIpAddrTable().updateIpInterfaceData(node, ipAddr);
        }

    }

    /**
     * @param node
     * @return
     */
    private Set<Integer> getIfIndicesToUpdate(OnmsNode node) {
        //return getIfIndicesForImportedIpAddresses(node);
        if (getIfTable().failed()) {
            return Collections.emptySet();
        } else {
            return getIfTable().getIfIndices();
        }
    }

    /**
     * @param node
     * @return
     */
    private Set<String> getIpAddressesToUpdate(OnmsNode node) {
        //return getImportedIpAddresses(node);
        if (getIpAddrTable().failed()) {
            return Collections.emptySet();
        }
        Set<String> ipAddrs = getIpAddrTable().getIpAddresses();
        for(Iterator<String> it = ipAddrs.iterator(); it.hasNext(); ) {
            String ipAddr = it.next();
            if (PeerFactory.verifyIpMatch(ipAddr, "127.*.*.*")) {
                it.remove();
            }
        }
        return ipAddrs;
    }

    /**
     * @param node
     * @return
     */
    private Set<Integer> getIfIndicesForImportedIpAddresses(OnmsNode node) {
        Set<Integer> ifIndices = new LinkedHashSet<Integer>();
        for(OnmsIpInterface ipIf : node.getIpInterfaces()) {
            Integer ifIndex = getIpAddrTable().getIfIndex(ipIf.getInetAddress());
            if (ifIndex != null) {
                ifIndices.add(ifIndex);
            }
        }
        return ifIndices;
    }

    /**
     * @param node
     * @return
     */
    private Set<String> getImportedIpAddresses(OnmsNode node) {
        Set<String> ipAddrs = new LinkedHashSet<String>();
        for (OnmsIpInterface ipIf : node.getIpInterfaces()) {
            String ipAddr = ipIf.getIpAddress();
            if (ipAddr != null) {
                ipAddrs.add(ipAddr);
            }
        }
        return ipAddrs;
    }

    /**
     * 
     */
    private void run() {
        SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(m_address);
        
        CollectionTracker tracker = createCollectionTracker();
        
        if (log().isDebugEnabled())
            log().debug("run: collecting for: "+m_address+" with agentConfig: "+agentConfig);
        
        SnmpWalker walker = SnmpUtils.createWalker(agentConfig, "system/ifTable/ifXTable/ipAddrTable", tracker);
        walker.start();
        
        try {
            walker.waitFor();
        
            // Log any failures
            //
            if (getSystemGroup().failed())
                log().info("IfSnmpCollector: failed to collect System group for " + m_address.getHostAddress());
            if (getIfTable().failed())
                log().info("IfSnmpCollector: failed to collect ifTable for " + m_address.getHostAddress());
            if (getIpAddrTable().failed())
                log().info("IfSnmpCollector: failed to collect ipAddrTable for " + m_address.getHostAddress());
            if (getIfXTable().failed())
                log().info("IfSnmpCollector: failed to collect ifXTable for " + m_address.getHostAddress());
        
        } catch (InterruptedException e) {
        
            tracker.setFailed(true);
        
            log().warn("IfSnmpCollector: collection interrupted, exiting", e);
        
        }
    }

    /**
     * @param ifSnmpCollector TODO
     * @return
     */
    public AggregateTracker createCollectionTracker() {
        return new AggregateTracker(new CollectionTracker[] { getSystemGroup(), getIfTable(), getIpAddrTable(), getIfXTable()});
    }

}
