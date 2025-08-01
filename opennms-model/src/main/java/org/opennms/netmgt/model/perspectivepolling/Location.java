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
