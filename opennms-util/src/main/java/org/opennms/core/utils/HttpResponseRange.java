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

package org.opennms.core.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpResponseRange {
    private static final Pattern RANGE_PATTERN = Pattern.compile("([1-5][0-9][0-9])(?:-([1-5][0-9][0-9]))?");
    private final int m_begin;
    private final int m_end;

    public HttpResponseRange(String rangeSpec) {
        Matcher matcher = RANGE_PATTERN.matcher(rangeSpec);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid range spec: " + rangeSpec);
        }

        String beginSpec = matcher.group(1);
        String endSpec = matcher.group(2);

        m_begin = Integer.parseInt(beginSpec);

        if (endSpec == null) {
            m_end = m_begin;
        } else {
            m_end = Integer.parseInt(endSpec);
        }
    }

    public boolean contains(int responseCode) {
        return (m_begin <= responseCode && responseCode <= m_end);
    }

    @Override
    public String toString() {
        if (m_begin == m_end) {
            return Integer.toString(m_begin);
        } else {
            return Integer.toString(m_begin) + '-' + Integer.toString(m_end);
        }
    }
}