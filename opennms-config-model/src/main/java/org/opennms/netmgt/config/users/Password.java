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
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

@XmlRootElement(name = "password")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("users.xsd")
public class Password implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlAttribute(name = "salt", required = false)
    private Boolean m_salt;

    @XmlValue
    private String m_password;

    public Password() {
        setEncryptedPassword("");
    }

    public Password(final String password) {
        m_password = password;
    }

    public Password(final String password, final Boolean salt) {
        m_password = password;
        m_salt = salt;
    }

    public Boolean getSalt() {
        return m_salt == null? Boolean.valueOf(false) : m_salt;
    }

    public void setSalt(final Boolean salt) {
        m_salt = salt;
    }

    public String getEncryptedPassword() {
        return m_password;
    }

    public void setEncryptedPassword(final String password) {
        ConfigUtils.assertNotNull(password, "password");
        m_password = password;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                            m_password, 
                            m_salt);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof Password) {
            final Password temp = (Password)obj;
            return Objects.equals(temp.m_password, m_password)
                    && Objects.equals(temp.m_salt, m_salt);
        }
        return false;
    }

    @Override
    public String toString() {
        return "Password[salt=" + m_salt + ", password=" + m_password + "]";
    }

}
