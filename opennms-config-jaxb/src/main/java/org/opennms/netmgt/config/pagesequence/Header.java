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
