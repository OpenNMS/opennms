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
package org.opennms.netmgt.collection.api;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.netmgt.collection.adapters.AttributeTypeAdapter;

/**
 * Defines how a particular attribute should be persisted.
 *
 * @author jwhite
 */
@XmlJavaTypeAdapter(AttributeTypeAdapter.class)
public enum AttributeType {
    GAUGE("gauge", true, "gauge32", "integer32"),
    COUNTER("counter", true, "counter32"),
    STRING("string", false);

    private final String m_name;
    private final boolean m_isNumeric;
    private final String[] m_aliases;

    private AttributeType(String name, boolean isNumeric, String... aliases) {
        m_name = name;
        m_isNumeric = isNumeric;
        m_aliases = aliases;
    }

    public String getName() {
        return m_name;
    }

    public boolean isNumeric() {
        return m_isNumeric;
    }

    public String[] getAliases() {
        return m_aliases;
    }

    /**
     * Parses the attribute type from the given string.
     *
     * @param typeAsString type
     * @return the matching attribute, or null if none was found
     */
    public static AttributeType parse(String typeAsString) {
        for (AttributeType type : AttributeType.values()) {
            if (type.getName().equalsIgnoreCase(typeAsString)) {
                return type;
            } else {
                // Attribute types can be referred to by many names
                for (String alias : type.getAliases()) {
                    if (alias.equalsIgnoreCase(typeAsString)) {
                        return type;
                    }
                }
            }
        }
        return null;
    }
}
