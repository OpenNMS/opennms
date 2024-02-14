/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
