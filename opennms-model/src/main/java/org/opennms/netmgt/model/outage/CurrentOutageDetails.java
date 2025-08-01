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
package org.opennms.netmgt.model.outage;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="outage-details")
@XmlAccessorType(XmlAccessType.NONE)
public class CurrentOutageDetails {
    @XmlAttribute(name="id")
    private Integer m_outageId;

    @XmlAttribute(name="monitoredServiceId")
    private Integer m_monitoredServiceId;

    @XmlAttribute(name="serviceName")
    private String m_serviceName;

    @XmlAttribute(name="ifLostService")
    private Date m_ifLostService;

    @XmlAttribute(name="nodeId")
    private Integer m_nodeId;

    @XmlAttribute(name="foreignSource")
    private String m_foreignSource;

    @XmlAttribute(name="foreignId")
    private String m_foreignId;

    @XmlAttribute(name="location")
    private String m_location;

    public CurrentOutageDetails() {}
    public CurrentOutageDetails(final Integer outageId, final Integer monitoredServiceId, final String serviceName, final Date ifLostService, final Integer nodeId, final String foreignSource, final String foreignId, final String location) {
        m_outageId = outageId;
        m_monitoredServiceId = monitoredServiceId;
        m_serviceName = serviceName;
        m_ifLostService = ifLostService;
        m_nodeId = nodeId;
        m_foreignSource = foreignSource;
        m_foreignId = foreignId;
        m_location = location;
    }

    public Integer getOutageId() {
        return m_outageId;
    }

    public Integer getMonitoredServiceId() {
        return m_monitoredServiceId;
    }

    public String getServiceName() {
        return m_serviceName;
    }

    public Date getIfLostService() {
        return m_ifLostService;
    }

    public Integer getNodeId() {
        return m_nodeId;
    }

    public String getForeignSource() {
        return m_foreignSource;
    }

    public String getForeignId() {
        return m_foreignId;
    }

    public String getLocation() {
        return m_location;
    }
    @Override
    public String toString() {
        return "CurrentOutageDetails [outageId=" + m_outageId
                + ", monitoredServiceId=" + m_monitoredServiceId
                + ", serviceName=" + m_serviceName
                + ", ifLostService=" + m_ifLostService
                + ", nodeId=" + m_nodeId
                + ", foreignSource=" + m_foreignSource
                + ", foreignId=" + m_foreignId
                + ", location=" + m_location + "]";
    }
}
