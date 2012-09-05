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

package org.opennms.netmgt.eventd.db;

import java.io.StringWriter;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.model.events.Constants;
import org.springframework.dao.DataAccessException;

/**
 * This is an utility class used to format the event correlation info - to be
 * inserted into the 'events' table - it simply returns the correlation as an
 * 'XML' block
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Kumaraswamy </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @see org.opennms.netmgt.model.events.Constants#VALUE_TRUNCATE_INDICATOR
 */
public class Correlation {
    /**
     * Format the correlation block to have the xml
     *
     * @param ec
     *            the correlation
     * @param sz
     *            the size to which the formatted string is to be limited
     *            to(usually the size of the column in the database)
     * @return the formatted event correlation
     */
    public static String format(final org.opennms.netmgt.xml.event.Correlation ec, final int sz) {
        StringWriter out = new StringWriter();
        try {
            JaxbUtils.marshal(ec, out);
        } catch (DataAccessException e) {
            ThreadCategory.getInstance(Correlation.class).error("Failed to convert new event to XML", e);
            return null;
        }

        String outstr = out.toString();
        if (outstr.length() >= sz) {
            StringBuffer buf = new StringBuffer(outstr);

            buf.setLength(sz - 4);
            buf.append(Constants.VALUE_TRUNCATE_INDICATOR);

            return buf.toString();
        } else {
            return outstr;
        }

    }

}
