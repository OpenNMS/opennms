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
package org.opennms.netmgt.trapd;

import java.net.InetAddress;
import java.util.Objects;

import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpVarBindDTO;
import org.opennms.netmgt.snmp.TrapInformation;

public class TrapUtils {

    /**
     * SNMP-COMMUNITY-MIB: snmpTrapAddress (1.3.6.1.6.3.18.1.3.0) of type IpAddress 
     */
    protected static final SnmpObjId SNMP_TRAP_ADDRESS_OID = SnmpObjId.get(".1.3.6.1.6.3.18.1.3.0");

    public static final String GET_TRAP_ADDRESS_FROM_VARBIND_SYS_PROP = "org.opennms.trapd";

    public static InetAddress getEffectiveTrapAddress(TrapInformation trapInfo, boolean useAddressFromVarbind) {
        if (useAddressFromVarbind) {
            final SnmpVarBindDTO varBindDTO = getFirstVarBindWithOid(trapInfo, SNMP_TRAP_ADDRESS_OID);
            if (varBindDTO != null) {
                return varBindDTO.getSnmpValue().toInetAddress();
            }
        }
        return trapInfo.getTrapAddress();
    }

    public static SnmpVarBindDTO getFirstVarBindWithOid(TrapInformation trapInfo, SnmpObjId oid) {
        for (int i = 0; i < trapInfo.getPduLength(); i++) {
            final SnmpVarBindDTO varBindDTO = trapInfo.getSnmpVarBindDTO(i);
            if (varBindDTO != null && Objects.equals(oid, varBindDTO.getSnmpObjectId())) {
                return varBindDTO;
            }
        }
        return null;
    }
}
