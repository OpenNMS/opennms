/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model;

import java.io.Serializable;
import java.util.Map.Entry;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name="property")
@XmlAccessorType(XmlAccessType.NONE)
public class ScanReportProperty implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlAttribute(name="name")
    private String m_name;
    @XmlValue
    private String m_value;

    public ScanReportProperty() {}
    public ScanReportProperty(final String name, final String value) {
        m_name = name;
        m_value = value;
    }

    public ScanReportProperty(final Entry<String, String> entry) {
        m_name = entry.getKey();
        m_value = entry.getValue();
    }

    public String getName() {
        return m_name;
    }
    public String getValue() {
        return m_value;
    }
    @Override
    public int hashCode() {
        return Objects.hash(m_name, m_value);
    }
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ScanReportProperty)) {
            return false;
        }
        final ScanReportProperty that = (ScanReportProperty)obj;
        return Objects.equals(this.m_name, that.m_name) && Objects.equals(this.m_value, that.m_value);
    }

    @Override
    public String toString() {
        return "ScanReportProperty [" + m_name + "=" + m_value + "]";
    }
}