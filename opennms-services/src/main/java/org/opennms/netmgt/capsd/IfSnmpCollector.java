/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.capsd;

import java.net.InetAddress;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.capsd.snmp.IfTable;
import org.opennms.netmgt.capsd.snmp.IfXTable;
import org.opennms.netmgt.capsd.snmp.IpAddrTable;
import org.opennms.netmgt.capsd.snmp.SystemGroup;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is designed to collect the necessary SNMP information from the
 * target address and store the collected information. When the class is
 * initially constructed no information is collected. The SNMP Session creating
 * and colletion occurs in the main run method of the instance. This allows the
 * collection to occur in a thread if necessary.
 *
 * @author <a href="mailto:brozow@opennms.org">brozow </a>
 */
public final class IfSnmpCollector implements Runnable {
    
    private static final Logger LOG = LoggerFactory.getLogger(IfSnmpCollector.class);

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
     * @param address a {@link java.net.InetAddress} object.
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
     *
     * @return a boolean.
     */
    public boolean failed() {
        return !hasSystemGroup() || !hasIfTable() || !hasIpAddrTable();
    }

    /**
     * Returns true if the system group was collected successfully
     *
     * @return a boolean.
     */
    public boolean hasSystemGroup() {
        if (m_sysGroup == null) {
            LOG.debug("hasSystemGroup: No system group present.");
        }

        return (m_sysGroup != null && !m_sysGroup.failed());
    }

    /**
     * Returns the collected system group.
     *
     * @return a {@link org.opennms.netmgt.capsd.snmp.SystemGroup} object.
     */
    public SystemGroup getSystemGroup() {
        return m_sysGroup;
    }

    /**
     * Returns true if the interface table was collected.
     *
     * @return a boolean.
     */
    public boolean hasIfTable() {
        // FIXME What should we do if the table had no error but was empty
		if (m_ifTable == null) {
			LOG.debug("hasIfTable: No interface table present.");
		}
        return (m_ifTable != null && !m_ifTable.failed());
    }

    /**
     * Returns the collected interface table.
     *
     * @return a {@link org.opennms.netmgt.capsd.snmp.IfTable} object.
     */
    public IfTable getIfTable() {
        return m_ifTable;
    }

    /**
     * Returns true if the IP Interface Address table was collected.
     *
     * @return a boolean.
     */
    public boolean hasIpAddrTable() {
        // FIXME What should we do if the table had no error but was empty
		if (m_ipAddrTable == null) {
			LOG.debug("hasIpAddrTable: No IP interface address table present.");
		}
        return (m_ipAddrTable != null && !m_ipAddrTable.failed());
    }

    /**
     * Returns the collected IP Interface Address table.
     *
     * @return a {@link org.opennms.netmgt.capsd.snmp.IpAddrTable} object.
     */
    public IpAddrTable getIpAddrTable() {
        return m_ipAddrTable;
    }

    /**
     * Returns true if the interface extensions table was collected.
     *
     * @return a boolean.
     */
    public boolean hasIfXTable() {
        // FIXME What should we do if the table had no error but was empty
    		if (m_ifXTable == null) {
    			LOG.debug("hasIfXTable: No interface extensions table present.");
    		}
        return (m_ifXTable != null && !m_ifXTable.failed());
    }

    /**
     * Returns the collected interface extensions table.
     *
     * @return a {@link org.opennms.netmgt.capsd.snmp.IfXTable} object.
     */
    public IfXTable getIfXTable() {
        return m_ifXTable;
    }

    /**
     * Returns the target address that the collection occured for.
     *
     * @return a {@link java.net.InetAddress} object.
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
     * @throws java.lang.IndexOutOfBoundsException
     *             Thrown if the index cannot be resolved due to an incomplete
     *             table.
     * @return an array of {@link java.net.InetAddress} objects.
     */
    public InetAddress[] getIfAddressAndMask(int ifIndex) {
        if (!hasIpAddrTable()) {
            throw new IndexOutOfBoundsException("Illegal Index, no table present");
        }

        return m_ipAddrTable.getIfAddressAndMask(ifIndex);
    }

    /**
     * <p>getAdminStatus</p>
     *
     * @param ifIndex a int.
     * @return a int.
     */
    public int getAdminStatus(int ifIndex) {
        if (!hasIfTable()) {
            throw new IndexOutOfBoundsException("Illegal Index, no table present");
        }
        
        return m_ifTable.getAdminStatus(ifIndex);
    }
    
    /**
     * <p>getOperStatus</p>
     *
     * @param ifIndex a int.
     * @return a int.
     */
    public int getOperStatus(int ifIndex) {
        if (!hasIfTable()) {
            throw new IndexOutOfBoundsException("Illegal Index, no table present");
        }
        
        return m_ifTable.getOperStatus(ifIndex);
    }

    /**
     * <p>getIfType</p>
     *
     * @param ifIndex a int.
     * @return a int.
     */
    public int getIfType(int ifIndex) {
        if (!hasIfTable()) {
            throw new IndexOutOfBoundsException("Illegal Index, no table present");
        }
        
        return m_ifTable.getIfType(ifIndex);
    }

