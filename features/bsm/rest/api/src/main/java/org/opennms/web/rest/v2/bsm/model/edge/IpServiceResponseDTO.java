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
package org.opennms.web.rest.v2.bsm.model.edge;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.opennms.web.rest.api.ResourceLocation;
import org.opennms.web.rest.api.support.JAXBResourceLocationAdapter;
import org.opennms.web.rest.api.support.JsonResourceLocationDeserializationProvider;
import org.opennms.web.rest.api.support.JsonResourceLocationSerializationProvider;

import com.google.common.base.Objects;

@XmlRootElement(name = "ip-service")
@XmlAccessorType(XmlAccessType.FIELD)
public class IpServiceResponseDTO {

    @XmlElement(name="id")
    private int m_id;

    @XmlElement(name="service-name")
    private String m_serviceName;

    @XmlElement(name="node-label")
    private String m_nodeLabel;

    @XmlElement(name="ip-address")
    private String m_ipAddress;

    @XmlElement(name="location")
    @XmlJavaTypeAdapter(JAXBResourceLocationAdapter.class)
    @JsonSerialize(using = JsonResourceLocationSerializationProvider.class)
    @JsonDeserialize(using = JsonResourceLocationDeserializationProvider.class)
    private ResourceLocation location;

    public int getId() {
        return m_id;
    }

    public void setId(int id) {
        this.m_id = id;
    }

    public ResourceLocation getLocation() {
        return location;
    }

    public void setLocation(ResourceLocation location) {
        this.location = location;
    }

    public String getServiceName() {
        return m_serviceName;
    }

    public void setServiceName(String serviceName) {
        this.m_serviceName = serviceName;
    }

    public String getNodeLabel() {
        return m_nodeLabel;
    }

    public void setNodeLabel(String nodeLabel) {
        this.m_nodeLabel = nodeLabel;
    }

    public String getIpAddress() {
        return m_ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.m_ipAddress = ipAddress;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IpServiceResponseDTO other = (IpServiceResponseDTO) obj;
        final boolean equals = Objects.equal(m_id, other.m_id)
                && Objects.equal(m_serviceName, other.m_serviceName)
                && Objects.equal(m_nodeLabel, other.m_nodeLabel)
                && Objects.equal(m_ipAddress, other.m_ipAddress)
                && Objects.equal(location, other.location);
        return equals;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(m_id,
                m_serviceName,
                m_nodeLabel,
                m_ipAddress,
                location);
    }

    @Override
    public String toString() {
        return com.google.common.base.MoreObjects.toStringHelper(this)
                .add("id", m_id)
                .add("serviceName", m_serviceName)
                .add("nodeLabel", m_nodeLabel)
                .add("ipAddress", m_ipAddress)
                .add("location", location)
                .toString();
    }
}
