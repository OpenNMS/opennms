/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.opennms.core.utils.OwnedInterval;
import org.opennms.core.utils.OwnedIntervalSequence;
import org.opennms.core.utils.Owner;
import org.opennms.core.utils.TimeInterval;
import org.opennms.netmgt.config.groups.Schedule;
import org.opennms.netmgt.config.poller.outages.Outage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>BasicScheduleUtils class.</p>
 */
public abstract class BasicScheduleUtils {
    private static final Logger LOG = LoggerFactory.getLogger(BasicScheduleUtils.class);
    private static final Pattern SPECIFIC_DATE_PATTERN = Pattern.compile("^(\\d\\d\\d\\d-\\d\\d-\\d\\d|\\d\\d-...-\\d\\d\\d\\d) .*$");

    /**
     * The day of the week values to name mapping
     */
    protected static final ThreadLocal<Map<String,Integer>> m_dayOfWeekMap = new ThreadLocal<Map<String,Integer>>() {
        @Override protected Map<String,Integer> initialValue() {
            final Map<String,Integer> map = new HashMap<String,Integer>();
            map.put("sunday", Calendar.SUNDAY);
            map.put("monday", Calendar.MONDAY);
            map.put("tuesday", Calendar.TUESDAY);
            map.put("wednesday", Calendar.WEDNESDAY);
            map.put("thursday", Calendar.THURSDAY);
            map.put("friday", Calendar.FRIDAY);
            map.put("saturday", Calendar.SATURDAY);
            return map;
        };
    };

    /** Constant <code>FORMAT1="dd-MMM-yyyy HH:mm:ss"</code> */
    public static final String FORMAT1 = "dd-MMM-yyyy HH:mm:ss";
    /** Constant <code>FORMAT2="HH:mm:ss"</code> */
    public static final String FORMAT2 = "HH:mm:ss";

    /**
     * <p>isTimeInSchedule</p>
     *
     * @param cal a {@link java.util.Calendar} object.
     * @param sched a {@link org.opennms.netmgt.config.poller.outages.common.BasicSchedule} object.
     * @return a boolean.
     */
    public static boolean isTimeInSchedule(final Calendar cal, final BasicSchedule sched) {
        if (cal == null) {
            LOG.warn("isTimeInOutage: calendar is null");
            return false;
        } else if (sched == null) {
            LOG.warn("isTimeInOutage: schedule is null");
            return false;
        }

        LOG.debug("isTimeInOutage: checking for time '{}' in schedule '{}'", cal.getTime(), sched.getName());

        Calendar outCalBegin = new GregorianCalendar();
        Calendar outCalEnd = new GregorianCalendar();
        
        long curCalTime = cal.getTimeInMillis();
        // check if day is part of outage
        boolean inOutage = false;
        Enumeration<Time> e = sched.enumerateTime();
        while (e.hasMoreElements() && !inOutage) {
            outCalBegin.setTimeInMillis(curCalTime);
            outCalEnd.setTimeInMillis(curCalTime);
    
            final Time oTime = (Time) e.nextElement();
    
            final String oTimeDay = oTime.getDay().orElse(null);
            final String begins = oTime.getBegins();
            final String ends = oTime.getEnds();
    
            if (oTimeDay != null) {
                // see if outage time was specified as sunday/monday..
                final Integer dayInMap = getDayOfWeekIndex(oTimeDay);
                if (dayInMap != null) {
                    // check if value specified matches current date
                    if (cal.get(Calendar.DAY_OF_WEEK) == dayInMap.intValue()) {
                        inOutage = true;
                    }
    
                    outCalBegin.set(Calendar.DAY_OF_WEEK, dayInMap.intValue());
                    outCalEnd.set(Calendar.DAY_OF_WEEK, dayInMap.intValue());
                }
                // else see if outage time was specified as day of month
                else {
                    int intOTimeDay = Integer.valueOf(oTimeDay);
    
                    if (cal.get(Calendar.DAY_OF_MONTH) == intOTimeDay) {
                        inOutage = true;
                    }
    
                    outCalBegin.set(Calendar.DAY_OF_MONTH, intOTimeDay);
                    outCalEnd.set(Calendar.DAY_OF_MONTH, intOTimeDay);
                }
            }
    
            // if time of day was specified and did not match, continue
            if (oTimeDay != null && !inOutage) continue;
    
            // set time in out calendars
            setOutCalTime(outCalBegin, begins);
            setOutCalTime(outCalEnd, ends);
    
            // check if calendar passed is in the out cal range
            LOG.debug("isTimeInOutage: checking begin/end time...\n current: {}\n begin: {}\n end: {}", cal.getTime(), outCalBegin.getTime(), outCalEnd.getTime());
    
            // round these to the surrounding seconds since we can only specify
            // this to seconds
            // accuracy in the config file
            final long outCalBeginTime = outCalBegin.getTimeInMillis() / 1000 * 1000;
            final long outCalEndTime = (outCalEnd.getTimeInMillis() / 1000 + 1) * 1000;
    
            if (curCalTime >= outCalBeginTime && curCalTime < outCalEndTime) {
                inOutage = true;
            }
            else {
                inOutage = false;
            }
        }
        return inOutage;
    }

