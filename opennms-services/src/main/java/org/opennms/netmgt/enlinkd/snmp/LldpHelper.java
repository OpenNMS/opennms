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

import org.opennms.core.utils.LldpUtils;
import org.opennms.core.utils.LldpUtils.LldpChassisIdSubType;
import org.opennms.core.utils.LldpUtils.LldpPortIdSubType;
import org.opennms.netmgt.snmp.SnmpValue;

public abstract class LldpHelper {


    public static String decodeLldpChassisId(final SnmpValue lldpchassisid, Integer lldpLocChassisidSubType) {
        LldpChassisIdSubType type = LldpChassisIdSubType.get(lldpLocChassisidSubType);
        
        /*
         *  If the associated LldpChassisIdSubtype object has a value of
         *  'chassisComponent(1)', then the octet string identifies
         *  a particular instance of the entPhysicalAlias object
         *  (defined in IETF RFC 2737) for a chassis component (i.e.,
         *  an entPhysicalClass value of 'chassis(3)').
         *
         *  If the associated LldpChassisIdSubtype object has a value
         *  of 'interfaceAlias(2)', then the octet string identifies
         *  a particular instance of the ifAlias object (defined in
         *  IETF RFC 2863) for an interface on the containing chassis.
         *  If the particular ifAlias object does not contain any values,
         *  another chassis identifier type should be used.
         *
         *  If the associated LldpChassisIdSubtype object has a value
         *  of 'portComponent(3)', then the octet string identifies a
         *  particular instance of the entPhysicalAlias object (defined
         *  in IETF RFC 2737) for a port or backplane component within
         *  the containing chassis.
         *
         *  If the associated LldpChassisIdSubtype object has a value of
         *  'macAddress(4)', then this string identifies a particular
         *  unicast source address (encoded in network byte order and
         *  IEEE 802.3 canonical bit order), of a port on the containing
         *  chassis as defined in IEEE Std 802-2001.
         *
         *  If the associated LldpChassisIdSubtype object has a value of
         *  'networkAddress(5)', then this string identifies a particular
         *  network address, encoded in network byte order, associated
         *  with one or more ports on the containing chassis. The first
         *  octet contains the IANA Address Family Numbers enumeration
         *  value for the specific address type, and octets 2 through
         *  N contain the network address value in network byte order.
         *
         *  If the associated LldpChassisIdSubtype object has a value
         *  of 'interfaceName(6)', then the octet string identifies
         *  a particular instance of the ifName object (defined in
         *  IETF RFC 2863) for an interface on the containing chassis.
         *  If the particular ifName object does not contain any values,
         *  another chassis identifier type should be used.
         *
         * If the associated LldpChassisIdSubtype object has a value of
         * 'local(7)', then this string identifies a locally assigned
         * Chassis ID.
         * 
         */
        switch (type) {
         case LLDP_CHASSISID_SUBTYPE_CHASSISCOMPONENT:
         case LLDP_CHASSISID_SUBTYPE_INTERFACEALIAS:
         case LLDP_CHASSISID_SUBTYPE_PORTCOMPONENT:
         case LLDP_CHASSISID_SUBTYPE_INTERFACENAME:  
         case LLDP_CHASSISID_SUBTYPE_LOCAL:
             return lldpchassisid.toDisplayString();
         case LLDP_CHASSISID_SUBTYPE_MACADDRESS:
             return lldpchassisid.toHexString();
         case LLDP_CHASSISID_SUBTYPE_NETWORKADDRESS:
             LldpUtils.decodeNetworkAddress(lldpchassisid.toDisplayString());
        }
    	return lldpchassisid.toDisplayString();
    }

    
    public static String decodeLldpPortId(Integer lldpPortIdSubType,SnmpValue lldpportid) {
        LldpPortIdSubType type=LldpPortIdSubType.get(lldpPortIdSubType);
        /*
         * 
         *       If the associated LldpPortIdSubtype object has a value of
         *       'interfaceAlias(1)', then the octet string identifies a
         *       particular instance of the ifAlias object (defined in IETF
         *       RFC 2863). If the particular ifAlias object does not contain
         *       any values, another port identifier type should be used.
         *
         *       If the associated LldpPortIdSubtype object has a value of
         *       'portComponent(2)', then the octet string identifies a
         *       particular instance of the entPhysicalAlias object (defined
         *       in IETF RFC 2737) for a port or backplane component.
         *
         *       If the associated LldpPortIdSubtype object has a value of
         *       'macAddress(3)', then this string identifies a particular
         *       unicast source address (encoded in network byte order
         *       and IEEE 802.3 canonical bit order) associated with the port
         *       (IEEE Std 802-2001).
         *
         *       If the associated LldpPortIdSubtype object has a value of
         *       'networkAddress(4)', then this string identifies a network
         *       address associated with the port. The first octet contains
         *       the IANA AddressFamilyNumbers enumeration value for the
         *       specific address type, and octets 2 through N contain the
         *       networkAddress address value in network byte order.
         *
         *       If the associated LldpPortIdSubtype object has a value of
         *       'interfaceName(5)', then the octet string identifies a
         *       particular instance of the ifName object (defined in IETF
         *       RFC 2863). If the particular ifName object does not contain
         *       any values, another port identifier type should be used.
         *
         *       If the associated LldpPortIdSubtype object has a value of
         *       'agentCircuitId(6)', then this string identifies a agent-local
         *       identifier of the circuit (defined in RFC 3046).
         *
         *       If the associated LldpPortIdSubtype object has a value of
         *       'local(7)', then this string identifies a locally
         *       assigned port ID."
         */
        switch (type) {
        case LLDP_PORTID_SUBTYPE_AGENTCIRCUITID:
        case LLDP_PORTID_SUBTYPE_INTERFACEALIAS:
        case LLDP_PORTID_SUBTYPE_INTERFACENAME:
        case LLDP_PORTID_SUBTYPE_PORTCOMPONENT:
        case LLDP_PORTID_SUBTYPE_LOCAL:
            return lldpportid.toDisplayString();
        case LLDP_PORTID_SUBTYPE_MACADDRESS:
            return lldpportid.toHexString();
        case LLDP_PORTID_SUBTYPE_NETWORKADDRESS:
            LldpUtils.decodeNetworkAddress(lldpportid.toDisplayString());
       }
    	return lldpportid.toDisplayString();
    }

}
