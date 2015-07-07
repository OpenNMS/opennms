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

package org.opennms.netmgt.model;

import static org.opennms.core.utils.InetAddressUtils.toInteger;

import java.io.Serializable;
import java.math.BigInteger;
import java.net.InetAddress;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.core.network.InetAddressXmlAdapter;

@SuppressWarnings("serial")
@XmlRootElement(name = "monitored-service")
public class OnmsMonitoredServiceDetail implements Serializable, Comparable<OnmsMonitoredServiceDetail> {

    private String m_statusCode;

    private String m_status;

    private String m_nodeLabel;

    private String m_serviceName;

    private InetAddress m_ipAddress;

    private boolean m_isMonitored;

    private boolean m_isDown;

    public OnmsMonitoredServiceDetail() {
    }

    public OnmsMonitoredServiceDetail(OnmsMonitoredService service) {
        m_nodeLabel = service.getIpInterface().getNode().getLabel();
        m_ipAddress = service.getIpAddress();
        m_serviceName = service.getServiceName();
        m_isMonitored = "A".equals(service.getStatus());
        m_isDown = service.isDown();
        m_statusCode = service.getStatus();
        m_status = service.getStatusLong();
    }

    @XmlElement(name="status")
    public String getStatus() {
        return m_status;
    }

    public void setStatus(String status) {
        this.m_status = status;
    }

    @XmlAttribute(name="statusCode")
    public String getStatusCode() {
        return m_statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.m_statusCode = statusCode;
    }

    @XmlElement(name="node")
    public String getNodeLabel() {
        return m_nodeLabel;
    }

    public void setNodeLabel(String nodeLabel) {
        this.m_nodeLabel = nodeLabel;
    }

    @XmlElement(name="serviceName")
    public String getServiceName() {
        return m_serviceName;
    }

    public void setServiceName(String serviceName) {
        this.m_serviceName = serviceName;
    }

    @XmlElement(name="ipAddress")
    @XmlJavaTypeAdapter(InetAddressXmlAdapter.class)
    public InetAddress getIpAddress() {
        return m_ipAddress;
    }

    public void setIpAddress(InetAddress ipAddress) {
        this.m_ipAddress = ipAddress;
    }

    @XmlAttribute(name="isMonitored")
    public boolean isMonitored() {
        return m_isMonitored;
    }

    @XmlAttribute(name="isDown")
    public boolean isDown() {
        return m_isDown;
    }

    @Override
    public int compareTo(OnmsMonitoredServiceDetail o) {
        int diff;

        diff = getNodeLabel().compareToIgnoreCase(o.getNodeLabel());
        if (diff != 0) {
            return diff;
        }

        BigInteger a = toInteger(getIpAddress());
        BigInteger b = toInteger(o.getIpAddress());
        diff = a.compareTo(b);
        if (diff != 0) {
            return diff;
        }

        return getServiceName().compareToIgnoreCase(o.getServiceName());
    }

}
