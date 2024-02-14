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
