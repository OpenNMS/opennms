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

import org.opennms.netmgt.snmp.AggregateTracker;
import org.opennms.netmgt.snmp.SnmpResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class OspfGeneralGroup extends AggregateTracker {
    
    private static final Logger LOG = LoggerFactory.getLogger(OspfGeneralGroup.class);

    public final static String OSPF_ROUTER_ID_ALIAS = "ospfRouterId";
    public final static String OSPF_ROUTER_ID_OID = ".1.3.6.1.2.1.14.1.1";
        
    public static NamedSnmpVar[] ms_elemList = null;
    
    static {
        ms_elemList = new NamedSnmpVar[1];
        int ndx = 0;

        /**
         * <P>
         * SYNTAX   RouterID
         * MAX-ACCESS   read-only
         * STATUS   current
         * DESCRIPTION
         * "A  32-bit  integer  uniquely  identifying  the
         * router in the Autonomous System.
         * 
         * By  convention,  to  ensure  uniqueness,   this
         * should  default  to  the  value  of  one of the
         * router's IP interface addresses."
         * REFERENCE
         * "OSPF Version 2, C.1 Global parameters"
         * </P>
         */
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPIPADDRESS,OSPF_ROUTER_ID_ALIAS,OSPF_ROUTER_ID_OID);

    }
    
    public static final String OSPF_GENERAL_GROUP_OID = ".1.3.6.1.2.1.14.1";

    private SnmpStore m_store;
    private InetAddress m_address;
    
    public OspfGeneralGroup(InetAddress address) {
        super(NamedSnmpVar.getTrackersFor(ms_elemList));
        m_address = address;
        m_store = new SnmpStore(ms_elemList);
    }
    
    public InetAddress getOspfRouterId() {
        return m_store.getIPAddress(OSPF_ROUTER_ID_ALIAS);        
    }
       
    /** {@inheritDoc} */
    @Override
    protected void storeResult(SnmpResult res) {
        m_store.storeResult(res);
    }

    /** {@inheritDoc} */
    @Override
    protected void reportGenErr(String msg) {
        log().warn("Error retrieving lldpLocalGroup from "+m_address+". "+msg);
    }

    /** {@inheritDoc} */
    @Override
    protected void reportNoSuchNameErr(String msg) {
        log().info("Error retrieving lldpLocalGroup from "+m_address+". "+msg);
    }

    private Logger log() {
        return LOG;
    }


}
