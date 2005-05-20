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
import org.opennms.netmgt.utils.BarrierSignaler;
import org.opennms.protocols.snmp.SnmpPeer;
import org.opennms.protocols.snmp.SnmpSMI;
import org.opennms.protocols.snmp.SnmpSession;

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
final class IfSnmpCollector implements Runnable {

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
     * The default constructor. Since this class requires an IP address to
     * collect SNMP information the default constructor is declared private and
     * will also throw an exception
     * 
     * @throws java.lang.UnsupportedOperationException
     *             Always thrown.
     */
    private IfSnmpCollector() {
        throw new UnsupportedOperationException("default constructor not supported");
    }

    /**
     * Constructs a new snmp collector for a node using the passed interface as
     * the collection point. The collection does not occur until the
     * <code>run</code> method is invoked.
     * 
     */
    IfSnmpCollector(InetAddress address) {
        m_address = address;
        m_sysGroup = null;
        m_ifTable = null;
        m_ifXTable = null;
        m_ipAddrTable = null;
    }

    /**
     * Returns true if any part of the collection failed.
     */
    boolean failed() {
        return !hasSystemGroup() || !hasIfTable() || !hasIpAddrTable();
    }

    /**
     * Returns true if the system group was collected successfully
     */
    boolean hasSystemGroup() {
        // FIXME What should we do if the table had no error but was empty
        return (m_sysGroup != null && !m_sysGroup.failed());
    }

    /**
     * Returns the collected system group.
     */
    SystemGroup getSystemGroup() {
        return m_sysGroup;
    }

    /**
     * Returns true if the interface table was collected.
     */
    boolean hasIfTable() {
        // FIXME What should we do if the table had no error but was empty
        return (m_ifTable != null && !m_ifTable.failed());
    }

    /**
     * Returns the collected interface table.
     */
    IfTable getIfTable() {
        return m_ifTable;
    }

    /**
     * Returns true if the IP Interface Address table was collected.
     */
    boolean hasIpAddrTable() {
        // FIXME What should we do if the table had no error but was empty
        return (m_ipAddrTable != null && !m_ipAddrTable.failed());
    }

    /**
     * Returns the collected IP Interface Address table.
     */
    IpAddrTable getIpAddrTable() {
        return m_ipAddrTable;
    }

    /**
     * Returns true if the interface extensions table was collected.
     */
    boolean hasIfXTable() {
        // FIXME What should we do if the table had no error but was empty
        return (m_ifXTable != null && !m_ifXTable.failed());
    }

    /**
     * Returns the collected interface extensions table.
     */
    IfXTable getIfXTable() {
        return m_ifXTable;
    }

    /**
     * Returns the target address that the collection occured for.
     */
    InetAddress getCollectorTargetAddress() {
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
    InetAddress[] getIfAddressAndMask(int ifIndex) {
        if (!hasIpAddrTable()) {
            throw new IndexOutOfBoundsException("Illegal Index, no table present");
        }

        return m_ipAddrTable.getIfAddressAndMask(ifIndex);
    }

    int getAdminStatus(int ifIndex) {
        if (!hasIfTable()) {
            throw new IndexOutOfBoundsException("Illegal Index, no table present");
        }
        
        return m_ifTable.getAdminStatus(ifIndex);
    }

    int getIfType(int ifIndex) {
        if (!hasIfTable()) {
            throw new IndexOutOfBoundsException("Illegal Index, no table present");
        }
        
        return m_ifTable.getIfType(ifIndex);
    }

    int getIfIndex(InetAddress address) {
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
    String getIfName(int ifIndex) {
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

    /**
     * 
     */
    String getIfAlias(int ifIndex) {
        String snmpIfAlias = null;

        if (hasIfXTable()) {
            snmpIfAlias = m_ifXTable.getIfIndex(ifIndex);
        }

        // Debug
        if (snmpIfAlias != null) {
            log().debug("getIfAlias: ifIndex " + ifIndex + " has ifAlias '" + snmpIfAlias + "'");
        } else {
            log().debug("getIfAlias: no ifAlias found for ifIndex " + ifIndex);
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
        SnmpSession session = null;
        try {
            SnmpPeer m_peer = SnmpPeerFactory.getInstance().getPeer(m_address);
            log().debug("IfSnmpCollector.run: address: " + m_address.getHostAddress() + " Snmp version: " + SnmpSMI.getVersionString(m_peer.getParameters().getVersion()));
            session = new SnmpSession(m_peer);

            BarrierSignaler signaler = new BarrierSignaler(4);
            m_sysGroup = new SystemGroup(session, m_address, signaler);
            m_ifTable = new IfTable(session, m_address, signaler, m_peer.getParameters().getVersion());
            m_ipAddrTable = new IpAddrTable(session, m_address, signaler, m_peer.getParameters().getVersion());
            m_ifXTable = new IfXTable(session, m_address, signaler, m_peer.getParameters().getVersion());

            try {
                // wait a maximum of five minutes!
                //
                // FIXME: Why do we do this. If we are successfully processing responses shouldn't we keep going?
                signaler.waitFor(300000);
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

        } catch (java.net.SocketException e) {
            log().error("Failed to create SNMP session to connect to host " + m_address.getHostAddress(), e);
        } finally {
            if (session != null)
                session.close();
        }
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }
}
