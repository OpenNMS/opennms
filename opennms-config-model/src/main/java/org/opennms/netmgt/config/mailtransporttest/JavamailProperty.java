/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.mailtransporttest;

import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Use these name value pairs to configure freeform properties from
 * the JavaMail class.
 */

@XmlRootElement(name="javamail-property")
@XmlAccessorType(XmlAccessType.FIELD)
public class JavamailProperty implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Field m_name.
     */
    @XmlAttribute(name="name")
    private String m_name;

    /**
     * Field m_value.
     */
    @XmlAttribute(name="value")
    private String m_value;

    public JavamailProperty() {
        super();
    }

    public JavamailProperty(final String name, final String value) {
        if (name == null || value == null) {
            throw new IllegalArgumentException("'name' and 'value' are required attributes!");
        }
        m_name = name;
        m_value = value;
    }

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        if (name == null) {
            throw new IllegalArgumentException("'name' is a required attribute!");
        }
        m_name = name;
    }

    public String getValue() {
        return m_value;
    }

    public void setValue(final String value) {
        if (value == null) {
            throw new IllegalArgumentException("'value' is a required attribute!");
        }
        m_value = value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_name, m_value);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;

        if (obj instanceof JavamailProperty) {
            final JavamailProperty that = (JavamailProperty) obj;
            return Objects.equals(this.m_name, that.m_name) &&
                    Objects.equals(this.m_value, that.m_value);
        }
        return false;
    }

}
