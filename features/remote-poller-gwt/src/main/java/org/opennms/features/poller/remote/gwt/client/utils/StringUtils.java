/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

/**
 * <p>StringUtils class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
package org.opennms.features.poller.remote.gwt.client.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public abstract class StringUtils {
    /**
     * <p>join</p>
     *
     * @param s a {@link java.util.Collection} object.
     * @param delimiter a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String join(final Collection<?> s, final String delimiter) {
        if (s == null) {
            return "null";
        }
        final StringBuilder builder = new StringBuilder();
        final Iterator<?> iter = s.iterator();
        while (iter.hasNext()) {
            builder.append(iter.next());
            if (!iter.hasNext()) {
                break;
            }
            builder.append(delimiter);
        }
        return builder.toString();
    }

    /**
     * <p>join</p>
     *
     * @param s a {@link java.util.List} object.
     * @return a {@link java.lang.String} object.
     */
    public static String join(final List<String> s) {
        return join(s, ", ");
    }
}
