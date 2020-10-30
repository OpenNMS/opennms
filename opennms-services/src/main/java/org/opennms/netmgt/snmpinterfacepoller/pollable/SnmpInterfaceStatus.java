/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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
