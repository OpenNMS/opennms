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

public class OspfNbrTableEntry extends SnmpStore {

    public final static String OSPF_NBR_IPADDRESS_ALIAS    = "ospfNbrIpAddr";
    public final static String OSPF_NBR_ADDRESS_LESS_INDEX_ALIAS = "ospfNbrAddressLessIndex";
    public final static String OSPF_NBR_ROUTERID_ALIAS = "ospfNbrRtrId";
    public final static String OSPF_NBR_STATE_ALIAS = "ospfNbrState";
    
    public final static String OSPF_NBR_IPADDRESS_ALIAS_OID       = ".1.3.6.1.2.1.14.10.1.1";
    public final static String OSPF_NBR_ADDRESS_LESS_INDEX_OID    = ".1.3.6.1.2.1.14.10.1.2";
    public final static String OSPF_NBR_ROUTERID_OID              = ".1.3.6.1.2.1.14.10.1.3";
    public final static String OSPF_NBR_STATE_OID                 = ".1.3.6.1.2.1.14.10.1.6";

    public final static Integer OSPF_NBR_STATE_DOWN = 1; 
    public final static Integer OSPF_NBR_STATE_ATTEMPT = 2;    
    public final static Integer OSPF_NBR_STATE_INIT = 3;
    public final static Integer OSPF_NBR_STATE_TWOWAY = 4;
    public final static Integer OSPF_NBR_STATE_EXCHANGESTART = 5;
    public final static Integer OSPF_NBR_STATE_EXCHANGE = 6;
    public final static Integer OSPF_NBR_STATE_LOADING = 7;
    public final static Integer OSPF_NBR_STATE_FULL = 8;

    public static final NamedSnmpVar[] ospfnbrtable_elemList = new NamedSnmpVar[] {
        
        /**
         * <p>
         * "The IP address this neighbor is using  in  its
         * IP  Source  Address.  Note that, on addressless
         * links, this will not be 0.0.0.0,  but  the  ad-
         * dress of another of the neighbor's interfaces."
         * </p>
        */
        new NamedSnmpVar(NamedSnmpVar.SNMPIPADDRESS, OSPF_NBR_IPADDRESS_ALIAS, OSPF_NBR_IPADDRESS_ALIAS_OID, 1),
        
        /**
         * <p>
         * "On an interface having an  IP  Address,  zero.
         * On  addressless  interfaces,  the corresponding
         * value of ifIndex in the Internet Standard  MIB.
         * On  row  creation, this can be derived from the
         * instance."
         * </p>
         */
        new NamedSnmpVar(NamedSnmpVar.SNMPINT32, OSPF_NBR_ADDRESS_LESS_INDEX_ALIAS, OSPF_NBR_ADDRESS_LESS_INDEX_OID, 2),

        /**
         * <p>
         * "A 32-bit integer (represented as a type  IpAd-
         * dress)  uniquely  identifying  the  neighboring
         * router in the Autonomous System."
         * DEFVAL   { '00000000'H }    -- 0.0.0.0
         * </p>
         */
        new NamedSnmpVar(NamedSnmpVar.SNMPIPADDRESS, OSPF_NBR_ROUTERID_ALIAS, OSPF_NBR_ROUTERID_OID, 3),

        /**
         * <p>
         * SYNTAX   INTEGER    {
         *      down (1),
         *      attempt (2),
         *      init (3),
         *      twoWay (4),
         *      exchangeStart (5),
         *      exchange (6),
         *      loading (7),
         *      full (8)
         *      }
         *      
         *      "The State of the relationship with this Neigh-
         *      bor."
         *      </p>
         */
        new NamedSnmpVar(NamedSnmpVar.SNMPINT32, OSPF_NBR_STATE_ALIAS, OSPF_NBR_STATE_OID, 4),

    };
    
    public static final String TABLE_OID = ".1.3.6.1.2.1.14.7.1"; // start of table (GETNEXT)
    
    public OspfNbrTableEntry() {
        super(ospfnbrtable_elemList);
    }

    public InetAddress getOspfNbrIpAddress() {
        return getIPAddress(OSPF_NBR_IPADDRESS_ALIAS);
    }

    public InetAddress getOspfNbrRouterId() {
        return getIPAddress(OSPF_NBR_ROUTERID_ALIAS);
    }

    public Integer getOspfNbrAddressLessIndex() {
        return getInt32(OSPF_NBR_ADDRESS_LESS_INDEX_ALIAS);
    }
    
    public Integer getOspfNbrState() {
        return getInt32(OSPF_NBR_STATE_ALIAS);
    }

}
