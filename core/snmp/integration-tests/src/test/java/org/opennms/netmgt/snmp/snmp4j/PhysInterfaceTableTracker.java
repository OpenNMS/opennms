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

package org.opennms.netmgt.snmp.snmp4j;

import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.TableTracker;

/**
 * A minimal PhysInterfaceTableTracker used to testing
 */
class PhysInterfaceTableTracker extends TableTracker {

    private static final SnmpObjId IF_TABLE_ENTRY = SnmpObjId.get(".1.3.6.1.2.1.2.2.1");
    private static final SnmpObjId IF_INDEX = SnmpObjId.get(IF_TABLE_ENTRY, "1");
    private static final SnmpObjId IF_DESCR = SnmpObjId.get(IF_TABLE_ENTRY, "2");
    private static final SnmpObjId IF_TYPE = SnmpObjId.get(IF_TABLE_ENTRY, "3");
    private static final SnmpObjId IF_MTU = SnmpObjId.get(IF_TABLE_ENTRY, "4");
    private static final SnmpObjId IF_SPEED = SnmpObjId.get(IF_TABLE_ENTRY, "5");
    private static final SnmpObjId IF_PHYS_ADDR = SnmpObjId.get(IF_TABLE_ENTRY, "6");
    private static final SnmpObjId IF_ADMIN_STATUS = SnmpObjId.get(IF_TABLE_ENTRY, "7");
    private static final SnmpObjId IF_OPER_STATUS = SnmpObjId.get(IF_TABLE_ENTRY, "8");
    private static final SnmpObjId IF_LAST_CHANGE = SnmpObjId.get(IF_TABLE_ENTRY, "9");
    private static final SnmpObjId IF_XTABLE_ENTRY = SnmpObjId.get( ".1.3.6.1.2.1.31.1.1.1");
    private static final SnmpObjId IF_NAME = SnmpObjId.get(IF_XTABLE_ENTRY, "1");

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
        IF_NAME
    };

    PhysInterfaceTableTracker() {
        super(s_tableColumns);
    }

}
