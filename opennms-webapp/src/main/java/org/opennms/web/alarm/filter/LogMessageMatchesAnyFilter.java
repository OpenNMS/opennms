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

package org.opennms.web.alarm.filter;

import org.opennms.web.filter.SubstringFilter;

/**
 * <p>LogMessageMatchesAnyFilter class.</p>
 *
 * @author <A HREF="mailto:jamesz@opennms.com">James Zuo </A>
 * @version $Id: $
 * @since 1.8.1
 */
public class LogMessageMatchesAnyFilter extends SubstringFilter {
    /** Constant <code>TYPE="msgmatchany"</code> */
    public static final String TYPE = "msgmatchany";

    /**
     * <p>Constructor for LogMessageMatchesAnyFilter.</p>
     *
     * @param substring a {@link java.lang.String} object.
     */
    public LogMessageMatchesAnyFilter(String substring) {
        super(TYPE, "logMsg", "logMsg", substring);
    }

    /**
     * <p>getTextDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getTextDescription() {
        StringBuffer buffer = new StringBuffer("message containing \"");
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
        return this.toString().equals(obj.toString());
    }

}
