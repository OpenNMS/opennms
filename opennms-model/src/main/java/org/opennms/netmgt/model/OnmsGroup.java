/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;

@XmlRootElement(name="group")
@XmlAccessorType(XmlAccessType.NONE)
public class OnmsGroup implements Serializable {
    private static final long serialVersionUID = 417800322426757366L;

    @XmlElement(name="name", required=true)
    private String m_name;

    @XmlElement(name="comments", required=false)
    private String m_comments;

    @XmlElement(name="user", required=false)
    private List<String> m_users = new ArrayList<>();

    public OnmsGroup() { }

    public OnmsGroup(final String groupName) {
        m_name = groupName;
    }

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = name;
    }

    public String getComments() {
        return m_comments;
    }

    public void setComments(final String comments) {
        m_comments = comments;
    }

    public List<String> getUsers() {
        return m_users;
    }

    public void setUsers(final List<String> users) {
        m_users = users;
    }

    public void addUser(final String userName) {
        if (m_users == null) {
            m_users = new ArrayList<>();
        }
        m_users.add(userName.intern());
    }

    public void removeUser(final String userName) {
        if (m_users == null) return;
        m_users.remove(userName);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
        .append("name", m_name)
        .append("comments", m_comments)
        .append("users", m_users)
        .toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_comments == null) ? 0 : m_comments.hashCode());
        result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
        result = prime * result + ((m_users == null) ? 0 : m_users.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof OnmsGroup)) {
            return false;
        }
        final OnmsGroup other = (OnmsGroup) obj;
        if (m_comments == null) {
            if (other.m_comments != null) {
                return false;
            }
        } else if (!m_comments.equals(other.m_comments)) {
            return false;
        }
        if (m_name == null) {
            if (other.m_name != null) {
                return false;
            }
        } else if (!m_name.equals(other.m_name)) {
            return false;
        }
        if (m_users == null) {
            if (other.m_users != null) {
                return false;
            }
        } else if (!m_users.equals(other.m_users)) {
            return false;
        }
        return true;
    }
}
