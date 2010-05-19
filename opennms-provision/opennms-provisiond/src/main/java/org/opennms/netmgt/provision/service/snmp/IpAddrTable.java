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
// 2003 Sep 29: Modifications to allow for OpenNMS to handle duplicate IP Addresses.
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
//

package org.opennms.netmgt.provision.service.snmp;

import java.net.InetAddress;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
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

    public IpAddrTable(InetAddress address, Set<SnmpInstId> ipAddrs) {
        super(address, "ipAddrTable", IpAddrTableEntry.ms_elemList, ipAddrs);
    }

    protected IpAddrTableEntry createTableEntry(SnmpObjId base, SnmpInstId inst, Object val) {
        return new IpAddrTableEntry();
    }
    
    public Set<Integer> getIfIndices() {
        Set<Integer> ifIndices = new TreeSet<Integer>();
        for(IpAddrTableEntry entry : getEntries()) {
            Integer ifIndex = entry.getIpAdEntIfIndex();
            if (ifIndex != null) {
                ifIndices.add(ifIndex);
            }
        }
        return ifIndices;
    }

    public InetAddress[] getIfAddressAndMask(int ifIndex) {
        IpAddrTableEntry entry = getEntryByIfIndex(ifIndex);
        return entry == null ? null : new InetAddress[] { entry.getIpAdEntAddr(), entry.getIpAdEntNetMask() };
    }
    
    public InetAddress getIfAddress(int ifIndex) {
        IpAddrTableEntry entry = getEntryByIfIndex(ifIndex);
        return entry == null ? null : entry.getIpAdEntAddr();
    }
    
    public InetAddress getNetMask(int ifIndex) {
        IpAddrTableEntry entry = getEntryByIfIndex(ifIndex);
        return entry == null ? null : entry.getIpAdEntNetMask();
        
    }
    
    public InetAddress getNetMask(InetAddress address) {
        return getEntry(address) == null ? null : getEntry(address).getIpAdEntNetMask();
    }
    
    public Integer getIfIndex(InetAddress address) {
        return getEntry(address) == null ? null : getEntry(address).getIpAdEntIfIndex();
    }


    public IpAddrTableEntry getEntryByIfIndex(int ifIndex) {
        if (getEntries() == null) {
            return null;
        }
        
        for(IpAddrTableEntry entry : getEntries()) {
            Integer ndx = entry.getIpAdEntIfIndex();
            if (ndx != null && ndx.intValue() == ifIndex) {
                return entry;
            }
        }
        return null;
    }
     
    public IpAddrTableEntry getEntry(InetAddress address) {
        return getEntry(new SnmpInstId(address.getHostAddress()));
    }

    protected final ThreadCategory log() {
        return ThreadCategory.getInstance(IpAddrTable.class);
    }

    public void updateIpInterfaceData(OnmsNode node) {
        for(IpAddrTableEntry entry : getEntries()) {
            updateIpInterfaceData(node, entry.getIpAdEntAddr().getHostAddress());
        }
    }

        /**
     * @param node
     * @param ipAddr
     */
    public void updateIpInterfaceData(OnmsNode node, String ipAddr) {
        OnmsIpInterface ipIf = node.getIpInterfaceByIpAddress(ipAddr);
        if (ipIf == null) {
            ipIf = new OnmsIpInterface(ipAddr, node);
        }

        InetAddress inetAddr = ipIf.getInetAddress();
        Integer ifIndex = getIfIndex(inetAddr);

        // if we've found an ifIndex for this interface
        if (ifIndex != null) {

            // first look to see if an snmpIf was created already
            OnmsSnmpInterface snmpIf = node.getSnmpInterfaceWithIfIndex(ifIndex);

            if (snmpIf == null) {
                // if not then create one
                snmpIf = new OnmsSnmpInterface(ipAddr, ifIndex, node);
            }

            // make sure the snmpIf has the ipAddr of the primary interface
            snmpIf.setIpAddress(ipAddr);
            InetAddress mask = getNetMask(inetAddr);
            if (mask != null) {
                snmpIf.setNetMask(mask.getHostAddress());
            }

            snmpIf.setCollectionEnabled(true);
            
            ipIf.setSnmpInterface(snmpIf);

        }

        ipIf.setIpHostName(ipAddr);
    }

    /**
     * 
     */
    public Set<String> getIpAddresses() {
        Set<String> ipAddrs = new LinkedHashSet<String>();
        for(SnmpInstId inst : getInstances()) {
            ipAddrs.add(inst.toString());
        }
        return ipAddrs;
        
    }

}
