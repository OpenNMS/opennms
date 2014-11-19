/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service.snmp;

import java.net.InetAddress;

import org.opennms.netmgt.snmp.NamedSnmpVar;

/**
 * <P>
 * The IpAddrTableEntry class is designed to hold all the MIB-II information for
 * one entry in the ipAddrTable. The table effectively contains a list of these
 * entries, each entry having information about one address. The entry contains
 * an IP Address, its netmask, interface binding, broadcast address, and maximum
 * packet reassembly size.
 * </P>
 *
 * <P>
 * This object is used by the IpAddrTable to hold information single entries in
 * the table. See the IpAddrTable documentation form more information.
 * </P>
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya </A>
 * @author <A HREF="mailto:weave@oculan.com">Weave </A>
 * @author <A>Jon Whetzel </A>
 * @see IpAddrTable
 * @see <A HREF="http://www.ietf.org/rfc/rfc1213.txt">RFC1213 </A>
 */
public final class IpAddrTableEntry extends SnmpTableEntry {
    // Lookup strings for specific table entries
    //
    /** Constant <code>IP_ADDR_ENT_ADDR="ipAdEntAddr"</code> */
    public static final String IP_ADDR_ENT_ADDR = "ipAdEntAddr";

    /** Constant <code>IP_ADDR_IF_INDEX="ipAdEntIfIndex"</code> */
    public static final String IP_ADDR_IF_INDEX = "ipAdEntIfIndex";

    /** Constant <code>IP_ADDR_ENT_NETMASK="ipAdEntNetMask"</code> */
    public static final String IP_ADDR_ENT_NETMASK = "ipAdEntNetMask";

    /** Constant <code>IP_ADDR_ENT_BCASTADDR="ipAdEntBcastAddr"</code> */
    public static final String IP_ADDR_ENT_BCASTADDR = "ipAdEntBcastAddr";

    /** Constant <code>ms_elemList</code> */
    public static NamedSnmpVar[] ms_elemList = null;

    /**
     * <P>
     * Initialize the element list for the class. This is class wide data, but
     * will be used by each instance.
     * </P>
     */
    static {
        // Array size has changed from 5 to 4...no longer going after
        // ipAdEntReasmMaxSize variable because we aren't currently using
        // it and not all agents implement it which causes the collection
        // of the ipAddrTable to fail
        IpAddrTableEntry.ms_elemList = new NamedSnmpVar[4];
        int ndx = 0;

        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPIPADDRESS, IP_ADDR_ENT_ADDR, ".1.3.6.1.2.1.4.20.1.1", 1);
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32, IP_ADDR_IF_INDEX, ".1.3.6.1.2.1.4.20.1.2", 2);
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPIPADDRESS, IP_ADDR_ENT_NETMASK, ".1.3.6.1.2.1.4.20.1.3", 3);
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32, IP_ADDR_ENT_BCASTADDR, ".1.3.6.1.2.1.4.20.1.4", 4);
        // ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
        // IP_ADDR_ENT_REASM_MAXSIZE, ".1.3.6.1.2.1.4.20.1.5", 5);
    }

    /**
     * <P>
     * The TABLE_OID is the object identifier that represents the root of the IP
     * Address table in the MIB forest.
     * </P>
     */
    public static final String TABLE_OID = ".1.3.6.1.2.1.4.20.1"; // start of
                                                                    // table
                                                                    // (GETNEXT)

    /**
     * <P>
     * The class constructor used to initialize the object to its initial state.
     * Although the object's member variables can change after an instance is
     * created, this constructor will initialize all the variables as per their
     * named variable from the passed array of SNMP varbinds.
     * </P>
     *
     * <P>
     * If the information in the object should not be modified then a <EM>final
     * </EM> modifier can be applied to the created object.
     * </P>
     */
    public IpAddrTableEntry() {
        super(ms_elemList);
    }

    /**
     * <p>getIpAdEntAddr</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
    public InetAddress getIpAdEntAddr() {
        return getIPAddress(IpAddrTableEntry.IP_ADDR_ENT_ADDR);
    }

    /**
     * <p>getIpAdEntIfIndex</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getIpAdEntIfIndex() {
        return getInt32(IpAddrTableEntry.IP_ADDR_IF_INDEX);
    }

    /**
     * <p>getIpAdEntNetMask</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
    public InetAddress getIpAdEntNetMask() {
        return getIPAddress(IpAddrTableEntry.IP_ADDR_ENT_NETMASK);
    }
    
    /**
     * <p>getIpAdEntBcastAddr</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
    public InetAddress getIpAdEntBcastAddr() {
        return getIPAddress(IpAddrTableEntry.IP_ADDR_ENT_BCASTADDR);
    }

    
}
