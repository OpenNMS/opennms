/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.jasper.helper;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * JasperReports scriptlet to retrieve start and end dates from predefined
 * ranges: last year, last month, this year, this month
 *
 * @author ronny
 */
public enum TimeRange {

    LAST_SEVEN_DAYS {
        @Override
        public Date getStartDate() {
            return new Date(new GregorianCalendar(new GregorianCalendar().get(Calendar.YEAR), new GregorianCalendar().get(Calendar.MONTH), new GregorianCalendar().get(Calendar.DATE) - 7).getTimeInMillis());
        }

    },
    LAST_MONTH {
        @Override
        public Date getStartDate() {
            return new Date(new GregorianCalendar(new GregorianCalendar().get(Calendar.YEAR), new GregorianCalendar().get(Calendar.MONTH) -1, 1).getTimeInMillis());
        }

        @Override
        public Date getEndDate() {
            return new Date(new GregorianCalendar(new GregorianCalendar().get(Calendar.YEAR), new GregorianCalendar().get(Calendar.MONTH), 0, 23, 59, 59).getTimeInMillis());
        }

    },
    LAST_YEAR {
        @Override
        public Date getStartDate() {
            return new Date(new GregorianCalendar(new GregorianCalendar().get(Calendar.YEAR) - 1, Calendar.JANUARY, 1).getTimeInMillis());
        }

        @Override
        public Date getEndDate() {
            return new Date(new GregorianCalendar(new GregorianCalendar().get(Calendar.YEAR) - 1, Calendar.UNDECIMBER, 0, 23, 59, 59).getTimeInMillis());
        }

    },
    THIS_MONTH {
        @Override
        public Date getStartDate() {
            return new Date(new GregorianCalendar(new GregorianCalendar().get(Calendar.YEAR), new GregorianCalendar().get(Calendar.MONTH), 1).getTimeInMillis());
        }

    },
    THIS_YEAR {
        @Override
        public Date getStartDate() {
            return new Date(new GregorianCalendar(new GregorianCalendar().get(Calendar.YEAR), Calendar.JANUARY, 1).getTimeInMillis());
        }

    };

    public Date getEndDate() {
        return new Date( new GregorianCalendar(new GregorianCalendar().get(Calendar.YEAR), new GregorianCalendar().get(Calendar.MONTH), new GregorianCalendar().get(Calendar.DATE) ).getTimeInMillis() );
    }

    public abstract Date getStartDate();


    /**
     * <p>
     * getStartDate
     * </p>
     * 
     * @param range
     *            a {@link java.lang.String} object
     * @return a {@link java.sql.Timestamp} object
     */
    public static Date getStartDate(String range) {
        TimeRange timeRange = getTimeRange(range);
        return timeRange != null ? timeRange.getStartDate() : null;
    }

    /**
     * <p>
     * getEndDate
     * </p>
     * 
     * @param range
     *            a {@link java.lang.String} object
     * @return a {@link java.sql.Timestamp} object
     */
    public static Date getEndDate(String range) {
        TimeRange timeRange = getTimeRange(range);
        return timeRange != null ? timeRange.getEndDate() : null;
    }

    private static TimeRange getTimeRange(String range) {
        for(TimeRange eachTimeRange : values()) {
            if (eachTimeRange.name().equalsIgnoreCase(range)) {
                return eachTimeRange;
            }
        }
        return null;
    }
}
