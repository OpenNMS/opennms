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

import java.util.Date;

import org.opennms.web.filter.LessThanFilter;
import org.opennms.web.filter.SQLType;

/**
 * <p>BeforeDateFilter class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class BeforeDateFilter extends LessThanFilter<Date> {
    /** Constant <code>TYPE="beforedate"</code> */
    public static final String TYPE = "beforedate";

    /**
     * <p>Constructor for BeforeDateFilter.</p>
     *
     * @param date a java$util$Date object.
     */
    public BeforeDateFilter(Date date) {
        super(TYPE, SQLType.DATE, "EVENTTIME", "eventTime", date);
    }

    /**
     * <p>Constructor for BeforeDateFilter.</p>
     *
     * @param epochTime a long.
     */
    public BeforeDateFilter(long epochTime) {
        this(new Date(epochTime));
    }

    /**
     * <p>getTextDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getTextDescription() {
        return ("date before \"" + getValue() + "\"");
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return ("<BeforeTimeFilter: " + this.getDescription() + ">");
    }

    /**
     * <p>getDate</p>
     *
     * @return a java$util$Date object.
     */
    public Date getDate() {
        return getValue();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof BeforeDateFilter)) return false;
        return (this.toString().equals(obj.toString()));
    }
}
