/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "users")
@XmlAccessorType(XmlAccessType.NONE)
public class OnmsUserList implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name="user")
    private List<OnmsUser> m_users = new ArrayList<OnmsUser>();

    private Integer m_totalCount;
    
    public OnmsUserList() {
    }

    public OnmsUserList(final Collection<? extends OnmsUser> c) {
        m_users.addAll(c);
    }

    public List<OnmsUser> getUsers() {
        return m_users;
    }
    
    public void setUsers(final List<OnmsUser> users) {
        if (users == m_users) return;
        m_users.clear();
        m_users.addAll(users);
    }
    
    public void add(final OnmsUser user) {
        m_users.add(user);
    }

    @XmlAttribute(name="count")
    public Integer getCount() {
        if (m_users.size() == 0) {
            return null;
        } else {
            return m_users.size();
        }
    }
    public void setCount(final Integer count) {
    }
    public int size() {
        return m_users.size();
    }

    @XmlAttribute(name="totalCount")
    public Integer getTotalCount() {
        return m_totalCount == null? getCount() : m_totalCount;
    }
    public void setTotalCount(final Integer totalCount) {
        m_totalCount = totalCount;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_totalCount == null) ? 0 : m_totalCount.hashCode());
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
        if (!(obj instanceof OnmsUserList)) {
            return false;
        }
        final OnmsUserList other = (OnmsUserList) obj;
        if (getTotalCount() == null) {
            if (other.getTotalCount() != null) {
                return false;
            }
        } else if (!getTotalCount().equals(other.getTotalCount())) {
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
