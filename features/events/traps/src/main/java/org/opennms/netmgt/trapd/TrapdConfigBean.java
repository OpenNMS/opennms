/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

import java.util.List;

import org.opennms.netmgt.config.TrapdConfig;
import org.opennms.netmgt.snmp.SnmpV3User;

/**
 * This is a bean container that can be used as a {@link TrapdConfig}
 * service.
 * 
 * @author dp044946
 */
public class TrapdConfigBean implements TrapdConfig {

	private String m_snmpTrapAddress;
	private int m_snmpTrapPort;
	private boolean m_newSuspectOnTrap;
	private List<SnmpV3User> m_snmpV3Users;

	public void setSnmpTrapAddress(String snmpTrapAddress) {
		this.m_snmpTrapAddress = snmpTrapAddress;
	}

	public void setSnmpTrapPort(int snmpTrapPort) {
		this.m_snmpTrapPort = snmpTrapPort;
	}

	public void setNewSuspectOnTrap(boolean newSuspectOnTrap) {
		this.m_newSuspectOnTrap = newSuspectOnTrap;
	}

	public void setSnmpV3Users(List<SnmpV3User> snmpV3Users) {
		this.m_snmpV3Users = snmpV3Users;
	}

	@Override
	public String getSnmpTrapAddress() {
		return m_snmpTrapAddress;
	}

	@Override
	public int getSnmpTrapPort() {
		return m_snmpTrapPort;
	}

	@Override
	public boolean getNewSuspectOnTrap() {
		return m_newSuspectOnTrap;
	}

	@Override
	public List<SnmpV3User> getSnmpV3Users() {
		return m_snmpV3Users;
	}

}
