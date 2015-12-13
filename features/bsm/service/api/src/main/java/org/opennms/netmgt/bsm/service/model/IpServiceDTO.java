/*******************************************************************************
 * This file is part of OpenNMS(R).
 * <p>
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 * http://www.gnu.org/licenses/
 * <p>
 * For more information contact:
 * OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/
 * http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.bsm.service.model;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Objects;

import org.opennms.web.rest.api.JAXBResourceLocationAdapter;
import org.opennms.web.rest.api.ResourceLocation;

@XmlRootElement(name = "ip-service")
@XmlAccessorType(XmlAccessType.NONE)
public class IpServiceDTO {

    @XmlID
    @XmlElement(name="id")
    private String id;

    @XmlElement(name="location")
    @XmlJavaTypeAdapter(JAXBResourceLocationAdapter.class)
    private ResourceLocation location;

    private String serviceName;

    private String nodeLabel;

    private String ipAddress;

//    @XmlElement(name="reductionKey")
//    @XmlElementWrapper(name="reductionKeys")
    private Set<String> reductionKeys = new HashSet<>();

    public ResourceLocation getLocation() {
        return location;
    }

    public String getId() {
        return id;
    }

    public void setLocation(ResourceLocation location) {
        this.location = location;
    }

    public void setId(String id) {
        this.id = id;
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
                && Objects.equal(location, other.location);
        return equals;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getNodeLabel() {
        return nodeLabel;
    }

    public void setNodeLabel(String nodeLabel) {
        this.nodeLabel = nodeLabel;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Set<String> getReductionKeys() {
        return reductionKeys;
    }

    public void setReductionKeys(Set<String> reductionKeys) {
        this.reductionKeys = reductionKeys;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, location);
    }

    @Override
    public String toString() {
        return getNodeLabel()+"/"+getIpAddress()+"/"+getServiceName();
    }
}
