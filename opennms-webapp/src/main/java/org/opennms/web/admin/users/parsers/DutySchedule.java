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

package org.opennms.web.admin.users.parsers;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * This class holds information on the duty schedules that users can have.
 * Converstion between different formats of the duty schedule information are
 * possible, as is the comparision between a Calendar passed in and the start
 * and stop times of each day in a duty schedule.
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class DutySchedule implements Cloneable {
    /**
     * Each boolean in the bit set represents a day of the week. Monday = 0,
     * Tuesday = 1 ... Sunday = 6
     */
    private BitSet m_days;

    /**
     * The starting time of this DutySchedule
     */
    private int m_startTime;

    /**
     * The ending time of this DutySchedule
     */
    private int m_stopTime;

    /**
     * A series of constants to identify the days of the week as used by the
     * DutySchedule class
     */
    public static final int MONDAY = 0;

    /** Constant <code>TUESDAY=1</code> */
    public static final int TUESDAY = 1;

    /** Constant <code>WEDNESDAY=2</code> */
    public static final int WEDNESDAY = 2;

    /** Constant <code>THURSDAY=3</code> */
    public static final int THURSDAY = 3;

    /** Constant <code>FRIDAY=4</code> */
    public static final int FRIDAY = 4;

    /** Constant <code>SATURDAY=5</code> */
    public static final int SATURDAY = 5;

    /** Constant <code>SUNDAY=6</code> */
    public static final int SUNDAY = 6;

    /**
     * A list of names to abbreviate the days of the week
     */
    public static final String[] DAY_NAMES = { "Mo", "Tu", "We", "Th", "Fr", "Sa", "Su" };

    /**
     * A mapping between the days of the week as indexed by the DutySchedule
     * class and those of the Calendar class
     */
    private static final int[] CALENDAR_DAY_MAPPING = { Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY };

    /**
     * Create a new DutySchedule. Builds the BitSet used to identify the days of
     * the week that are set.
     */
    public DutySchedule() {
        m_days = new BitSet(7);
    }

    /**
     * Create a new DutySchedule. This constructor is designed to convert from a
     * Vector filled with 7 Boolean objects and two String objects into the
     * BitSet and integer start and stop time. Very useful for the ModifyUser
     * screen when it is converting from a table display to save the information
     * to a string format for the users.xml.
     *
     * @param aSchedule
     *            Vector filled with 7 Boolean objects and two String objects
     */
    public DutySchedule(Vector<?> aSchedule) {
        m_days = new BitSet(7);

        // set each day that is set to true
        for (int i = 0; i < 7; i++) {
            if (((Boolean) aSchedule.get(i)).booleanValue()) {
                m_days.set(i);
            }
        }

        // initialize the start and stop times, which should be in the last to
        // indexes of the vector
        m_startTime = Integer.parseInt((String) aSchedule.get(7));
        m_stopTime = Integer.parseInt((String) aSchedule.get(8));
    }

    /**
     * Create a new DutySchedule. This constructor is designed to build a new
     * DutySchedule from a String representation formatted as such.
     * <day_of_week_abbr><start>- <stop>eg. MoWeFr800-1700, TuTh900-1500.
     *
     * @param aSchedule
     *            the string to convert to a new DutySchedule
     */
    public DutySchedule(String aSchedule) {
        m_days = new BitSet(7);

        // parse the endtime and day/begin time out
        StringTokenizer timeTokens = new StringTokenizer(aSchedule, "-");
        String daysAndStartTime = timeTokens.nextToken();

        m_stopTime = Integer.parseInt(timeTokens.nextToken());

        // loop through the first half of the string and get each two letter
        // day abbreviation, set the appropriate BitSet values
        for (int j = 0; j < daysAndStartTime.length(); j++) {
            // check to see if there is a character or digit at the current
            // index
            if (!Character.isDigit(daysAndStartTime.charAt(j))) {
                // look at the current and next characters, advance the loop
                // counter
                // by one and add one to get the propert substring
                m_days.set(getDayInt(daysAndStartTime.substring(j, ++j + 1)));
            } else {
                // if a digit was seen this is the start time, get it and stop
                // the loop
                m_startTime = Integer.parseInt(daysAndStartTime.substring(j, daysAndStartTime.length()));
                break;
            }
        }
    }

    /**
     * Gets the index value of a day. This method returns the index value of a
     * day abbreviation
     * 
     * @param aDay
     *            the day abbreviation
     * @return the index associated with this abbreviation
     */
    private int getDayInt(String aDay) {
        int value = -1;

        for (int i = 0; i < DAY_NAMES.length; i++) {
            if (aDay.equals(DAY_NAMES[i])) {
                value = i;
                break;
            }
        }

        return value;
    }

    /**
     * Sets the day. This method sets the BitSet that tracks what days this
     * DutySchedule applies to.
     *
     * @param aDay
     *            the day index to set in the BitSet
     */
    public void setDay(int aDay) {
        m_days.set(aDay);
    }

    /**
     * Gets the start time. This method return the start time as an integer
     *
     * @return the start time of this DutySchedule
     */
    public int getStartTime() {
        return m_startTime;
    }

    /**
     * Gets the stop time. This method return the stop time as an integer
     *
     * @return the stop time of this DutySchedule
     */
    public int getStopTime() {
        return m_stopTime;
    }
    
    /**
     * Gets the days this DutySchedule is active.
     *
     * @return the days this DutySchedule is active.
     */
    public List<Boolean> getDaysAsBooleanList() {
        List<Boolean> list = new ArrayList<Boolean>(m_days.size());
        for (int i = 0; i < m_days.size(); i++) {
            list.add(m_days.get(i));
        }
        return list;
    }

    /**
     * Gets the Vector representation of the DutySchedule. This method formats
     * the DutySchedule as a vector populated with the first seven objects as
     * Booleans set to indicate what days of the week are stored, and the last
     * two objects as Strings that reflect the start time and stop time
     * respectively. This method gives a Vector that can be passed to the
     * DutySchedule(Vector) constructor to create a new DutySchedule
     *
     * @return a Vector properly formatted to reflect this DutySchedule
     * @deprecated call the individual getters
     */
    public Vector<Object> getAsVector() {
        Vector<Object> vector = new Vector<Object>();

        for (int i = 0; i < 7; i++) {
            vector.add(new Boolean(m_days.get(i)));
        }

        vector.add(String.valueOf(m_startTime));
        vector.add(String.valueOf(m_stopTime));

        return vector;
    }

    /**
     * Test if time is contined in schedule. This method decides if a given time
     * falls within the duty schedule contained in this object. It creates two
     * partial Calendars from the Calendar that is passed in and then sets the
     * start time for one and the end time for the other. Then in a loop it
     * reassigns the day of week according to the BitSet. It makes a comparision
     * to see if the argument Calendar is between the start and stop times and
     * returns true immediately if it is.
     *
     * @param aTime
     *            the time to check
     * @return true if the Calendar is contained in the duty schedule, false if
     *         it isn't.
     */
    public boolean isInSchedule(Calendar aTime) {
        boolean response = false;

        // make two new Calendar objects from the YEAR, MONTH and DATE of the
        // date we are checking.
        Calendar startTime = new GregorianCalendar(aTime.get(Calendar.YEAR), aTime.get(Calendar.MONTH), aTime.get(Calendar.DATE));

        // the hour will be the integer part of the start time divided by 100
        // cause it should be
        // in military time
        startTime.set(Calendar.HOUR_OF_DAY, (m_startTime / 100));

        // the minute will be the start time mod 100 cause it should be in
        // military time
        startTime.set(Calendar.MINUTE, (m_startTime % 100));
        startTime.set(Calendar.SECOND, 0);

        Calendar endTime = new GregorianCalendar(aTime.get(Calendar.YEAR), aTime.get(Calendar.MONTH), aTime.get(Calendar.DATE));

        endTime.set(Calendar.HOUR_OF_DAY, (m_stopTime / 100));
        endTime.set(Calendar.MINUTE, (m_stopTime % 100));
        endTime.set(Calendar.SECOND, 0);

        // look at the BitSet to see what days are set for this duty schedule,
        // reassign the
        // day of weak for the start and stop time, then see if the argument
        // Calendar is
        // between these times.
        for (int i = 0; i < 7; i++) {
            // see if the now time corresponds to a day when the user is on duty
            if (m_days.get(i) && CALENDAR_DAY_MAPPING[i] == aTime.get(Calendar.DAY_OF_WEEK)) {
                // now check to see if the time given is between these two times
                // inclusive, if it is quit loop
                // we want the begin and end times for the ranges to be
                // includsive, so convert to milliseconds so we
                // can do a greater than/less than equal to comparisons
                long dateMillis = aTime.getTime().getTime();
                long startMillis = startTime.getTime().getTime();
                long endMillis = endTime.getTime().getTime();

                // return true if the agrument date falls between the start and
                // stop time
                if ((startMillis <= dateMillis) && (dateMillis <= endMillis)) {
                    response = true;
                    break;
                }
            }
        }

        return response;
    }

    /**
     * Sets the start Hour. This method sets the start time of this DutySchedule
     *
     * @param anHour
     *            the hour in military time to set the start time for the
     *            DutySchedule
     */
    public void setStartHour(int anHour) {
        m_startTime = anHour;
    }

    /**
     * Sets the stop Hour. This method sets the stop time of this DutySchedule
     *
     * @param anHour
     *            the hour in military time to set the end time for the
     *            DutySchedule
     */
    public void setEndHour(int anHour) {
        m_stopTime = anHour;
    }

    /**
     * String representation. This method returns the DutySchedule formatted as
     * a string that the DutySchedule(String) constructor could parse. The
     * string will be formatted as such: <day_of_week_abbr><start>- <stop>eg.
     * MoWeFr800-1700, TuTh900-1500.
     *
     * @return a string representation of this DutySchedule
     */
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();

        // put in abbreviations for the days of the week
        for (int i = 0; i < DAY_NAMES.length; i++) {
            if (m_days.get(i)) {
                buffer.append(DAY_NAMES[i]);
            }
        }

        // add the start and stop times to the end of the string
        buffer.append(m_startTime + "-" + m_stopTime);

        return buffer.toString();
    }
    
    /**
     * <p>hasDay</p>
     *
     * @param aDay a int.
     * @return a boolean.
     */
    public boolean hasDay(int aDay) {
        return m_days.get(aDay);
    }
    
    /**
     * <p>clone</p>
     *
     * @return a {@link org.opennms.web.admin.users.parsers.DutySchedule} object.
     */
    @Override
    public DutySchedule clone() {
        return new DutySchedule(toString());
    }
}
