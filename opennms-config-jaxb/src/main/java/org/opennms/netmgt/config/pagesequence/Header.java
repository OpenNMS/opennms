/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.pagesequence;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.google.common.base.Objects;

@XmlRootElement(name="header")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder={"m_name", "m_value"})
public class Header implements Serializable {
    private static final long serialVersionUID = 5655167778463737674L;

    @XmlAttribute(name="name")
    private String m_name;

    @XmlAttribute(name="value")
    private String m_value;

    public Header() {
    }

    public Header(final String name, final String value) {
        m_name = name;
        m_value = value;
    }

    public String getName() {
        return m_name;
    }

    public String getValue() {
        return m_value;
    }

    public void setName(final String name) {
        m_name = name;
    }

    public void setValue(final String value) {
        m_value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Header header = (Header) o;
        return Objects.equal(m_name, header.m_name) &&
                Objects.equal(m_value, header.m_value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(m_name, m_value);
    }

    @Override
    public String toString() {
        return "Header [name=" + m_name + ", value=" + m_value + "]";
    }
}
