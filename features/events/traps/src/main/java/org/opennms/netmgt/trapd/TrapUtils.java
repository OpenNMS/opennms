/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
