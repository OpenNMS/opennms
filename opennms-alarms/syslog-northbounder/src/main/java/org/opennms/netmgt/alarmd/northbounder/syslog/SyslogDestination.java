/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.alarmd.northbounder.syslog;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.opennms.netmgt.alarmd.api.Destination;
 
/**
 * Configuration for the various Syslog hosts to receive alarms via Syslog\
 * 
 * @author <a href="mailto:david@opennms.org>David Hustace</a>
 *
 */
@XmlRootElement(name="syslog-destination")
@XmlAccessorType(XmlAccessType.FIELD)
public class SyslogDestination implements Destination {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@XmlType
    @XmlEnum(String.class)
    public static enum SyslogProtocol {
        UDP ("udp"), TCP ("tcp");
        
        private String m_id;
        
        SyslogProtocol(String id) {
        	m_id = id;
        }
        
        public String getId() {
        	return m_id;
        }
    }
    
    @XmlType
    @XmlEnum(String.class)
    public static enum SyslogFacility {
    	KERN ("KERN"), USER ("USER"), 
    	MAIL ("MAIL"), DAEMON ("DAEMON"),
    	AUTH ("AUTH"), SYSLOG ("SYSLOG"),
    	LPR ("LPR"), NEWS ("NEWS"),
    	UUCP ("UUCP"), CRON ("CRON"),
    	AUTHPRIV ("AUTHPRIV"), FTP ("FTP"),
    	LOCAL0 ("LOCAL0"), LOCAL1 ("LOCAL1"),
    	LOCAL2 ("LOCAL2"), LOCAL3 ("LOCAL3"),
    	LOCAL4 ("LOCAL4"), LOCAL5 ("LOCAL5"),
    	LOCAL6 ("LOCAL6"), LOCAL7 ("LOCAL7"),
    	;
    	
    	private String m_id;

		SyslogFacility(String facility) {
    		m_id = facility;
    	}
		
		public String getId() {
			return m_id;
		}
    }


    @XmlElement(name="destination-name", required=true)
    private String m_destinationName;
    
    @XmlElement(name="host", defaultValue="localhost", required=false)
	private String m_host = "localhost";
    
    @XmlElement(name="port", defaultValue="514", required=false)
	private int m_port = 514;
    
    @XmlElement(name="ip-protocol", defaultValue="udp", required=false)
	private SyslogProtocol m_protocol = SyslogProtocol.UDP;
    
    @XmlElement(name="facility", defaultValue="USER", required=false)
	private SyslogFacility m_facility = SyslogFacility.USER;
    
    @XmlElement(name="char-set", defaultValue="UTF-8", required=false)
	private String m_charSet = "UTF-8";
    
    @XmlElement(name="max-message-length", defaultValue="1024", required=false)
	private int m_maxMessageLength = 1024;
    
    @XmlElement(name="send-local-name", defaultValue="true", required=false)
	private boolean m_sendLocalName = true;
    
    @XmlElement(name="send-local-time", defaultValue="true", required=false)
	private boolean m_sendLocalTime = true;
    
    @XmlElement(name="truncate-message", defaultValue="false", required=false)
	private boolean m_truncateMessage = false;
    
    @XmlElement(name="first-occurrence-only", defaultValue="false", required=false)
    private boolean m_firstOccurrenceOnly = false;
	

	public SyslogDestination() {
	}
	
	public SyslogDestination(String name, SyslogProtocol protocol, SyslogFacility facility) {
		m_destinationName = name;
		m_protocol = protocol;
		m_facility = facility;
	}
	
	public String getName() {
		return m_destinationName;
	}
	
	public void setName(String name) {
		m_destinationName = name;
	}
	
	public String getHost() {
		return m_host;
	}

	public void setHost(String m_host) {
		this.m_host = m_host;
	}

	public int getPort() {
		return m_port;
	}

	public void setPort(int m_port) {
		this.m_port = m_port;
	}

	public SyslogProtocol getProtocol() {
		return m_protocol;
	}

	public void setProtocol(SyslogProtocol m_protocol) {
		this.m_protocol = m_protocol;
	}

	public SyslogFacility getFacility() {
		return m_facility;
	}
	
	public String getCharSet() {
		return m_charSet;
	}
	
	public void setCharSet(String charSet) {
		m_charSet = charSet;
	}

	public int getMaxMessageLength() {
		return m_maxMessageLength;
	}
	
	public void setMaxMessageLength(int maxMessageLength) {
		m_maxMessageLength = maxMessageLength;
	}

	public boolean isSendLocalName() {
		return m_sendLocalName;
	}
	
	public void setSendLocalName(boolean sendLocalName) {
		m_sendLocalName = sendLocalName;
	}
	
	public boolean isSendLocalTime() {
		return m_sendLocalTime;
	}
	
	public void setSendLocalTime(boolean sendLocalTime) {
		m_sendLocalTime = sendLocalTime;
	}

	public boolean isTruncateMessage() {
		return m_truncateMessage;
	}
	
	public void setTruncateMessage(boolean truncateMessage) {
		m_truncateMessage = truncateMessage;
	}

	public boolean isFirstOccurrenceOnly() {
		return m_firstOccurrenceOnly;
	}

	public void setFirstOccurrenceOnly(boolean firstOccurrenceOnly) {
		m_firstOccurrenceOnly = firstOccurrenceOnly;
	}
	
}
