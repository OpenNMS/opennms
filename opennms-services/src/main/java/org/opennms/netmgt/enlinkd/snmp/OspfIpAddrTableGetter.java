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

import java.net.InetAddress;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.OspfElement;
import org.opennms.netmgt.model.OspfLink;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.TableTracker;

public class OspfIpAddrTableGetter extends TableTracker {

    public final static SnmpObjId IPADENT_IFINDEX = SnmpObjId.get(".1.3.6.1.2.1.4.20.1.2");
    public final static SnmpObjId IPADENT_NETMASK = SnmpObjId.get(".1.3.6.1.2.1.4.20.1.3");

	/**
	 * The SnmpPeer object used to communicate via SNMP with the remote host.
	 */
	private SnmpAgentConfig m_agentConfig;

	public OspfIpAddrTableGetter(SnmpAgentConfig peer) {
		m_agentConfig = peer;
	}

	public OspfElement get(OspfElement element) {
		//loopback mask by default
	    element.setOspfRouterIdNetmask(InetAddressUtils.addr("255.255.255.255"));
		SnmpValue[] val = get(element.getOspfRouterId());
		if (val != null && val.length == 2 ) {
			if (!val[0].isNull() && val[0].isNumeric() )
				element.setOspfRouterIdIfindex(val[0].toInt());
			if (!val[1].isNull() && !val[1].isError()) {
				try {
					element.setOspfRouterIdNetmask(val[1].toInetAddress());
				} catch (IllegalArgumentException e) {
					
				}
			}
		}
		return element;
	}
	
	public OspfLink get(OspfLink link) {
		//use point to point by default
	    link.setOspfIpMask(InetAddressUtils.addr("255.255.255.252"));
		SnmpValue[] val = get(link.getOspfIpAddr());
		if (val != null && val.length == 2 ) {
			if (!val[0].isNull() && val[0].isNumeric() )
				link.setOspfIfIndex(val[0].toInt());
			if (!val[1].isNull() && !val[1].isError()) {
				try {
					link.setOspfIpMask(val[1].toInetAddress());
				} catch (IllegalArgumentException e) {
					
				}
			}
		}
		return link;
	}

	private SnmpValue[] get(InetAddress addr) {
		SnmpObjId instance = SnmpObjId.get(addr.getHostAddress());
		SnmpObjId[] oids = new SnmpObjId[]
				{SnmpObjId.get(IPADENT_IFINDEX, instance),
					SnmpObjId.get(IPADENT_NETMASK, instance)};
		
		return SnmpUtils.get(m_agentConfig, oids);
	}

}
