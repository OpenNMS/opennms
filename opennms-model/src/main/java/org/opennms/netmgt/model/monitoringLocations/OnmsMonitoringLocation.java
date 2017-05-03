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

package org.opennms.netmgt.model.monitoringLocations;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.EqualsBuilder;

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
@Entity
@Table(name="monitoringLocations")
@XmlRootElement(name="location")
@XmlAccessorType(XmlAccessType.NONE)
public class OnmsMonitoringLocation implements Serializable {
    private static final long serialVersionUID = -7651610012389148818L;

    /**
     * The name of the location.  This must be a unique identifier.
     */
    private String m_locationName;

    /**
     * The name of the monitoring area.  This field is used to group
     * multiple locations together, ie, a region, or abstract category.
     */
    private String m_monitoringArea;

    /**
     * The polling packages associated with this monitoring location.
     */
    private List<String> m_pollingPackageNames;

    /**
     * The collection packages associated with this monitoring location.
     */
    private List<String> m_collectionPackageNames;

    /**
     * The geolocation (address) of this monitoring location.
     */
    private String m_geolocation;

    /**
     * The latitude of this monitoring location.
     */
    private Float m_longitude;

    /**
     * The latitude of this monitoring location.
     */
    private Float m_latitude;

    /**
     * The priority of the location. (1=highest)
     */
    private Long m_priority;

    private List<String> m_tags;

    public OnmsMonitoringLocation() {
        super();
    }

    /**
     * This constructor is only used during unit testing.
     * 
     * @param locationName
     * @param monitoringArea
     * @param pollingPackageName
     */
    public OnmsMonitoringLocation(final String locationName, final String monitoringArea) {
        this(locationName, monitoringArea, null, null, null, null, null, null);
    }

    /**
     * This constructor is only used during unit testing.
     * 
     * @param locationName
     * @param monitoringArea
     * @param pollingPackageName
     */
    public OnmsMonitoringLocation(final String locationName, final String monitoringArea, final String pollingPackageName) {
        this(locationName, monitoringArea, null, new String[] { pollingPackageName }, null, null, null, null);
    }

    public OnmsMonitoringLocation(final String locationName, final String monitoringArea, final String[] pollingPackageNames, final String[] collectionPackageNames, final String geolocation, final Float latitude, final Float longitude, final Long priority, final String... tags) {
        m_locationName = locationName;
        m_monitoringArea = monitoringArea;
        m_pollingPackageNames = (pollingPackageNames == null ? Collections.emptyList() : Arrays.asList(pollingPackageNames));
        m_collectionPackageNames = (collectionPackageNames == null ? Collections.emptyList() : Arrays.asList(collectionPackageNames));
        m_geolocation = geolocation;
        m_latitude = latitude;
        m_longitude = longitude;
        m_priority = priority;
        // Because tags is a vararg, if you have no arguments for it, it comes in as String[0]
        m_tags = ((tags == null || tags.length == 0) ? Collections.emptyList() : Arrays.asList(tags));
    }

    @XmlID
    @XmlAttribute(name="location-name")
    @Id
    @Column(name="id", nullable=false)
    public String getLocationName() {
        return m_locationName;
    }

    public void setLocationName(final String locationName) {
        m_locationName = locationName;
    }

    @XmlAttribute(name="monitoring-area")
    @Column(name="monitoringArea", nullable=false)
    public String getMonitoringArea() {
        return m_monitoringArea;
    }

    public void setMonitoringArea(final String monitoringArea) {
        m_monitoringArea = monitoringArea;
    }

    @XmlElementWrapper(name="polling-package-names")
    @XmlElement(name="polling-package-name")
    @ElementCollection
    @JoinTable(name="monitoringLocationsPollingPackages", joinColumns = @JoinColumn(name="monitoringLocationId"))
    @Column(name="packageName")
    public List<String> getPollingPackageNames() {
        return m_pollingPackageNames;
    }

    public void setPollingPackageNames(final List<String> pollingPackageNames) {
        m_pollingPackageNames = pollingPackageNames;
    }

