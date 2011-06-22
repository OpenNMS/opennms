/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 2 of the License,
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

package org.opennms.netmgt.provision.service.snmp;

import static org.opennms.core.utils.InetAddressUtils.getInetAddress;
import static org.opennms.core.utils.InetAddressUtils.str;

import java.net.InetAddress;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.provision.service.IPAddressTableTracker;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpValue;

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
public final class IpAddressTableEntry extends SnmpTableEntry {
    // Lookup strings for specific table entries
    //
    /** Constant <code>IP_ADDRESS_IF_INDEX="ipAdEntIfIndex"</code> */
    public final static String IP_ADDRESS_IF_INDEX = "ipAddressIfIndex";

    /** Constant <code>IP_ADDR_ENT_NETMASK="ipAdEntNetMask"</code> */
    public final static String IP_ADDR_ENT_NETMASK = "ipAddressPrefix";

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
        IpAddressTableEntry.ms_elemList = new NamedSnmpVar[2];
        int ndx = 0;

        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPOBJECTID, IP_ADDRESS_IF_INDEX, ".1.3.6.1.2.1.4.34.1.3", 1);
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPOBJECTID, IP_ADDR_ENT_NETMASK, ".1.3.6.1.2.1.4.34.1.5", 2);
    }

    /**
     * <P>
     * The TABLE_OID is the object identifier that represents the root of the IP
     * Address table in the MIB forest.
     * </P>
     */
    public static final String TABLE_OID = "..1.3.6.1.2.1.4.34.1";

    private InetAddress m_inetAddress = null;

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
    public IpAddressTableEntry() {
        super(ms_elemList);
    }

    /**
     * <p>getIpAdEntAddr</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
    public InetAddress getIpAddress() {
    	// LogUtils.debugf(this, "getIpAddress: ipAddress = %s", InetAddressUtils.str(m_inetAddress));
    	return m_inetAddress;
    }

    /**
     * <p>getIpAdEntIfIndex</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getIpAddressIfIndex() {
    	// final SnmpValue value = getValue(IP_ADDRESS_IF_INDEX);
    	// LogUtils.debugf(this, "getIpAddressIfIndex: value = %s", value.toDisplayString());
        return getInt32(IpAddressTableEntry.IP_ADDRESS_IF_INDEX);
    }

    /**
     * <p>getIpAdEntNetMask</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
    public String getIpAddressNetMask() {
    	final SnmpValue value = getValue(IP_ADDR_ENT_NETMASK);
    	// LogUtils.debugf(this, "getIpAddressNetMask: value = %s", value.toDisplayString());
    	final SnmpObjId netmaskRef = value.toSnmpObjId().getInstance(IPAddressTableTracker.IP_ADDRESS_PREFIX_ORIGIN_INDEX);
    	
    	final int[] rawIds = netmaskRef.getIds();
    	final int addressType = rawIds[1];
    	final int addressLength = rawIds[2];
    	final InetAddress address = getInetAddress(rawIds, 3, addressLength);
    	final int mask = rawIds[rawIds.length - 1];

		if (addressType == IPAddressTableTracker.TYPE_IPV4 || addressType == IPAddressTableTracker.TYPE_IPV6) {
			final String netmask = str(address) + "/" + mask;
			// LogUtils.debugf(this, "getIpAddressNetMask: returning %s", netmask);
			return netmask;
    	} else {
    		LogUtils.warnf(this, "unknown address type, expected 1 (IPv4) or 2 (IPv6), but got %d", addressType);
    		return null;
    	}
    }
    
    /**
     * This is a hack, we get the IP address from the instance information.  :P
     */
    public void storeResult(final SnmpResult result) {
    	// LogUtils.debugf(this, "storeResult: %s", result);

    	final int[] instanceIds = result.getInstance().getIds();
    	final int addressType = instanceIds[1];
		if (addressType == IPAddressTableTracker.TYPE_IPV4 || addressType == IPAddressTableTracker.TYPE_IPV6) {
			m_inetAddress = InetAddressUtils.getInetAddress(instanceIds, 2, addressType);
		} else {
			LogUtils.warnf(this, "Unable to determine IP address type (%d)", addressType);
		}

    	super.storeResult(result);
    }
}
