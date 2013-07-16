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

package org.opennms.netmgt.capsd.snmp;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <P>
 * IpAddrTable uses a SnmpSession to collect the ipAddrTable entries It
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
public class IpAddrTable extends SnmpTable<IpAddrTableEntry> {
    
    
    private static final Logger LOG = LoggerFactory.getLogger(IpAddrTable.class);

    /**
     * <P>
     * Constructs an IpAddrTable object that is used to collect the address
     * elements from the remote agent. Once all the elements are collected, or
     * there is an error in the collection the signaler object is <EM>notified
     * </EM> to inform other threads.
     * </P>
     *
     * @param address TODO
     * @see IpAddrTableEntry
     */
    public IpAddrTable(InetAddress address) {
        super(address, "ipAddrTable", IpAddrTableEntry.ms_elemList);
    }

    /** {@inheritDoc} */
    @Override
    protected IpAddrTableEntry createTableEntry(SnmpObjId base, SnmpInstId inst, Object val) {
        return new IpAddrTableEntry();
    }

     /**
      * <p>getIfAddressAndMask</p>
      *
      * @param ifIndex a int.
      * @return an array of {@link java.net.InetAddress} objects.
      */
     public InetAddress[] getIfAddressAndMask(int ifIndex) {
        for(IpAddrTableEntry entry : this) {

            Integer ndx = entry.getIpAdEntIfIndex();
            if (ndx != null && ndx.intValue() == ifIndex) {
                // found it
                // extract the address
                //
                InetAddress[] pair = new InetAddress[2];
                pair[0] = entry.getIpAdEntAddr();
                pair[1] = entry.getIpAdEntNetMask();
                return pair;
            }
        }
        return null;
    }

    /**
     * <p>getIfIndex</p>
     *
     * @param address a {@link java.net.InetAddress} object.
     * @return a int.
     */
    public int getIfIndex(InetAddress address) {
        LOG.debug("getIfIndex: num ipAddrTable entries: {}", this.size());

        for(IpAddrTableEntry entry : this) {

            InetAddress ifAddr = entry.getIpAdEntAddr();
            if (ifAddr != null && ifAddr.equals(address)) {
                // found it
                // extract the ifIndex
                //
                Integer ndx = entry.getIpAdEntIfIndex();
                LOG.debug("getIfIndex: got a match for address {} index: {}", InetAddressUtils.str(address), ndx);
                if (ndx != null)
                    return ndx.intValue();
            }
        }
        LOG.debug("getIfIndex: no matching ipAddrTable entry for {}", InetAddressUtils.str(address));
        return -1;
    }

    /**
     * Returns all Internet addresses at the corresponding index. If the address
     * cannot be resolved then a null reference is returned.
     *
     * @return list of InetAddress objects representing each of the interfaces
     *         IP addresses.
     * @param index a int.
     */
    public List<InetAddress> getIpAddresses(int index) {
        if (index == -1) {
            return null;
        }
        
        List<InetAddress> addresses = new ArrayList<InetAddress>();
        
        for(IpAddrTableEntry entry : this) {

            Integer ndx = entry.getIpAdEntIfIndex();
            if (ndx != null && ndx.intValue() == index) {
                
                InetAddress ifAddr = entry.getIpAdEntAddr();
                if (ifAddr != null) {
                    addresses.add(ifAddr);
                }
            }
        }
        return addresses;
    }

    /**
     * Returns all Internet addresses in the ipAddrEntry list. If the address
     * cannot be resolved then a null reference is returned.
     *
     * @return list of InetAddress objects representing each of the interfaces
     *         IP addresses.
     */
    public List<InetAddress> getIpAddresses() {
        List <InetAddress>addresses = new ArrayList<InetAddress>();
        
        for(IpAddrTableEntry entry : this) {

            Integer ndx = entry.getIpAdEntIfIndex();
            if (ndx != null) {
        
                InetAddress ifAddr = entry.getIpAdEntAddr();
                if (ifAddr != null) {
                    addresses.add(ifAddr);
                }
        
            }
        }
        return addresses;
    }
}
