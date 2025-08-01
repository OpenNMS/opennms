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
