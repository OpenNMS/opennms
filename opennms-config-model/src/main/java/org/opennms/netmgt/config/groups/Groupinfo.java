/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.groups;


import java.io.Serializable;
import java.util.ArrayList;
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
    private List<Group> m_groups = new ArrayList<>();

    @XmlElementWrapper(name = "roles")
    @XmlElement(name = "role")
    private List<Role> m_roles = new ArrayList<>();

    public Groupinfo() {
    }

    public Header getHeader() {
        return m_header;
    }

    public void setHeader(final Header header) {
        m_header = header;
    }

    public List<Group> getGroups() {
        return m_groups;
    }

    public void setGroups(final List<Group> groups) {
        m_groups = groups;
    }

    public void addGroup(final Group group) {
        m_groups.add(group);
    }

    public List<Role> getRoles() {
        return m_roles;
    }

    public void setRoles(final List<Role> roles) {
        m_roles = roles == null? new ArrayList<>() : roles;
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
            final Groupinfo temp = (Groupinfo)obj;
            return Objects.equals(temp.m_header, m_header)
                && Objects.equals(temp.m_groups, m_groups)
                && Objects.equals(temp.m_roles, m_roles);
        }
        return false;
    }

    @Override
    public String toString() {
        return "Groupinfo [header=" + m_header + ", groups=" + m_groups
                + ", roles=" + m_roles + "]";
    }

}