    /**
     * Set the time in outCal from timeStr. 'timeStr'is in either the
     * 'dd-MMM-yyyy HH:mm:ss' or the 'HH:mm:ss' formats
     *
     * @param outCal
     *            the calendar in which time is to be set
     * @param timeStr
     *            the time string
     */
    public static void setOutCalTime(final Calendar outCal, final String timeStr) {
        if (timeStr.length() == BasicScheduleUtils.FORMAT1.length()) {
            SimpleDateFormat format = new SimpleDateFormat(BasicScheduleUtils.FORMAT1);
    
            // parse the date string passed
            Date tempDate = null;
            try {
                tempDate = format.parse(timeStr);
            } catch (ParseException pE) {
                tempDate = null;
            }
            if (tempDate == null)
                return;
    
            Calendar tempCal = new GregorianCalendar();
            tempCal.setTime(tempDate);
    
            // set outCal
            outCal.set(Calendar.YEAR, tempCal.get(Calendar.YEAR));
            outCal.set(Calendar.MONTH, tempCal.get(Calendar.MONTH));
            outCal.set(Calendar.DAY_OF_MONTH, tempCal.get(Calendar.DAY_OF_MONTH));
            outCal.set(Calendar.HOUR_OF_DAY, tempCal.get(Calendar.HOUR_OF_DAY));
            outCal.set(Calendar.MINUTE, tempCal.get(Calendar.MINUTE));
            outCal.set(Calendar.SECOND, tempCal.get(Calendar.SECOND));
            outCal.set(Calendar.MILLISECOND, 0);
        } else if (timeStr.length() == BasicScheduleUtils.FORMAT2.length()) {
            SimpleDateFormat format = new SimpleDateFormat(BasicScheduleUtils.FORMAT2);
    
            // parse the date string passed
            Date tempDate = null;
            try {
                tempDate = format.parse(timeStr);
            } catch (ParseException pE) {
                tempDate = null;
            }
            if (tempDate == null)
                return;
    
            Calendar tempCal = new GregorianCalendar();
            tempCal.setTime(tempDate);
    
            // set outCal
            outCal.set(Calendar.HOUR_OF_DAY, tempCal.get(Calendar.HOUR_OF_DAY));
            outCal.set(Calendar.MINUTE, tempCal.get(Calendar.MINUTE));
            outCal.set(Calendar.SECOND, tempCal.get(Calendar.SECOND));
            outCal.set(Calendar.MILLISECOND, 0);
        }
    }
    
    /**
     * <p>getDayOfWeekIndex</p>
     *
     * @param dayName a {@link java.lang.String} object.
     * @return a {@link java.lang.Integer} object.
     */
    public static Integer getDayOfWeekIndex(final String dayName) {
        if (dayName == null) return null;
        return m_dayOfWeekMap.get().get(dayName.toLowerCase());
    }
    
