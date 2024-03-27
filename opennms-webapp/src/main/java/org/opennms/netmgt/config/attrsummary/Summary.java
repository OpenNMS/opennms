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

@XmlRootElement(name = "summary")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("attr-summary.xsd")
public class Summary implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "resource")
    private List<Resource> m_resources = new ArrayList<>();

    public Summary() {
    }

    public Summary(final Resource... resources) {
        for (final Resource r : resources) {
            m_resources.add(r);
        }
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
        return Objects.hash(m_resources);
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
        Summary other = (Summary) obj;

        return Objects.equals(m_resources, other.m_resources);
    }

}
