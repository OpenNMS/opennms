/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.util;

import org.opennms.netmgt.model.events.Constants;
import org.opennms.netmgt.xml.event.Autoaction;

/**
 * This is an utility class used to format the event autoaction info - to be
 * inserted into the 'events' table
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj </A>
 */
public class AutoAction {
    /**
     * Format each autoaction entry
     *
     * @param autoact
     *            the entry
     * @return the formatted string
     */
    public static String format(Autoaction autoact) {
        String text = autoact.getContent();
        String state = autoact.getState();

        return Constants.escape(text, Constants.DB_ATTRIB_DELIM) + Constants.DB_ATTRIB_DELIM + state;

    }

    /**
     * Format the list of autoaction entries of the event
     *
     * @param autoacts
     *            the list
     * @param sz
     *            the size to which the formatted string is to be limited
     *            to(usually the size of the column in the database)
     * @return the formatted string
     */
    public static String format(Autoaction[] autoacts, int sz) {
        StringBuffer buf = new StringBuffer();
        boolean first = true;

        for (int index = 0; index < autoacts.length; index++) {
            if (!first)
                buf.append(Constants.MULTIPLE_VAL_DELIM);
            else
                first = false;

            buf.append(Constants.escape(format(autoacts[index]), Constants.MULTIPLE_VAL_DELIM));
        }

        if (buf.length() >= sz) {
            buf.setLength(sz - 4);
            buf.append(Constants.VALUE_TRUNCATE_INDICATOR);
        }

        return buf.toString();
    }
}
