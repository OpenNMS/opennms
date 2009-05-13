//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 May 10: Improve logging in getIfAlias. - dj@opennms.org
// 2003 Jan 31: Cleaned up some unused imports.
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8

package org.opennms.netmgt.capsd;

import java.net.InetAddress;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.capsd.snmp.IfTable;
import org.opennms.netmgt.capsd.snmp.IfXTable;
import org.opennms.netmgt.capsd.snmp.IpAddrTable;
import org.opennms.netmgt.capsd.snmp.SystemGroup;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpWalker;

/**
 * This class is designed to collect the necessary SNMP information from the
 * target address and store the collected information. When the class is
 * initially constructed no information is collected. The SNMP Session creating
 * and colletion occurs in the main run method of the instance. This allows the
 * collection to occur in a thread if necessary.
 * 
 * @author <a href="mailto:brozow@opennms.org">brozow </a>
 * @author <a href="http://www.opennms.org">OpenNMS </a>
 * 
 */
public final class IfSnmpCollector implements Runnable {

    /**
     * The IP address to used to collect the SNMP information
     */
    private final InetAddress m_address;

    /**
     * The system group information
     */
    private SystemGroup m_sysGroup;

    /**
     * The interface table information
     */
    private IfTable m_ifTable;

    /**
     * The IP address table
     */
    private IpAddrTable m_ipAddrTable;

    /**
     * The interface extensions table information
     */
    private IfXTable m_ifXTable;

    /**
     * Constructs a new snmp collector for a node using the passed interface as
     * the collection point. The collection does not occur until the
     * <code>run</code> method is invoked.
     * 
     */
    public IfSnmpCollector(InetAddress address) {
        m_address = address;
        m_sysGroup = null;
        m_ifTable = null;
        m_ifXTable = null;
        m_ipAddrTable = null;
    }

    /**
     * Returns true if any part of the collection failed.
     */
    public boolean failed() {
        return !hasSystemGroup() || !hasIfTable() || !hasIpAddrTable();
    }

    /**
     * Returns true if the system group was collected successfully
     */
    public boolean hasSystemGroup() {
        return (m_sysGroup != null && !m_sysGroup.failed());
    }

    /**
     * Returns the collected system group.
     */
    public SystemGroup getSystemGroup() {
        return m_sysGroup;
    }

    /**
     * Returns true if the interface table was collected.
     */
    public boolean hasIfTable() {
        // FIXME What should we do if the table had no error but was empty
		if (m_ifTable == null) {
			log().debug("hasIfTable: No interface table present.");
		}
        return (m_ifTable != null && !m_ifTable.failed());
    }

    /**
     * Returns the collected interface table.
     */
    public IfTable getIfTable() {
        return m_ifTable;
    }

    /**
     * Returns true if the IP Interface Address table was collected.
     */
    public boolean hasIpAddrTable() {
        // FIXME What should we do if the table had no error but was empty
		if (m_ipAddrTable == null) {
			log().debug("hasIpAddrTable: No IP interface address table present.");
		}
        return (m_ipAddrTable != null && !m_ipAddrTable.failed());
    }

    /**
     * Returns the collected IP Interface Address table.
     */
    public IpAddrTable getIpAddrTable() {
        return m_ipAddrTable;
    }

    /**
     * Returns true if the interface extensions table was collected.
     */
    public boolean hasIfXTable() {
        // FIXME What should we do if the table had no error but was empty
    		if (m_ifXTable == null) {
    			log().debug("hasIfXTable: No interface extensions table present.");
    		}
        return (m_ifXTable != null && !m_ifXTable.failed());
    }

    /**
     * Returns the collected interface extensions table.
     */
    public IfXTable getIfXTable() {
        return m_ifXTable;
    }

    /**
     * Returns the target address that the collection occured for.
     */
    public InetAddress getCollectorTargetAddress() {
        return m_address;
    }

    /**
     * Returns the Internet address at the corresponding index. If the address
     * cannot be resolved then a null reference is returned.
     * 
     * @param ifIndex
     *            The index to search for.
     * 
     * @throws java.lang.IndexOutOfBoundsException
     *             Thrown if the index cannot be resolved due to an incomplete
     *             table.
     */
    public InetAddress[] getIfAddressAndMask(int ifIndex) {
        if (!hasIpAddrTable()) {
            throw new IndexOutOfBoundsException("Illegal Index, no table present");
        }

        return m_ipAddrTable.getIfAddressAndMask(ifIndex);
    }

    public int getAdminStatus(int ifIndex) {
        if (!hasIfTable()) {
            throw new IndexOutOfBoundsException("Illegal Index, no table present");
        }
        
        return m_ifTable.getAdminStatus(ifIndex);
    }
    
    public int getOperStatus(int ifIndex) {
        if (!hasIfTable()) {
            throw new IndexOutOfBoundsException("Illegal Index, no table present");
        }
        
        return m_ifTable.getOperStatus(ifIndex);
    }

    public int getIfType(int ifIndex) {
        if (!hasIfTable()) {
            throw new IndexOutOfBoundsException("Illegal Index, no table present");
        }
        
        return m_ifTable.getIfType(ifIndex);
    }

