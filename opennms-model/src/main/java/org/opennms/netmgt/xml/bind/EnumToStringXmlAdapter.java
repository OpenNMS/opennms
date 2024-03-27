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
package org.opennms.netmgt.xml.bind;

import java.util.EnumSet;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 */
public class EnumToStringXmlAdapter<T extends Enum<T>> extends XmlAdapter<String, T> {

    private Class<T> m_class;
    private T m_defaultValue;

    protected EnumToStringXmlAdapter(Class<T> clazz, T defaultValue) {
        m_class = clazz;
        m_defaultValue = defaultValue;
    }

    /** {@inheritDoc} */
    @Override
    public String marshal(T nodeType) throws Exception {
        return nodeType.toString();
    }

    /** {@inheritDoc} */
    @Override
    public T unmarshal(String status) throws Exception {
        for (T type : EnumSet.allOf(m_class)) {
            if (type.toString().equalsIgnoreCase(status)) {
                return type;
            }
        }
        return m_defaultValue;
    }

}