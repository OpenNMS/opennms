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
import org.opennms.netmgt.config.poller.Interface;
import org.opennms.netmgt.config.poller.Outage;
import org.opennms.netmgt.config.poller.Outages;
import org.opennms.netmgt.config.poller.Time;

/**
 * Represents a PollOutagesConfigManager 
 *
 * @author brozow
 */
public class PollOutagesConfigManager implements PollOutagesConfig {

    /**
     * The config class loaded from the config file
     */
    private Outages m_config;
    /**
     * The day of the week values to name mapping
     */
    protected static Map m_dayOfWeekMap;
    public static String FORMAT1 = "dd-MMM-yyyy HH:mm:ss";
    public static String FORMAT2 = "HH:mm:ss";

    /**
     * @param config The config to set.
     */
    protected void setConfig(Outages config) {
        m_config = config;
    }

    /**
     * @return Returns the config.
     */
    protected Outages getConfig() {
        return m_config;
    }

    /**
     * Create the day of week mapping
     */
    protected static void createDayOfWeekMapping() {
        if (m_dayOfWeekMap == null) {
            m_dayOfWeekMap = new HashMap();
            m_dayOfWeekMap.put("sunday", new Integer(Calendar.SUNDAY));
            m_dayOfWeekMap.put("monday", new Integer(Calendar.MONDAY));
            m_dayOfWeekMap.put("tuesday", new Integer(Calendar.TUESDAY));
            m_dayOfWeekMap.put("wednesday", new Integer(Calendar.WEDNESDAY));
            m_dayOfWeekMap.put("thursday", new Integer(Calendar.THURSDAY));
            m_dayOfWeekMap.put("friday", new Integer(Calendar.FRIDAY));
            m_dayOfWeekMap.put("saturday", new Integer(Calendar.SATURDAY));
        }
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
    private void setOutCalTime(Calendar outCal, String timeStr) {
        if (timeStr.length() == FORMAT1.length()) {
            SimpleDateFormat format = new SimpleDateFormat(FORMAT1);
    
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
        } else if (timeStr.length() == FORMAT2.length()) {
            SimpleDateFormat format = new SimpleDateFormat(FORMAT2);
    
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

    /**
     * Return the outages configured.
     * 
     * @return the outages configured
     */
    public synchronized Outage[] getOutages() {
        return getConfig().getOutage();
    }

    /**
     * Return the specified outage.
     * 
     * @param name
     *            the outage that is to be looked up
     * 
     * @return the specified outage, null if not found
     */
    public synchronized Outage getOutage(String name) {
        Enumeration e = getConfig().enumerateOutage();
        while (e.hasMoreElements()) {
            Outage out = (Outage) e.nextElement();
            if (out.getName().equals(name)) {
                return out;
            }
        }
    
        return null;
    }

    /**
     * Return the type for specified outage.
     * 
     * @param name
     *            the outage that is to be looked up
     * 
     * @return the type for the specified outage, null if not found
     */
    public synchronized String getOutageType(String name) {
        Outage out = getOutage(name);
        if (out == null)
            return null;
        else
            return out.getType();
    }

    /**
     * Return the outage times for specified outage.
     * 
     * @param name
     *            the outage that is to be looked up
     * 
     * @return the outage times for the specified outage, null if not found
     */
    public synchronized Time[] getOutageTimes(String name) {
        Outage out = getOutage(name);
        if (out == null)
            return null;
        else
            return out.getTime();
    }

    /**
     * Return the interfaces for specified outage.
     * 
     * @param name
     *            the outage that is to be looked up
     * 
     * @return the interfaces for the specified outage, null if not found
     */
    public synchronized Interface[] getInterfaces(String name) {
        Outage out = getOutage(name);
        if (out == null)
            return null;
        else
            return out.getInterface();
    }

    /**
     * Return if interfaces is part of specified outage.
     * 
     * @param linterface
     *            the interface to be looked up
     * @param outName
     *            the outage name
     * 
     * @return the interface is part of the specified outage
     */
    public synchronized boolean isInterfaceInOutage(String linterface, String outName) {
        Outage out = getOutage(outName);
        if (out == null)
            return false;
    
        return isInterfaceInOutage(linterface, out);
    }

    /**
     * Return if interfaces is part of specified outage.
     * 
     * @param linterface
     *            the interface to be looked up
     * @param out
     *            the outage
     * 
     * @return the interface is part of the specified outage
     */
    public synchronized boolean isInterfaceInOutage(String linterface, Outage out) {
        if (out == null)
            return false;
    
        Enumeration e = out.enumerateInterface();
        while (e.hasMoreElements()) {
            Interface ointerface = (Interface) e.nextElement();
            if (ointerface.getAddress().equals(linterface)) {
                return true;
            }
        }
    
        return false;
    }

    /**
     * Return if time is part of specified outage.
     * 
     * @param cal
     *            the calendar to lookup
     * @param outName
     *            the outage name
     * 
     * @return true if time is in outage
     */
    public synchronized boolean isTimeInOutage(Calendar cal, String outName) {
        Outage out = getOutage(outName);
        if (out == null)
            return false;
    
        return isTimeInOutage(cal, out);
    }

    /**
     * Return if time is part of specified outage.
     * 
     * @param time
     *            the time in millis to look up
     * @param outName
     *            the outage name
     * 
     * @return true if time is in outage
     */
    public synchronized boolean isTimeInOutage(long time, String outName) {
        Outage out = getOutage(outName);
        if (out == null)
            return false;
    
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        return isTimeInOutage(cal, out);
    }

    /**
     * Return if time is part of specified outage.
     * 
     * @param cal
     *            the calendar to lookup
     * @param out
     *            the outage
     * 
     * @return true if time is in outage
     */
    public synchronized boolean isTimeInOutage(Calendar cal, Outage out) {
        Category log = ThreadCategory.getInstance(getClass());
    
        if (log.isDebugEnabled())
            log.debug("isTimeInOutage: checking for time '" + cal.getTime() + "' in outage '" + out.getName() + "'");
    
        if (out == null)
            return false;
    
        long curCalTime = cal.getTime().getTime();
    
        // check if day is part of outage
        boolean inOutage = false;
    
        // flag indicating that day(which is optional) was not specified in the
        // time
    
        Enumeration e = out.enumerateTime();
        while (e.hasMoreElements() && !inOutage) {
            Calendar outCalBegin = new GregorianCalendar();
            Calendar outCalEnd = new GregorianCalendar();
    
            Time oTime = (Time) e.nextElement();
    
            String oTimeDay = oTime.getDay();
            String begins = oTime.getBegins();
            String ends = oTime.getEnds();
    
            if (oTimeDay != null) {
                // see if outage time was specified as sunday/monday..
                Integer dayInMap = (Integer) m_dayOfWeekMap.get(oTimeDay);
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
            
            // round these to the surrounding seconds since we can only specify this to seconds
            // accuracy in the config file
            long outCalBeginTime = outCalBegin.getTime().getTime() / 1000 * 1000;
            long outCalEndTime = (outCalEnd.getTime().getTime()/1000 + 1) * 1000;
    
            if (curCalTime >= outCalBeginTime && curCalTime < outCalEndTime)
                inOutage = true;
            else
                inOutage = false;

        }
    
        return inOutage;
    
    }

    /**
     * Return if current time is part of specified outage.
     * 
     * @param outName
     *            the outage name
     * 
     * @return true if current time is in outage
     */
    public synchronized boolean isCurTimeInOutage(String outName) {
        // get current time
        Calendar cal = new GregorianCalendar();
    
        return isTimeInOutage(cal, outName);
    }

    /**
     * Return if current time is part of specified outage.
     * 
     * @param out
     *            the outage
     * 
     * @return true if current time is in outage
     */
    public synchronized boolean isCurTimeInOutage(Outage out) {
        // get current time
        Calendar cal = new GregorianCalendar();
    
        return isTimeInOutage(cal, out);
    }

}
