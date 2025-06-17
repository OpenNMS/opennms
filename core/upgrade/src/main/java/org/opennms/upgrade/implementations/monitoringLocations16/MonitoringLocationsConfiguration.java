/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
