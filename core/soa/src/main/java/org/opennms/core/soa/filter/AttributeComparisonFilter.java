/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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

package org.opennms.core.soa.filter;

import java.util.Map;

/**
 * AttributeComparisonFilter
 *
 * @author brozow
 */
public abstract class AttributeComparisonFilter extends AbstractFilter {
    
    private String m_attribute;
    
    protected AttributeComparisonFilter(String attribute) {
        m_attribute = attribute;
    }
    
    protected String getAttribute() {
        return m_attribute;
    }

    @Override
    public boolean match(Map<String, String> properties) {
        if (properties == null || !properties.containsKey(m_attribute)) {
            return false;
        } else {
            return valueMatches(properties.get(m_attribute));
        }
    }

    abstract protected boolean valueMatches(String value);

    @Override
    abstract public String toString();

}
