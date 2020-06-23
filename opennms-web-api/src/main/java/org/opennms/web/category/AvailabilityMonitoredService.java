/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.category;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.netmgt.model.OnmsMonitoredService;

@XmlRootElement(name="service")
@XmlAccessorType(XmlAccessType.NONE)
public class AvailabilityMonitoredService {
    @XmlAttribute(name="id")
    private final Integer m_id;

    @XmlAttribute(name="name")
    private final String m_name;

    @XmlAttribute(name="availability")
    private final double m_availability;

    public AvailabilityMonitoredService() {
        m_id = -1;
        m_name = "";
        m_availability = -1d;
    }

    public AvailabilityMonitoredService(final OnmsMonitoredService svc, final double availability) {
        m_id = svc.getId();
        m_name = svc.getServiceName();
        m_availability = availability;
    }

    public Integer getId() {
        return m_id;
    }

    public double getAvailability() {
        return m_availability;
    }

    public String getName() {
        return m_name;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", this.getId())
            .append("availability", this.getAvailability())
            .append("name", this.getName())
            .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AvailabilityMonitoredService)) return false;
        final AvailabilityMonitoredService that = (AvailabilityMonitoredService)o;
        return new EqualsBuilder()
            .append(this.getId(), that.getId())
            .append(this.getAvailability(), that.getAvailability())
            .append(this.getName(), that.getName())
            .isEquals();
    }
}
