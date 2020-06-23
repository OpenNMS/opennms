/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.core.soa.filter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.opennms.core.soa.Filter;

/**
 * OrFilter
 *
 * @author brozow
 */
public class OrFilter extends AbstractFilter {
    
    private List<Filter> m_filters;

    public OrFilter(List<Filter> filters) {
        m_filters = filters;
    }

    public OrFilter(Filter... filters) {
        this(Arrays.asList(filters));
    }

    @Override
    public boolean match(Map<String, String> properties) {
        for(Filter f : m_filters) {
            if (f.match(properties)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append("(|");
        for(Filter f : m_filters) {
            buf.append(f);
        }
        buf.append(")");
        return buf.toString();
    }

}
