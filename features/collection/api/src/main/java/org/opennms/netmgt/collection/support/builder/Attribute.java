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
package org.opennms.netmgt.collection.support.builder;

import java.util.Objects;

import org.opennms.netmgt.collection.api.AttributeType;

/**
 * Used to represent an abstract attribute that was collected from some agent.
 *
 * Includes methods common to both numeric and string attributes.
 *
 * @author jwhite
 */
public abstract class Attribute<T> {
    private final String m_group;
    private final String m_name;
    private final T m_value;
    private final AttributeType m_type;
    private final String m_identifier;

    public Attribute(String group, String name, T value, AttributeType type, String identifier) {
        m_group = Objects.requireNonNull(group, "group argument");
        m_name = Objects.requireNonNull(name, "name argument");
        m_value = Objects.requireNonNull(value, "value argument");
        m_type = Objects.requireNonNull(type, "type argument");
        m_identifier = identifier;
    }

    public abstract Number getNumericValue();

    public abstract String getStringValue();

    public String getGroup() {
        return m_group;
    }

    public String getName() {
        return m_name;
    }

    public T getValue() {
        return m_value;
    }

    public AttributeType getType() {
        return m_type;
    }

    public String getIdentifier() {
        return m_identifier;
    }

    @Override
    public String toString() {
        return String.format("Attribute[group=%s, name=%s, value=%s, type=%s, identifier=%s]",
                m_group, m_name, m_value, m_type, m_identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_group, m_name, m_value, m_type, m_identifier);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (!(obj instanceof Attribute)) {
            return false;
        }
        Attribute<?> other = (Attribute<?>) obj;
        return Objects.equals(this.m_group, other.m_group)
               && Objects.equals(this.m_name, other.m_name)
               && Objects.equals(this.m_type, other.m_type)
               && Objects.equals(this.m_identifier, other.m_identifier);
    }
}
