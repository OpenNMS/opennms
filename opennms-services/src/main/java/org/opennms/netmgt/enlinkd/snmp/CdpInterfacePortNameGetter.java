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


import org.opennms.netmgt.model.CdpLink;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;

public class CdpInterfacePortNameGetter extends SnmpGetter {

    public final static SnmpObjId CDP_INTERFACE_NAME = SnmpObjId.get(".1.3.6.1.4.1.9.9.23.1.1.1.1.6");
    public final static SnmpObjId MIB2_INTERFACE_NAME = SnmpObjId.get(".1.3.6.1.2.1.2.2.1.2");
	/**
	 * The SnmpPeer object used to communicate via SNMP with the remote host.
	 */
    public CdpInterfacePortNameGetter(SnmpAgentConfig peer, LocationAwareSnmpClient client, String location, Integer nodeid) {
        super(peer, client,location,nodeid);
    }

    public CdpLink get(CdpLink link) {
        SnmpValue ifName = getInterfaceNameFromCiscoCdpMib(link.getCdpCacheIfIndex());
        if (ifName == null) {
            ifName = getInterfaceNameFromMib2(link.getCdpCacheIfIndex());
        }
        if (ifName != null)
            link.setCdpInterfaceName(ifName.toDisplayString());
        return link;
    }
    
   public SnmpValue getInterfaceNameFromCiscoCdpMib(Integer ifindex) {
       return get(CDP_INTERFACE_NAME, ifindex);
   }
   
   public SnmpValue getInterfaceNameFromMib2(Integer ifindex) {
       return get(MIB2_INTERFACE_NAME,ifindex);
   }

   
}
