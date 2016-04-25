/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collection.support.builder;

import java.util.Objects;

/**
 * Used to represent an abstract attribute that was collected from some agent.
 *
 * Includes methods common to both numeric and string attributes.
 *
 * @author jwhite
 */
public abstract class Attribute<T> {
    private final Resource m_resource;
    private final String m_group;
    private final String m_name;
    private final T m_value;
    private final AttributeType m_type;
    private final String m_identifier;

    public Attribute(Resource resource, String group, String name, T value, AttributeType type, String identifier) {
        m_resource = Objects.requireNonNull(resource, "resource argument");
        m_group = Objects.requireNonNull(group, "group argument");
        m_name = Objects.requireNonNull(name, "name argument");
        m_value = Objects.requireNonNull(value, "value argument");
        m_type = Objects.requireNonNull(type, "type argument");
        m_identifier = identifier;
    }

    public abstract Number getNumericValue();

    public abstract String getStringValue();

    public Resource getResource() {
        return m_resource;
    }

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
}
