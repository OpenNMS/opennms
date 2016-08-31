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
import org.opennms.netmgt.snmp.SnmpResult;

@XmlRootElement(name="trap-dto")
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
    
    private String m_systemId;
    
    private String m_location;
    
    private InetAddress m_sourceAddress;
	
    private String m_community;
	
    private String m_pduLength;
	
    private String m_version;
	
    private Long m_timestamp;
	
    private InetAddress m_trapAddress;	
	
    @XmlElement(name="result")
    private List<SnmpResult> results = new ArrayList<>(0);
    
	public String getSystemId() {
		return m_systemId;
	}

	public void setSystemId(String m_systemId) {
		this.m_systemId = m_systemId;
		super.getHeaders().put(MinionDTO.SYSTEM_ID, m_systemId);
	}

	public String getLocation() {
		return m_location;
	}

	public void setLocation(String m_location) {
		this.m_location = m_location;
		super.getHeaders().put(MinionDTO.LOCATION, m_location);
	}

	public InetAddress getSourceAddress() {
		return m_sourceAddress;
	}

	public void setSourceAddress(InetAddress m_sourceAddress) {
		this.m_sourceAddress = m_sourceAddress;
		super.getHeaders().put(MinionDTO.SOURCE_ADDRESS, m_sourceAddress.toString());
	}

	public String getCommunity() {
		return m_community;
	}

	public void setCommunity(String m_community) {
		this.m_community = m_community;
		super.getHeaders().put(COMMUNITY, m_community);
	}

	public String getPduLength() {
		return m_pduLength;
	}

	public void setPduLength(String m_pduLength) {
		this.m_pduLength = m_pduLength;
		super.getHeaders().put(PDU_LENGTH, m_pduLength);
	}

	public String getVersion() {
		return m_version;
	}

	public void setVersion(String m_version) {
		this.m_version = m_version;
		super.getHeaders().put(VERSION, m_version);
	}

	public Long getTimestamp() {
		return m_timestamp;
	}

	public void setTimestamp(Long m_timestamp) {
		this.m_timestamp = m_timestamp;
		super.getHeaders().put(TIMESTAMP, String.valueOf(m_timestamp));
	}

	public InetAddress getTrapAddress() {
		return m_trapAddress;
	}

	public void setTrapAddress(InetAddress m_trapAddress) {
		this.m_trapAddress = m_trapAddress;
		super.getHeaders().put(TRAP_ADDRESS, String.valueOf(m_trapAddress));
	}

	public List<SnmpResult> getResults() {
		return results;
	}

	public void setResults(List<SnmpResult> results) {
		this.results = results;
	}
	
}