    /**
     * <p>getEndOfSchedule</p>
     *
     * @param out a {@link org.opennms.netmgt.config.poller.outages.common.BasicSchedule} object.
     * @return a {@link java.util.Calendar} object.
     */
    public static Calendar getEndOfSchedule(final BasicSchedule out) {
        final long curCalTime = System.currentTimeMillis();
        final Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(curCalTime);

        // check if day is part of outage
        boolean inOutage = false;
        
        final Enumeration<Time> en = out.enumerateTime();
        while (en.hasMoreElements() && !inOutage) {
            final Calendar outCalBegin = new GregorianCalendar();
            final Calendar outCalEnd = new GregorianCalendar();

            final Time oTime = en.nextElement();

            final String oTimeDay = oTime.getDay().orElse(null);
            final String begins = oTime.getBegins();
            final String ends = oTime.getEnds();

            if (oTime.getDay().isPresent()) {
                // see if outage time was specified as sunday/monday..
                final Integer dayInMap = getDayOfWeekIndex(oTimeDay);
                if (dayInMap != null) {
                    // check if value specified matches current date
                    if (cal.get(Calendar.DAY_OF_WEEK) == dayInMap.intValue()) inOutage = true;
                    outCalBegin.set(Calendar.DAY_OF_WEEK, dayInMap.intValue());
                    outCalEnd.set(Calendar.DAY_OF_WEEK, dayInMap.intValue());
                }
                // else see if outage time was specified as day of month
                else {
                    final int intOTimeDay = Integer.valueOf(oTimeDay).intValue();
    
                    if (cal.get(Calendar.DAY_OF_MONTH) == intOTimeDay) inOutage = true;
    
                    outCalBegin.set(Calendar.DAY_OF_MONTH, intOTimeDay);
                    outCalEnd.set(Calendar.DAY_OF_MONTH, intOTimeDay);
                }
            }

            // if time of day was specified and did not match, continue
            if (oTimeDay != null && !inOutage) {
                continue;
            }

            // set time in out calendars
            setOutCalTime(outCalBegin, begins);
            setOutCalTime(outCalEnd, ends);

            final long outCalBeginTime = outCalBegin.getTime().getTime() / 1000 * 1000;
            final long outCalEndTime = (outCalEnd.getTime().getTime() / 1000 + 1) * 1000;

            if (curCalTime >= outCalBeginTime && curCalTime < outCalEndTime) return outCalEnd;
        }
        return null; // Couldn't find a time period that matches
    }

