/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.core.utils;

import java.util.Date;

/**
 * 
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 */
public class FuzzyDateFormatter {
    public static final double MILLISECONDS_PER_SECOND = 1000.0;
    public static final double MILLISECONDS_PER_DAY = 86400000.0;
    public static final double MILLISECONDS_PER_HOUR = 3600000.0;
    public static final double MILLISECONDS_PER_MINUTE = 60000.0;

    public FuzzyDateFormatter() {
    }

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