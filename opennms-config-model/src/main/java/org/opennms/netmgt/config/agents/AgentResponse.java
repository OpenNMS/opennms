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