    /**
     * <p>getIfIndex</p>
     *
     * @param address a {@link java.net.InetAddress} object.
     * @return a int.
     */
    public int getIfIndex(InetAddress address) {
        LOG.debug("getIfIndex: retrieving ifIndex for {}", InetAddressUtils.str(address));
        if (!hasIpAddrTable()) {
            LOG.debug("getIfIndex: Illegal index, no table present.");
            throw new IndexOutOfBoundsException("Illegal Index, no table present");
        }

        return m_ipAddrTable.getIfIndex(address);
    }

    /**
     * <p>getIfName</p>
     *
     * @param ifIndex a int.
     * @return a {@link java.lang.String} object.
     */
    public String getIfName(int ifIndex) {
        String snmpIfName = null;

        if (hasIfXTable()) {
            snmpIfName = m_ifXTable.getIfName(ifIndex);
        }

        // Debug
        if (snmpIfName != null) {
            LOG.debug("getIfName: ifIndex {} has ifName '{}'", ifIndex, snmpIfName);
        } else {
            LOG.debug("getIfName: no ifName found for ifIndex {}", ifIndex);
        }

        return snmpIfName;
    }
    
    /**
     * <p>getIfDescr</p>
     *
     * @param ifIndex a int.
     * @return a {@link java.lang.String} object.
     */
    public String getIfDescr(final int ifIndex) {
        String ifDescr = null;
        
        if (hasIfTable()) {
            ifDescr = m_ifTable.getIfDescr(ifIndex);
        }
        return ifDescr;
    }
    
    /**
     * <p>getInterfaceSpeed</p>
     *
     * @param ifIndex a int.
     * @return a {@link java.lang.Long} object.
     */
    public Long getInterfaceSpeed(final int ifIndex) {
        Long ifSpeed = null;
        
        try {

            if (hasIfXTable() && getIfXTable().getIfHighSpeed(ifIndex) != null && getIfXTable().getIfHighSpeed(ifIndex) > 4294) {
                ifSpeed = getIfXTable().getIfHighSpeed(ifIndex)*1000000L;
                LOG.debug("getInterfaceSpeed:  Using ifHighSpeed for ifIndex {}: {}", ifIndex, ifSpeed);
            } else if (hasIfTable()) {
                ifSpeed = m_ifTable.getIfSpeed(ifIndex);
                LOG.debug("getInterfaceSpeed:  Using ifSpeed for ifIndex {}: {}", ifIndex, ifSpeed);
            }
            
        } catch(Throwable e) {
            LOG.warn("getInterfaceSpeed: exception retrieving interface speed for ifIndex {}", ifIndex);
        }
        return ifSpeed;
    }
    
    /**
     * <p>getPhysAddr</p>
     *
     * @param ifIndex a int.
     * @return a {@link java.lang.String} object.
     */
    public String getPhysAddr(final int ifIndex) {
        String physAddr = null;
        
        if (hasIfTable()) {
            physAddr = m_ifTable.getPhysAddr(ifIndex);
        }
        return physAddr;
    }
    
    /**
     * <p>getIfAlias</p>
     *
     * @param ifIndex a int.
     * @return a {@link java.lang.String} object.
     */
    public String getIfAlias(int ifIndex) {
        String snmpIfAlias = null;

        if (hasIfXTable()) {
            snmpIfAlias = m_ifXTable.getIfAlias(ifIndex);

            if (LOG.isDebugEnabled()) {
                if (snmpIfAlias != null) {
                    LOG.debug("getIfAlias: ifIndex {} has ifAlias '{}'", ifIndex, snmpIfAlias);
                } else {
                    LOG.debug("getIfAlias: no ifAlias found for ifIndex {}", ifIndex);
                }
            }
        } else {
            LOG.debug("getIfAlias: no ifXTable retrieved from {}", m_address);
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
     */
    @Override
    public void run() {

        m_sysGroup = new SystemGroup(m_address);
        m_ifTable = new IfTable(m_address);
        m_ipAddrTable = new IpAddrTable(m_address);
        m_ifXTable = new IfXTable(m_address);
        
        SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(m_address);
        
        LOG.debug("run: collecting for: {} with agentConfig: {}", m_address, agentConfig);
        
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

            LOG.warn("IfSnmpCollector: collection interrupted, exiting", e);
            return;
        }

        if (walker.failed()) {
            LOG.info("IfSnmpCollector: walker failed with error message:", walker.getErrorMessage());
        }

        // Log any failures
        //
        if (!this.hasSystemGroup())
            LOG.info("IfSnmpCollector: failed to collect System group for {}", InetAddressUtils.str(m_address));
        if (!this.hasIfTable())
            LOG.info("IfSnmpCollector: failed to collect ifTable for {}", InetAddressUtils.str(m_address));
        if (!this.hasIpAddrTable())
            LOG.info("IfSnmpCollector: failed to collect ipAddrTable for {}", InetAddressUtils.str(m_address));
        if (!this.hasIfXTable())
            LOG.info("IfSnmpCollector: failed to collect ifXTable for {}", InetAddressUtils.str(m_address));
    }
}
