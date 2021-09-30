/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
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
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.attrsummary;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;

@XmlRootElement(name = "resource")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("attr-summary.xsd")
public class Resource implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "name", required = true)
    private String m_name;

    @XmlElement(name = "attribute")
    private List<Attribute> m_attributes = new ArrayList<>();

    @XmlElement(name = "resource")
    private List<Resource> m_resources = new ArrayList<>();

    public Resource() {
    }

    public Resource(final String name) {
        m_name = name;
    }

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = name;
    }

    public List<Attribute> getAttributes() {
        return m_attributes;
    }

    public void addAttribute(final Attribute attribute) {
        m_attributes.add(attribute);
    }

    public void setAttributes(final List<Attribute> attributes) {
        m_attributes.clear();
        m_attributes.addAll(attributes);
    }

    public List<Resource> getResources() {
        return m_resources;
    }

    public void addResource(final Resource resource) {
        m_resources.add(resource);
    }

    public void setResources(final List<Resource> resources) {
        m_resources.clear();
        m_resources.addAll(resources);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_name, m_attributes, m_resources);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Resource other = (Resource) obj;

        return Objects.equals(m_name, other.m_name) &&
                Objects.equals(m_attributes, other.m_attributes) &&
                Objects.equals(m_resources, other.m_resources);
    }

}
