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

package org.opennms.netmgt.config.users;


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

/**
 * The top-level element of the users.xml configuration
 *  file.
 */
@XmlRootElement(name = "userinfo")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("users.xsd")
public class Userinfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Header containing information about this configuration
     *  file.
     */
    @XmlElement(name = "header")
    private Header m_header;

    @XmlElementWrapper(name = "users")
    @XmlElement(name = "user")
    private List<User> m_users = new ArrayList<>();

    public Userinfo() {
    }

    public Userinfo(final List<User> users) {
        m_users = users;
    }

    public Header getHeader() {
        return m_header;
    }

    public void setHeader(final Header header) {
        m_header = header;
    }

    public List<User> getUsers() {
        return m_users;
    }

    public void setUsers(final List<User> users) {
        if (users == m_users) return;
        m_users.clear();
        if (users != null) m_users = users;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                            m_header, 
                            m_users);
    }
    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof Userinfo) {
            final Userinfo temp = (Userinfo)obj;
            return Objects.equals(temp.m_header, m_header)
                    && Objects.equals(temp.m_users, m_users);
        }
        return false;
    }


    @Override
    public String toString() {
        return "Userinfo[header=" + m_header + ", users=" + m_users + "]";
    }

}
