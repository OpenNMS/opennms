/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.snmp;

import java.util.Arrays;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement(name="snmp-result")
@XmlAccessorType(XmlAccessType.NONE)
public class SnmpResult implements Comparable<SnmpResult> {
    @XmlElement(name="base")
    @XmlJavaTypeAdapter(SnmpObjIdXmlAdapter.class)
    private SnmpObjId m_base;
    @XmlElement(name="instance")
    @XmlJavaTypeAdapter(SnmpInstIdXmlAdapter.class)
    private SnmpInstId m_instance;
    @XmlElement(name="value")
    @XmlJavaTypeAdapter(SnmpValueXmlAdapter.class)
    private SnmpValue m_value;

    protected SnmpResult() {
        // No-arg constructor for JAXB
    }

    public SnmpResult(SnmpObjId base, SnmpInstId instance, SnmpValue value) {
        m_base = base;
        m_instance = instance;
        m_value = value;
    }

    public SnmpObjId getBase() {
        return m_base;
    }

    public void setBase(SnmpObjId base) {
        m_base = base;
    }

    public SnmpInstId getInstance() {
        return m_instance;
    }

    public void setInstance(SnmpInstId instance) {
        m_instance = instance;
    }

    public SnmpValue getValue() {
        return m_value;
    }

    public void setValue(SnmpValue value) {
        m_value = value;
    }

    public SnmpObjId getAbsoluteInstance() {
        return getBase().append(getInstance());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("base", getBase())
            .append("instance", getInstance())
            .append("value", getValue())
            .toString();
    }

	@Override
	public int compareTo(SnmpResult other) {
		return getAbsoluteInstance().compareTo(other.getAbsoluteInstance());
	}

    @Override
    public int hashCode() {
        return Objects.hash(m_base, m_instance, m_value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SnmpResult other = (SnmpResult) obj;
        // Compare the type and byte contents to determine if two values are equal
        // Do not rely on the equals() method for this attribute
        if (m_value == null) {
            if (other.m_value != null)
                return false;
        } else if (m_value.getType() != other.m_value.getType()
                    || !Arrays.equals(m_value.getBytes(), other.m_value.getBytes())) {
           return false;
        }
        return Objects.equals(this.m_base, other.m_base)
                && Objects.equals(this.m_instance, other.m_instance);
    }
}
