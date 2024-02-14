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

    private String m_id;

    private String m_statusCode;

    private String m_status;

    private String m_nodeLabel;

    private String m_serviceName;

    private InetAddress m_ipAddress;

    private boolean m_isMonitored;

    private boolean m_isDown;

    private Integer m_ipInterfaceId;

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
        m_id = service.getXmlId();
        m_ipInterfaceId = service.getIpInterfaceId();
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

    @XmlAttribute(name="id")
    public String getId() {
        return m_id;
    }

    public void setId(String id) {
        m_id = id;
    }

    @XmlAttribute(name="ipInterfaceId")
    public Integer getIpInterfaceId() {
        return m_ipInterfaceId;
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
