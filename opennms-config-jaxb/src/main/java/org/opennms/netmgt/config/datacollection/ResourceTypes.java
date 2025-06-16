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
package org.opennms.netmgt.config.datacollection;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.opennms.core.xml.ValidateUsing;

@XmlRootElement(name="resource-types", namespace="http://xmlns.opennms.org/xsd/config/datacollection")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder={"m_resourceTypes"})
@ValidateUsing("datacollection-groups.xsd")
public class ResourceTypes {
    /**
     * Custom resource types
     */
    @XmlElement(name="resourceType")
    private List<ResourceType> m_resourceTypes = null;

    public List<ResourceType> getResourceTypes() {
        if (m_resourceTypes == null) {
            m_resourceTypes = new ArrayList<>();
        }
        return m_resourceTypes;
    }

    public void setResourceTypes(List<ResourceType> resourceTypes) {
        m_resourceTypes = resourceTypes;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_resourceTypes);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ResourceTypes other = (ResourceTypes) obj;
        return Objects.equals(this.m_resourceTypes, other.m_resourceTypes);
    }
}
