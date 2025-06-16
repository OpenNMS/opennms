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
