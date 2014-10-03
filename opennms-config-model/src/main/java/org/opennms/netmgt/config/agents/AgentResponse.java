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

package org.opennms.netmgt.config.agents;

import java.net.InetAddress;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.core.network.InetAddressXmlAdapter;

@XmlRootElement(name="agent")
public class AgentResponse {
    private InetAddress m_address;
    private Integer m_port;
    private String m_serviceName;
    private Map<String,String> m_parameters;

    public AgentResponse() {
    }

    public AgentResponse(final InetAddress address, final Integer port, final String serviceName, final Map<String,String> parameters) {
        m_address = address;
        m_port = port;
        m_serviceName = serviceName;
        m_parameters = parameters;
    }

    @XmlElement(name="address")
    @XmlJavaTypeAdapter(InetAddressXmlAdapter.class)
    public InetAddress getAddress() {
        return m_address;
    }
    @XmlElement(name="port")
    public Integer getPort() {
        return m_port;
    }
    @XmlElement(name="serviceName")
    public String getServiceName() {
        return m_serviceName;
    }
    @XmlElementWrapper(name="parameters")
    @XmlElement(name="parameter")
    public Map<String,String> getParameters() {
        return m_parameters;
    }

    @Override
    public String toString() {
        return "AgentResponse [address=" + m_address + ", port=" + m_port + ", serviceName=" + m_serviceName + ", parameters=" + m_parameters + "]";
    }
}