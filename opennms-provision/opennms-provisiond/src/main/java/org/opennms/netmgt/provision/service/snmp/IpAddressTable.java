/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service.snmp;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;

/**
 * <P>
 * IpAddressTable uses a SnmpSession to collect the IpAddressTable entries It
 * implements the SnmpHandler to receive notifications when a reply is
 * received/error occurs in the SnmpSession used to send requests /recieve
 * replies.
 * </P>
 *
 * @author <A HREF="mailto:brozow@opennms.org">Matt Brozowski</A>
 * @author <A HREF="mailto:jamesz@opennms.org">James Zuo </A>
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya </A>
 * @author <A HREF="mailto:weave@oculan.com">Weave </A>
 * @see <A HREF="http://www.ietf.org/rfc/rfc1213.txt">RFC1213 </A>
 */
public class IpAddressTable extends SnmpTable<IpAddressTableEntry> {

    protected static final int INSTANCE_TYPE_IPV4 = 1;
    protected static final int INSTANCE_TYPE_IPV6 = 2;

	private final Set<InetAddress> m_addresses;

	/**
     * <P>
     * Constructs an IpAddressTable object that is used to collect the address
     * elements from the remote agent. Once all the elements are collected, or
     * there is an error in the collection the signaler object is <EM>notified
     * </EM> to inform other threads.
     * </P>
     *
     * @param address TODO
     * @see IpAddressTableEntry
     */
    public IpAddressTable(final InetAddress address) {
        super(address, "ipAddressTable", IpAddressTableEntry.ms_elemList);
        m_addresses = Collections.emptySet();
    }

    /**
     * <p>Constructor for IpAddressTable.</p>
     *
     * @param address a {@link java.net.InetAddress} object.
     * @param ipAddresses a {@link java.util.Set} object.
     */
    public IpAddressTable(final InetAddress address, final Set<InetAddress> inetAddresses, final Set<SnmpInstId> ipAddresses) {
        super(address, "IpAddressTable", IpAddressTableEntry.ms_elemList, ipAddresses);
        m_addresses = inetAddresses;
    }

    /** {@inheritDoc} */
    @Override
    protected IpAddressTableEntry createTableEntry(final SnmpObjId base, final SnmpInstId inst, final Object val) {
        return new IpAddressTableEntry();
    }
    
    /**
     * <p>getIfIndices</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<Integer> getIfIndices() {
        Set<Integer> ifIndices = new TreeSet<Integer>();
        for(IpAddressTableEntry entry : getEntries()) {
            Integer ifIndex = entry.getIpAddressIfIndex();
            if (ifIndex != null) {
                ifIndices.add(ifIndex);
            }
        }
        return ifIndices;
    }

    /**
     * <p>getIfAddress</p>
     *
     * @param ifIndex a int.
     * @return a {@link java.net.InetAddress} object.
     */
    public InetAddress getIfAddress(int ifIndex) {
        IpAddressTableEntry entry = getEntryByIfIndex(ifIndex);
        return entry == null ? null : entry.getIpAddress();
    }
    
    /**
     * <p>getNetMask</p>
     *
     * @param ifIndex a int.
     * @return a {@link java.net.InetAddress} object.
     */
    public InetAddress getNetMask(int ifIndex) {
        IpAddressTableEntry entry = getEntryByIfIndex(ifIndex);
        return entry == null ? null : entry.getIpAddressNetMask();
        
    }
    
    /**
     * <p>getNetMask</p>
     *
     * @param address a {@link java.net.InetAddress} object.
     * @return a {@link java.net.InetAddress} object.
     */
    public InetAddress getNetMask(InetAddress address) {
        return getEntry(address) == null ? null : getEntry(address).getIpAddressNetMask();
    }
    
