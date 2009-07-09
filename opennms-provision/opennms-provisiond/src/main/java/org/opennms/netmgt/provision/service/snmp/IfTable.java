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


package org.opennms.netmgt.provision.service.snmp;

import java.net.InetAddress;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;

public final class IfTable extends SnmpTable<IfTableEntry> {
    
    public IfTable(InetAddress address) {
        this(address, null);
    }
    
    public IfTable(InetAddress address, Set<SnmpInstId> ifIndices) {
        super(address, "ifTable", IfTableEntry.ms_elemList, ifIndices);
    }

    protected IfTableEntry createTableEntry(SnmpObjId base, SnmpInstId inst, Object val) {
        return new IfTableEntry();
    }


    protected final Category log() {
        return ThreadCategory.getInstance(IfTable.class);
    }
    
    public Integer getOperStatus(int ifIndex) {
        return (getEntry(ifIndex) == null ? null : getEntry(ifIndex).getIfOperStatus());
    }
        
    public Integer getAdminStatus(int ifIndex) {
        return (getEntry(ifIndex) == null ? null : getEntry(ifIndex).getIfAdminStatus());
    }

    public Integer getIfType(int ifIndex) {
        return (getEntry(ifIndex) == null ? null : getEntry(ifIndex).getIfType());
    }

    public String getIfDescr(final int ifIndex) {
        return (getEntry(ifIndex) == null ? null : getEntry(ifIndex).getIfDescr());
    }

    public Long getIfSpeed(final int ifIndex) {
        return (getEntry(ifIndex) == null ? null : getEntry(ifIndex).getIfSpeed());
    }
    
    public String getPhysAddr(final int ifIndex) {
        return (getEntry(ifIndex) == null ? null : getEntry(ifIndex).getPhysAddr());
    }

    public void updateSnmpInterfaceData(OnmsNode node) {
        for(IfTableEntry entry : getEntries()) {
            updateSnmpInterfaceData(node, entry.getIfIndex());
        }
    }

    public void updateSnmpInterfaceData(OnmsNode node, Integer ifIndex) {
        // first look to see if an snmpIf was created already
        OnmsSnmpInterface snmpIf = node.getSnmpInterfaceWithIfIndex(ifIndex);
        
        if (snmpIf == null) {
            // if not then create one
            snmpIf = new OnmsSnmpInterface(null, ifIndex, node);
        }
        
        updateSnmpInterfaceData(ifIndex, snmpIf);
    }

    private void updateSnmpInterfaceData(Integer ifIndex, OnmsSnmpInterface snmpIf) {
        // IfTable Attributes
        snmpIf.setIfType(getIfType(ifIndex));
        snmpIf.setIfAdminStatus(getAdminStatus(ifIndex));
        snmpIf.setIfDescr(getIfDescr(ifIndex));
        snmpIf.setIfSpeed(getIfSpeed(ifIndex));
        snmpIf.setPhysAddr(getPhysAddr(ifIndex));
    }

    public Set<Integer> getIfIndices() {
        Set<Integer> ifIndices = new LinkedHashSet<Integer>();
        for(SnmpInstId inst : getInstances()) {
            ifIndices.add(inst.toInt());
        }
        return ifIndices;
    }

}
