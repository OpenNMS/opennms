/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.event.filter;

import org.opennms.web.filter.NotEqualOrNullFilter;
import org.opennms.web.filter.SQLType;

public class NegativeNodeLocationFilter extends NotEqualOrNullFilter<String> {
    public static final String TYPE = "nodelocationnot";
    private String m_location;

    public NegativeNodeLocationFilter(final String location) {
        super(TYPE, SQLType.STRING, "NODE.LOCATION", "node.location.locationName", location);
        m_location = location;
    }

    @Override
    public String getTextDescription() {
        return ("Node location is not " + m_location);
    }

    @Override
    public String toString() {
        return ("<WebEventRepository.NegativeNodeLocationFilter: " + getDescription() + ">");
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof NegativeNodeLocationFilter)) return false;
        return (this.toString().equals(obj.toString()));
    }
}
