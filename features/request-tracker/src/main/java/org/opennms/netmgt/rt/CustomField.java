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
package org.opennms.netmgt.rt;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

public class CustomField implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 4640559401699963154L;
    private String m_name;
    private List<CustomFieldValue> m_values;

    public CustomField() {
        this(null, new ArrayList<CustomFieldValue>());
    }

    public CustomField(final String name) {
        this(name, new ArrayList<CustomFieldValue>());
    }

    public CustomField(final String name, final String value, final boolean csv) {
        this(name);
        if (csv && value != null) {
            for (String aValue : value.split(",")) {
                m_values.add(new CustomFieldValue(aValue));
            }
        } else {
            m_values.add(new CustomFieldValue(value));
        }
    }

    public CustomField(final String name, List<CustomFieldValue> values) {
        m_name = name;
        m_values = values;
    }

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = name;
    }

    public List<CustomFieldValue> getValues() {
        return m_values;
    }

    public void setValues(final List<CustomFieldValue> values) {
        m_values = values;
    }

    public void addValue(final CustomFieldValue value) {
        m_values.add(value);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
        .append("Name", m_name)
        .append("Value count", m_values.size())
        .append("Values", StringUtils.join(m_values, ", "))
        .toString();
    }
}
