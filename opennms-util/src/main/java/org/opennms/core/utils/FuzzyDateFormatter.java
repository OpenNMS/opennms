package org.opennms.core.utils;

import java.util.Date;

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