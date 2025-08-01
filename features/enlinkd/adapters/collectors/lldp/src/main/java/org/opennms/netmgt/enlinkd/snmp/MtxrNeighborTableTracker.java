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

import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpRowResult;
import org.opennms.netmgt.snmp.TableTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MtxrNeighborTableTracker extends TableTracker {

    private final static Logger LOG = LoggerFactory.getLogger(MtxrNeighborTableTracker.class);

    /*
        mtxrNeighborIndex OBJECT-TYPE
            SYNTAX ObjectIndex
            MAX-ACCESS not-accessible
            STATUS current
            DESCRIPTION ""
            ::= { mtxrNeighborTableEntry 1 }

        mtxrNeighborIpAddress OBJECT-TYPE
            SYNTAX IpAddress
            MAX-ACCESS read-only
            STATUS current
            DESCRIPTION ""
            ::= { mtxrNeighborTableEntry 2 }

        mtxrNeighborMacAddress OBJECT-TYPE
            SYNTAX MacAddress
            MAX-ACCESS read-only
            STATUS current
            DESCRIPTION ""
            ::= { mtxrNeighborTableEntry 3 }

        mtxrNeighborVersion OBJECT-TYPE
            SYNTAX DisplayString
            MAX-ACCESS read-only
            STATUS current
            DESCRIPTION ""
            ::= { mtxrNeighborTableEntry 4 }

        mtxrNeighborPlatform OBJECT-TYPE
            SYNTAX DisplayString
            MAX-ACCESS read-only
            STATUS current
            DESCRIPTION ""
            ::= { mtxrNeighborTableEntry 5 }

        mtxrNeighborIdentity OBJECT-TYPE
            SYNTAX DisplayString
            MAX-ACCESS read-only
            STATUS current
            DESCRIPTION ""
            ::= { mtxrNeighborTableEntry 6 }

        mtxrNeighborSoftwareID OBJECT-TYPE
            SYNTAX DisplayString
            MAX-ACCESS read-only
            STATUS current
            DESCRIPTION ""
            ::= { mtxrNeighborTableEntry 7 }

        mtxrNeighborInterfaceID OBJECT-TYPE
            SYNTAX ObjectIndex
            MAX-ACCESS read-only
            STATUS current
            DESCRIPTION ""
            ::= { mtxrNeighborTableEntry 8 }
     */

    public final static SnmpObjId MTXR_NEIGHBOR_INTERFACE_ID_OID = SnmpObjId.get(".1.3.6.1.4.1.14988.1.1.11.1.1.8");
    public final static String MTXR_NEIGHBOR_INTERFACE_ID = "mtxrNeighborInterfaceID";

    public static final SnmpObjId[] s_mtxrneiinterfaceid_elemList = new SnmpObjId[] {
            MTXR_NEIGHBOR_INTERFACE_ID_OID
    };

    public static class MtxrNeighborRow extends SnmpRowResult {
        public MtxrNeighborRow(int columnCount, SnmpInstId instance) {
            super(columnCount, instance);
            LOG.debug( "column count = {}, instance = {}", columnCount, instance);
        }

        public Integer getMtxrNeighborIndex() {
            return getInstance().getSubIdAt(0);
        }

        public Integer getMtxrNeighborInterfaceId() {
            return getValue(MTXR_NEIGHBOR_INTERFACE_ID_OID).toInt();
        }

    }

    public MtxrNeighborTableTracker() {
	    super(s_mtxrneiinterfaceid_elemList);
	}

    /** {@inheritDoc} */
    @Override
    public SnmpRowResult createRowResult(final int columnCount, final SnmpInstId instance) {
        return new MtxrNeighborRow(columnCount, instance);
    }

    /** {@inheritDoc} */
    @Override
    public void rowCompleted(final SnmpRowResult row) {
        processMtxrIndexPortRow((MtxrNeighborRow)row);
    }

    public void processMtxrIndexPortRow(final MtxrNeighborRow row) {
        System.out.printf("\t\t%s (%s)= %s \n", MTXR_NEIGHBOR_INTERFACE_ID_OID + "." + row.getInstance().toString(), MTXR_NEIGHBOR_INTERFACE_ID, row.getMtxrNeighborInterfaceId());
    }

}
