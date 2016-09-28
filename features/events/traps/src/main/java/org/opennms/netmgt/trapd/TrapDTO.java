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
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.core.camel.MinionDTO;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.snmp.SnmpResult;

@XmlRootElement(name = "trap-dto")
@XmlAccessorType(XmlAccessType.NONE)
public class TrapDTO extends MinionDTO {

	public static final String COMMUNITY = "community";
	public static final String CREATION_TIME = "creationTime";
	public static final String PDU_LENGTH = "pduLength";
	public static final String VERSION = "version";
	public static final String TIMESTAMP = "timestamp";
	public static final String AGENT_ADDRESS = "agentAddress";

	protected TrapDTO() {
		// No-arg constructor for JAXB
		super();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("systemId", super.getHeaders().get(SYSTEM_ID))
				.append("location", super.getHeaders().get(LOCATION))
				.append("sourceAddress", super.getHeaders().get(SOURCE_ADDRESS))
				.append("sourcePort", super.getHeaders().get(SOURCE_PORT))
				.append("agentAddress", super.getHeaders().get(AGENT_ADDRESS))
				.append("community", super.getHeaders().get(COMMUNITY))
				.append("creationTime", super.getHeaders().get(CREATION_TIME))
				.append("pduLength", super.getHeaders().get(PDU_LENGTH))
				.append("timestamp", super.getHeaders().get(TIMESTAMP))
				.append("version", super.getHeaders().get(VERSION))
				.append("body", super.getBody()).toString();
	}

	@XmlElementWrapper(name = "results")
	@XmlElement(name = "result")
	private List<SnmpResult> results = new ArrayList<>(0);

	public void setCommunity(String m_community) {
		super.putHeader(COMMUNITY, m_community);
	}

	public void setCreationTime(long m_creationTime) {
		super.putHeader(CREATION_TIME, String.valueOf(m_creationTime));
	}

	public void setPduLength(int m_pduLength) {
		super.putHeader(PDU_LENGTH, String.valueOf(m_pduLength));
	}

	public void setVersion(String m_version) {
		super.putHeader(VERSION, m_version);
	}

	public void setTimestamp(Long m_timestamp) {
		super.putHeader(TIMESTAMP, Long.toString(m_timestamp));
	}

	public void setAgentAddress(InetAddress m_agentAddress) {
		super.putHeader(AGENT_ADDRESS, InetAddressUtils.str(m_agentAddress));
	}

	public List<SnmpResult> getResults() {
		return results;
	}

	public void setResults(List<SnmpResult> results) {
		this.results = results;
	}

}
