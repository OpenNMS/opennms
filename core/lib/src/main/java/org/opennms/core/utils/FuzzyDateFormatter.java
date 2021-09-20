/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.core.utils;

import java.util.Date;

/**
 * <p>FuzzyDateFormatter class.</p>
 *
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 * @version $Id: $
 */
public abstract class FuzzyDateFormatter {
    /** Constant <code>MILLISECONDS_PER_SECOND=1000.0</code> */
    private static final double MILLISECONDS_PER_SECOND = 1000.0;
    /** Constant <code>MILLISECONDS_PER_DAY=86400000.0</code> */
    private static final double MILLISECONDS_PER_DAY = 86400000.0;
    /** Constant <code>MILLISECONDS_PER_HOUR=3600000.0</code> */
    private static final double MILLISECONDS_PER_HOUR = 3600000.0;
    /** Constant <code>MILLISECONDS_PER_MINUTE=60000.0</code> */
    private static final double MILLISECONDS_PER_MINUTE = 60000.0;

    /**
     * <p>Constructor for FuzzyDateFormatter.</p>
     */
    public FuzzyDateFormatter() {
    }

    /**
     * <p>formatNumber</p>
     *
     * @param number a {@link java.lang.Double} object.
     * @param singular a {@link java.lang.String} object.
     * @param plural a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    protected static String formatNumber(Double number, String singular, String plural) {
        String returnVal;
        returnVal = Long.toString(Math.round(number.doubleValue()));
        
        if (returnVal.equals("1")) {
            returnVal = number.intValue() + " " + singular;
        } else {
            returnVal = returnVal + " " + plural;
        }
        // System.err.println("returning " + returnVal + " for number " + number.doubleValue());
        return returnVal;
    }

    /**
     * <p>calculateDifference</p>
     *
     * @param start a {@link java.util.Date} object.
     * @param end a {@link java.util.Date} object.
     * @return a {@link java.lang.String} object.
     */
    public static String calculateDifference(Date start, Date end) {
        Long difference = Math.abs(end.getTime() - start.getTime());
        // System.err.println("difference = " + difference);
        
        double days = (difference.doubleValue() / MILLISECONDS_PER_DAY);
        // System.err.println("days = " + days);
        
        if (days < 1) {
            double hours = (difference.doubleValue() / MILLISECONDS_PER_HOUR);
            if (hours < 1) {
                double minutes = (difference.doubleValue() / MILLISECONDS_PER_MINUTE);
                if (minutes < 1) {
                    double seconds = (difference.floatValue() / MILLISECONDS_PER_SECOND);
                    return formatNumber(seconds, "second", "seconds");
                } else {
                    return formatNumber(minutes, "minute", "minutes");
                }
            } else {
                return formatNumber(hours, "hour", "hours");
            }
        } else if (days >= 365.0) {
            return formatNumber((days / 365.0), "year", "years");
        } else if (days >= 30.0) {
            return formatNumber((days / 30.0), "month", "months");
        } else if (days >= 7.0) {
            return formatNumber((days / 7.0), "week", "weeks");
        } else if (days >= 1.0) {
            return formatNumber(days, "day", "days");
        }

        return null;
    }

}
