/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.trapd;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.camel.MinionDTO;
import org.opennms.netmgt.snmp.InetAddrUtils;
import org.opennms.netmgt.snmp.SnmpResult;

@XmlRootElement(name = "trap-dto")
@XmlAccessorType(XmlAccessType.NONE)
public class TrapDTO extends MinionDTO {

	public static String COMMUNITY = "community";
	public static String PDU_LENGTH = "pduLength";
	public static String VERSION = "version";
	public static String TIMESTAMP = "timestamp";
	public static String TRAP_ADDRESS = "trapAddress";

	protected TrapDTO() {
		// No-arg constructor for JAXB
		super();
	}

	@XmlElement(name = "result")
	private List<SnmpResult> results = new ArrayList<>(0);

	public void setCommunity(String m_community) {
		super.putIntoMap(COMMUNITY, m_community);
	}

	public void setPduLength(String m_pduLength) {
		super.putIntoMap(PDU_LENGTH, m_pduLength);
	}

	public void setVersion(String m_version) {
		super.putIntoMap(VERSION, m_version);
	}

	public void setTimestamp(Long m_timestamp) {
		super.putIntoMap(TIMESTAMP, Long.toString(m_timestamp));
	}

	public void setTrapAddress(InetAddress m_trapAddress) {
		super.putIntoMap(TRAP_ADDRESS, InetAddrUtils.str(m_trapAddress));
	}

	public void setAgentAddress(InetAddress m_agentAddress) {
		super.putIntoMap(SOURCE_ADDRESS, InetAddrUtils.str(m_agentAddress));
	}

	public List<SnmpResult> getResults() {
		return results;
	}

	public void setResults(List<SnmpResult> results) {
		this.results = results;
	}

}