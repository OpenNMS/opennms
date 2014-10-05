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

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.LldpElement.LldpChassisIdSubType;
import org.opennms.netmgt.model.LldpLink.LldpPortIdSubType;
import org.opennms.netmgt.snmp.SnmpValue;

public abstract class LldpHelper {

    public static String decodeLldpChassisId(final SnmpValue lldpchassisid, Integer lldpLocChassisidSubType) {
    	String  lldpLocChassisId = lldpchassisid.toDisplayString();
    	if (lldpLocChassisidSubType.intValue() == LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS.getValue().intValue())
    		lldpLocChassisId = lldpchassisid.toHexString();
    	if (lldpLocChassisidSubType.intValue() == LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_NETWORKADDRESS.getValue().intValue())
    		lldpLocChassisId = InetAddressUtils.str(lldpchassisid.toInetAddress());
    	return lldpLocChassisId;
    }

	public static String decodeLldpLink(Integer lldpPortIdSubType,SnmpValue lldpportid) {
		String lldpPortId = lldpportid.toDisplayString();
    	if (lldpPortIdSubType.intValue() == LldpPortIdSubType.LLDP_PORTID_SUBTYPE_MACADDRESS.getValue().intValue())
    		lldpPortId = lldpportid.toHexString();
    	if (lldpPortIdSubType.intValue() == LldpPortIdSubType.LLDP_PORTID_SUBTYPE_NETWORKADDRESS.getValue())
    		lldpPortId = InetAddressUtils.str(lldpportid.toInetAddress());
    	return lldpPortId;

	}

}
