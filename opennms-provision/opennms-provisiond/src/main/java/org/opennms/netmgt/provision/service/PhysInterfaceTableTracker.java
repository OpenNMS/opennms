/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service;

import org.opennms.core.utils.InetAddressUtils;

import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.snmp.RowCallback;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpRowResult;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.TableTracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PhysInterfaceTableTracker
 *
 * @author brozow
 * @version $Id: $
 */
public class PhysInterfaceTableTracker extends TableTracker {
    private static final Logger LOG = LoggerFactory.getLogger(PhysInterfaceTableTracker.class);
 
    /** Constant <code>IF_TABLE_ENTRY</code> */
    public static final SnmpObjId IF_TABLE_ENTRY = SnmpObjId.get(".1.3.6.1.2.1.2.2.1");
    /** Constant <code>IF_INDEX</code> */
    public static final SnmpObjId IF_INDEX = SnmpObjId.get(IF_TABLE_ENTRY, "1");
    /** Constant <code>IF_DESCR</code> */
    public static final SnmpObjId IF_DESCR = SnmpObjId.get(IF_TABLE_ENTRY, "2");
    /** Constant <code>IF_TYPE</code> */
    public static final SnmpObjId IF_TYPE = SnmpObjId.get(IF_TABLE_ENTRY, "3");
    /** Constant <code>IF_MTU</code> */
    public static final SnmpObjId IF_MTU = SnmpObjId.get(IF_TABLE_ENTRY, "4");
    /** Constant <code>IF_SPEED</code> */
    public static final SnmpObjId IF_SPEED = SnmpObjId.get(IF_TABLE_ENTRY, "5");
    /** Constant <code>IF_PHYS_ADDR</code> */
    public static final SnmpObjId IF_PHYS_ADDR = SnmpObjId.get(IF_TABLE_ENTRY, "6");
    /** Constant <code>IF_ADMIN_STATUS</code> */
    public static final SnmpObjId IF_ADMIN_STATUS = SnmpObjId.get(IF_TABLE_ENTRY, "7");
    /** Constant <code>IF_OPER_STATUS</code> */
    public static final SnmpObjId IF_OPER_STATUS = SnmpObjId.get(IF_TABLE_ENTRY, "8");
    /** Constant <code>IF_LAST_CHANGE</code> */
    public static final SnmpObjId IF_LAST_CHANGE = SnmpObjId.get(IF_TABLE_ENTRY, "9");
    
    
    /** Constant <code>IF_XTABLE_ENTRY</code> */
    public static final SnmpObjId IF_XTABLE_ENTRY = SnmpObjId.get( ".1.3.6.1.2.1.31.1.1.1");
    /** Constant <code>IF_NAME</code> */
    public static final SnmpObjId IF_NAME = SnmpObjId.get(IF_XTABLE_ENTRY, "1");
    /** Constant <code>IF_IN_MCAST_PKTS</code> */
    public static final SnmpObjId IF_IN_MCAST_PKTS = SnmpObjId.get(IF_XTABLE_ENTRY, "2");
    /** Constant <code>IF_IN_BCAST_PKTS</code> */
    public static final SnmpObjId IF_IN_BCAST_PKTS = SnmpObjId.get(IF_XTABLE_ENTRY, "3");
    /** Constant <code>IF_OUT_MCAST_PKTS</code> */
    public static final SnmpObjId IF_OUT_MCAST_PKTS = SnmpObjId.get(IF_XTABLE_ENTRY, "4");
    /** Constant <code>IF_LINK_UP_DOWN_TRAP_ENABLE</code> */
    public static final SnmpObjId IF_LINK_UP_DOWN_TRAP_ENABLE = SnmpObjId.get(IF_XTABLE_ENTRY, "14");
    /** Constant <code>IF_HIGH_SPEED</code> */
    public static final SnmpObjId IF_HIGH_SPEED = SnmpObjId.get(IF_XTABLE_ENTRY, "15");
    /** Constant <code>IF_PROMISCUOUS_MODE</code> */
    public static final SnmpObjId IF_PROMISCUOUS_MODE = SnmpObjId.get(IF_XTABLE_ENTRY, "16");
    /** Constant <code>IF_CONNECTOR_PRESENT</code> */
    public static final SnmpObjId IF_CONNECTOR_PRESENT = SnmpObjId.get(IF_XTABLE_ENTRY, "17");
    /** Constant <code>IF_ALIAS</code> */
    public static final SnmpObjId IF_ALIAS = SnmpObjId.get(IF_XTABLE_ENTRY, "18");
    /** Constant <code>IF_COUNTER_DISCONTINUITY_TIME</code> */
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
    
    static class PhysicalInterfaceRow extends SnmpRowResult {

        public PhysicalInterfaceRow(final int columnCount, final SnmpInstId instance) {
            super(columnCount, instance);
        }
        
        public Integer getIfIndex() {
        	final SnmpValue value = getValue(IF_INDEX);
            if (value != null) {
                return value.toInt();
            } else {
            	// ifIndex is the instance id as well
                final SnmpInstId inst = getInstance();
                if (inst != null && inst.length() == 1) {
                    return inst.toInt();
                }
            }
            return null;
        }
        
