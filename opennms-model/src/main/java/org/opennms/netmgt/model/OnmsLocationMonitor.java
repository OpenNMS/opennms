/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model;

import java.util.Date;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.MapKey;
import org.springframework.core.style.ToStringCreator;

/**
 * Represents the current status of a location monitor from the
 * view of the controlling OpenNMS daemon.
 *
 * Note: this class has a natural ordering that is inconsistent
 * with equals.
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
@Entity
@Table(name = "location_monitors")
public class OnmsLocationMonitor implements Comparable<OnmsLocationMonitor> {
    
    public static enum MonitorStatus {
    	/** @deprecated */
        NEW,
        REGISTERED,
        STARTED,
        STOPPED,
        /** @deprecated */
        UNRESPONSIVE,
        DISCONNECTED,
        PAUSED,
        CONFIG_CHANGED, 
        DELETED
    }

    private Integer m_id;

    //private String m_name;
    
    private MonitorStatus m_status = MonitorStatus.REGISTERED;
    
    private Date m_lastCheckInTime;

    /*
     * Needed for locating XML-configured location definition and
     * creating m_locationDefintion.
     */
    private String m_definitionName;

    //private OnmsMonitoringLocationDefinition m_locationDefinition;

    private Map<String, String> m_details;

    /**
     * <p>getId</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Id
    @Column(nullable=false)
    @SequenceGenerator(name = "opennmsSequence", sequenceName = "opennmsNxtId")
    @GeneratedValue(generator = "opennmsSequence")
    public Integer getId() {
        return m_id;
    }

    /**
     * <p>setId</p>
     *
     * @param id a {@link java.lang.Integer} object.
     */
    public void setId(Integer id) {
        m_id = id;
    }

    /**
     * <p>getDefinitionName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name = "definitionName", length = 31, nullable = false)
    public String getDefinitionName() {
        return m_definitionName;
    }

    /**
     * <p>setDefinitionName</p>
     *
     * @param definitionName a {@link java.lang.String} object.
     */
    public void setDefinitionName(String definitionName) {
        m_definitionName = definitionName;
    }

    /**
     * <p>getStatus</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsLocationMonitor.MonitorStatus} object.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length=31, nullable=false)
    public MonitorStatus getStatus() {
        return normalize(m_status);
    }

    /**
     * <p>setStatus</p>
     *
     * @param status a {@link org.opennms.netmgt.model.OnmsLocationMonitor.MonitorStatus} object.
     */
    public void setStatus(MonitorStatus status) {
    	m_status = normalize(status);
    }
    
    private MonitorStatus normalize(MonitorStatus status) {
    	switch(status) {
    	case UNRESPONSIVE:
    		return MonitorStatus.DISCONNECTED;
    	case NEW:
    		return MonitorStatus.REGISTERED;
    	default:
    		return status;
    	}
    }
    
    /**
     * <p>getLastCheckInTime</p>
     *
     * @return a {@link java.util.Date} object.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "lastCheckInTime")
    public Date getLastCheckInTime() {
        return m_lastCheckInTime;
    }
    
    /**
     * <p>setLastCheckInTime</p>
     *
     * @param lastCheckInTime a {@link java.util.Date} object.
     */
    public void setLastCheckInTime(Date lastCheckInTime) {
        m_lastCheckInTime = lastCheckInTime;
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Transient
    public String getName() {
        return m_definitionName+'-'+getId(); 
    }

    /**
     * <p>getDetails</p>
     *
     * @return a {@link java.util.Map} object.
     */
    @CollectionOfElements
    @JoinTable(name="location_monitor_details", joinColumns = @JoinColumn(name="locationMonitorId"))
    @MapKey(columns=@Column(name="property"))
    @Column(name="propertyValue", nullable=false)
    public Map<String, String> getDetails() {
        return m_details;
    }

    /**
     * <p>setDetails</p>
     *
     * @param pollerDetails a {@link java.util.Map} object.
     */
    public void setDetails(Map<String, String> pollerDetails) {
        m_details = pollerDetails;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return new ToStringCreator(this)
        .append("id", m_id)
        .append("status", m_status)
        .toString();
    }

    /**
     * <p>compareTo</p>
     *
     * @param o a {@link org.opennms.netmgt.model.OnmsLocationMonitor} object.
     * @return a int.
     */
    @Override
    public int compareTo(OnmsLocationMonitor o) {
        int diff = getDefinitionName().compareTo(o.getDefinitionName());
        if (diff != 0) {
            return diff;
        }
        return getId().compareTo(o.getId());
    }
    
}
