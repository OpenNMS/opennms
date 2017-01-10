/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
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

package org.opennms.web.event.filter;

import org.opennms.web.filter.NotEqualOrNullFilter;
import org.opennms.web.filter.SQLType;

public class NegativeLocationFilter extends NotEqualOrNullFilter<String> {
    public static final String TYPE = "locationnot";
    private String m_location;

    public NegativeLocationFilter(String location) {
        super(TYPE, SQLType.STRING, "MONITORINGSYSTEMS.LOCATION", "distPoller.location", location);
        m_location = location;
    }

    @Override
    public String getTextDescription() {
        return ("Location is not " + m_location);
    }

    @Override
    public String toString() {
        return ("<WebEventRepository.NegativeLocationFilter: " + getDescription() + ">");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof NegativeLocationFilter)) return false;
        return (this.toString().equals(obj.toString()));
    }
}
