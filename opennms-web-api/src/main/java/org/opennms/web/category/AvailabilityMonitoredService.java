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
package org.opennms.web.category;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.netmgt.model.OnmsMonitoredService;

@XmlRootElement(name="service")
@XmlAccessorType(XmlAccessType.NONE)
public class AvailabilityMonitoredService {
    @XmlAttribute(name="id")
    private final Integer m_id;

    @XmlAttribute(name="name")
    private final String m_name;

    /**
     * Service type id, as distinct from m_id, which is the ifservices row id. Carried so a client
     * can build a service detail link without a separate lookup of the service type by name.
     */
    @XmlAttribute(name="serviceId")
    private final Integer m_serviceId;

    @XmlAttribute(name="availability")
    private final double m_availability;

    @XmlAttribute(name="up")
    private final boolean up;

    public AvailabilityMonitoredService() {
        m_id = -1;
        m_name = "";
        m_serviceId = -1;
        m_availability = -1d;
        this.up = false;
    }

    public AvailabilityMonitoredService(final OnmsMonitoredService svc, final double availability, final boolean up) {
        m_id = svc.getId();
        m_name = svc.getServiceName();
        m_serviceId = svc.getServiceId();
        m_availability = availability;
        this.up = up;
    }

    public Integer getId() {
        return m_id;
    }

    public double getAvailability() {
        return m_availability;
    }

    public boolean isUp() {
        return this.up;
    }

    public String getName() {
        return m_name;
    }

    public Integer getServiceId() {
        return m_serviceId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", this.getId())
            .append("availability", this.getAvailability())
            .append("up", this.isUp())
            .append("name", this.getName())
            .append("serviceId", this.getServiceId())
            .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AvailabilityMonitoredService)) return false;
        final AvailabilityMonitoredService that = (AvailabilityMonitoredService)o;
        return new EqualsBuilder()
            .append(this.getId(), that.getId())
            .append(this.getAvailability(), that.getAvailability())
            .append(this.isUp(), that.isUp())
            .append(this.getName(), that.getName())
            .isEquals();
    }
}
