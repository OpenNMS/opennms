/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.alarmd.northbounder.syslog;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Configuration for Syslog NBI implementation.
 * FIXME: This needs lots of work.
 * 
 * @author <a mailto:david@opennms.org>David Hustace</a>
 */
@XmlRootElement(name="syslog-northbounder-config")
@XmlAccessorType(XmlAccessType.FIELD)
public class SyslogNorthbounderConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlElement(name="enabled", required=false, defaultValue="false")
	private Boolean m_enabled;
    
    @XmlElement(name="nagles-delay", required=false, defaultValue="1000")
    private Integer m_naglesDelay = 1000;
    
    @XmlElement(name="batch-size", required=false, defaultValue="100")
    private Integer m_batchSize = 100;
    
    @XmlElement(name="queue-size", required=false, defaultValue="300000")
    private Integer m_queueSize = 300000;
    
    @XmlElement(name="message-format", required=false, defaultValue="ALARM ID:${alarmId} NODE:${nodeLabel} ${logMsg}")
    private String m_messageFormat = "ALARM ID:${alarmId} NODE:${nodeLabel} ${logMsg}";
    
    @XmlElement(name="destination")
    private List<SyslogDestination> m_destinations;
    
    @XmlElement(name="uei", required = false)
    private List<String> m_ueis;

    //Getters & Setters
    public List<SyslogDestination> getDestinations() {
    	return m_destinations;
    }
    public void setDestinations(List<SyslogDestination> destinations) {
    	m_destinations = destinations;
    }
    
    public List<String> getUeis() {
    	return m_ueis;
    }
    public void setUeis(List<String> ueis) {
    	m_ueis = ueis;
    }
    
	public String getMessageFormat() {
		return m_messageFormat;
	}
	
	public void setMessageFormat(String messageFormat) {
		m_messageFormat = messageFormat;
	}
	
	public Integer getNaglesDelay() {
		return m_naglesDelay;
	}
	
	public void setNaglesDelay(Integer naglesDelay) {
		m_naglesDelay = naglesDelay;
	}
	
	public Integer getBatchSize() {
		return m_batchSize;
	}
	
	public void setBatchSize(Integer batchSize) {
		m_batchSize = batchSize;
	}
	
	public Integer getQueueSize() {
		return m_queueSize;
	}
	
	public void setQueueSize(Integer alarmQueueSize) {
		m_queueSize = alarmQueueSize;
	}
	public Boolean isEnabled() {
		return m_enabled;
	}
	public void setEnabled(Boolean enabled) {
		m_enabled = enabled;
	}
	
	public SyslogDestination getDestination(String destinationName) {
		SyslogDestination destination = null;
		for (SyslogDestination dest : m_destinations) {
			if (dest.getName().equals(destinationName)) {
				destination = dest;
			}
		}
		return destination;
	}

}
