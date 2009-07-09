/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2002-2009 The OpenNMS Group, Inc.  All rights reserved.
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
import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;

public final class IfTable extends SnmpTable<IfTableEntry> {
    
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
