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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>
 * This element contains the name of the location, the name of the
 * monitoring area (used to aggregate locations, example: Area San Francisco,
 * location name "SFO" which becomes SFO-1 or SFO-BuildingA, etc.)
 * Additionally, a geolocation can be provided (an address or other
 * identifying location that can be looked up with a geolocation
 *  API), as well as coordinates (latitude,longitude). Finally, a
 * priority can be assigned to the location, for purposes of sorting
 * (1 = highest, 100 = lowest).
 * </p>
 * <p>
 * The polling package name is used to associate with a polling
 * configuration found in the polling-configuration.xml file. 
 * </p>
 * <p>
 * The collection package name is used to associate with a collection
 * configuration found in the collectd-configuration.xml file.
 */

@XmlRootElement(name="location-def")
@XmlAccessorType(XmlAccessType.NONE)
public class LocationDef implements Serializable {
    private static final long serialVersionUID = -7651610012389148818L;

    /**
     * The name of the location.  This must be a unique identifier.
     */
    @XmlAttribute(name="location-name")
    private String m_locationName;

    /**
     * The name of the monitoring area.  This field is used to group
     * multiple locations together, ie, a region, or abstract category.
     */
    @XmlAttribute(name="monitoring-area")
    private String m_monitoringArea;

    /**
     * The geolocation (address) of this monitoring location.
     */
    @XmlAttribute(name="geolocation")
    private String m_geolocation;

    /**
     * The coordinates (latitude,longitude) of this monitoring location.
     */
    @XmlAttribute(name="coordinates")
    private String m_coordinates;

    /**
     * The priority of the location. (1=highest)
     */
    @XmlAttribute(name="priority")
    private Long m_priority;

    @XmlElementWrapper(name="tags")
    @XmlElement(name="tag")
    private List<Tag> m_tags;

    public LocationDef() {
        super();
    }

    public LocationDef(final String locationName, final String monitoringArea, final String pollingPackageName, final String collectionPackageName, final String geolocation, final String coordinates, final Long priority, final String... tags) {
        this();
        m_locationName = locationName;
        m_monitoringArea = monitoringArea;
        m_geolocation = geolocation;
        m_coordinates = coordinates;
        m_priority = priority;
        for (final String tag : tags) {
            if (m_tags == null) {
                m_tags = new ArrayList<Tag>(tags.length);
            }
            m_tags.add(new Tag(tag));
        }
    }

    public String getLocationName() {
        return m_locationName;
    }

    public void setLocationName(final String locationName) {
        m_locationName = locationName;
    }

    public String getMonitoringArea() {
        return m_monitoringArea;
    }

    public void setMonitoringArea(final String monitoringArea) {
        m_monitoringArea = monitoringArea;
    }

    public String getGeolocation() {
        return m_geolocation;
    }

    public void setGeolocation(final String geolocation) {
        m_geolocation = geolocation;
    }

    public String getCoordinates() {
        return m_coordinates;
    }

    public void setCoordinates(final String coordinates) {
        m_coordinates = coordinates;
    }

    public Long getPriority() {
        return m_priority == null? 100L : m_priority;
    }

    public void setPriority(final Long priority) {
        m_priority = priority;
    }

    public List<Tag> getTags() {
        if (m_tags == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(m_tags);
        }
    }

    public void setTags(final List<Tag> tags) {
        if (tags == null || tags.size() == 0) {
            m_tags = null;
        } else {
            m_tags = new ArrayList<Tag>(tags);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 353;
        int result = 1;
        result = prime * result + ((m_coordinates == null) ? 0 : m_coordinates.hashCode());
        result = prime * result + ((m_geolocation == null) ? 0 : m_geolocation.hashCode());
        result = prime * result + ((m_locationName == null) ? 0 : m_locationName.hashCode());
        result = prime * result + ((m_monitoringArea == null) ? 0 : m_monitoringArea.hashCode());
        result = prime * result + ((m_priority == null) ? 0 : m_priority.hashCode());
        result = prime * result + ((m_tags == null || m_tags.size() == 0) ? 0 : m_tags.hashCode());
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
        if (!(obj instanceof LocationDef)) {
            return false;
        }
        final LocationDef other = (LocationDef) obj;
        if (m_coordinates == null) {
            if (other.m_coordinates != null) {
                return false;
            }
        } else if (!m_coordinates.equals(other.m_coordinates)) {
            return false;
        }
        if (m_geolocation == null) {
            if (other.m_geolocation != null) {
                return false;
            }
        } else if (!m_geolocation.equals(other.m_geolocation)) {
            return false;
        }
        if (m_locationName == null) {
            if (other.m_locationName != null) {
                return false;
            }
        } else if (!m_locationName.equals(other.m_locationName)) {
            return false;
        }
        if (m_monitoringArea == null) {
            if (other.m_monitoringArea != null) {
                return false;
            }
        } else if (!m_monitoringArea.equals(other.m_monitoringArea)) {
            return false;
        }
        if (m_priority == null) {
            if (other.m_priority != null) {
                return false;
            }
        } else if (!m_priority.equals(other.m_priority)) {
            return false;
        }
        if (m_tags == null || m_tags.size() == 0) {
            if (other.m_tags != null && other.m_tags.size() > 0) {
                return false;
            }
        } else {
            if (other.m_tags == null || other.m_tags.size() == 0) {
                return false;
            } else if (!m_tags.equals(other.m_tags)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "OnmsMonitoringLocation [location-name=" + m_locationName +
                ", monitoring-area=" + m_monitoringArea +
                ", geolocation=" + m_geolocation +
                ", coordinates=" + m_coordinates +
                ", priority=" + m_priority +
                ", tags=" + m_tags + "]";
    }

}