    @XmlElementWrapper(name="collection-package-names")
    @XmlElement(name="collection-package-name")
    @ElementCollection
    @JoinTable(name="monitoringLocationsCollectionPackages", joinColumns = @JoinColumn(name="monitoringLocationId"))
    @Column(name="packageName")
    public List<String> getCollectionPackageNames() {
        return m_collectionPackageNames;
    }

    public void setCollectionPackageNames(final List<String> collectionPackageNames) {
        m_collectionPackageNames = collectionPackageNames;
    }

    @XmlAttribute(name="geolocation")
    @Column(name="geolocation")
    public String getGeolocation() {
        return m_geolocation;
    }

    public void setGeolocation(final String geolocation) {
        m_geolocation = geolocation;
    }

    /**
     * The longitude coordinate of this node.
     * @return
     */
    @XmlAttribute(name="longitude")
    @Column(name="longitude")
    public Float getLongitude() {
        return m_longitude;
    }

    public void setLongitude(final Float longitude) {
        m_longitude = longitude;
    }

    /**
     * The latitude coordinate of this node.
     * @return
     */
    @XmlAttribute(name="latitude")
    @Column(name="latitude")
    public Float getLatitude() {
        return m_latitude;
    }

    public void setLatitude(final Float latitude) {
        m_latitude = latitude;
    }

    @XmlAttribute(name="priority")
    @Column(name="priority")
    public Long getPriority() {
        return m_priority == null ? 100L : m_priority;
    }

    public void setPriority(final Long priority) {
        m_priority = priority;
    }

    @XmlElementWrapper(name="tags")
    @XmlElement(name="tag")
    @ElementCollection
    @JoinTable(name="monitoringLocationsTags", joinColumns = @JoinColumn(name="monitoringLocationId"))
    @Column(name="tag")
    public List<String> getTags() {
        if (m_tags == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(m_tags);
        }
    }

    public void setTags(final List<String> tags) {
        if (tags == null || tags.size() == 0) {
            m_tags = Collections.emptyList();
        } else {
            m_tags = new ArrayList<String>(tags);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 353;
        int result = 1;
        result = prime * result + ((m_latitude == null) ? 0 : m_latitude.hashCode());
        result = prime * result + ((m_longitude == null) ? 0 : m_longitude.hashCode());
        result = prime * result + ((m_geolocation == null) ? 0 : m_geolocation.hashCode());
        result = prime * result + ((m_locationName == null) ? 0 : m_locationName.hashCode());
        result = prime * result + ((m_monitoringArea == null) ? 0 : m_monitoringArea.hashCode());
        result = prime * result + ((m_pollingPackageNames == null || m_pollingPackageNames.size() == 0) ? 0 : m_pollingPackageNames.hashCode());
        result = prime * result + ((m_collectionPackageNames == null || m_collectionPackageNames.size() == 0) ? 0 : m_collectionPackageNames.hashCode());
        result = prime * result + ((m_priority == null) ? 0 : m_priority.hashCode());
        result = prime * result + ((m_tags == null || m_tags.size() == 0) ? 0 : m_tags.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof OnmsMonitoringLocation)) {
            return false;
        }
        final OnmsMonitoringLocation other = (OnmsMonitoringLocation) obj;
        return new EqualsBuilder()
            .append(getLatitude(), other.getLatitude())
            .append(getLongitude(), other.getLongitude())
            .append(getGeolocation(), other.getGeolocation())
            .append(getLocationName(), other.getLocationName())
            .append(getMonitoringArea(), other.getMonitoringArea())
            .append(getPollingPackageNames(), other.getPollingPackageNames())
            .append(getCollectionPackageNames(), other.getCollectionPackageNames())
            .append(getPriority(), other.getPriority())
            .append(getTags(), other.getTags())
            .isEquals();
    }

    @Override
    public String toString() {
        return "OnmsMonitoringLocation [location-name=" + m_locationName +
                ", monitoring-area=" + m_monitoringArea +
                ", polling-package-names=" + m_pollingPackageNames +
                ", collection-package-names=" + m_collectionPackageNames +
                ", geolocation=" + m_geolocation +
                ", latitude=" + m_latitude +
                ", longitude=" + m_longitude +
                ", priority=" + m_priority +
                ", tags=" + m_tags + "]";
    }

}
