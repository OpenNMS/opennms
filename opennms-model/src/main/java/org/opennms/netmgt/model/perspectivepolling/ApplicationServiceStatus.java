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
