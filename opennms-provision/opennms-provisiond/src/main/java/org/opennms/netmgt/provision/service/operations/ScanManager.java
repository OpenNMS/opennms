/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.provision.service.operations;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.opennms.core.utils.InetAddressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.provision.service.snmp.IfTable;
import org.opennms.netmgt.provision.service.snmp.IfXTable;
import org.opennms.netmgt.provision.service.snmp.IpAddrTable;
import org.opennms.netmgt.provision.service.snmp.IpAddressTable;
import org.opennms.netmgt.provision.service.snmp.SystemGroup;
import org.opennms.netmgt.snmp.AggregateTracker;
import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpWalker;

public class ScanManager {
    private static final Logger LOG = LoggerFactory.getLogger(ScanManager.class);
    
    private final InetAddress m_address;
    private SystemGroup m_systemGroup;
    private IfTable m_ifTable;
    private IpAddrTable m_ipAddrTable;
    private IpAddressTable m_ipAddressTable;
    private IfXTable m_ifXTable;

    ScanManager(InetAddress address) {
        m_address = address;
    }
    
    /**
     * <p>getSystemGroup</p>
     *
     * @return a {@link org.opennms.netmgt.provision.service.snmp.SystemGroup} object.
     */
    public SystemGroup getSystemGroup() {
        return m_systemGroup;
    }

    /**
     * <p>getIfTable</p>
     *
     * @return the ifTable
     */
    public IfTable getIfTable() {
        return m_ifTable;
    }

    /**
     * <p>getIpAddrTable</p>
     *
     * @return the ipAddrTable
     */
    public IpAddrTable getIpAddrTable() {
        return m_ipAddrTable;
    }

    public IpAddressTable getIpAddressTable() {
    	return m_ipAddressTable;
    }

    /**
     * <p>getIfXTable</p>
     *
     * @return the ifXTable
     */
    public IfXTable getIfXTable() {
        return m_ifXTable;
    }

    InetAddress getNetMask(final int ifIndex) {
    	final InetAddress ipAddressNetmask = getIpAddressTable().getNetMask(ifIndex);
    	if (ipAddressNetmask == null) {
    		return getIpAddrTable().getNetMask(ifIndex);
    	} else {
    		return ipAddressNetmask;
    	}
    }

    boolean isSnmpDataForInterfacesUpToDate() {
        return (!getIfTable().failed() && !getIpAddrTable().failed()) || !getIpAddressTable().failed();
    }

    boolean isSnmpDataForNodeUpToDate() {
        return !getSystemGroup().failed();
    }

    void updateSnmpData(final OnmsNode node) {
        
        try {

            m_systemGroup = new SystemGroup(m_address);

            final Set<SnmpInstId> ipAddrs = new TreeSet<SnmpInstId>();
            final Set<InetAddress> ipAddresses = new HashSet<InetAddress>();

            for(final OnmsIpInterface iface : node.getIpInterfaces()) {
            	final InetAddress addr = iface.getIpAddress();

            	if (addr != null && addr instanceof Inet4Address) {
            		ipAddrs.add(new SnmpInstId(InetAddressUtils.toOid(addr)));
            	}

            	ipAddresses.add(addr);
            }

            m_ipAddrTable = new IpAddrTable(m_address, ipAddrs);
            m_ipAddressTable = IpAddressTable.createTable(m_address, ipAddresses);

            final SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(m_address);
            SnmpWalker walker = SnmpUtils.createWalker(agentConfig, "system/ipAddrTable/ipAddressTable", m_systemGroup, m_ipAddrTable, m_ipAddressTable);

            walker.start();
            walker.waitFor();

            final Set<SnmpInstId> ifIndices = new TreeSet<SnmpInstId>();

            for(final Integer ifIndex : m_ipAddrTable.getIfIndices()) {
                ifIndices.add(new SnmpInstId(ifIndex));
            }

            m_ifTable = new IfTable(m_address, ifIndices);
            m_ifXTable = new IfXTable(m_address, ifIndices);

            walker = SnmpUtils.createWalker(agentConfig, "ifTable/ifXTable", m_ifTable, m_ifXTable);
            walker.start();

            walker.waitFor();

            m_systemGroup.updateSnmpDataForNode(node);
        
            for(final SnmpInstId ifIndex : ifIndices) {
                m_ifTable.updateSnmpInterfaceData(node, ifIndex.toInt());
            }

            for(final SnmpInstId ifIndex : ifIndices) {
                m_ifXTable.updateSnmpInterfaceData(node, ifIndex.toInt());
            }

            for(final SnmpInstId ipAddr : ipAddrs) {   
                m_ipAddrTable.updateIpInterfaceData(node, ipAddr.toString());
            }

            for (final InetAddress addr : ipAddresses) {
            	m_ipAddressTable.updateIpInterfaceData(node, InetAddressUtils.str(addr));
            }
        } catch (final InterruptedException e) {
            LOG.info("thread interrupted while updating SNMP data", e);
            Thread.currentThread().interrupt();

        }
        

    }

    /**
     * <p>createCollectionTracker</p>
     *
     * @return a {@link org.opennms.netmgt.snmp.AggregateTracker} object.
     */
    public AggregateTracker createCollectionTracker() {
        return new AggregateTracker(new CollectionTracker[] { getSystemGroup(), getIfTable(), getIpAddrTable(), getIfXTable(), getIpAddressTable() });
    }

}
