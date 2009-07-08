/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2002-2004, 2006, 2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/


package org.opennms.netmgt.eventd.db;

import java.io.StringWriter;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;

/**
 * This is an utility class used to format the event correlation info - to be
 * inserted into the 'events' table - it simply returns the correlation as an
 * 'XML' block
 * 
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Kumaraswamy </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 * @see org.opennms.netmgt.eventd.db.Constants#VALUE_TRUNCATE_INDICATOR
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
     * 
     * @return the formatted event correlation
     */
    public static String format(org.opennms.netmgt.xml.event.Correlation ec, int sz) {
        StringWriter out = new StringWriter();
        try {
            Marshaller.marshal(ec, out);
        } catch (MarshalException e) {
            ThreadCategory.getInstance(Correlation.class).error("Failed to convert new event to XML", e);
            return null;
        } catch (ValidationException e) {
            ThreadCategory.getInstance(Correlation.class).error("Failed to convert new event to XML", e);
            return null;
        }

        String outstr = out.getBuffer().toString();
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
