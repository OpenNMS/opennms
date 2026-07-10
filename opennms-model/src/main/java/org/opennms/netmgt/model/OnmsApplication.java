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
package org.opennms.netmgt.model;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonBackReference;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;

import com.google.common.base.MoreObjects;

@Entity
@Table(name = "applications")
@XmlRootElement(name="application")
/**
 * An Application is a grouping of services that belong together.
 * They can run in different locations.
 * An example would be "website", or "database".
 */
public class OnmsApplication implements Comparable<OnmsApplication> {

    private Integer id;

    private String name;

    private Set<OnmsMonitoredService> monitoredServices = new LinkedHashSet<>();

    /**
     * These are locations from where the application is monitored.
     */
    private Set<OnmsMonitoringLocation> perspectiveLocations = new LinkedHashSet<>();

    @Id
    @Column(nullable=false)
    @SequenceGenerator(name = "opennmsSequence", sequenceName = "opennmsNxtId", allocationSize = 1)
    @GeneratedValue(generator = "opennmsSequence")
    @XmlAttribute
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Column(name = "name", length=32, nullable=false, unique=true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ManyToMany(
                mappedBy="applications",
                cascade={CascadeType.PERSIST, CascadeType.MERGE}
    )
    @XmlIDREF
    @XmlElement(name="monitoredServiceId")
    @XmlElementWrapper(name="monitoredServices")
    @JsonBackReference
    public Set<OnmsMonitoredService> getMonitoredServices() {
        return monitoredServices;
    }

    public void setMonitoredServices(Set<OnmsMonitoredService> services) {
        monitoredServices = services;
    }

    public void addMonitoredService(OnmsMonitoredService service) {
        getMonitoredServices().add(service);
    }

    public void removeMonitoredService(OnmsMonitoredService service) {
        getMonitoredServices().remove(service);
    }


    @ManyToMany( cascade={CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name="application_perspective_location_map",
            joinColumns=@JoinColumn(name="appid", referencedColumnName = "id"),
            inverseJoinColumns=@JoinColumn(name="monitoringlocationid", referencedColumnName = "id"))
    @XmlIDREF
    @XmlElement(name="perspectiveLocationId")
    @XmlElementWrapper(name="perspectiveLocations")
    public Set<OnmsMonitoringLocation> getPerspectiveLocations() {
        return this.perspectiveLocations;
    }

    public void setPerspectiveLocations(Set<OnmsMonitoringLocation> perspectiveLocations) {
        this.perspectiveLocations = perspectiveLocations;
    }

    public void addPerspectiveLocation(OnmsMonitoringLocation perspectiveLocation) {
        getPerspectiveLocations().add(perspectiveLocation);
    }

    @Override
    public int compareTo(OnmsApplication o) {
        return getName().compareTo(o.getName());
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
        .add("id", getId())
        .add("name", getName())
        .toString();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof OnmsApplication) {
            OnmsApplication app = (OnmsApplication)obj;
            return getName().equals(app.getName());
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return getName().hashCode();
    }

}
