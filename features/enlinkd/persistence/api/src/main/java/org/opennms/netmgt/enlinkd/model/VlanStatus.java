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
package org.opennms.netmgt.enlinkd.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class VlanStatus implements Comparable<VlanStatus>, Serializable {

    private static final long serialVersionUID = -5676188320482765289L;
    /**
     * <p>String identifiers for the enumeration of values:</p>
     */
    public static final int VLAN_STATUS_UNKNOWN = 0;
    /** Constant <code>CISCOVTP_VLAN_STATUS_OPERATIONAL=1</code> */
    public static final int CISCOVTP_VLAN_STATUS_OPERATIONAL = 1;
    /** Constant <code>CISCOVTP_VLAN_STATUS_SUSPENDED=2</code> */
    public static final int CISCOVTP_VLAN_STATUS_SUSPENDED = 2;
    /** Constant <code>CISCOVTP_VLAN_STATUS_mtuTooBigForDevice=3</code> */
    public static final int CISCOVTP_VLAN_STATUS_mtuTooBigForDevice = 3;
    /** Constant <code>CISCOVTP_VLAN_STATUS_mtuTooBigForTrunk=4</code> */
    public static final int CISCOVTP_VLAN_STATUS_mtuTooBigForTrunk = 4;

    public static final int SNMPV2C_ROWSTATUS_ACTIVE = 5;
    public static final int SNMPV2C_ROWSTATUS_NOTINSERVICE = 6;
    public static final int SNMPV2C_ROWSTATUS_NOTREADY = 7;
    public static final int SNMPV2C_ROWSTATUS_CREATEANDGO = 8;
    public static final int SNMPV2C_ROWSTATUS_CREATEANDWAIT = 9;
    public static final int SNMPV2C_ROWSTATUS_DESTROY = 10;

    private static final Integer[] s_order = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

    private final Integer m_vlanStatus;

    private static final Map<Integer, String> vlanStatusMap = new HashMap<>();

    static {
        vlanStatusMap.put(0, "unknown");
        vlanStatusMap.put(1, "operational");
        vlanStatusMap.put(2, "ciscovtp/suspended");
        vlanStatusMap.put(3, "ciscovtp/mtuTooBigForDevice");
        vlanStatusMap.put(4, "ciscovtp/mtuTooBigForTrunk");
        vlanStatusMap.put(5, "rowStatus/active");
        vlanStatusMap.put(6, "rowStatus/notInService");
        vlanStatusMap.put(7, "rowStatus/notReady");
        vlanStatusMap.put(8, "rowStatus/createAndGo");
        vlanStatusMap.put(9, "rowStatus/createAndWait");
        vlanStatusMap.put(10, "rowStatus/destroy");
    }

    public VlanStatus(Integer vlanType) {
        m_vlanStatus = vlanType;
    }

    public Integer getIntCode() {
        return m_vlanStatus;
    }

    @Override
    public int compareTo(VlanStatus o) {
        return getIndex(m_vlanStatus) - getIndex(o.m_vlanStatus);
    }

    private static int getIndex(Integer code) {
        for (int i = 0; i < s_order.length; i++) {
            if (s_order[i].equals(code)) {
                return i;
            }
        }
        throw new IllegalArgumentException("illegal vlanStatus code '" + code + "'");
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof VlanStatus) {
            return m_vlanStatus.intValue() == ((VlanStatus) o).m_vlanStatus.intValue();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        return String.valueOf(m_vlanStatus);
    }

    public static VlanStatus get(Integer code) {
        if (code == null)
            return VlanStatus.UNKNOWN;
        switch (code) {
            case VLAN_STATUS_UNKNOWN:
                return UNKNOWN;
            case CISCOVTP_VLAN_STATUS_OPERATIONAL:
                return CISCOVTP_OPERATIONAL;
            case CISCOVTP_VLAN_STATUS_SUSPENDED:
                return CISCOVTP_SUSPENDED;
            case CISCOVTP_VLAN_STATUS_mtuTooBigForDevice:
                return CISCOVTP_mtuTooBigForDevice;
            case CISCOVTP_VLAN_STATUS_mtuTooBigForTrunk:
                return CISCOVTP_mtuTooBigForTrunk;
            case SNMPV2C_ROWSTATUS_ACTIVE:
                return ROWSTATUS_ACTIVE;
            case SNMPV2C_ROWSTATUS_NOTINSERVICE:
                return ROWSTATUS_NOTINSERVICE;
            case SNMPV2C_ROWSTATUS_NOTREADY:
                return ROWSTATUS_NOTREADY;
            case SNMPV2C_ROWSTATUS_CREATEANDGO:
                return ROWSTATUS_CREATEANDGO;
            case SNMPV2C_ROWSTATUS_CREATEANDWAIT:
                return ROWSTATUS_CREATEANDWAIT;
            case SNMPV2C_ROWSTATUS_DESTROY:
                return ROWSTATUS_DESTROY;
            default:
                throw new IllegalArgumentException("Cannot create vlanStatus from code " + code);
        }
    }

    /**
     * <p>getVlanStatusString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public static String getVlanStatusString(Integer code) {
        if (vlanStatusMap.containsKey(code))
            return vlanStatusMap.get(code);
        return null;
    }

    public static final VlanStatus UNKNOWN = new VlanStatus(VLAN_STATUS_UNKNOWN);
    public static final VlanStatus CISCOVTP_OPERATIONAL = new VlanStatus(CISCOVTP_VLAN_STATUS_OPERATIONAL);
    public static final VlanStatus CISCOVTP_SUSPENDED = new VlanStatus(CISCOVTP_VLAN_STATUS_SUSPENDED);
    public static final VlanStatus CISCOVTP_mtuTooBigForDevice = new VlanStatus(CISCOVTP_VLAN_STATUS_mtuTooBigForDevice);
    public static final VlanStatus CISCOVTP_mtuTooBigForTrunk = new VlanStatus(CISCOVTP_VLAN_STATUS_mtuTooBigForTrunk);
    public static final VlanStatus ROWSTATUS_ACTIVE = new VlanStatus(SNMPV2C_ROWSTATUS_ACTIVE);
    public static final VlanStatus ROWSTATUS_NOTINSERVICE = new VlanStatus(SNMPV2C_ROWSTATUS_NOTINSERVICE);
    public static final VlanStatus ROWSTATUS_NOTREADY = new VlanStatus(SNMPV2C_ROWSTATUS_NOTREADY);
    public static final VlanStatus ROWSTATUS_CREATEANDGO = new VlanStatus(SNMPV2C_ROWSTATUS_CREATEANDGO);
    public static final VlanStatus ROWSTATUS_CREATEANDWAIT = new VlanStatus(SNMPV2C_ROWSTATUS_CREATEANDWAIT);
    public static final VlanStatus ROWSTATUS_DESTROY = new VlanStatus(SNMPV2C_ROWSTATUS_DESTROY);
}
