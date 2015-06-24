/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>OnmsMonitoringLocationDefinition class.</p>
 */
@XmlRootElement
public class OnmsMonitoringLocationDefinition implements Serializable {

    private static final long serialVersionUID = -8271267205352524478L;

    private String m_area;
    private String m_name;
    private String m_collectionPackageName;
    private String m_pollingPackageName;
    private String m_geolocation;
    private String m_coordinates;
    private Long m_priority;
    private Set<String> m_tags = new HashSet<String>();
    
    /**
     * <p>Constructor for OnmsMonitoringLocationDefinition.</p>
     */
    public OnmsMonitoringLocationDefinition() {
        
    }
    
    /**
     * <p>Constructor for OnmsMonitoringLocationDefinition.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param pollingPackageName a {@link java.lang.String} object.
     */
    public OnmsMonitoringLocationDefinition(final String name, final String pollingPackageName) {
        m_name = name;
        m_pollingPackageName = pollingPackageName;
    }
    
    /**
     * <p>Constructor for OnmsMonitoringLocationDefinition.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param pollingPackageName a {@link java.lang.String} object.
     * @param area a {@link java.lang.String} object.
     */
    public OnmsMonitoringLocationDefinition(final String name, final String pollingPackageName, final String area) {
        m_name = name;
        m_pollingPackageName = pollingPackageName;
        m_area = area;
    }

    /**
     * <p>Constructor for OnmsMonitoringLocationDefinition.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param pollingPackageName a {@link java.lang.String} object.
     * @param area a {@link java.lang.String} object.
     * @param geolocation a {@link java.lang.String} object.
     */
    public OnmsMonitoringLocationDefinition(final String name, final String pollingPackageName, final String area, final String geolocation) {
    	m_name = name;
    	m_pollingPackageName = pollingPackageName;
    	m_area = area;
    	m_geolocation = geolocation;
    }

    /**
     * <p>Constructor for OnmsMonitoringLocationDefinition.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param pollingPackageName a {@link java.lang.String} object.
     * @param area a {@link java.lang.String} object.
     * @param geolocation a {@link java.lang.String} object.
     * @param coordinates a {@link java.lang.String} object.
     */
    public OnmsMonitoringLocationDefinition(final String name, final String pollingPackageName, final String area, final String geolocation, final String coordinates) {
    	m_name = name;
    	m_pollingPackageName = pollingPackageName;
    	m_area = area;
    	m_geolocation = geolocation;
    	m_coordinates = coordinates;
    }

    /**
     * <p>getArea</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getArea() {
        return m_area;
    }

    /**
     * <p>setArea</p>
     *
     * @param area a {@link java.lang.String} object.
     */
    public void setArea(final String area) {
        m_area = area;
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @XmlElement(name="name")
    public String getName() {
        return m_name;
    }

    /**
     * <p>setName</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public void setName(final String name) {
        m_name = name;
    }

    /**
     * <p>getCollectionPackageName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getCollectionPackageName() {
        return m_collectionPackageName;
    }

    /**
     * <p>setCollectionPackageName</p>
     *
     * @param collectionPackageName a {@link java.lang.String} object.
     */
    public void setCollectionPackageName(final String collectionPackageName) {
        m_collectionPackageName = collectionPackageName;
    }

    /**
     * <p>getPollingPackageName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPollingPackageName() {
        return m_pollingPackageName;
    }

    /**
     * <p>setPollingPackageName</p>
     *
     * @param pollingPackageName a {@link java.lang.String} object.
     */
    public void setPollingPackageName(final String pollingPackageName) {
        m_pollingPackageName = pollingPackageName;
    }
    
	/**
	 * <p>getGeolocation</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getGeolocation() {
		return m_geolocation;
	}

	/**
	 * <p>setGeolocation</p>
	 *
	 * @param location a {@link java.lang.String} object.
	 */
	public void setGeolocation(final String location) {
		m_geolocation = location;
	}
	
	/**
	 * <p>getCoordinates</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getCoordinates() {
		return m_coordinates;
	}

	/**
	 * <p>setCoordinates</p>
	 *
	 * @param coordinates a {@link java.lang.String} object.
	 */
	public void setCoordinates(final String coordinates) {
		m_coordinates = coordinates;
	}
	
	/**
	 * <p>getPriority</p>
	 *
	 * @return a {@link java.lang.Long} object.
	 */
	public Long getPriority() {
		return m_priority;
	}
	
	/**
	 * <p>setPriority</p>
	 *
	 * @param priority a {@link java.lang.Long} object.
	 */
	public void setPriority(final Long priority) {
		m_priority = priority;
	}
	
	/**
	 * <p>getTags</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	public Set<String> getTags() {
		return m_tags;
	}
	
	/**
	 * <p>setTags</p>
	 *
	 * @param tags a {@link java.util.Set} object.
	 */
	public void setTags(final Set<String> tags) {
		m_tags = tags;
	}

	/** {@inheritDoc} */
	@Override
    public String toString() {
        return "OnmsMonitoringLocationDefinition@" + Integer.toHexString(hashCode()) + ": Name \"" + m_name + "\", polling package name \"" + m_pollingPackageName + "\", area \"" + m_area + "\", geolocation \"" + m_geolocation + "\", coordinates \"" + m_coordinates + "\"";
    }
}
