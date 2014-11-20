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

import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.TableTracker;

public class Dot1dBasePortIfIndexGetter extends TableTracker {

    public final static SnmpObjId DOT1DBASE_PORT_IFINDEX = SnmpObjId.get(".1.3.6.1.2.1.17.1.4.1.2");

	/**
	 * The SnmpPeer object used to communicate via SNMP with the remote host.
	 */
	private SnmpAgentConfig m_agentConfig;

	public Dot1dBasePortIfIndexGetter(SnmpAgentConfig peer) {
		m_agentConfig = peer;
	}

	public SnmpValue get(Integer cdpInterfaceIndex) {
		SnmpObjId instance = SnmpObjId.get(new int[] {cdpInterfaceIndex});
		SnmpObjId[] oids = new SnmpObjId[]
				{SnmpObjId.get(DOT1DBASE_PORT_IFINDEX, instance)};
		
		return SnmpUtils.get(m_agentConfig, oids)[0];
	}

}
