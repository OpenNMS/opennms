//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.config;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.common.BasicSchedule;
import org.opennms.netmgt.config.common.Time;

public class BasicScheduleUtils {

    /**
     * The day of the week values to name mapping
     */
    protected static Map m_dayOfWeekMap;
    public static String FORMAT1 = "dd-MMM-yyyy HH:mm:ss";
    public static String FORMAT2 = "HH:mm:ss";

    public static boolean isTimeInSchedule(Calendar cal, BasicSchedule sched) {
        Category log = ThreadCategory.getInstance(BasicScheduleUtils.class);
        
        if (log.isDebugEnabled())
            log.debug("isTimeInOutage: checking for time '" + cal.getTime() + "' in schedule '" + sched.getName() + "'");
        if (sched == null)
            return false;
        long curCalTime = cal.getTime().getTime();
        // check if day is part of outage
        boolean inOutage = false;
        Enumeration e = sched.enumerateTime();
        while (e.hasMoreElements() && !inOutage) {
            Calendar outCalBegin = new GregorianCalendar();
            Calendar outCalEnd = new GregorianCalendar();
            
            outCalBegin.setTimeInMillis(curCalTime);
            outCalEnd.setTimeInMillis(curCalTime);
    
            Time oTime = (Time) e.nextElement();
    
            String oTimeDay = oTime.getDay();
            String begins = oTime.getBegins();
            String ends = oTime.getEnds();
    
            if (oTimeDay != null) {
                // see if outage time was specified as sunday/monday..
                Integer dayInMap = getDayOfWeekIndex(oTimeDay);
                if (dayInMap != null) {
                    // check if value specified matches current date
                    if (cal.get(Calendar.DAY_OF_WEEK) == dayInMap.intValue())
                        inOutage = true;
    
                    outCalBegin.set(Calendar.DAY_OF_WEEK, dayInMap.intValue());
                    outCalEnd.set(Calendar.DAY_OF_WEEK, dayInMap.intValue());
                }
                // else see if outage time was specified as day of month
                else {
                    int intOTimeDay = (new Integer(oTimeDay)).intValue();
    
                    if (cal.get(Calendar.DAY_OF_MONTH) == intOTimeDay)
                        inOutage = true;
    
                    outCalBegin.set(Calendar.DAY_OF_MONTH, intOTimeDay);
                    outCalEnd.set(Calendar.DAY_OF_MONTH, intOTimeDay);
                }
            }
    
            // if time of day was specified and did not match, continue
            if (oTimeDay != null && !inOutage)
                continue;
    
            // set time in out calendars
            setOutCalTime(outCalBegin, begins);
            setOutCalTime(outCalEnd, ends);
    
            // check if calendar passed is in the out cal range
            if (log.isDebugEnabled())
                log.debug("isTimeInOutage: checking begin/end time...\n current: " + cal.getTime() + "\n begin: " + outCalBegin.getTime() + "\n end: " + outCalEnd.getTime());
    
            // round these to the surrounding seconds since we can only specify
            // this to seconds
            // accuracy in the config file
            long outCalBeginTime = outCalBegin.getTime().getTime() / 1000 * 1000;
            long outCalEndTime = (outCalEnd.getTime().getTime() / 1000 + 1) * 1000;
    
            if (curCalTime >= outCalBeginTime && curCalTime < outCalEndTime)
                inOutage = true;
            else
                inOutage = false;
    
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
    public static void setOutCalTime(Calendar outCal, String timeStr) {
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
        }
    }
    
    public static Integer getDayOfWeekIndex(String dayName) {
        createDayOfWeekMapping();
        return (Integer)m_dayOfWeekMap.get(dayName);
    }
    
    

    public static Calendar getEndOfSchedule(BasicSchedule out) {
        long curCalTime = System.currentTimeMillis();
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(curCalTime);
        // check if day is part of outage
        boolean inOutage = false;
        Enumeration en = out.enumerateTime();
        while (en.hasMoreElements() && !inOutage) {
            Calendar outCalBegin = new GregorianCalendar();
            Calendar outCalEnd = new GregorianCalendar();
    
            Time oTime = (Time) en.nextElement();
    
            String oTimeDay = oTime.getDay();
            String begins = oTime.getBegins();
            String ends = oTime.getEnds();
    
            if (oTimeDay != null) {
                // see if outage time was specified as sunday/monday..
                Integer dayInMap = getDayOfWeekIndex(oTimeDay);
                if (dayInMap != null) {
                    // check if value specified matches current date
                    if (cal.get(Calendar.DAY_OF_WEEK) == dayInMap.intValue())
                        inOutage = true;
    
                    outCalBegin.set(Calendar.DAY_OF_WEEK, dayInMap.intValue());
                    outCalEnd.set(Calendar.DAY_OF_WEEK, dayInMap.intValue());
                } // else see if outage time was specified as day of month
                else {
                    int intOTimeDay = (new Integer(oTimeDay)).intValue();
    
                    if (cal.get(Calendar.DAY_OF_MONTH) == intOTimeDay)
                        inOutage = true;
    
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
    
            long outCalBeginTime = outCalBegin.getTime().getTime() / 1000 * 1000;
            long outCalEndTime = (outCalEnd.getTime().getTime() / 1000 + 1) * 1000;
    
            if (curCalTime >= outCalBeginTime && curCalTime < outCalEndTime)
                return outCalEnd;
        }
        return null; // Couldn't find a time period that matches
    }

    /**
     * Create the day of week mapping
     */
    private static void createDayOfWeekMapping() {
        if (BasicScheduleUtils.m_dayOfWeekMap == null) {
            BasicScheduleUtils.m_dayOfWeekMap = new HashMap();
            BasicScheduleUtils.m_dayOfWeekMap.put("sunday", new Integer(Calendar.SUNDAY));
            BasicScheduleUtils.m_dayOfWeekMap.put("monday", new Integer(Calendar.MONDAY));
            BasicScheduleUtils.m_dayOfWeekMap.put("tuesday", new Integer(Calendar.TUESDAY));
            BasicScheduleUtils.m_dayOfWeekMap.put("wednesday", new Integer(Calendar.WEDNESDAY));
            BasicScheduleUtils.m_dayOfWeekMap.put("thursday", new Integer(Calendar.THURSDAY));
            BasicScheduleUtils.m_dayOfWeekMap.put("friday", new Integer(Calendar.FRIDAY));
            BasicScheduleUtils.m_dayOfWeekMap.put("saturday", new Integer(Calendar.SATURDAY));
        }
    }

    public static boolean isTimeInSchedule(Date time, BasicSchedule sched) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(time);
        return isTimeInSchedule(cal, sched);
    }

}
