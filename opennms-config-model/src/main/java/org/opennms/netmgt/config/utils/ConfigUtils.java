/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.utils;

import java.util.Collection;

public abstract class ConfigUtils {
    public static <T> T assertNotNull(final T value, final String name) throws IllegalArgumentException {
        if (value == null) {
            throw new IllegalArgumentException("'" + name + "' cannot be null!");
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    public static <T> T assertNotEmpty(final T value, final String name) throws IllegalArgumentException {
        final T check = value instanceof String? (T)normalizeString((String) value) : value;
        assertNotNull(value, name);
        return check;
    }
    
    public static String normalizeString(final String s) {
        if ("".equals(s)) return null;
        return s;
    }

    public static String normalizeAndTrimString(final String value) {
        if (value == null) return null;
        return normalizeString(value).trim();
    }

    public static <T extends Number> T assertMinimumInclusive(final T value, final long minimum, final String name) {
        if (value != null && value.longValue() < minimum) {
            throw new IllegalArgumentException("'" + name + "' must be at least " + minimum);
        }
        return value;
    }

    public static <T extends Collection<?>> T assertMinimumSize(final T value, final long minimum, final String name) {
        if (value != null && value.size() < minimum) {
            throw new IllegalArgumentException("'" + name + "' must contain at least " + minimum + " entry!");
        }
        return value;
    }
}
