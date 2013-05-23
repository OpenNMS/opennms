/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
