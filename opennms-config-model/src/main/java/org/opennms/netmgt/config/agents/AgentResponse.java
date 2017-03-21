/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

import java.io.Serializable;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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
public class AgentResponse implements Serializable {
    private static final long serialVersionUID = 1L;

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
        return Objects.hash(m_address, m_port, m_serviceName, m_parameters);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof AgentResponse) {
            final AgentResponse that = (AgentResponse) obj;
            return Objects.equals(this.m_address, that.m_address) &&
                    Objects.equals(this.m_port, that.m_port) &&
                    Objects.equals(this.m_serviceName, that.m_serviceName) &&
                    Objects.equals(this.m_parameters, that.m_parameters);
        }
        return false;
    }
}