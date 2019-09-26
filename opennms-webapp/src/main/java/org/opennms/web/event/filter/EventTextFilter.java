/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

import org.opennms.web.filter.OrFilter;

public class EventTextFilter extends OrFilter {
    public static final String TYPE = "eventtext";

    private final String value;

    public EventTextFilter(String substring) {
        super(new LogMessageSubstringFilter(substring), new DescriptionSubstringFilter(substring));
        this.value = substring;
    }

    @Override
    public String getTextDescription() {
        return ("event text containing \"" + value + "\"");
    }

    @Override
    public String toString() {
        return ("<EventTextFilter: " + this.getDescription() + ">");
    }

    @Override
    public String getDescription() {
        return TYPE + "=" + value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof EventTextFilter)) return false;
        return (this.toString().equals(obj.toString()));
    }
}
