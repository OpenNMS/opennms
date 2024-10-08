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
//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.3-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.01.29 at 01:15:48 PM EST 
//


package org.opennms.netmgt.provision.persist.requisition;

import javax.xml.bind.ValidationException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.CompareToBuilder;

/**
 * <p>RequisitionAsset class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="")
@XmlRootElement(name="asset")
public class RequisitionAsset implements Comparable<RequisitionAsset> {

    @XmlAttribute(name="name", required=true)
    protected String m_name;

    @XmlAttribute(name="value", required=true)
    protected String m_value;

    /**
     * <p>Constructor for RequisitionAsset.</p>
     */
    public RequisitionAsset() {
    }

    /**
     * <p>Constructor for RequisitionAsset.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param value a {@link java.lang.String} object.
     */
    public RequisitionAsset(String name, String value) {
        m_name = name;
        m_value = value;
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return m_name;
    }

    /**
     * <p>setName</p>
     *
     * @param value a {@link java.lang.String} object.
     */
    public void setName(String value) {
        m_name = value;
    }

    /**
     * <p>getValue</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getValue() {
        return m_value;
    }

    /**
     * <p>setValue</p>
     *
     * @param value a {@link java.lang.String} object.
     */
    public void setValue(String value) {
        m_value = value;
    }

    public void validate() throws ValidationException {
        if (m_name == null) {
            throw new ValidationException("Requisition asset 'name' is a required attribute!");
        }
        if (m_value == null) {
            throw new ValidationException("Requisition asset 'value' is a required attribute!");
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
        result = prime * result + ((m_value == null) ? 0 : m_value.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!(obj instanceof RequisitionAsset)) return false;
        final RequisitionAsset other = (RequisitionAsset) obj;
        if (m_name == null) {
            if (other.m_name != null) return false;
        } else if (!m_name.equals(other.m_name)) {
            return false;
        }
        if (m_value == null) {
            if (other.m_value != null) return false;
        } else if (!m_value.equals(other.m_value)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "RequisitionAsset [name=" + m_name + ", value=" + m_value + "]";
    }

    @Override
    public int compareTo(final RequisitionAsset other) {
        return new CompareToBuilder()
            .append(m_name, other.m_name)
            .append(m_value, other.m_value)
            .toComparison();
    }
}
