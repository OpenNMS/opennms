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

package org.opennms.web.utils;

import javax.servlet.ServletException;

public class ExceptionUtils {

    /**
     * Recursively attempts to cast the given {@link Throwable} and it's cause
     * to an {@link Throwable} of the given type.
     *
     * @param t
     * @param type
     * @return null of no suitable cause was found.
     * @throws ServletException
     */
    public static <T extends Throwable> T getRootCause(Throwable t, Class<T> type) throws ServletException {
        if (t == null) {
            throw new ServletException("Null exceptions are not supported.");
        }

        // Can we cast the exception directly?
        if (t.getClass().isAssignableFrom(type)) {
            return type.cast(t);
        }

        // Recurse with the root cause
        if (t instanceof ServletException) {
            final ServletException se = (ServletException)t;
            final Throwable cause = se.getRootCause();
            if (cause != null) {
                return getRootCause(cause, type);
            }
        }

        throw new ServletException("Unsupported exception of type " + t.getClass().getCanonicalName(), t);
    }
}
