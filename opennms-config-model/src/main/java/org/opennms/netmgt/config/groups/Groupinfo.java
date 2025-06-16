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
package org.opennms.netmgt.config.groups;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;

@XmlRootElement(name = "groupinfo")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("groups.xsd")
public class Groupinfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "header")
    private Header m_header;

    @XmlElementWrapper(name = "groups")
    @XmlElement(name = "group")
    private List<Group> m_groups;

    @XmlElementWrapper(name = "roles")
    @XmlElement(name = "role")
    private List<Role> m_roles;

    public Groupinfo() {
    }

    public Header getHeader() {
        return m_header;
    }

    public void setHeader(final Header header) {
        m_header = header;
    }

    public List<Group> getGroups() {
        return m_groups == null? Collections.emptyList() : m_groups;
    }

    public void setGroups(final List<Group> groups) {
        m_groups = groups;
    }

    public void addGroup(final Group group) {
        if (group != null) {
            if (m_groups == null) {
                m_groups = new ArrayList<>();
            }
            m_groups.add(group);
        }
    }

    public List<Role> getRoles() {
        return m_roles == null? Collections.emptyList() : m_roles;
    }

    public void setRoles(final List<Role> roles) {
        m_roles = roles;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                            m_header, 
                            m_groups, 
                            m_roles);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof Groupinfo) {
            final Groupinfo that = (Groupinfo)obj;
            return Objects.equals(this.m_header, that.m_header)
                    && Objects.equals(this.m_groups, that.m_groups)
                    && Objects.equals(this.m_roles, that.m_roles);
        }
        return false;
    }

    @Override
    public String toString() {
        return "Groupinfo [header=" + m_header + ", groups=" + m_groups
                + ", roles=" + m_roles + "]";
    }

}
