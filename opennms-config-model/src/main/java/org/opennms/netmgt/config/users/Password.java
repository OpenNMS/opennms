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
