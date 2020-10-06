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

import java.util.Objects;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.MoreObjects;

@XmlRootElement
public class Location {
    private String name;

    private String responseResourceId;

    private Double aggregatedStatus;

    public Location() {
    }

    @XmlAttribute
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @XmlElement(name="response-resource-id", required = false)
    public String getResponseResourceId() {
        return responseResourceId;
    }

    public void setResponseResourceId(final String responseResourceId) {
        this.responseResourceId = responseResourceId;
    }

    @XmlElement(name="aggregated-status")
    public Double getAggregatedStatus() {
        return aggregatedStatus;
    }

    public void setAggregatedStatus(final Double aggregatedStatus) {
        this.aggregatedStatus = aggregatedStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return Objects.equals(name, location.name) &&
                Objects.equals(responseResourceId, location.responseResourceId) &&
                Objects.equals(aggregatedStatus, location.aggregatedStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, responseResourceId, aggregatedStatus);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", name)
                .add("responseResourceId", responseResourceId)
                .add("aggregatedStatus", aggregatedStatus)
                .toString();
    }
}