    /**
     * <p>getIfIndex</p>
     *
     * @param address a {@link java.net.InetAddress} object.
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getIfIndex(final InetAddress address) {
        return getEntry(address) == null ? null : getEntry(address).getIpAddressIfIndex();
    }


    /**
     * <p>getEntryByIfIndex</p>
     *
     * @param ifIndex a int.
     * @return a {@link org.opennms.netmgt.provision.service.snmp.IpAddressTableEntry} object.
     */
    public IpAddressTableEntry getEntryByIfIndex(int ifIndex) {
        if (getEntries() == null) {
            return null;
        }
        
        for(IpAddressTableEntry entry : getEntries()) {
            Integer ndx = entry.getIpAddressIfIndex();
            if (ndx != null && ndx.intValue() == ifIndex) {
                return entry;
            }
        }
        return null;
    }
     
    /**
     * <p>getEntry</p>
     *
     * @param address a {@link java.net.InetAddress} object.
     * @return a {@link org.opennms.netmgt.provision.service.snmp.IpAddressTableEntry} object.
     */
    public IpAddressTableEntry getEntry(final InetAddress address) {
        return getEntry(getInstanceForAddress(address));
    }

    /**
     * <p>updateIpInterfaceData</p>
     *
     * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    public void updateIpInterfaceData(final OnmsNode node) {
        for(final IpAddressTableEntry entry : getEntries()) {
            updateIpInterfaceData(node, InetAddressUtils.str(entry.getIpAddress()));
        }
    }

    /**
     * <p>updateIpInterfaceData</p>
     *
     * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
     * @param ipAddr a {@link java.lang.String} object.
     */
    public void updateIpInterfaceData(final OnmsNode node, final String ipAddr) {
    	OnmsIpInterface ipIf = node.getIpInterfaceByIpAddress(ipAddr);
        if (ipIf == null) {
            ipIf = new OnmsIpInterface(ipAddr, node);
        }

        final InetAddress inetAddr = ipIf.getIpAddress();
        final Integer ifIndex = getIfIndex(inetAddr);

        // if we've found an ifIndex for this interface
        if (ifIndex != null) {

            // first look to see if an snmpIf was created already
            OnmsSnmpInterface snmpIf = node.getSnmpInterfaceWithIfIndex(ifIndex);

            if (snmpIf == null) {
                // if not then create one
                snmpIf = new OnmsSnmpInterface(node, ifIndex);
            }

            final InetAddress mask = getNetMask(inetAddr);
            if (mask != null) {
                snmpIf.setNetMask(mask);
            }

            snmpIf.setCollectionEnabled(true);
            
            ipIf.setSnmpInterface(snmpIf);

        }

        ipIf.setIpHostName(ipAddr);
    }

    /**
     * <p>getIpAddresses</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<String> getIpAddresses() {
    	final Set<String> ipAddrs = new LinkedHashSet<String>();
    	for (final InetAddress addr : m_addresses) {
    		ipAddrs.add(InetAddressUtils.str(addr));
    	}
        return ipAddrs;
        
    }

	public static IpAddressTable createTable(final InetAddress address, final Set<InetAddress> ipAddresses) {
		return new IpAddressTable(address, ipAddresses, getInstanceIds(ipAddresses));
	}

    public static Set<SnmpInstId> getInstanceIds(final Set<InetAddress> ipAddresses) {
    	final Set<SnmpInstId> ids = new HashSet<SnmpInstId>();
    	for (final InetAddress addr : ipAddresses) {
    		ids.add(getInstanceForAddress(addr));
    	}
    	return ids;
    }

	public static SnmpInstId getInstanceForAddress(final InetAddress address) {
		final int type;
		if (address instanceof Inet4Address) {
			type = INSTANCE_TYPE_IPV4;
		} else if (address instanceof Inet6Address) {
			type = INSTANCE_TYPE_IPV6;
		} else {
			return null;
		}
		return new SnmpInstId(type + "." + address.getAddress().length + "." + InetAddressUtils.toOid(address));
	}

}