    public int getIfIndex(InetAddress address) {
        log().debug("getIfIndex: retrieving ifIndex for " + address.getHostAddress());
        if (!hasIpAddrTable()) {
            log().debug("getIfIndex: Illegal index, no table present.");
            throw new IndexOutOfBoundsException("Illegal Index, no table present");
        }

        return m_ipAddrTable.getIfIndex(address);
    }

    /**
     * 
     */
    public String getIfName(int ifIndex) {
        String snmpIfName = null;

        if (hasIfXTable()) {
            snmpIfName = m_ifXTable.getIfName(ifIndex);
        }

        // Debug
        if (snmpIfName != null) {
            log().debug("getIfName: ifIndex " + ifIndex + " has ifName '" + snmpIfName);
        } else {
            log().debug("getIfName: no ifName found for ifIndex " + ifIndex);
        }

        return snmpIfName;
    }
    
    public String getIfDescr(final int ifIndex) {
        String ifDescr = null;
        
        if (hasIfTable()) {
            ifDescr = m_ifTable.getIfDescr(ifIndex);
        }
        return ifDescr;
    }
    
    public Long getInterfaceSpeed(final int ifIndex) {
        Long ifSpeed = null;
        
        try {

            if (hasIfXTable() && getIfXTable().getIfHighSpeed(ifIndex) != null && getIfXTable().getIfHighSpeed(ifIndex) > 0) {
                ifSpeed = getIfXTable().getIfHighSpeed(ifIndex)*1000000L;
                log().debug("getInterfaceSpeed:  Using ifHighSpeed for ifIndex "+ifIndex+": "+ ifSpeed);
            } else if (hasIfTable()) {
                ifSpeed = m_ifTable.getIfSpeed(ifIndex);
                log().debug("getInterfaceSpeed:  Using ifSpeed for ifIndex "+ifIndex+": "+ ifSpeed);
            }
            
        } catch(Exception e) {
            log().warn("getInterfaceSpeed: exception retrieving interface speed for ifIndex " + ifIndex);
        }
        return ifSpeed;
    }
    
    public String getPhysAddr(final int ifIndex) {
        String physAddr = null;
        
        if (hasIfTable()) {
            physAddr = m_ifTable.getPhysAddr(ifIndex);
        }
        return physAddr;
    }
    
    /**
     * 
     */
    public String getIfAlias(int ifIndex) {
        String snmpIfAlias = null;

        if (hasIfXTable()) {
            snmpIfAlias = m_ifXTable.getIfAlias(ifIndex);

            if (log().isDebugEnabled()) {
                if (snmpIfAlias != null) {
                    log().debug("getIfAlias: ifIndex " + ifIndex + " has ifAlias '" + snmpIfAlias + "'");
                } else {
                    log().debug("getIfAlias: no ifAlias found for ifIndex " + ifIndex);
                }
            }
        } else {
            if (log().isDebugEnabled()) {
                log().debug("getIfAlias: no ifXTable retrieved from " + m_address);
            }
        }

        return snmpIfAlias;
    }

    /**
     * <p>
     * Preforms the collection for the targeted internet address. The success or
     * failure of the collection should be tested via the <code>failed</code>
     * method.
     * </p>
     * 
     * <p>
     * No synchronization is preformed, so if this is used in a separate thread
     * context synchornization must be added.
     * </p>
     * 
     */
    public void run() {

        m_sysGroup = new SystemGroup(m_address);
        m_ifTable = new IfTable(m_address);
        m_ipAddrTable = new IpAddrTable(m_address);
        m_ifXTable = new IfXTable(m_address);
        
        SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(m_address);
        
        if (log().isDebugEnabled())
            log().debug("run: collecting for: "+m_address+" with agentConfig: "+agentConfig);
        
        SnmpWalker walker = SnmpUtils.createWalker(agentConfig, "system/ifTable/ifXTable/ipAddrTable", new CollectionTracker[] { m_sysGroup, m_ifTable, m_ipAddrTable, m_ifXTable});
        walker.start();

        try {
            // wait a maximum of five minutes!
            //
            // FIXME: Why do we do this. If we are successfully processing responses shouldn't we keep going?
            walker.waitFor(300000);
        } catch (InterruptedException e) {
            m_sysGroup = null;
            m_ifTable = null;
            m_ipAddrTable = null;
            m_ifXTable = null;

            log().warn("IfSnmpCollector: collection interrupted, exiting", e);
            return;
        }

        // Log any failures
        //
        if (!this.hasSystemGroup())
            log().info("IfSnmpCollector: failed to collect System group for " + m_address.getHostAddress());
        if (!this.hasIfTable())
            log().info("IfSnmpCollector: failed to collect ifTable for " + m_address.getHostAddress());
        if (!this.hasIpAddrTable())
            log().info("IfSnmpCollector: failed to collect ipAddrTable for " + m_address.getHostAddress());
        if (!this.hasIfXTable())
            log().info("IfSnmpCollector: failed to collect ifXTable for " + m_address.getHostAddress());
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass()+"."+m_address);
    }
}
