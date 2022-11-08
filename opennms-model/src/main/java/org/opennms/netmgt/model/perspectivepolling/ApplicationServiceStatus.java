/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model.perspectivepolling;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.MoreObjects;

@XmlRootElement(name = "application-service-status")
public class ApplicationServiceStatus {
    private Integer applicationId;

    private Integer monitoredServiceId;

    private Long start;

    private Long end;

    private List<Location> locations = new ArrayList<>();

    public ApplicationServiceStatus() {
    }

    @XmlAttribute(name="applicationId")
    public Integer getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(final Integer applicationId) {
        this.applicationId = applicationId;
    }

    @XmlAttribute(name="start")
    public Long getStart() {
        return start;
    }

    public void setStart(final Long start) {
        this.start = start;
    }

    @XmlAttribute(name="end")
    public Long getEnd() {
        return end;
    }

    public void setEnd(final Long end) {
        this.end = end;
    }

    @XmlAttribute(name="monitoredServiceId")
    public Integer getMonitoredServiceId() {
        return monitoredServiceId;
    }

    public void setMonitoredServiceId(final Integer monitoredServiceId) {
        this.monitoredServiceId = monitoredServiceId;
    }

    @XmlElement(name = "location")
    public List<Location> getLocations() {
        return locations;
    }

    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApplicationServiceStatus that = (ApplicationServiceStatus) o;
        return Objects.equals(applicationId, that.applicationId) &&
                Objects.equals(monitoredServiceId, that.monitoredServiceId) &&
                Objects.equals(start, that.start) &&
                Objects.equals(end, that.end) &&
                Objects.equals(locations, that.locations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(applicationId, monitoredServiceId, start, end, locations);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("applicationId", applicationId)
                .add("monitoredServiceId", monitoredServiceId)
                .add("start", start)
                .add("end", end)
                .add("locations", locations)
                .toString();
    }
}
