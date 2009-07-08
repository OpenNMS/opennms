/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2002-2008 The OpenNMS Group, Inc.  All rights reserved.
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


package org.opennms.netmgt.capsd.snmp;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;

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
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 * @see <A HREF="http://www.ietf.org/rfc/rfc1213.txt">RFC1213 </A>
 */
public class IpAddrTable extends SnmpTable<IpAddrTableEntry> {

    /**
     * <P>
     * Constructs an IpAddrTable object that is used to collect the address
     * elements from the remote agent. Once all the elements are collected, or
     * there is an error in the collection the signaler object is <EM>notified
     * </EM> to inform other threads.
     * </P>
     * @param address TODO
     * @see IpAddrTableEntry
     */
    public IpAddrTable(InetAddress address) {
        super(address, "ipAddrTable", IpAddrTableEntry.ms_elemList);
    }

    protected IpAddrTableEntry createTableEntry(SnmpObjId base, SnmpInstId inst, Object val) {
        return new IpAddrTableEntry();
    }

     public InetAddress[] getIfAddressAndMask(int ifIndex) {
        if (getEntries() == null)
            return null;
        
        for(IpAddrTableEntry entry : getEntries()) {

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

    public int getIfIndex(InetAddress address) {
        if (getEntries() == null) {
            return -1;
        }
        if (log().isDebugEnabled())
            log().debug("getIfIndex: num ipAddrTable entries: " + getEntries().size());

        for(IpAddrTableEntry entry : getEntries()) {

            InetAddress ifAddr = entry.getIpAdEntAddr();
            if (ifAddr != null && ifAddr.equals(address)) {
                // found it
                // extract the ifIndex
                //
                Integer ndx = entry.getIpAdEntIfIndex();
                log().debug("getIfIndex: got a match for address " + address.getHostAddress() + " index: " + ndx);
                if (ndx != null)
                    return ndx.intValue();
            }
        }
        log().debug("getIfIndex: no matching ipAddrTable entry for " + address.getHostAddress());
        return -1;
    }

    protected final Category log() {
        return ThreadCategory.getInstance(IpAddrTable.class);
    }

    /**
     * Returns all Internet addresses at the corresponding index. If the address
     * cannot be resolved then a null reference is returned.
     * 
     * @param ifIndex
     *            The index to search for.
     * 
     * @return list of InetAddress objects representing each of the interfaces
     *         IP addresses.
     */
    
    public List<InetAddress> getIpAddresses(int index) {
        if (index == -1 || getEntries() == null) {
            return null;
        }
        
        List<InetAddress> addresses = new ArrayList<InetAddress>();
        
        for(IpAddrTableEntry entry : getEntries()) {

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
     * @param ipAddrEntries
     *            List of IpAddrTableEntry objects to search
     * 
     * @return list of InetAddress objects representing each of the interfaces
     *         IP addresses.
     */
    public List<InetAddress> getIpAddresses() {
        if (getEntries() == null) {
            return null;
        }
        
        List <InetAddress>addresses = new ArrayList<InetAddress>();
        
        for(IpAddrTableEntry entry : getEntries()) {

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
