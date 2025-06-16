/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.enlinkd.snmp;

import org.opennms.netmgt.enlinkd.model.VlanStatus;
import org.opennms.netmgt.enlinkd.model.VlanType;
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

    public static final String CISCO_VTP_VLAN_STATE ="vtpVlanState";
    public static final String CISCO_VTP_VLAN_TYPE ="vtpVlanType";
    public static final String CISCO_VTP_VLAN_NAME ="vtpVlanName";

    public static final SnmpObjId CISCO_VTP_VLAN_STATE_OID = SnmpObjId.get(CISCO_VTP_VLAN_TABLE_ENTRY, "2");
    public static final SnmpObjId CISCO_VTP_VLAN_TYPE_OID = SnmpObjId.get(CISCO_VTP_VLAN_TABLE_ENTRY, "3");
    public static final SnmpObjId CISCO_VTP_VLAN_NAME_OID = SnmpObjId.get(CISCO_VTP_VLAN_TABLE_ENTRY, "4");

    public static SnmpObjId[] cisco_vlan_elemList = new SnmpObjId[]{
            /*
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
            CISCO_VTP_VLAN_STATE_OID,
            /*
             * vtpVlanType OBJECT-TYPE
             * SYNTAX          VlanType
             * MAX-ACCESS      read-only
             * STATUS          current
             * DESCRIPTION
             *	"The type of this VLAN."
             */
            CISCO_VTP_VLAN_TYPE_OID,
            /*
             * vtpVlanName OBJECT-TYPE
             * SYNTAX          DisplayString (SIZE  (1..32))
             * MAX-ACCESS      read-only
             * STATUS          current
             * DESCRIPTION
             *   "The name of this VLAN.  This name is used as the ELAN-name
             *   for an ATM LAN-Emulation segment of this VLAN."
             */
            CISCO_VTP_VLAN_NAME_OID
    };

    public static class CiscoVtpVlanRow extends SnmpRowResult {

        public CiscoVtpVlanRow(int columnCount, SnmpInstId instance) {
            super(columnCount, instance);
        }

        public VlanStatus getVlanStatus() {
            return VlanStatus.get(getValue(CISCO_VTP_VLAN_STATE_OID).toInt());
        }

        public VlanType getVlanType() {
            return VlanType.get(getValue(CISCO_VTP_VLAN_TYPE_OID).toInt());
        }

        public Integer getVlanIndex() {
            return getInstance().getLastSubId();
        }

        public String getVlanName() {
            return getValue(CISCO_VTP_VLAN_NAME_OID).toDisplayString();
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
        System.out.printf("\t\t%s (%s)= %s (%s)\n", CISCO_VTP_VLAN_STATE_OID + "." + row.getVlanIndex(), CISCO_VTP_VLAN_STATE, row.getVlanStatus().getIntCode(), VlanStatus.getVlanStatusString(row.getVlanStatus().getIntCode()));
        System.out.printf("\t\t%s (%s)= %s (%s)\n", CISCO_VTP_VLAN_TYPE_OID + "." + row.getVlanIndex(), CISCO_VTP_VLAN_TYPE, row.getVlanType().getIntCode(), VlanType.getVlanTypeString(row.getVlanType().getIntCode()));
        System.out.printf("\t\t%s (%s)= %s \n", CISCO_VTP_VLAN_NAME_OID + "." + row.getVlanIndex(), CISCO_VTP_VLAN_NAME, row.getVlanName());
    }
}
