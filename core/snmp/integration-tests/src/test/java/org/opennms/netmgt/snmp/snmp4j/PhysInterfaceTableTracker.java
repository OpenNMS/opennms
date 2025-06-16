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