    /**
     * <p>isTimeInSchedule</p>
     *
     * @param time a {@link java.util.Date} object.
     * @param sched a {@link org.opennms.netmgt.config.poller.outages.common.BasicSchedule} object.
     * @return a boolean.
     */
    public static boolean isTimeInSchedule(final Date time, final BasicSchedule sched) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(time);
        return isTimeInSchedule(cal, sched);
    }

    /**
     * <p>isDaily</p>
     *
     * @param time a {@link org.opennms.netmgt.config.poller.outages.common.Time} object.
     * @return a boolean.
     */
    public static boolean isDaily(final Time time) {
    	return !time.getDay().isPresent() && !isSpecific(time);
    }
    
    /**
     * <p>isWeekly</p>
     *
     * @param time a {@link org.opennms.netmgt.config.poller.outages.common.Time} object.
     * @return a boolean.
     */
    public static boolean isWeekly(final Time time) {
        return getDayOfWeekIndex(time.getDay().orElse(null)) != null;
    }
    
    /**
     * <p>isMonthly</p>
     *
     * @param time a {@link org.opennms.netmgt.config.poller.outages.common.Time} object.
     * @return a boolean.
     */
    public static boolean isMonthly(final Time time) {
        return time.getDay().isPresent() && getDayOfWeekIndex(time.getDay().get()) == null; 
    }
    
    /**
     * <p>isSpecific</p>
     *
     * @param time a {@link org.opennms.netmgt.config.poller.outages.common.Time} object.
     * @return a boolean.
     */
    public static boolean isSpecific(final Time time) {
        if (!time.getDay().isPresent()) {
            return SPECIFIC_DATE_PATTERN.matcher(time.getBegins()).matches();
        }
        return false;
    }

    /**
     * <p>getSpecificTime</p>
     *
     * @param specificString a {@link java.lang.String} object.
     * @return a {@link java.util.Date} object.
     */
    public static Date getSpecificTime(final String specificString) {
        final Calendar cal = Calendar.getInstance();
        setOutCalTime(cal, specificString);
        return cal.getTime();
    }

    /**
     * <p>getMonthlyTime</p>
     *
     * @param referenceTime a {@link java.util.Date} object.
     * @param day a {@link java.lang.String} object.
     * @param timeString a {@link java.lang.String} object.
     * @return a {@link java.util.Date} object.
     */
    public static Date getMonthlyTime(final Date referenceTime, final String day, final String timeString) {
        final Calendar ref = Calendar.getInstance();
        ref.setTime(referenceTime);
        ref.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day));
        setOutCalTime(ref, timeString);
        return ref.getTime();
    }
    
    /**
     * <p>getWeeklyTime</p>
     *
     * @param referenceTime a {@link java.util.Date} object.
     * @param day a {@link java.lang.String} object.
     * @param timeString a {@link java.lang.String} object.
     * @return a {@link java.util.Date} object.
     */
    public static Date getWeeklyTime(final Date referenceTime, final String day, final String timeString) {
        final Calendar ref = Calendar.getInstance();
        ref.setTime(referenceTime);
        ref.set(Calendar.DAY_OF_WEEK, getDayOfWeekIndex(day).intValue());
        setOutCalTime(ref, timeString);
        return ref.getTime();
    }
    
    /**
     * <p>getDailyTime</p>
     *
     * @param referenceTime a {@link java.util.Date} object.
     * @param timeString a {@link java.lang.String} object.
     * @return a {@link java.util.Date} object.
     */
    public static Date getDailyTime(final Date referenceTime, final String timeString) {
        final Calendar ref = Calendar.getInstance();
    	ref.setTime(referenceTime);
    	setOutCalTime(ref, timeString);
    	return ref.getTime();
    }
    
    /**
     * <p>getInterval</p>
     *
     * @param ref a {@link java.util.Date} object.
     * @param time a {@link org.opennms.netmgt.config.poller.outages.common.Time} object.
     * @param owner a {@link org.opennms.core.utils.Owner} object.
     * @return a {@link org.opennms.core.utils.OwnedInterval} object.
     */
    public static OwnedInterval getInterval(final Date ref, final Time time, final Owner owner) {
        final String begins = time.getBegins();
        final String ends = time.getEnds();
        final String day = time.getDay().orElse(null);

        if (isWeekly(time)) {
            return new OwnedInterval(owner, getWeeklyTime(ref, day, begins), getWeeklyTime(ref, day, ends));
        } else if (isMonthly(time)) {
            return new OwnedInterval(owner, getMonthlyTime(ref, day, begins), getMonthlyTime(ref, day, ends));
        } else if (isDaily(time)) {
            return new OwnedInterval(owner, getDailyTime(ref, begins), getDailyTime(ref, ends));
        } else {
            return new OwnedInterval(owner, getSpecificTime(begins), getSpecificTime(ends));
        }
    }
    
    /**
     * <p>nextDay</p>
     *
     * @param date a {@link java.util.Date} object.
     * @return a {@link java.util.Date} object.
     */
    public static Date nextDay(final Date date) {
        final Calendar cal = Calendar.getInstance();
    	cal.setTime(date);
    	cal.add(Calendar.DAY_OF_MONTH, 1);
    	return cal.getTime();
    }
    
    /**
     * <p>nextWeek</p>
     *
     * @param date a {@link java.util.Date} object.
     * @return a {@link java.util.Date} object.
     */
    public static Date nextWeek(final Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_YEAR, 7);
        return cal.getTime();
    }
    
    /**
     * <p>nextMonth</p>
     *
     * @param date a {@link java.util.Date} object.
     * @return a {@link java.util.Date} object.
     */
    public static Date nextMonth(final Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, 1);
        return cal.getTime();
    }
    
    /**
     * <p>getIntervals</p>
     *
     * @param start a {@link java.util.Date} object.
     * @param end a {@link java.util.Date} object.
     * @param time a {@link org.opennms.netmgt.config.poller.outages.common.Time} object.
     * @param owner a {@link org.opennms.core.utils.Owner} object.
     * @return a {@link org.opennms.core.utils.OwnedIntervalSequence} object.
     */
    public static OwnedIntervalSequence getIntervals(final Date start, final Date end, final Time time, final Owner owner) {
        final OwnedIntervalSequence seq = new OwnedIntervalSequence();
        
        // return an empty list for entries that have a zero length interval specified
        if (time.getBegins().equals(time.getEnds())) return seq;
        
        if (isWeekly(time)) {
            final Date done = nextWeek(end);
            for(Date ref = start; done.after(ref); ref = nextWeek(ref)) {
                seq.addInterval(getInterval(ref, time, owner));
            }
        } else if (isMonthly(time)) {
            final Date done = nextMonth(end);
            for(Date ref = start; done.after(ref); ref = nextMonth(ref)) {
                seq.addInterval(getInterval(ref, time, owner));
            }
        } else if (isDaily(time)) {
            final Date done = nextDay(end);
        	for(Date ref = start; done.after(ref); ref = nextDay(ref)) {
        		seq.addInterval(getInterval(ref, time, owner));
        	}
        } else {
            seq.addInterval(getInterval(start, time, owner));
        }
        seq.bound(start, end);
        return seq;
    }
    
    /**
     * <p>getIntervals</p>
     *
     * @param interval a {@link org.opennms.core.utils.TimeInterval} object.
     * @param time a {@link org.opennms.netmgt.config.poller.outages.common.Time} object.
     * @param owner a {@link org.opennms.core.utils.Owner} object.
     * @return a {@link org.opennms.core.utils.OwnedIntervalSequence} object.
     */
    public static OwnedIntervalSequence getIntervals(final TimeInterval interval, final Time time, final Owner owner) {
        return getIntervals(interval.getStart(), interval.getEnd(), time, owner);
    }
    
    /**
     * <p>getIntervalsCovering</p>
     *
     * @param start a {@link java.util.Date} object.
     * @param end a {@link java.util.Date} object.
     * @param sched a {@link org.opennms.netmgt.config.poller.outages.common.BasicSchedule} object.
     * @param owner a {@link org.opennms.core.utils.Owner} object.
     * @return a {@link org.opennms.core.utils.OwnedIntervalSequence} object.
     */
    public static OwnedIntervalSequence getIntervalsCovering(final Date start, final Date end, final BasicSchedule sched, final Owner owner) {
        final OwnedIntervalSequence seq = new OwnedIntervalSequence();
        for (int i = 0; i < sched.getTimeCount(); i++) {
            final Time time = sched.getTime(i);
            final Owner thisOwner = owner.addTimeIndex(i);
            seq.addAll(getIntervals(start, end, time, thisOwner));
        }
        return seq;
    }
    
    /**
     * <p>getIntervalsCovering</p>
     *
     * @param interval a {@link org.opennms.core.utils.TimeInterval} object.
     * @param sched a {@link org.opennms.netmgt.config.poller.outages.common.BasicSchedule} object.
     * @param owner a {@link org.opennms.core.utils.Owner} object.
     * @return a {@link org.opennms.core.utils.OwnedIntervalSequence} object.
     */
    public static OwnedIntervalSequence getIntervalsCovering(final TimeInterval interval, final BasicSchedule sched, final Owner owner) {
        return getIntervalsCovering(interval.getStart(), interval.getEnd(), sched, owner);
    }

	static BasicSchedule getBasicOutageSchedule(final Outage out) {
		if (out == null) return null;
		final BasicSchedule schedule = new BasicSchedule();
		schedule.setName(out.getName());
		schedule.setType(out.getType());
		final Collection<Time> times = new ArrayList<Time>();
		for (final org.opennms.netmgt.config.poller.outages.Time time : out.getTimeCollection()) {
			times.add(new Time(time.getId(), time.getDay(), time.getBegins(), time.getEnds()));
		}
		schedule.setTimeCollection(times);
		
		return schedule;
	}

	public static BasicSchedule getGroupSchedule(final Schedule schedule) {
		if (schedule == null) return null;
		
		final BasicSchedule basicSchedule = new BasicSchedule();
		basicSchedule.setName(schedule.getName());
		basicSchedule.setType(schedule.getType());
		final Collection<Time> times = new ArrayList<Time>();
		for (final org.opennms.netmgt.config.groups.Time time : schedule.getTimes()) {
			times.add(new Time(time.getId().orElse(null), time.getDay().orElse(null), time.getBegins(), time.getEnds()));
		}
		basicSchedule.setTimeCollection(times);
		
		return basicSchedule;
	}

	public static BasicSchedule getRancidSchedule(org.opennms.netmgt.config.rancid.adapter.Schedule schedule) {
		if (schedule == null) return null;
		
		final BasicSchedule basicSchedule = new BasicSchedule();
		basicSchedule.setName(schedule.getName());
		basicSchedule.setType(schedule.getType());
		final Collection<Time> times = new ArrayList<Time>();
		for (final org.opennms.netmgt.config.rancid.adapter.Time time : schedule.getTimeCollection()) {
			times.add(new Time(time.getId(), time.getDay(), time.getBegins(), time.getEnds()));
		}
		basicSchedule.setTimeCollection(times);
		
		return basicSchedule;
	}

}
