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
