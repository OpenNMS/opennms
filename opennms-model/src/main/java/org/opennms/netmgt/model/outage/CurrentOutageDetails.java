/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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
