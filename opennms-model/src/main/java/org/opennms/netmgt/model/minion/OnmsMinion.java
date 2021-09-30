/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model.minion;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.model.OnmsMonitoringSystem;

/**
 * <p>The OnmsMinion represents a Minion node which has reported to OpenNMS.</p>
 */
@Entity
@DiscriminatorValue(OnmsMonitoringSystem.TYPE_MINION)
@XmlRootElement(name="minion")
public class OnmsMinion extends OnmsMonitoringSystem {

    private static final long serialVersionUID = 7512728871301272703L;

    @XmlAttribute(name="status")
    private String m_status;

    public OnmsMinion() {
    }

    public OnmsMinion(final String id, final String location, final String status, final Date lastUpdated) {
        super(id, location);
        setStatus(status);
        setLastUpdated(lastUpdated);
    }

    @Column(name="status")
    public String getStatus() {
        return m_status;
    }

    public void setStatus(final String status) {
        m_status = status;
    }

    @Override
    public String toString() {
        return "OnmsMinion [id=" + getId() + ", location=" + getLocation() + ", status=" + m_status + ", lastUpdated=" + getLastUpdated() + ", properties=" + getProperties() + "]";
    }
}
