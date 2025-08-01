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
package org.opennms.netmgt.snmpinterfacepoller.pollable;

import org.opennms.netmgt.xml.event.Snmp;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents possible values of ifAdminStatus and ifOperStatus of IF-MIB defined from RFC 2863
 *
 * @author <a href="mailto:dschlenk@convergeone.com">David Schlenk</a>
 * @version $Id: $
 */
public enum SnmpInterfaceStatus {
    INVALID(0, "Invalid"),
    UP(1, "Up"),
    DOWN(2, "Down"),
    TESTING(3, "Testing"),
    UNKNOWN(4, "Unknown"),
    DORMANT(5, "Dormant"),
    NOT_PRESENT(6, "Not Present"),
    LOWER_LAYER_DOWN(7, "Lower Layer Down");

    public final int m_mibValue;

    public final String m_label;

    private SnmpInterfaceStatus(int mibValue, String label) {
        m_mibValue = mibValue;
        m_label = label;
    }

    public int getMibValue() {
        return m_mibValue;
    }

    public String getLabel() {
        return m_label;
    }

    public static SnmpInterfaceStatus statusFromMibValue(int mibValue) {
        switch(mibValue) {
            case 1:
                return UP;
            case 2:
                return DOWN;
            case 3:
                return TESTING;
            case 4:
                return UNKNOWN;
            case 5:
                return DORMANT;
            case 6:
                return NOT_PRESENT;
            case 7:
                return LOWER_LAYER_DOWN;
            default:
                return INVALID;
        }
    }

    public static String labelFromMibValue(int mibValue) {
        return statusFromMibValue(mibValue).getLabel();
    }

    public static Set<SnmpInterfaceStatus> getStatuses(int[] statuses) {
        Set<SnmpInterfaceStatus> ret = new HashSet<SnmpInterfaceStatus>(statuses.length);
        for (int s : statuses) {
            ret.add(statusFromMibValue(s));
        }
        return ret;
    }
}
