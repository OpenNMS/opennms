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

@XmlRootElement(name = "application-status")
public class ApplicationStatus {
    private Integer applicationId;

    private Long start;

    private Long end;

    private List<Location> locations = new ArrayList<>();

    private Double overallStatus;

    public ApplicationStatus() {
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

    @XmlElement(name = "location")
    public List<Location> getLocations() {
        return locations;
    }

    public Location getLocation(final String location) {
        return locations.stream()
                .filter(e -> location.equals(e.getName()))
                .findFirst()
                .orElse(null);
    }

    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApplicationStatus that = (ApplicationStatus) o;
        return Objects.equals(applicationId, that.applicationId) &&
                Objects.equals(start, that.start) &&
                Objects.equals(end, that.end) &&
                Objects.equals(locations, that.locations);
    }

    @XmlElement(name = "overallStatus")
    public Double getOverallStatus() {
        return overallStatus;
    }

    public void setOverallStatus(final Double overallStatus) {
        this.overallStatus = overallStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(applicationId, start, end, locations, overallStatus);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("applicationId", applicationId)
                .add("overallStatus", overallStatus)
                .add("start", start)
                .add("end", end)
                .add("locations", locations)
                .toString();
    }
}
