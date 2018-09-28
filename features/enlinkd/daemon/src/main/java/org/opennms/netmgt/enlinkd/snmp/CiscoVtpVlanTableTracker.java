/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.enlinkd.snmp;

import org.opennms.netmgt.model.VlanStatus;
import org.opennms.netmgt.model.VlanType;
import org.opennms.netmgt.snmp.RowCallback;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpRowResult;
import org.opennms.netmgt.snmp.TableTracker;

public class CiscoVtpVlanTableTracker extends TableTracker {


    /**
     * <P>The CISCO_VTP_VLAN_TABLE_ENTRY is the object identifier that represents
     * the root of the table vtpVlanTable in the MIB forest.</P>
     */
    public static final SnmpObjId CISCO_VTP_VLAN_TABLE_ENTRY = SnmpObjId.get(".1.3.6.1.4.1.9.9.46.1.3.1.1");

    public static final SnmpObjId CISCO_VTP_VLAN_STATE = SnmpObjId.get(CISCO_VTP_VLAN_TABLE_ENTRY, "2");
    public static final SnmpObjId CISCO_VTP_VLAN_TYPE = SnmpObjId.get(CISCO_VTP_VLAN_TABLE_ENTRY, "3");
    public static final SnmpObjId CISCO_VTP_VLAN_NAME = SnmpObjId.get(CISCO_VTP_VLAN_TABLE_ENTRY, "4");

    public static SnmpObjId[] cisco_vlan_elemList = new SnmpObjId[]{
            /**
             * vtpVlanState OBJECT-TYPE
             * SYNTAX          INTEGER  {
             *               operational(1),
             *               suspended(2),
             *               mtuTooBigForDevice(3),
             *               mtuTooBigForTrunk(4)
             *           }
             *     MAX-ACCESS      read-only
             *     STATUS          current
             *     DESCRIPTION
             * "The state of this VLAN.
             *
             * The state 'mtuTooBigForDevice' indicates that this device
             * cannot participate in this VLAN because the VLAN's MTU is
             * larger than the device can support.
             *
             * The state 'mtuTooBigForTrunk' indicates that while this
             * VLAN's MTU is supported by this device, it is too large for
             * one or more of the device's trunk ports."
             *
             */
            CISCO_VTP_VLAN_STATE,
            /**
             * SYNTAX          VlanType
             * MAX-ACCESS      read-only
             * STATUS          current
             * DESCRIPTION
             *	"The type of this VLAN."
             */
            CISCO_VTP_VLAN_TYPE,
            CISCO_VTP_VLAN_NAME
    };

    public static class CiscoVtpVlanRow extends SnmpRowResult {

        public CiscoVtpVlanRow(int columnCount, SnmpInstId instance) {
            super(columnCount, instance);
        }

        public VlanStatus getVlanStatus() {
            return VlanStatus.get(getValue(CISCO_VTP_VLAN_STATE).toInt());
        }

        public VlanType getVlanType() {
            return VlanType.get(getValue(CISCO_VTP_VLAN_TYPE).toInt());
        }

        public Integer getVlanIndex() {
            return getInstance().getLastSubId();
        }

        public String getVlanName() {
            return getValue(CISCO_VTP_VLAN_NAME).toDisplayString();
        }

        public boolean isStatusOperational() {
            return getVlanStatus().equals(VlanStatus.CISCOVTP_OPERATIONAL);
        }

        public boolean isTypeEthernet() {
            return getVlanType().equals(VlanType.CISCO_VTP_ETHERNET);
        }
    }

    /**
     * <p>Constructor for CiscoVtpVlanTableTracker.</p>
     */
    public CiscoVtpVlanTableTracker() {
        super(cisco_vlan_elemList);
    }

    public CiscoVtpVlanTableTracker(RowCallback rowProcessor) {
        super(rowProcessor, cisco_vlan_elemList);
    }

    /** {@inheritDoc} */
    @Override
    public SnmpRowResult createRowResult(final int columnCount, final SnmpInstId instance) {
        return new CiscoVtpVlanRow(columnCount, instance);
    }

    /** {@inheritDoc} */
    @Override
    public void rowCompleted(final SnmpRowResult row) {
        processCiscoVtpVlanRow((CiscoVtpVlanRow) row);
    }

    /**
     * <p>processCiscoVtpVlanRow</p>
     *
     * @param row a {@link org.opennms.netmgt.enlinkd.snmp.CiscoVtpVlanTableTracker.CiscoVtpVlanRow} object.
     */
    public void processCiscoVtpVlanRow(final CiscoVtpVlanRow row) {
    }
}
