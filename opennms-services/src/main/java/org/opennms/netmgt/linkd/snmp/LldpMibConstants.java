/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.linkd.snmp;

public class LldpMibConstants  {
    
    /*
     * LldpChassisIdSubtype ::= TEXTUAL-CONVENTION
    STATUS      current
    DESCRIPTION
            "This TC describes the source of a chassis identifier.

            The enumeration 'chassisComponent(1)' represents a chassis
            identifier based on the value of entPhysicalAlias object
            (defined in IETF RFC 2737) for a chassis component (i.e.,
            an entPhysicalClass value of 'chassis(3)').

            The enumeration 'interfaceAlias(2)' represents a chassis
            identifier based on the value of ifAlias object (defined in
            IETF RFC 2863) for an interface on the containing chassis.

            The enumeration 'portComponent(3)' represents a chassis
            identifier based on the value of entPhysicalAlias object
            (defined in IETF RFC 2737) for a port or backplane
            component (i.e., entPhysicalClass value of 'port(10)' or
            'backplane(4)'), within the containing chassis.

            The enumeration 'macAddress(4)' represents a chassis
            identifier based on the value of a unicast source address
            (encoded in network byte order and IEEE 802.3 canonical bit
            order), of a port on the containing chassis as defined in
            IEEE Std 802-2001.

            The enumeration 'networkAddress(5)' represents a chassis
            identifier based on a network address, associated with
            a particular chassis.  The encoded address is actually
            composed of two fields.  The first field is a single octet,
            representing the IANA AddressFamilyNumbers value for the
            specific address type, and the second field is the network
            address value.

            The enumeration 'interfaceName(6)' represents a chassis
            identifier based on the value of ifName object (defined in
            IETF RFC 2863) for an interface on the containing chassis.

            The enumeration 'local(7)' represents a chassis identifier
            based on a locally defined value."
    SYNTAX  INTEGER {
            chassisComponent(1),
            interfaceAlias(2),
            portComponent(3),
            macAddress(4),
            networkAddress(5),
            interfaceName(6),
            local(7)
    }

     */
    public static final int LLDP_CHASSISID_SUBTYPE_CHASSISCOMPONENT = 1;
    public static final int LLDP_CHASSISID_SUBTYPE_INTERFACEALIAS = 2;
    public static final int LLDP_CHASSISID_SUBTYPE_PORTCOMPONENT = 3;
    public static final int LLDP_CHASSISID_SUBTYPE_MACADDRESS = 4;
    public static final int LLDP_CHASSISID_SUBTYPE_NETWORKADDRESS = 5;
    public static final int LLDP_CHASSISID_SUBTYPE_INTERFACENAME = 6;
    public static final int LLDP_CHASSISID_SUBTYPE_LOCAL = 7;
    /*
     * LldpPortIdSubtype ::= TEXTUAL-CONVENTION
    STATUS      current
    DESCRIPTION
            "This TC describes the source of a particular type of port
            identifier used in the LLDP MIB.

            The enumeration 'interfaceAlias(1)' represents a port
            identifier based on the ifAlias MIB object, defined in IETF
            RFC 2863.

            The enumeration 'portComponent(2)' represents a port
            identifier based on the value of entPhysicalAlias (defined in
            IETF RFC 2737) for a port component (i.e., entPhysicalClass
            value of 'port(10)'), within the containing chassis.

            The enumeration 'macAddress(3)' represents a port identifier
            based on a unicast source address (encoded in network
            byte order and IEEE 802.3 canonical bit order), which has
            been detected by the agent and associated with a particular
            port (IEEE Std 802-2001).

            The enumeration 'networkAddress(4)' represents a port
            identifier based on a network address, detected by the agent
            and associated with a particular port.

            The enumeration 'interfaceName(5)' represents a port
            identifier based on the ifName MIB object, defined in IETF
            RFC 2863.

            The enumeration 'agentCircuitId(6)' represents a port
            identifier based on the agent-local identifier of the circuit
            (defined in RFC 3046), detected by the agent and associated
            with a particular port.

            The enumeration 'local(7)' represents a port identifier
            based on a value locally assigned."

    SYNTAX  INTEGER {
            interfaceAlias(1),
            portComponent(2),
            macAddress(3),
            networkAddress(4),
            interfaceName(5),
            agentCircuitId(6),
            local(7)
    }
     */
    public static final int LLDP_PORTID_SUBTYPE_INTERFACEALIAS = 1;
    public static final int LLDP_PORTID_SUBTYPE_PORTCOMPONENT = 2;
    public static final int LLDP_PORTID_SUBTYPE_MACADDRESS = 3;
    public static final int LLDP_PORTID_SUBTYPE_NETWORKADDRESS = 4;
    public static final int LLDP_PORTID_SUBTYPE_INTERFACENAME = 5;    
    public static final int LLDP_PORTID_SUBTYPE_AGENTCIRCUITID = 6;
    public static final int LLDP_PORTID_SUBTYPE_LOCAL = 7;

}
