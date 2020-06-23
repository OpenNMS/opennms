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
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

@XmlRootElement(name = "contact")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("users.xsd")
public class Contact implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlAttribute(name = "type", required = true)
    private String m_type;

    @XmlAttribute(name = "info")
    private String m_info;

    @XmlAttribute(name = "serviceProvider")
    private String m_serviceProvider;

    public Contact() {
    }

    public Contact(final String type) {
        m_type = type;
    }

    public String getType() {
        return m_type;
    }

    public void setType(final String type) {
        ConfigUtils.assertNotEmpty(type, "type");
        m_type = type;
    }

    public Optional<String> getInfo() {
        return Optional.ofNullable(m_info);
    }

    public void setInfo(final String info) {
        m_info = ConfigUtils.normalizeString(info);
    }

    public Optional<String> getServiceProvider() {
        return Optional.ofNullable(m_serviceProvider);
    }

    public void setServiceProvider(final String serviceProvider) {
        m_serviceProvider = ConfigUtils.normalizeString(serviceProvider);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                            m_type, 
                            m_info, 
                            m_serviceProvider);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof Contact) {
            final Contact temp = (Contact)obj;
            return Objects.equals(temp.m_type, m_type)
                    && Objects.equals(temp.m_info, m_info)
                    && Objects.equals(temp.m_serviceProvider, m_serviceProvider);
        }
        return false;
    }

}
