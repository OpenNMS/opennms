/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2002-2004, 2006-2009 The OpenNMS Group, Inc.  All rights reserved.
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


package org.opennms.web.event.filter;

import org.opennms.web.filter.SubstringFilter;

/**
 * @author <A HREF="mailto:jamesz@opennms.com">James Zuo </A>
 */
public class LogMessageMatchesAnyFilter extends SubstringFilter {
    public static final String TYPE = "msgmatchany";

    /**
     * @param substring
     *            a space-delimited list of search substrings
     */
    public LogMessageMatchesAnyFilter(String substring) {
        super(TYPE, "EVENTLOGMSG", "eventLogMsg", substring);
    }

    public String getTextDescription() {
        StringBuffer buffer = new StringBuffer("message containing \"");
        buffer.append(getValue());
        buffer.append("\"");

        return buffer.toString();
    }

    public String toString() {
        return "<LogMessageMatchesAnyFilter: " + this.getDescription() + ">";
    }

    public boolean equals(Object obj) {
        return this.toString().equals(obj.toString());
    }

}
