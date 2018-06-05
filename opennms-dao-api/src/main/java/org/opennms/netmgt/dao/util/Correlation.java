/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.util;

import java.io.StringWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.events.api.EventDatabaseConstants;
import org.springframework.dao.DataAccessException;

/**
 * This is an utility class used to format the event correlation info - to be
 * inserted into the 'events' table - it simply returns the correlation as an
 * 'XML' block
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Kumaraswamy </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @see org.opennms.netmgt.events.api.EventDatabaseConstants#VALUE_TRUNCATE_INDICATOR
 */
public abstract class Correlation {
    private static final Logger LOG = LoggerFactory.getLogger(Correlation.class);
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
            LOG.error("Failed to convert new event to XML", e);
            return null;
        }

        String outstr = out.toString();
        if (outstr.length() >= sz) {
            final StringBuilder buf = new StringBuilder(outstr);

            buf.setLength(sz - 4);
            buf.append(EventDatabaseConstants.VALUE_TRUNCATE_INDICATOR);

            return buf.toString();
        } else {
            return outstr;
        }

    }

}
