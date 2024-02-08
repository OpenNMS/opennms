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

import static org.opennms.core.utils.InetAddressUtils.str;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.netmgt.model.OnmsIpInterface;

@XmlRootElement(name="ipinterface")
@XmlAccessorType(XmlAccessType.NONE)
public class AvailabilityIpInterface {
    @XmlAttribute(name="id")
    private final Integer m_id;

    @XmlAttribute(name="address")
    private final String m_address;

    @XmlAttribute(name="availability")
    private final double m_availability;

    @XmlElementWrapper(name="services")
    @XmlElement(name="service")
    private final List<AvailabilityMonitoredService> m_services = new ArrayList<>();

    public AvailabilityIpInterface() {
        m_id = -1;
        m_address = "";
        m_availability = -1d;
    }

    public AvailabilityIpInterface(final OnmsIpInterface iface, final double availability) {
        m_id = iface.getId();
        m_address = str(iface.getIpAddress());
        m_availability = availability;
    }

    public void addService(final AvailabilityMonitoredService service) {
        m_services.add(service);
    }

    public Integer getId() {
        return m_id;
    }

    public double getAvailability() {
        return m_availability;
    }

    public String getAddress() {
        return m_address;
    }

    public List<AvailabilityMonitoredService> getServices() {
        return m_services;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", this.getId())
            .append("availability", this.getAvailability())
            .append("address", this.getAddress())
            .append("services", this.getServices())
            .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AvailabilityIpInterface)) return false;
        final AvailabilityIpInterface that = (AvailabilityIpInterface)o;
        return new EqualsBuilder()
            .append(this.getId(), that.getId())
            .append(this.getAvailability(), that.getAvailability())
            .append(this.getAddress(), that.getAddress())
            .append(this.getServices(), that.getServices())
            .isEquals();
    }
}
