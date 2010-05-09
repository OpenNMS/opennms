//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class OnmsMonitoringLocationDefinition implements Serializable {

    private static final long serialVersionUID = 2L;

    private String m_area;
    private String m_name;
    private String m_pollingPackageName;
    private String m_geolocation;
    private String m_coordinates;
    private Long m_priority;
    private Set<String> m_tags = new HashSet<String>();
    
    public OnmsMonitoringLocationDefinition() {
        
    }
    
    public OnmsMonitoringLocationDefinition(final String name, final String pollingPackageName) {
        m_name = name;
        m_pollingPackageName = pollingPackageName;
    }
    
    public OnmsMonitoringLocationDefinition(final String name, final String pollingPackageName, final String area) {
        m_name = name;
        m_pollingPackageName = pollingPackageName;
        m_area = area;
    }

    public OnmsMonitoringLocationDefinition(final String name, final String pollingPackageName, final String area, final String geolocation) {
    	m_name = name;
    	m_pollingPackageName = pollingPackageName;
    	m_area = area;
    	m_geolocation = geolocation;
    }

    public OnmsMonitoringLocationDefinition(final String name, final String pollingPackageName, final String area, final String geolocation, final String coordinates) {
    	m_name = name;
    	m_pollingPackageName = pollingPackageName;
    	m_area = area;
    	m_geolocation = geolocation;
    	m_coordinates = coordinates;
    }

    public String getArea() {
        return m_area;
    }

    public void setArea(final String area) {
        m_area = area;
    }

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = name;
    }

    public String getPollingPackageName() {
        return m_pollingPackageName;
    }

    public void setPollingPackageName(final String pollingPackageName) {
        m_pollingPackageName = pollingPackageName;
    }
    
	public String getGeolocation() {
		return m_geolocation;
	}

	public void setGeolocation(final String location) {
		m_geolocation = location;
	}
	
	public String getCoordinates() {
		return m_coordinates;
	}

	public void setCoordinates(final String coordinates) {
		m_coordinates = coordinates;
	}
	
	public Long getPriority() {
		return m_priority;
	}
	
	public void setPriority(final Long priority) {
		m_priority = priority;
	}
	
	public Set<String> getTags() {
		return m_tags;
	}
	
	public void setTags(final Set<String> tags) {
		m_tags = tags;
	}

	@Override
    public String toString() {
        return "OnmsMonitoringLocationDefinition@" + Integer.toHexString(hashCode()) + ": Name \"" + m_name + "\", polling package name \"" + m_pollingPackageName + "\", area \"" + m_area + "\", geolocation \"" + m_geolocation + "\", coordinates \"" + m_coordinates + "\"";
    }
}
