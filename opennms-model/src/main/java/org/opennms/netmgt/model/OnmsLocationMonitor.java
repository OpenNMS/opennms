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

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;

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
@DiscriminatorValue(OnmsMonitoringSystem.TYPE_REMOTE_POLLER)
@XmlRootElement(name="locationMonitor")
public class OnmsLocationMonitor extends OnmsMonitoringSystem implements Comparable<OnmsLocationMonitor> {

    private static final long serialVersionUID = 7923969022838262552L;

    public static enum MonitorStatus {
        REGISTERED,
        STARTED,
        STOPPED,
        DISCONNECTED,
        PAUSED,
        CONFIG_CHANGED,
        DELETED
    }

    private MonitorStatus m_status = MonitorStatus.REGISTERED;

    /**
     * <p>getStatus</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsLocationMonitor.MonitorStatus} object.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length=31, nullable=false)
    public MonitorStatus getStatus() {
        return m_status;
    }

    /**
     * <p>setStatus</p>
     *
     * @param status a {@link org.opennms.netmgt.model.OnmsLocationMonitor.MonitorStatus} object.
     */
    public void setStatus(MonitorStatus status) {
    	m_status = status;
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Transient
    public String getName() {
        return getLocation() + '-' + getId(); 
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return new ToStringCreator(this)
        .append("id", getId())
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
        int diff = getLocation().compareTo(o.getLocation());
        if (diff != 0) {
            return diff;
        }
        return getId().compareTo(o.getId());
    }
}
