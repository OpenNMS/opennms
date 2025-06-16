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
package org.opennms.netmgt.config.mailtransporttest;

import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * Configure user based authentication.
 */

@XmlRootElement(name="user-auth")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("mail-transport-test.xsd")
public class UserAuth implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlAttribute(name="user-name")
    private String m_userName;

    @XmlAttribute(name="password")
    private String m_password;

    public UserAuth() {
    }


    public UserAuth(final String username, final String password) {
        setUserName(username);
        setPassword(password);
    }


    @Override()
    public boolean equals(final Object obj) {
        if ( this == obj ) return true;

        if (obj instanceof UserAuth) {
            final UserAuth that = (UserAuth)obj;
            return Objects.equals(this.m_userName, that.m_userName) &&
                    Objects.equals(this.m_password, that.m_password);
        }
        return false;
    }

    public String getPassword() {
        return m_password == null? "opennms" : m_password;
    }

    public String getUserName() {
        return m_userName == null? "opennms" : m_userName;
    }

    public int hashCode() {
        return Objects.hash(m_userName, m_password);
    }

    public void setPassword(final String password) {
        m_password = ConfigUtils.normalizeString(password);
    }

    public void setUserName(final String userName) {
        m_userName = ConfigUtils.normalizeString(userName);
    }

}
