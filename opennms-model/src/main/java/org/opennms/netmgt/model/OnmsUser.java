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
package org.opennms.netmgt.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;

@XmlRootElement(name="user")
@XmlAccessorType(XmlAccessType.NONE)
public class OnmsUser {

    @XmlElement(name="user-id", required=true)
    private String m_username;

    @XmlElement(name="full-name", required=false)
    private String m_fullName;

    @XmlElement(name="user-comments", required=false)
    private String m_comments;

    @XmlElement(name="email", required=false)
    private String m_email;

    @XmlElement(name="password", required=false)
    private String m_password;

    @XmlElement(name="passwordSalt", required=false)
    private Boolean m_passwordSalted;

    @XmlElement(name="duty-schedule", required=false)
    private List<String> m_dutySchedule = new ArrayList<>();

    @XmlElement(name="role", required=false)
    private List<String> m_roles = new ArrayList<>();

    public OnmsUser() { }

    public OnmsUser(final String username) {
        m_username = username;
    }

    public String getComments() {
        return m_comments;
    }

    public void setComments(String comments) {
        m_comments = comments;
    }

    public String getPassword() {
        return m_password;
    }

    public void setPassword(String password) {
        m_password = password;
    }

    public boolean getPasswordSalted() {
        return m_passwordSalted == null? false : m_passwordSalted;
    }

    public void setPasswordSalted(final Boolean passwordSalted) {
        m_passwordSalted = passwordSalted;
    }

    public String getFullName() {
        return m_fullName;
    }

    public void setFullName(String fullName) {
        m_fullName = fullName;
    }

    public String getUsername() {
        return m_username;
    }

    public void setUsername(String username) {
        m_username = username;
    }

    public List<String> getDutySchedule() {
        return m_dutySchedule;
    }

    public void setDutySchedule(final List<String> dutySchedule) {
        m_dutySchedule = dutySchedule;
    }

    public List<String> getRoles() {
        return m_roles;
    }

    public void addRole(String role) {
        if (!getRoles().contains(role)) {
            getRoles().add(role);
        }
    }

    public void setRoles(List<String> roles) {
        this.m_roles = roles;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
        .append("username", m_username)
        .append("full-name", m_fullName)
        .append("comments", m_comments)
        .append("roles", m_roles)
        .toString();
    }

    public String getEmail() {
        return m_email;
    }

    public void setEmail(String email) {
        m_email = email;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_comments == null) ? 0 : m_comments.hashCode());
        result = prime * result + ((m_roles == null) ? 0 : m_roles.hashCode());
        result = prime * result + ((m_dutySchedule == null) ? 0 : m_dutySchedule.hashCode());
        result = prime * result + ((m_email == null) ? 0 : m_email.hashCode());
        result = prime * result + ((m_fullName == null) ? 0 : m_fullName.hashCode());
        result = prime * result + ((m_password == null) ? 0 : m_password.hashCode());
        result = prime * result + ((m_passwordSalted == null) ? 0 : m_passwordSalted.hashCode());
        result = prime * result + ((m_username == null) ? 0 : m_username.hashCode());
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
        if (!(obj instanceof OnmsUser)) {
            return false;
        }
        final OnmsUser other = (OnmsUser) obj;
        if (m_comments == null) {
            if (other.m_comments != null) {
                return false;
            }
        } else if (!m_comments.equals(other.m_comments)) {
            return false;
        }
        if (m_roles == null) {
            if (other.m_roles != null) {
                return false;
            }
        } else if (!m_roles.equals(other.m_roles)) {
            return false;
        }
        if (m_dutySchedule == null) {
            if (other.m_dutySchedule != null) {
                return false;
            }
        } else if (!m_dutySchedule.equals(other.m_dutySchedule)) {
            return false;
        }
        if (m_email == null) {
            if (other.m_email != null) {
                return false;
            }
        } else if (!m_email.equals(other.m_email)) {
            return false;
        }
        if (m_fullName == null) {
            if (other.m_fullName != null) {
                return false;
            }
        } else if (!m_fullName.equals(other.m_fullName)) {
            return false;
        }
        if (m_password == null) {
            if (other.m_password != null) {
                return false;
            }
        } else if (!m_password.equals(other.m_password)) {
            return false;
        }
        if (m_passwordSalted == null) {
            if (other.m_passwordSalted != null) {
                return false;
            }
        } else if (!m_passwordSalted.equals(other.m_passwordSalted)) {
            return false;
        }
        if (m_username == null) {
            if (other.m_username != null) {
                return false;
            }
        } else if (!m_username.equals(other.m_username)) {
            return false;
        }
        return true;
    }
}
