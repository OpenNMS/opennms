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

package org.opennms.web.rest.v2.bsm.model;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


@XmlRootElement(name = "business-service")
@XmlAccessorType(XmlAccessType.NONE)
public class BusinessServiceRequestDTO {
    
    @XmlElement(name = "name")
    private String m_name;

    @XmlElement(name = "attributes", required = false)
    @XmlJavaTypeAdapter(JAXBMapAdapter.class)
    private Map<String, String> m_attributes = Maps.newLinkedHashMap();

    @XmlElement(name="ip-service")
    @XmlElementWrapper(name="ip-services")
    private Set<Integer> m_ipServices = Sets.newLinkedHashSet();

    @XmlElement(name="child-service")
    @XmlElementWrapper(name="child-services")
    private Set<Long> m_childServices = Sets.newLinkedHashSet();

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

    public Set<Integer> getIpServices() {
        return m_ipServices;
    }

    public void setIpServices(Set<Integer> ipServices) {
        m_ipServices = ipServices;
    }

    public Set<Long> getChildServices() {
        return m_childServices;
    }

    public void setChildServices(Set<Long> childServices) {
        m_childServices = childServices;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BusinessServiceRequestDTO other = (BusinessServiceRequestDTO) obj;

        return Objects.equals(m_name, other.m_name)
                && Objects.equals(m_attributes, other.m_attributes)
                && Objects.equals(m_ipServices, other.m_ipServices)
                && Objects.equals(m_childServices, other.m_childServices);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_name, m_attributes, m_ipServices);
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("name", m_name)
                .add("attributes", m_attributes)
                .add("ipServices", m_ipServices)
                .add("childServices", m_childServices)
                .toString();
    }
}
