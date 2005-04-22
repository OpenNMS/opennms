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
import java.util.Iterator;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.capsd.snmp.IfTable;
import org.opennms.netmgt.capsd.snmp.IfTableEntry;
import org.opennms.netmgt.capsd.snmp.IfXTable;
import org.opennms.netmgt.capsd.snmp.IfXTableEntry;
import org.opennms.netmgt.capsd.snmp.IpAddrTable;
import org.opennms.netmgt.capsd.snmp.IpAddrTableEntry;
import org.opennms.netmgt.capsd.snmp.SystemGroup;
import org.opennms.netmgt.utils.BarrierSignaler;
import org.opennms.protocols.snmp.SnmpBadConversionException;
import org.opennms.protocols.snmp.SnmpIPAddress;
import org.opennms.protocols.snmp.SnmpInt32;
import org.opennms.protocols.snmp.SnmpOctetString;
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
 * @author <a href="mailto:weave@oculan.com">Weave </a>
 * @author <a href="http://www.opennms.org">OpenNMS </a>
 * 
 */
final class IfSnmpCollector implements Runnable {
    /**
     * The SnmpPeer object used to communicate via SNMP with the remote host.
     */
    private SnmpPeer m_peer;

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
     * @param peer
     *            The SnmpPeer object to collect from.
     * 
     */
    IfSnmpCollector(SnmpPeer peer) {
        m_peer = peer;
        m_address = peer.getPeer();
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
     * Returns the netmask address at the corresponding index. If the address
     * cannot be resolved then a null reference is returned.
     * 
     * NOTE: If an interface has more than one IP address associated with it
     * only the FIRST match is returned.
     * 
     * @param ifIndex
     *            The index to search for.
     * 
     * @throws java.lang.IndexOutOfBoundsException
     *             Thrown if the index cannot be resolved due to an incomplete
     *             table.
     */
    InetAddress getMask(int ifIndex) {
        if (m_ipAddrTable == null || m_ipAddrTable.getEntries() == null) {
            throw new IndexOutOfBoundsException("Illegal Index, no table present");
        }

        Iterator i = m_ipAddrTable.getEntries().iterator();
        while (i.hasNext()) {
            IpAddrTableEntry entry = (IpAddrTableEntry) i.next();
            SnmpInt32 ndx = (SnmpInt32) entry.get(IpAddrTableEntry.IP_ADDR_IF_INDEX);
            if (ndx != null && ndx.getValue() == ifIndex) {
                // found it
                // extract the address
                //
                SnmpIPAddress maskAddr = (SnmpIPAddress) entry.get(IpAddrTableEntry.IP_ADDR_ENT_NETMASK);
                if (maskAddr != null) {
                    try {
                        return maskAddr.convertToIpAddress();
                    } catch (SnmpBadConversionException e) {
                        Category log = ThreadCategory.getInstance(getClass());
                        log.error("Failed to convert snmp netmask: " + maskAddr, e);
                    }
                }
            }
        }
        return null;
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
        if (m_ipAddrTable == null || m_ipAddrTable.getEntries() == null) {
            throw new IndexOutOfBoundsException("Illegal Index, no table present");
        }

        Iterator i = m_ipAddrTable.getEntries().iterator();
        while (i.hasNext()) {
            IpAddrTableEntry entry = (IpAddrTableEntry) i.next();
            SnmpInt32 ndx = (SnmpInt32) entry.get(IpAddrTableEntry.IP_ADDR_IF_INDEX);
            if (ndx != null && ndx.getValue() == ifIndex) {
                // found it
                // extract the address
                //
                SnmpIPAddress ifAddr = (SnmpIPAddress) entry.get(IpAddrTableEntry.IP_ADDR_ENT_ADDR);
                SnmpIPAddress ifMask = (SnmpIPAddress) entry.get(IpAddrTableEntry.IP_ADDR_ENT_NETMASK);
                if (ifAddr != null) {
                    try {
                        InetAddress[] pair = new InetAddress[2];
                        pair[0] = ifAddr.convertToIpAddress();
                        pair[1] = ifMask.convertToIpAddress();
                        return pair;
                    } catch (SnmpBadConversionException e) {
                        Category log = ThreadCategory.getInstance(getClass());
                        log.error("Failed to convert snmp collected address: " + ifAddr, e);
                    }
                }
            }
        }
        return null;
    }

    int getAdminStatus(int ifIndex) {
        if (m_ifTable == null || m_ifTable.getEntries() == null) {
            throw new IndexOutOfBoundsException("Illegal Index, no table present");
        }

        Iterator i = m_ifTable.getEntries().iterator();
        while (i.hasNext()) {
            IfTableEntry entry = (IfTableEntry) i.next();
            SnmpInt32 ndx = (SnmpInt32) entry.get(IfTableEntry.IF_INDEX);
            if (ndx != null && ndx.getValue() == ifIndex) {
                // found it
                // extract the admin status
                //
                SnmpInt32 ifStatus = (SnmpInt32) entry.get(IfTableEntry.IF_ADMIN_STATUS);
                if (ifStatus != null)
                    return ifStatus.getValue();
            }
        }
        return -1;
    }

    int getIfType(int ifIndex) {
        if (m_ifTable == null || m_ifTable.getEntries() == null) {
            throw new IndexOutOfBoundsException("Illegal Index, no table present");
        }

        Iterator i = m_ifTable.getEntries().iterator();
        while (i.hasNext()) {
            IfTableEntry entry = (IfTableEntry) i.next();
            SnmpInt32 ndx = (SnmpInt32) entry.get(IfTableEntry.IF_INDEX);
            if (ndx != null && ndx.getValue() == ifIndex) {
                // found it
                // extract the ifType
                //
                SnmpInt32 ifType = (SnmpInt32) entry.get(IfTableEntry.IF_TYPE);
                if (ifType != null)
                    return ifType.getValue();
            }
        }
        return -1;
    }

    int getIfIndex(InetAddress address) {
        Category log = ThreadCategory.getInstance(getClass());

        log.debug("getIfIndex: retrieving ifIndex for " + address.getHostAddress());
        if (m_ipAddrTable == null || m_ipAddrTable.getEntries() == null) {
            log.debug("getIfIndex: Illegal index, no table present.");
            throw new IndexOutOfBoundsException("Illegal Index, no table present");
        }

        if (log.isDebugEnabled())
            log.debug("getIfIndex: num ipAddrTable entries: " + m_ipAddrTable.getEntries().size());
        Iterator i = m_ipAddrTable.getEntries().iterator();
        while (i.hasNext()) {
            IpAddrTableEntry entry = (IpAddrTableEntry) i.next();
            SnmpIPAddress snmpAddr = (SnmpIPAddress) entry.get(IpAddrTableEntry.IP_ADDR_ENT_ADDR);
            if (snmpAddr != null) {
                InetAddress ifAddr = null;
                try {
                    ifAddr = snmpAddr.convertToIpAddress();
                } catch (SnmpBadConversionException e) {
                    log.error("Failed to convert snmp collected address: " + ifAddr, e);
                    continue;
                }

                if (ifAddr.equals(address)) {
                    // found it
                    // extract the ifIndex
                    //
                    SnmpInt32 ndx = (SnmpInt32) entry.get(IpAddrTableEntry.IP_ADDR_IF_INDEX);
                    log.debug("getIfIndex: got a match for address " + address.getHostAddress() + " index: " + ndx);
                    if (ndx != null)
                        return ndx.getValue();
                }
            }
        }
        log.debug("getIfIndex: no matching ipAddrTable entry for " + address.getHostAddress());
        return -1;
    }

    /**
     * 
     */
    SnmpOctetString getIfName(int ifIndex) {
        Category log = ThreadCategory.getInstance(getClass());
        SnmpOctetString snmpIfName = null;

        if (m_ifXTable != null && !m_ifXTable.failed()) {
            // Find ifXTable entry with matching ifIndex
            //
            Iterator iter = m_ifXTable.getEntries().iterator();
            while (iter.hasNext()) {
                IfXTableEntry ifXEntry = (IfXTableEntry) iter.next();

                int ifXIndex = -1;
                SnmpInt32 snmpIfIndex = (SnmpInt32) ifXEntry.get(IfXTableEntry.IF_INDEX);
                if (snmpIfIndex != null)
                    ifXIndex = snmpIfIndex.getValue();

                // compare with passed ifIndex
                if (ifXIndex == ifIndex) {
                    // Found match! Get the ifName
                    snmpIfName = (SnmpOctetString) ifXEntry.get(IfXTableEntry.IF_NAME);
                    break;
                }

            }
        }

        // Debug
        if (snmpIfName != null) {
            if (log.isDebugEnabled())
                log.debug("getIfName: ifIndex " + ifIndex + " has ifName '" + snmpIfName);
        } else {
            if (log.isDebugEnabled())
                log.debug("getIfName: no ifName found for ifIndex " + ifIndex);
        }

        return snmpIfName;
    }

    /**
     * 
     */
    SnmpOctetString getIfAlias(int ifIndex) {
        Category log = ThreadCategory.getInstance(getClass());
        SnmpOctetString snmpIfAlias = null;

        if (m_ifXTable != null && !m_ifXTable.failed()) {
            // Find ifXTable entry with matching ifIndex
            //
            Iterator iter = m_ifXTable.getEntries().iterator();
            while (iter.hasNext()) {
                IfXTableEntry ifXEntry = (IfXTableEntry) iter.next();

                int ifXIndex = -1;
                SnmpInt32 snmpIfIndex = (SnmpInt32) ifXEntry.get(IfXTableEntry.IF_INDEX);
                if (snmpIfIndex != null)
                    ifXIndex = snmpIfIndex.getValue();

                // compare with passed ifIndex
                if (ifXIndex == ifIndex) {
                    // Found match! Get the ifAlias
                    snmpIfAlias = (SnmpOctetString) ifXEntry.get(IfXTableEntry.IF_ALIAS);
                    break;
                }

            }
        }

        // Debug
        if (snmpIfAlias != null) {
            if (log.isDebugEnabled())
                log.debug("getIfAlias: ifIndex " + ifIndex + " has ifAlias '" + snmpIfAlias + "'");
        } else {
            if (log.isDebugEnabled())
                log.debug("getIfAlias: no ifAlias found for ifIndex " + ifIndex);
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
        Category log = ThreadCategory.getInstance(getClass());

        SnmpSession session = null;
        try {
            log.debug("IfSnmpCollector.run: address: " + m_address.getHostAddress() + " Snmp version: " + ((m_peer.getParameters().getVersion() == SnmpSMI.SNMPV1) ? "SNMPv1" : "SNMPv2"));
            session = new SnmpSession(m_peer);

            BarrierSignaler signaler = new BarrierSignaler(3);
            synchronized (signaler) {
                m_sysGroup = new SystemGroup(session, signaler);
                m_ifTable = new IfTable(session, signaler, m_peer.getParameters().getVersion());
                m_ipAddrTable = new IpAddrTable(session, signaler, m_peer.getParameters().getVersion());

                try {
                    // wait a maximum of five minutes!
                    //
                    signaler.wait(300000);
                } catch (InterruptedException e) {
                    m_sysGroup = null;
                    m_ifTable = null;
                    m_ipAddrTable = null;

                    log.warn("IfSnmpCollector: collection interrupted, exiting", e);
                    return;
                }
            }

            // Log any failures
            //
            if (!this.hasSystemGroup())
                log.info("IfSnmpCollector: failed to collect System group for " + m_address.getHostAddress());
            if (!this.hasIfTable())
                log.info("IfSnmpCollector: failed to collect ifTable for " + m_address.getHostAddress());
            if (!this.hasIpAddrTable())
                log.info("IfSnmpCollector: failed to collect ipAddrTable for " + m_address.getHostAddress());

            // If ifTable collection succeeded go after the ifXTable
            //
            if (this.hasIfTable()) {
                signaler = new BarrierSignaler(1);
                synchronized (signaler) {
                    m_ifXTable = new IfXTable(session, signaler, m_peer.getParameters().getVersion());

                    try {
                        signaler.wait(300000);
                    } catch (InterruptedException e) {
                        m_ifXTable = null;

                        log.warn("IfSnmpCollector: ifXTable collection interrupted, exiting", e);
                        return;
                    }
                }
            }
        } catch (java.net.SocketException e) {
            log.error("Failed to create SNMP session to connect to host " + m_address.getHostAddress(), e);
        } finally {
            if (session != null)
                session.close();
        }
    }
}
