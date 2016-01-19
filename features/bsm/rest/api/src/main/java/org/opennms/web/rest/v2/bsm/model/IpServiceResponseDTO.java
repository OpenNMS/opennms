/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
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
 * http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.rest.v2.bsm.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.web.rest.api.ResourceLocation;
import org.opennms.web.rest.api.support.JAXBResourceLocationAdapter;
import org.opennms.web.rest.api.support.JsonResourceLocationDeserializationProvider;
import org.opennms.web.rest.api.support.JsonResourceLocationSerializationProvider;

import com.google.common.base.Objects;

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

    @XmlElement(name="operational-status")
    private OnmsSeverity m_operationalStatus;

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

    public OnmsSeverity getOperationalStatus() {
        return this.m_operationalStatus;
    }

    public void setOperationalStatus(final OnmsSeverity operationalStatus) {
        this.m_operationalStatus = operationalStatus;
    }

    public ResourceLocation getLocation() {
        return location;
    }

    public void setLocation(ResourceLocation location) {
        this.location = location;
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
        final IpServiceDTO other = (IpServiceDTO) obj;
        final boolean equals = Objects.equal(id, other.id)
                && Objects.equal(serviceName, other.serviceName)
                && Objects.equal(nodeLabel, other.nodeLabel)
                && Objects.equal(ipAddress, other.ipAddress)
                && Objects.equal(location, other.location)
                && Objects.equal(reductionKeys, other.reductionKeys);
        return equals;
    }

    public Set<String> getReductionKeys() {
        return reductionKeys;
    }

    public void setReductionKeys(Set<String> reductionKeys) {
        this.reductionKeys = reductionKeys;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, serviceName, nodeLabel,ipAddress, location, reductionKeys);
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("id", id)
                .add("serviceName", serviceName)
                .add("nodeLabel", nodeLabel)
                .add("ipAddress", ipAddress)
                .add("location", location)
                .add("reductionKeys", reductionKeys)
                .toString();
    }
}
