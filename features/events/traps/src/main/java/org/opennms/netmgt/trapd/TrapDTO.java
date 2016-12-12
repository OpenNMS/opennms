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

import org.opennms.core.camel.MinionDTO;
import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.snmp.InetAddrUtils;
import org.opennms.netmgt.snmp.SnmpResult;
import org.snmp4j.smi.OID;

import com.google.common.base.MoreObjects;

@XmlRootElement(name = "trap-dto")
@XmlAccessorType(XmlAccessType.NONE)
public class TrapDTO extends MinionDTO implements Message {

	private static final String COMMUNITY = "community";
	private static final String CREATION_TIME = "creationTime";
	private static final String PDU_LENGTH = "pduLength";
	private static final String VERSION = "version";
	private static final String TIMESTAMP = "timestamp";
	private static final String AGENT_ADDRESS = "agentAddress";
	private static final String GENERIC = "generic";
	private static final String ENTERPRISEID = "enterpriseId";
	private static final String SPECIFIC = "specific";

	// No-arg constructor for JAXB
	public TrapDTO() {

	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("systemId", getHeaders().get(SYSTEM_ID))
				.add("location", getHeaders().get(LOCATION))
				.add("sourceAddress", getHeaders().get(SOURCE_ADDRESS))
				.add("sourcePort", getHeaders().get(SOURCE_PORT))
				.add("agentAddress", getHeaders().get(AGENT_ADDRESS))
				.add("community", getHeaders().get(COMMUNITY))
				.add("generic", getHeaders().get(GENERIC))
				.add("enterpriseId", getHeaders().get(ENTERPRISEID))
				.add("specific", getHeaders().get(SPECIFIC))
				.add("creationTime", getHeaders().get(CREATION_TIME))
				.add("pduLength", getHeaders().get(PDU_LENGTH))
				.add("timestamp", getHeaders().get(TIMESTAMP))
				.add("version", getHeaders().get(VERSION))
				.add("body", getBody()).toString();
	}

	@XmlElementWrapper(name = "results")
	@XmlElement(name = "result")
	private List<SnmpResult> results = new ArrayList<>();

	public void setCommunity(String m_community) {
		putHeader(COMMUNITY, m_community);
	}

	public void setCreationTime(long m_creationTime) {
		putHeader(CREATION_TIME, String.valueOf(m_creationTime));
	}

	public void setPduLength(int m_pduLength) {
		putHeader(PDU_LENGTH, String.valueOf(m_pduLength));
	}

	public void setVersion(String m_version) {
		putHeader(VERSION, m_version);
	}

	public void setTimestamp(Long m_timestamp) {
		putHeader(TIMESTAMP, Long.toString(m_timestamp));
	}

	public void setAgentAddress(InetAddress m_agentAddress) {
		putHeader(AGENT_ADDRESS, InetAddressUtils.str(m_agentAddress));
	}

	public List<SnmpResult> getResults() {
		return results;
	}

	public void setResults(List<SnmpResult> results) {
		this.results = results;
	}
	
	public void setEnterpriseId(OID enterpriseId) {
		putHeader(ENTERPRISEID, enterpriseId.toString());
	}
	
	public void setGeneric(int generic) {
		putHeader(GENERIC, Integer.toString(generic));
	}
	
	public void setSpecific(int specific) {
		putHeader(SPECIFIC, Integer.toString(specific));
	}

	public String getVersion() {
		return getHeader(VERSION);
	}

	public long getTimestamp() {
		return Long.parseLong(getHeader(TIMESTAMP));
	}

	public String getEnterpriseId() {
		return getHeader(ENTERPRISEID);
	}

	public int getGeneric() {
		return Integer.parseInt(getHeader(GENERIC));
	}

	public int getSpecific() {
		return Integer.parseInt(getHeader(SPECIFIC));
	}

	public InetAddress getAgentAddress() {
		return InetAddrUtils.addr(getHeader(TrapDTO.AGENT_ADDRESS));
	}

	public String getCommunity() {
		return getHeader(COMMUNITY);
	}

	public long getCreationTime() {
		return Long.parseLong(getHeader(TrapDTO.CREATION_TIME));
	}

	public InetAddress getSourceAddress() {
		return InetAddrUtils.addr(getHeader(TrapDTO.SOURCE_ADDRESS));
	}
}
