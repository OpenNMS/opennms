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
//

package org.opennms.netmgt.capsd.snmp;

import java.net.InetAddress;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;

/**
 * <P>
 * The IfTable uses a SnmpSession to collect the entries in the remote agent's
 * interface table. It implements the SnmpHandler to receive notifications and
 * handle errors associated with the data collection. Data is collected using a
 * series of GETNEXT PDU request to walk multiple parts of the interface table
 * at once. The number of SNMP packets should not exceed the number of interface +
 * 1, assuming no lost packets or error conditions occur.
 * </P>
 * 
 * <p>
 * <em>Addition by Jon Whetzel</em>
 * </p>
 * <p>
 * IfTable has an extra class variable for the SNMP version setting. If this is
 * set for SNMPv2, then a GETBULK command will be used for retrieving the
 * necessary data. Otherwise, the method will resort to its previous
 * implementation with GETNEXT commands.
 * </p>
 * 
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya </A>
 * @author <A HREF="mailto:weave@oculan.com">Weave </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 * @see <A HREF="http://www.ietf.org/rfc/rfc1213.txt">RFC1213 </A>
 */
public final class IfTable extends SnmpTable<IfTableEntry> {
    

    /**
     * <P>
     * Constructs an IfTable object that is used to collect the interface
     * elements from the remote agent. Once all the interfaces are collected, or
     * there is an error in the collection the signaler object is <EM>notified
     * </EM> to inform other threads.
     * </P>
     * @param address TODO
     * @see IfTableEntry
     */
    public IfTable(InetAddress address) {
        super(address, "ifTable", IfTableEntry.ms_elemList);
    }
    
    protected IfTableEntry createTableEntry(SnmpObjId base, SnmpInstId inst, Object val) {
        return new IfTableEntry();
    }


    protected final Category log() {
        return ThreadCategory.getInstance(IfTable.class);
    }
    
    public IfTableEntry getEntry(int ifIndex) {
        if (getEntries() == null) {
            return null;
        }
        
        for(IfTableEntry entry : getEntries()) {
            Integer ndx = entry.getIfIndex();
            if (ndx != null && ndx.intValue() == ifIndex) {
                return entry;
            }
        }
        
        return null;
    }
    
    public int getOperStatus(int ifIndex) {
        if (getEntries() == null)
            return -1;
        
        for(IfTableEntry entry : getEntries()) {
            Integer ndx = entry.getIfIndex();
            if (ndx != null && ndx.intValue() == ifIndex) {
                // found it
                // extract the admin status
                //
                Integer ifStatus = entry.getIfOperStatus();
                if (ifStatus != null)
                    return ifStatus.intValue();
            }
        }
        return -1;
    }
        
    public int getAdminStatus(int ifIndex) {
        if (getEntries() == null)
            return -1;
        
        for(IfTableEntry entry : getEntries()) {
            Integer ndx = entry.getIfIndex();
            if (ndx != null && ndx.intValue() == ifIndex) {
                // found it
                // extract the admin status
                //
                Integer ifStatus = entry.getIfAdminStatus();
                if (ifStatus != null)
                    return ifStatus.intValue();
            }
        }
        return -1;
    }

    public int getIfType(int ifIndex) {
        if (getEntries() == null)
            return -1;
        
        for(IfTableEntry entry : getEntries()) {
            Integer ndx = entry.getIfIndex();
            if (ndx != null && ndx.intValue() == ifIndex) {
                // found it
                // extract the ifType
                //
                Integer ifType = entry.getIfType();
                if (ifType != null)
                    return ifType.intValue();
            }
        }
        return -1;
    }

    public String getIfDescr(final int ifIndex) {
        String ifDescr = null;   
        if (getEntries() != null) {
            for(IfTableEntry entry : getEntries()) {
                Integer ndx = entry.getIfIndex();
                if (ndx != null && ndx.intValue() == ifIndex) {
                    ifDescr = entry.getIfDescr();
                }
            }
        }
        return ifDescr;
    }

    public Long getIfSpeed(final int ifIndex) {
        Long ifSpeed = null;   
        if (getEntries() != null) {
            for(IfTableEntry entry : getEntries()) {
                Integer ndx = entry.getIfIndex();
                if (ndx != null && ndx.intValue() == ifIndex) {
                    ifSpeed = entry.getIfSpeed();
                }
            }
        }
        return ifSpeed;
    }
    
    public String getPhysAddr(final int ifIndex) {
        String physAddr = null;   
        if (getEntries() != null) {
            for(IfTableEntry entry : getEntries()) {
                Integer ndx = entry.getIfIndex();
                if (ndx != null && ndx.intValue() == ifIndex) {
                    physAddr = entry.getPhysAddr();
                }
            }
        }
        return physAddr;
    }

}
