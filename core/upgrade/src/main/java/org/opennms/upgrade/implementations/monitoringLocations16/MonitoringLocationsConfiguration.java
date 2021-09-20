/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.upgrade.implementations.monitoringLocations16;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Top-level element for configuration remote monitoring locations.
 */

@XmlRootElement(name="monitoring-locations-configuration")
@XmlAccessorType(XmlAccessType.NONE)
//@ValidateUsing("monitoring-locations.xsd")
public class MonitoringLocationsConfiguration implements Serializable {
    private static final long serialVersionUID = 4774677097952128710L;

    @XmlElementWrapper(name="locations")
    @XmlElement(name="location-def")
    private List<LocationDef> m_locations;

    public MonitoringLocationsConfiguration() {
        super();
    }

    public List<LocationDef> getLocations() {
        if (m_locations == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(m_locations);
        }
    }

    public void setLocations(final List<LocationDef> locations) {
        if (locations == null || locations.size() == 0) {
            m_locations = null;
        } else {
            m_locations = new ArrayList<LocationDef>(locations);
        }
    }

    public LocationDef getLocation(final String location) {
        for (final LocationDef def : m_locations) {
            if (def.getLocationName().equals(location)) {
                return def;
            }
        }
        return null;
    }

    public void addLocation(final LocationDef location) {
        if (m_locations == null) {
            m_locations = new ArrayList<>();
        }
        m_locations.add(location);
    }

    public void addLocation(final String locationName, final String monitoringArea, final String pollingPackageName, final String collectionPackageName, final String geolocation, final String coordinates, final Long priority, final String... tags) {
        addLocation(new LocationDef(locationName, monitoringArea, pollingPackageName, collectionPackageName, geolocation, coordinates, priority, tags));
    }

    @Override
    public int hashCode() {
        final int prime = 383;
        int result = 1;
        result = prime * result + ((m_locations == null || m_locations.size() == 0) ? 0 : m_locations.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof MonitoringLocationsConfiguration)) {
            return false;
        }
        final MonitoringLocationsConfiguration other = (MonitoringLocationsConfiguration) obj;
        if (m_locations == null || m_locations.size() == 0) {
            if (other.m_locations != null && other.m_locations.size() > 0) {
                return false;
            }
        } else {
            if (other.m_locations == null || other.m_locations.size() == 0) {
                return false;
            } else if (!m_locations.equals(other.m_locations)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "MonitoringLocationsConfiguration [locations=" + m_locations + "]";
    }
}
