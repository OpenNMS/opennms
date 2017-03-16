/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.agents;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.core.network.InetAddressXmlAdapter;
import org.opennms.core.xml.JaxbMapAdapter;


@XmlRootElement(name="agent")
@XmlAccessorType(XmlAccessType.FIELD)
public class AgentResponse {
    @XmlElement(name="address")
    @XmlJavaTypeAdapter(InetAddressXmlAdapter.class)
    private InetAddress m_address;

    @XmlElement(name="port")
    private Integer m_port;

    @XmlElement(name="serviceName")
    private String m_serviceName;

    @XmlElement(name = "parameters")
    @XmlJavaTypeAdapter(JaxbMapAdapter.class)
    private Map<String,String> m_parameters = new HashMap<>();

    public AgentResponse() { }

    public AgentResponse(final InetAddress address, final Integer port, final String serviceName, final Map<String,String> parameters) {
        m_address = address;
        m_port = port;
        m_serviceName = serviceName;
        m_parameters = parameters;
    }

    public Optional<InetAddress> getAddress() {
        return Optional.ofNullable(m_address);
    }

    public Optional<Integer> getPort() {
        return Optional.ofNullable(m_port);
    }

    public Optional<String> getServiceName() {
        return Optional.ofNullable(m_serviceName);
    }

    public Map<String,String> getParameters() {
        return m_parameters;
    }

    @Override
    public String toString() {
        return "AgentResponse [address=" + m_address + ", port=" + m_port + ", serviceName=" + m_serviceName + ", parameters=" + m_parameters + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((m_address == null) ? 0 : m_address.hashCode());
        result = prime * result
                + ((m_parameters == null) ? 0 : m_parameters.hashCode());
        result = prime * result + ((m_port == null) ? 0 : m_port.hashCode());
        result = prime * result
                + ((m_serviceName == null) ? 0 : m_serviceName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AgentResponse other = (AgentResponse) obj;
        if (m_address == null) {
            if (other.m_address != null)
                return false;
        } else if (!m_address.equals(other.m_address))
            return false;
        if (m_parameters == null) {
            if (other.m_parameters != null)
                return false;
        } else if (!m_parameters.equals(other.m_parameters))
            return false;
        if (m_port == null) {
            if (other.m_port != null)
                return false;
        } else if (!m_port.equals(other.m_port))
            return false;
        if (m_serviceName == null) {
            if (other.m_serviceName != null)
                return false;
        } else if (!m_serviceName.equals(other.m_serviceName))
            return false;
        return true;
    }
}