        private Integer getIfType() {
            final SnmpValue value = getValue(IF_TYPE);
            return value == null ? null : value.toInt();

        }

        private Long getIfSpeed() {
            final SnmpValue value = getValue(IF_SPEED);
            return value == null ? null : value.toLong();
        }
        
        private Long getIfHighSpeed() {
            final SnmpValue value = getValue(IF_HIGH_SPEED);
            return value == null ? null : value.toLong();
        }
        
        private Long getSpeed() {
            final Long highSpeed = getIfHighSpeed();
            final Long ifSpeed = getIfSpeed();
            if (highSpeed != null && highSpeed > 4294L) {
                return highSpeed * 1000000L;
            }
            if (ifSpeed == null) {
                if (highSpeed != null && highSpeed > 0) {
                    LOG.warn("the ifSpeed for ifIndex {} is null, which is a violation of the standard (this seems to be related to a broken SNMP agent). But, the ifHighSpeed is {}, so that value will be used instead.", getIfIndex(), highSpeed);
                    return highSpeed * 1000000L;
                } else {
                    LOG.warn("the ifSpeed for ifIndex {} is null, which is a violation of the standard (this seems to be related to a broken SNMP agent). Returning 0 instead", getIfIndex());
                    return 0L;
                }
            }
            return ifSpeed;
        }

        private Integer getIfOperStatus() {
            final SnmpValue value = getValue(IF_OPER_STATUS);
            return value == null ? null : value.toInt();
        }

        private String getIfName() {
            final SnmpValue value = getValue(IF_NAME);
            return value == null ? null : value.toDisplayString();
        }

        private String getIfDescr() {
            final SnmpValue value = getValue(IF_DESCR);
            return value == null ? null : value.toDisplayString();
        }

        private String getIfAlias() {
            final SnmpValue value = getValue(IF_ALIAS);
            return value == null ? null : value.toDisplayString();
        }

        private Integer getIfAdminStatus() {
            final SnmpValue value = getValue(IF_ADMIN_STATUS);
            return value == null ? null : value.toInt();
        }
        
        private String getPhysAddr() {
            final SnmpValue value = getValue(IF_PHYS_ADDR);
            if (value == null || value.isError()) {
                return null;
            }
            String hexString = value == null ? null : value.toHexString();
            String displayString = value == null ? null : value.toDisplayString();
            // See ifTableEntry: NMS-4902 (revision cee964fe979e6465aeb4e2efd4772e50ebc54831)
            try {
                if (hexString != null && hexString.length() == 12) {
                    // If the hex string is 12 characters long, than the agent is kinda weird and
                    // is returning the value as a raw binary value that is 6 bytes in length.
                    // But that's OK, as long as we can convert it into a string, that's fine. 
                    return hexString;
                } else {
                    // This is the normal case that most agents conform to: the value is an ASCII 
                    // string representing the colon-separated MAC address. We just need to reformat 
                    // it to remove the colons and convert it into a 12-character string.
                    return displayString == null || displayString.trim().isEmpty() ? null : InetAddressUtils.normalizeMacAddress(displayString);
                }
            } catch (IllegalArgumentException e) {
                LOG.warn(e.getMessage(), e);
                return displayString;
            }
        }
        
        public OnmsSnmpInterface createInterfaceFromRow() {
            final OnmsSnmpInterface snmpIface = new OnmsSnmpInterface(null, getIfIndex());
            snmpIface.setIfAdminStatus(getIfAdminStatus());
            snmpIface.setIfAlias(getIfAlias());
            snmpIface.setIfDescr(getIfDescr());
            snmpIface.setIfName(getIfName());
            snmpIface.setIfOperStatus(getIfOperStatus());
            snmpIface.setIfSpeed(getSpeed());
            snmpIface.setIfType(getIfType());
            snmpIface.setPhysAddr(getPhysAddr());
            snmpIface.setPoll("N");
            return snmpIface;
        }

    }
    
    /**
     * <p>Constructor for PhysInterfaceTableTracker.</p>
     */
    public PhysInterfaceTableTracker() {
        super(s_tableColumns);
    }

    /**
     * <p>Constructor for PhysInterfaceTableTracker.</p>
     *
     * @param rowProcessor a {@link org.opennms.netmgt.snmp.RowCallback} object.
     */
    public PhysInterfaceTableTracker(final RowCallback rowProcessor) {
        super(rowProcessor, s_tableColumns);
    }
    
    /** {@inheritDoc} */
    @Override
    public SnmpRowResult createRowResult(final int columnCount, final SnmpInstId instance) {
        return new PhysicalInterfaceRow(columnCount, instance);
    }

    /** {@inheritDoc} */
    @Override
    public void rowCompleted(final SnmpRowResult row) {
        processPhysicalInterfaceRow((PhysicalInterfaceRow)row);
    }

    /**
     * <p>processPhysicalInterfaceRow</p>
     *
     * @param row a {@link org.opennms.netmgt.provision.service.PhysInterfaceTableTracker.PhysicalInterfaceRow} object.
     */
    public void processPhysicalInterfaceRow(final PhysicalInterfaceRow row) {
        
    }

}
