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
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.xml.rtc.Node;

@XmlRootElement(name="node")
@XmlAccessorType(XmlAccessType.NONE)
public class AvailabilityNode {
    @XmlAttribute(name="id")
    private Long m_nodeId;

    @XmlAttribute(name="availability")
    private Double m_availability;

    @XmlAttribute(name="service-count")
    private Long m_serviceCount;

    @XmlAttribute(name="service-down-count")
    private Long m_serviceDownCount;

    @XmlElementWrapper(name="ipinterfaces")
    @XmlElement(name="ipinterface")
    private final List<AvailabilityIpInterface> m_ipInterfaces = new ArrayList<>();

    public AvailabilityNode() {
        m_nodeId = -1l;
        m_availability = -1d;
        m_serviceCount = 0l;
        m_serviceDownCount = 0l;
    }

    public AvailabilityNode(final Node node) {
        m_nodeId = node.getNodeid();
        m_availability = node.getNodevalue();
        m_serviceCount = node.getNodesvccount();
        m_serviceDownCount = node.getNodesvcdowncount();
    }

    public AvailabilityNode(final OnmsNode node, final double availability) {
        m_nodeId = node.getId().longValue();
        m_availability = availability;
        m_serviceCount = 0l;
        m_serviceDownCount = 0l;
        for (final OnmsIpInterface iface : node.getIpInterfaces()) {
            for (final OnmsMonitoredService svc : iface.getMonitoredServices()) {
                m_serviceCount++;
                if (svc.isDown()) {
                    m_serviceDownCount++;
                }
            }
        }
    }

    public Long getId() {
        return m_nodeId;
    }

    public void addIpInterface(final AvailabilityIpInterface iface) {
        m_ipInterfaces.add(iface);
    }

    public Double getAvailability() {
        return m_availability;
    }

    public List<AvailabilityIpInterface> getIpInterfaces() {
        return m_ipInterfaces;
    }

    public Long getServiceCount() {
        return m_serviceCount;
    }

    public Long getServiceDownCount() {
        return m_serviceDownCount;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", this.getId())
            .append("availability", this.getAvailability())
            .append("serviceCount", this.getServiceCount())
            .append("serviceDownCount", this.getServiceDownCount())
            .append("ipInterfaces", this.getIpInterfaces())
            .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AvailabilityNode)) return false;
        final AvailabilityNode that = (AvailabilityNode)o;
        return new EqualsBuilder()
            .append(this.getId(), that.getId())
            .append(this.getAvailability(), that.getAvailability())
            .append(this.getServiceCount(), that.getServiceCount())
            .append(this.getServiceDownCount(), that.getServiceDownCount())
            .append(this.getIpInterfaces(), that.getIpInterfaces())
            .isEquals();
    }
}
