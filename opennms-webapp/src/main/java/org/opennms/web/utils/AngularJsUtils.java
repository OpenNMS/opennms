/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2026 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2026 The OpenNMS Group, Inc.
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

package org.opennms.web.utils;

/**
 * Encodes values for AngularJS expressions in JSP pages.
 * JSP EL calls the static methods. EL needs a public class that is not abstract.
 */
public final class AngularJsUtils {

    private AngularJsUtils() {
    }

    /**
     * Encodes a value for a single-quoted string in an AngularJS expression, for example
     * {@code ng-click="open('${AngularJsUtils.escapeString(label)}')"}.
     *
     * Letters, digits and some URL characters stay. Other characters become backslash-u escapes,
     * which the AngularJS parser reads. The output has no quotes, no "&amp;", no "&lt;" and no "{".
     * Thus, it is safe in an HTML attribute, and AngularJS does not interpolate it.
     *
     * @param value the value, or null
     * @return the encoded value, or an empty string for null
     */
    public static String escapeString(final String value) {
        if (value == null) {
            return "";
        }
        final StringBuilder buffer = new StringBuilder(value.length());
        for (final char c : value.toCharArray()) {
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || " _-.:/,=?[]()".indexOf(c) >= 0) {
                buffer.append(c);
            } else {
                buffer.append(String.format("\\u%04x", (int) c));
            }
        }
        return buffer.toString();
    }
}
