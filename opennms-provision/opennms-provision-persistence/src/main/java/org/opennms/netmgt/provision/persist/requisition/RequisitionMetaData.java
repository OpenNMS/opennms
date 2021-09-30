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

package org.opennms.netmgt.provision.persist.requisition;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.CompareToBuilder;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="")
@XmlRootElement(name="meta-data")
public class RequisitionMetaData implements Comparable<RequisitionMetaData> {

    @XmlAttribute(name="context", required=true)
    protected String m_context;

    @XmlAttribute(name="key", required=true)
    protected String m_key;

    @XmlAttribute(name="value", required=true)
    protected String m_value;

    public RequisitionMetaData() { }

    public RequisitionMetaData(String context, String key, String value) {
        m_context = Objects.requireNonNull(context);
        m_key = Objects.requireNonNull(key);
        m_value = Objects.requireNonNull(value);
    }

    public String getContext() {
        return m_context;
    }

    public void setContext(String context) {
        m_context = context;
    }

    public String getKey() {
        return m_key;
    }

    public void setKey(String key) {
        m_key = key;
    }

    public String getValue() {
        return m_value;
    }

    public void setValue(String value) {
        m_value = value;
    }

    @Override
    public String toString() {
        return String.format("RequisitionMetaData [context=%s, key=%s, value=%s]",
                m_context, m_key, m_value);
    }

    @Override
    public int compareTo(final RequisitionMetaData other) {
        return new CompareToBuilder()
            .append(m_context, other.m_context)
            .append(m_key, other.m_key)
            .append(m_value, other.m_value)
            .toComparison();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequisitionMetaData that = (RequisitionMetaData) o;
        return Objects.equals(m_context, that.m_context) &&
                Objects.equals(m_key, that.m_key) &&
                Objects.equals(m_value, that.m_value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_context, m_key, m_value);
    }
}
