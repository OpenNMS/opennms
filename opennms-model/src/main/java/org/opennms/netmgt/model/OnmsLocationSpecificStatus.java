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

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.netmgt.poller.PollStatus;

import com.google.common.base.MoreObjects;

@Entity
@Table(name="location_specific_status_changes")
public class OnmsLocationSpecificStatus {

    private Integer m_id;
    private OnmsMonitoringLocation m_location;
    private OnmsMonitoredService m_monitoredService;
    private PollStatus m_pollResult;

    /**
     * <p>Constructor for OnmsLocationSpecificStatus.</p>
     */
    public OnmsLocationSpecificStatus() {
        // this is used by hibernate to construct an object from the db
    }

    /**
     * <p>Constructor for OnmsLocationSpecificStatus.</p>
     *
     * @param location a {@link org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation} object.
     * @param monitoredService a {@link org.opennms.netmgt.model.OnmsMonitoredService} object.
     * @param pollResult a {@link org.opennms.netmgt.poller.PollStatus} object.
     */
    public OnmsLocationSpecificStatus(final OnmsMonitoringLocation location, final OnmsMonitoredService monitoredService, final PollStatus pollResult) {
        m_location = location;
        m_monitoredService = monitoredService;
        m_pollResult = pollResult;
    }

    /**
     * <p>getId</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Id
    @Column(nullable=false)
    @SequenceGenerator(name="opennmsSequence", sequenceName="opennmsNxtId")
    @GeneratedValue(generator="opennmsSequence")
    public Integer getId() {
        return m_id;
    }

    /**
     * <p>setId</p>
     *
     * @param id a {@link java.lang.Integer} object.
     */
    public void setId(final Integer id) {
        m_id = id;
    }

    /**
     * <p>getLocationMonitor</p>
     *
     * @return a {@link org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation} object.
     */
    @JsonSerialize(using=MonitoringLocationJsonSerializer.class)
    @JsonDeserialize(using=MonitoringLocationJsonDeserializer.class)
    @XmlElement(name="location")
    @ManyToOne(optional=false, fetch=FetchType.EAGER)
    @JoinColumn(name="location")
    @XmlJavaTypeAdapter(MonitoringLocationIdAdapter.class)
    public OnmsMonitoringLocation getLocation() {
        return m_location;
    }

    /**
     * <p>setLocationMonitor</p>
     *
     * @param location a {@link org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation} object.
     */
    public void setLocation(final OnmsMonitoringLocation location) {
        m_location = location;
    }

    /**
     * <p>getMonitoredService</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsMonitoredService} object.
     */
    @ManyToOne(optional=false, fetch=FetchType.EAGER)
    @JoinColumn(name="ifServiceId")
    public OnmsMonitoredService getMonitoredService() {
        return m_monitoredService;
    }

    /**
     * <p>setMonitoredService</p>
     *
     * @param monitoredService a {@link org.opennms.netmgt.model.OnmsMonitoredService} object.
     */
    public void setMonitoredService(final OnmsMonitoredService monitoredService) {
        m_monitoredService = monitoredService;
    }

    /**
     * <p>getPollResult</p>
     *
     * @return a {@link org.opennms.netmgt.poller.PollStatus} object.
     */
    @Embedded
    public PollStatus getPollResult() {
        return m_pollResult;
    }

    /**
     * <p>setPollResult</p>
     *
     * @param newStatus a {@link org.opennms.netmgt.poller.PollStatus} object.
     */
    public void setPollResult(final PollStatus newStatus) {
        m_pollResult = newStatus;
    }

    /**
     * <p>getStatusCode</p>
     *
     * @return a int.
     */
    @Transient
    public int getStatusCode() {
        return m_pollResult.getStatusCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OnmsLocationSpecificStatus that = (OnmsLocationSpecificStatus) o;
        return Objects.equals(m_id, that.m_id) &&
                Objects.equals(m_location, that.m_location) &&
                Objects.equals(m_monitoredService, that.m_monitoredService) &&
                Objects.equals(m_pollResult, that.m_pollResult);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_id, m_location, m_monitoredService, m_pollResult);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", m_id)
                .add("location", m_location)
                .add("monitoredService", m_monitoredService)
                .add("pollResult", m_pollResult)
                .toString();
    }
}
