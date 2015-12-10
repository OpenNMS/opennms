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

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.web.rest.api.JAXBResourceLocationAdapter;
import org.opennms.web.rest.api.ResourceLocation;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;


@XmlRootElement(name = "business-service")
@XmlAccessorType(XmlAccessType.NONE)
public class BusinessServiceDTO {
    
    @XmlElement(name = "id")
    private Long m_id;

    @XmlElement(name = "name")
    private String m_name;

    @XmlElement(name="location")
    @XmlJavaTypeAdapter(JAXBResourceLocationAdapter.class)
    private ResourceLocation location;

    @XmlElement(name = "attributes", required = false)
    @XmlJavaTypeAdapter(JAXBMapAdapter.class)
    private Map<String, String> m_attributes = Maps.newLinkedHashMap();

    @XmlElement(name="ip-service")
    @XmlElementWrapper(name="ip-services")
    private Set<IpServiceDTO> m_ipServices = Sets.newLinkedHashSet();

    @XmlElement(name="child-service")
    @XmlElementWrapper(name="child-services")
    private Set<BusinessServiceDTO> m_childServices = Sets.newLinkedHashSet();

    public Long getId() {
        return m_id;
    }

    public void setId(Long id) {
        m_id = id;
    }

    public String getName() {
        return m_name;
    }

    public void setName(String name) {
        m_name = name;
    }

    public Map<String, String> getAttributes() {
        return m_attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        m_attributes = attributes;
    }

    public void setAttribute(String key, String value) {
        m_attributes.put(key, value);
    }

    public String removeAttribute(String key) {
        return m_attributes.remove(key);
    }

    public Set<IpServiceDTO> getIpServices() {
        return m_ipServices;
    }

    public void setIpServices(Set<IpServiceDTO> ipServices) {
        m_ipServices = ipServices;
    }

    public void addIpService(IpServiceDTO ipService) {
        m_ipServices.add(ipService);
    }

    public void removeIpService(IpServiceDTO ipService) {
        m_ipServices.remove(ipService);
    }

    public Set<BusinessServiceDTO> getChildServices() {
        return m_childServices;
    }

    public void setChildServices(Set<BusinessServiceDTO> childServices) {
        m_childServices = childServices;
    }

    public void addChildService(BusinessServiceDTO childService) {
        m_childServices.add(childService);
    }

    public void removeChildService(BusinessServiceDTO childService) {
        m_childServices.remove(childService);
    }

    public void setLocation(ResourceLocation location) {
        this.location = location;
    }

    public ResourceLocation getLocation() {
        return location;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BusinessServiceDTO other = (BusinessServiceDTO) obj;

        return Objects.equals(m_id, other.m_id)
                && Objects.equals(m_name, other.m_name)
                && Objects.equals(m_attributes, other.m_attributes)
                && Objects.equals(m_ipServices, other.m_ipServices)
                && Objects.equals(m_childServices, other.m_childServices)
                && Objects.equals(location, other.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_id, m_name, m_attributes, m_ipServices);
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this).add("id", m_id).add("name", m_name)
                .add("attributes", m_attributes)
                .add("ipServices", m_ipServices)
                .add("childServices", m_childServices)
                .add("location", location)
                .toString();
    }
}
