/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2008-2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.provision.service.operations;

import java.net.InetAddress;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
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
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpWalker;

public class ScanManager {
    
    private final InetAddress m_address;
    private SystemGroup m_systemGroup;
    private IfTable m_ifTable;
    private IpAddrTable m_ipAddrTable;
    private IfXTable m_ifXTable;

    ScanManager(InetAddress address) {
        m_address = address;
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
        
        try {

            m_systemGroup = new SystemGroup(m_address);

            Set<SnmpInstId> ipAddrs = new TreeSet<SnmpInstId>();
            for(OnmsIpInterface iface : node.getIpInterfaces()) {
                ipAddrs.add(new SnmpInstId(iface.getIpAddress()));
            }

            m_ipAddrTable = new IpAddrTable(m_address, ipAddrs);

            SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(m_address);
            SnmpWalker walker = SnmpUtils.createWalker(agentConfig, "system/ipAddrTable", m_systemGroup, m_ipAddrTable);
            walker.start();

            walker.waitFor();

            Set<SnmpInstId> ifIndices = new TreeSet<SnmpInstId>();

            for(Integer ifIndex : m_ipAddrTable.getIfIndices()) {
                ifIndices.add(new SnmpInstId(ifIndex));
            }


            
            m_ifTable = new IfTable(m_address, ifIndices);
            m_ifXTable = new IfXTable(m_address, ifIndices);

            walker = SnmpUtils.createWalker(agentConfig, "ifTable/ifXTable", m_ifTable, m_ifXTable);
            walker.start();

            walker.waitFor();

            m_systemGroup.updateSnmpDataForNode(node);
        
            for(SnmpInstId ifIndex : ifIndices) {
                m_ifTable.updateSnmpInterfaceData(node, ifIndex.toInt());
            }

            for(SnmpInstId ifIndex : ifIndices) {
                m_ifXTable.updateSnmpInterfaceData(node, ifIndex.toInt());
            }

            for(SnmpInstId ipAddr : ipAddrs) {   
                m_ipAddrTable.updateIpInterfaceData(node, ipAddr.toString());
            }

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

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
