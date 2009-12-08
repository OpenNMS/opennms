/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision.service;

import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.snmp.RowCallback;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpRowResult;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.TableTracker;

/**
 * PhysInterfaceTableTracker
 *
 * @author brozow
 */
public class PhysInterfaceTableTracker extends TableTracker {
    
    public static final SnmpObjId IF_TABLE_ENTRY = SnmpObjId.get(".1.3.6.1.2.1.2.2.1");
    public static final SnmpObjId IF_INDEX = SnmpObjId.get(IF_TABLE_ENTRY, "1");
    public static final SnmpObjId IF_DESCR = SnmpObjId.get(IF_TABLE_ENTRY, "2");
    public static final SnmpObjId IF_TYPE = SnmpObjId.get(IF_TABLE_ENTRY, "3");
    public static final SnmpObjId IF_MTU = SnmpObjId.get(IF_TABLE_ENTRY, "4");
    public static final SnmpObjId IF_SPEED = SnmpObjId.get(IF_TABLE_ENTRY, "5");
    public static final SnmpObjId IF_PHYS_ADDR = SnmpObjId.get(IF_TABLE_ENTRY, "6");
    public static final SnmpObjId IF_ADMIN_STATUS = SnmpObjId.get(IF_TABLE_ENTRY, "7");
    public static final SnmpObjId IF_OPER_STATUS = SnmpObjId.get(IF_TABLE_ENTRY, "8");
    public static final SnmpObjId IF_LAST_CHANGE = SnmpObjId.get(IF_TABLE_ENTRY, "9");
    
    
    public static final SnmpObjId IF_XTABLE_ENTRY = SnmpObjId.get( ".1.3.6.1.2.1.31.1.1.1");
    public static final SnmpObjId IF_NAME = SnmpObjId.get(IF_XTABLE_ENTRY, "1");
    public static final SnmpObjId IF_IN_MCAST_PKTS = SnmpObjId.get(IF_XTABLE_ENTRY, "2");
    public static final SnmpObjId IF_IN_BCAST_PKTS = SnmpObjId.get(IF_XTABLE_ENTRY, "3");
    public static final SnmpObjId IF_OUT_MCAST_PKTS = SnmpObjId.get(IF_XTABLE_ENTRY, "4");
    public static final SnmpObjId IF_LINK_UP_DOWN_TRAP_ENABLE = SnmpObjId.get(IF_XTABLE_ENTRY, "14");
    public static final SnmpObjId IF_HIGH_SPEED = SnmpObjId.get(IF_XTABLE_ENTRY, "15");
    public static final SnmpObjId IF_PROMISCUOUS_MODE = SnmpObjId.get(IF_XTABLE_ENTRY, "16");
    public static final SnmpObjId IF_CONNECTOR_PRESENT = SnmpObjId.get(IF_XTABLE_ENTRY, "17");
    public static final SnmpObjId IF_ALIAS = SnmpObjId.get(IF_XTABLE_ENTRY, "18");
    public static final SnmpObjId IF_COUNTER_DISCONTINUITY_TIME = SnmpObjId.get(IF_XTABLE_ENTRY, "19");
    
    private static SnmpObjId[] s_tableColumns = new SnmpObjId[] {
        IF_INDEX,
        IF_DESCR,
        IF_TYPE,
        IF_MTU,
        IF_SPEED,
        IF_PHYS_ADDR,
        IF_ADMIN_STATUS,
        IF_OPER_STATUS,
        IF_LAST_CHANGE,
        IF_NAME,
        IF_ALIAS,
        IF_HIGH_SPEED
    };
    
    class PhysicalInterfaceRow extends SnmpRowResult {

        public PhysicalInterfaceRow(int columnCount, SnmpInstId instance) {
            super(columnCount, instance);
        }
        
        public Integer getIfIndex() {
            SnmpValue value = getValue(IF_INDEX);
            if (value != null) {
                return value.toInt();
            } else {
                // ifIndex is the instance id as well
                SnmpInstId inst = getInstance();
                if (inst != null && inst.length() == 1) {
                    return inst.toInt();
                }
            }
            return null;
        }
        
        private Integer getIfType() {
            SnmpValue value = getValue(IF_TYPE);
            return value == null ? null : value.toInt();

        }

        private Long getIfSpeed() {
            SnmpValue value = getValue(IF_SPEED);
            return value == null ? null : value.toLong();
        }
        
        private Long getIfHighSpeed() {
            SnmpValue value = getValue(IF_HIGH_SPEED);
            return value == null ? null : value.toLong()*1000000L;
        }
        
        private Long getSpeed() {
            Long highSpeed = getIfHighSpeed();
            return highSpeed != null && highSpeed > 0 ? highSpeed : getIfSpeed(); 
        }

        private Integer getIfOperStatus() {
            SnmpValue value = getValue(IF_OPER_STATUS);
            return value == null ? null : value.toInt();
        }

        private String getIfName() {
            SnmpValue value = getValue(IF_NAME);
            return value == null ? null : value.toDisplayString();
        }

        private String getIfDescr() {
            SnmpValue value = getValue(IF_DESCR);
            return value == null ? null : value.toDisplayString();
        }

        private String getIfAlias() {
            SnmpValue value = getValue(IF_ALIAS);
            return value == null ? null : value.toDisplayString();
        }

        private Integer getIfAdminStatus() {
            SnmpValue value = getValue(IF_ADMIN_STATUS);
            return value == null ? null : value.toInt();
        }
        
        public OnmsSnmpInterface createInterfaceFromRow() {
            OnmsSnmpInterface snmpIface = new OnmsSnmpInterface("0.0.0.0", getIfIndex(), null);
            snmpIface.setIfAdminStatus(getIfAdminStatus());
            snmpIface.setIfAlias(getIfAlias());
            snmpIface.setIfDescr(getIfDescr());
            snmpIface.setIfName(getIfName());
            snmpIface.setIfOperStatus(getIfOperStatus());
            snmpIface.setIfSpeed(getSpeed());
            snmpIface.setIfType(getIfType());
            return snmpIface;
        }

    }
    
    public PhysInterfaceTableTracker() {
        super(s_tableColumns);
    }

    public PhysInterfaceTableTracker(RowCallback rowProcessor) {
        super(rowProcessor, s_tableColumns);
    }
    
    @Override
    public SnmpRowResult createRowResult(int columnCount, SnmpInstId instance) {
        return new PhysicalInterfaceRow(columnCount, instance);
    }

    @Override
    public void rowCompleted(SnmpRowResult row) {
        processPhysicalInterfaceRow((PhysicalInterfaceRow)row);
    }

    public void processPhysicalInterfaceRow(PhysicalInterfaceRow row) {
        
    }

}
