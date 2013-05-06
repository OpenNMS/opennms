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

import java.net.InetAddress;

public class OspfIfTableEntry extends SnmpStore {

    public final static String OSPF_IF_IPADDRESS_ALIAS    = "ospfIfIpAddress";
    public final static String OSPF_ADDRESS_LESS_IF_ALIAS = "ospfAddressLessIf";
    
    public final static String OSPF_IF_IPADDRESS_ALIAS_OID = ".1.3.6.1.2.1.14.7.1.1";
    public final static String OSPF_ADDRESS_LESS_IF_OID    = ".1.3.6.1.2.1.14.7.1.2";

    public static final NamedSnmpVar[] ospfiftable_elemList = new NamedSnmpVar[] {
        
        /**
         *  "The IP address of this OSPF interface."
        */
        new NamedSnmpVar(NamedSnmpVar.SNMPIPADDRESS, OSPF_IF_IPADDRESS_ALIAS, OSPF_IF_IPADDRESS_ALIAS_OID, 1),
        
        /**
         * "For the purpose of easing  the  instancing  of
         * addressed   and  addressless  interfaces;  This
         * variable takes the value 0 on  interfaces  with
         * IP  Addresses,  and  the corresponding value of
         * ifIndex for interfaces having no IP Address."
         * 
         */
        new NamedSnmpVar(NamedSnmpVar.SNMPINT32, OSPF_ADDRESS_LESS_IF_ALIAS, OSPF_ADDRESS_LESS_IF_OID, 2),
        
    };
    
    public static final String TABLE_OID = ".1.3.6.1.2.1.14.7.1"; // start of table (GETNEXT)
    
    public OspfIfTableEntry() {
        super(ospfiftable_elemList);
    }

    public InetAddress getOspfIpAddress() {
        return getIPAddress(OSPF_IF_IPADDRESS_ALIAS);
    }
    
    public Integer getOspfAddressLessIf() {
        return getInt32(OSPF_ADDRESS_LESS_IF_ALIAS);
    }
}
