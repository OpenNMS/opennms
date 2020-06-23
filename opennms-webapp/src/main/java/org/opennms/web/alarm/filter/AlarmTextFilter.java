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

package org.opennms.web.alarm.filter;

import org.opennms.web.filter.OrFilter;

public class AlarmTextFilter extends OrFilter {
    public static final String TYPE = "alarmtext";

    private final String value;

    public AlarmTextFilter(String substring) {
        super(new LogMessageSubstringFilter(substring), new DescriptionSubstringFilter(substring));
        this.value = substring;
    }

    @Override
    public String getTextDescription() {
        return ("alarm text containing \"" + value + "\"");
    }

    @Override
    public String toString() {
        return ("<AlarmTextFilter: " + this.getDescription() + ">");
    }

    @Override
    public String getDescription() {
        return TYPE + "=" + value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof AlarmTextFilter)) return false;
        return this.toString().equals(obj.toString());
    }
}
