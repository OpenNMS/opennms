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

package org.opennms.web.event.filter;

import org.opennms.web.filter.SubstringFilter;

/**
 * <p>LogMessageMatchesAnyFilter class.</p>
 *
 * @author <A HREF="mailto:jamesz@opennms.com">James Zuo </A>
 * 
 * @deprecated Replace calls to this with the identical {@link LogMessageSubstringFilter}
 */
public class LogMessageMatchesAnyFilter extends SubstringFilter {
    /** Constant <code>TYPE="msgmatchany"</code> */
    public static final String TYPE = "msgmatchany";

    /**
     * <p>Constructor for LogMessageMatchesAnyFilter.</p>
     *
     * @param substring
     *            a space-delimited list of search substrings
     */
    public LogMessageMatchesAnyFilter(String substring) {
        super(TYPE, "EVENTLOGMSG", "eventLogMsg", substring);
    }

    /**
     * <p>getTextDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getTextDescription() {
        final StringBuilder buffer = new StringBuilder("message containing \"");
        buffer.append(getValue());
        buffer.append("\"");

        return buffer.toString();
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return "<LogMessageMatchesAnyFilter: " + this.getDescription() + ">";
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof LogMessageMatchesAnyFilter)) return false;
        return this.toString().equals(obj.toString());
    }

}
