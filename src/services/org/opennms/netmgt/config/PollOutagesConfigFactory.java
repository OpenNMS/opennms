//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 Blast Internet Services, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of Blast Internet Services, Inc.
//
// Modifications:
//
// 2003 Jan 31: Cleaned up some unused imports.
// 2002 Oct 24: Changed all references to HastTable to HashMap.
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.blast.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.config;

import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

// castor classes generated from the poll-outages.xsd
import org.opennms.netmgt.config.poller.*;

import org.opennms.netmgt.ConfigFileConstants;

/**
 * <p>This is the singleton class used to load the configuration for
 * the poller outages from the poll-outages.xml.</p>
 *
 * <p><strong>Note:</strong>Users of this class should make sure the 
 * <em>init()</em> is called before calling any other method to ensure
 * the config is loaded before accessing other convenience methods</p>
 *
 * @author <a href="mailto:sowmya@opennms.org">Sowmya Nataraj</a>
 * @author <a href="http://www.opennms.org/">OpenNMS</a>
 */
public final class PollOutagesConfigFactory
{
	/**
	 * The singleton instance of this factory
	 */
	private static PollOutagesConfigFactory		m_singleton=null;

	/**
	 * The config class loaded from the config file
	 */
	private Outages					m_config;

	/**
	 * This member is set to true if the configuration file
	 * has been loaded.
	 */
	private static boolean				m_loaded=false;

	/**
	 * The day of the week values to name mapping
	 */
	private static Map 				m_dayOfWeekMap;

	private static String 				FORMAT1 = "dd-MMM-yyyy HH:mm:ss";
	private static String 				FORMAT2 = "HH:mm:ss";

	/**
	 * Create the day of week mapping
	 */
	private static void createDayOfWeekMapping()
	{
		m_dayOfWeekMap = new HashMap();
		m_dayOfWeekMap.put("sunday", new Integer(Calendar.SUNDAY));
		m_dayOfWeekMap.put("monday", new Integer(Calendar.MONDAY));
		m_dayOfWeekMap.put("tuesday", new Integer(Calendar.TUESDAY));
		m_dayOfWeekMap.put("wednesday", new Integer(Calendar.WEDNESDAY));
		m_dayOfWeekMap.put("thursday", new Integer(Calendar.THURSDAY));
		m_dayOfWeekMap.put("friday", new Integer(Calendar.FRIDAY));
		m_dayOfWeekMap.put("saturday", new Integer(Calendar.SATURDAY));
	}

	/**
	 *  Set the time in outCal from timeStr. 'timeStr'is in either the
	 * 'dd-MMM-yyyy HH:mm:ss' or the 'HH:mm:ss' formats
	 *
	 * @param outCal	the calendar in which time is to be set
	 * @param timeStr	the time string 
	 */
	private void setOutCalTime(Calendar outCal, String timeStr)
	{
		if (timeStr.length() == FORMAT1.length())
		{
			SimpleDateFormat format = new SimpleDateFormat(FORMAT1);

			// parse the date string passed
			Date tempDate = null;
			try
			{
				tempDate = format.parse(timeStr);
			}
			catch (ParseException pE)
			{
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
		}
		else if (timeStr.length() == FORMAT2.length())
		{
			SimpleDateFormat format = new SimpleDateFormat(FORMAT2);

			// parse the date string passed
			Date tempDate = null;
			try
			{
				tempDate = format.parse(timeStr);
			}
			catch (ParseException pE)
			{
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
	 * Private constructor
	 *
	 * @exception java.io.IOException Thrown if the specified config
	 * 	file cannot be read
	 * @exception org.exolab.castor.xml.MarshalException Thrown if the 
	 * 	file does not conform to the schema.
	 * @exception org.exolab.castor.xml.ValidationException Thrown if 
	 *	the contents do not match the required schema.
	 */
	private PollOutagesConfigFactory(String configFile)
		throws 	IOException,
			MarshalException, 
			ValidationException
	{
		InputStream cfgIn = new FileInputStream(configFile);

		m_config = (Outages) Unmarshaller.unmarshal(Outages.class, new InputStreamReader(cfgIn));
		cfgIn.close();

	}

	/**
	 * Load the config from the default config file and create the 
	 * singleton instance of this factory.
	 *
	 * @exception java.io.IOException Thrown if the specified config
	 * 	file cannot be read
	 * @exception org.exolab.castor.xml.MarshalException Thrown if the 
	 * 	file does not conform to the schema.
	 * @exception org.exolab.castor.xml.ValidationException Thrown if 
	 *	the contents do not match the required schema.
	 */
	public static synchronized void init()
		throws 	IOException,
			MarshalException, 
			ValidationException
	{
		if (m_loaded)
		{
			// init already called - return
			// to reload, reload() will need to be called
			return;
		}

		File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.POLL_OUTAGES_CONFIG_FILE_NAME);

		m_singleton = new PollOutagesConfigFactory(cfgFile.getPath());

		// create day of week mapping
		createDayOfWeekMapping();

		m_loaded = true;
	}

	/**
	 * Reload the config from the default config file
	 *
	 * @exception java.io.IOException Thrown if the specified config
	 * 	file cannot be read/loaded
	 * @exception org.exolab.castor.xml.MarshalException Thrown if the 
	 * 	file does not conform to the schema.
	 * @exception org.exolab.castor.xml.ValidationException Thrown if 
	 *	the contents do not match the required schema.
	 */
	public static synchronized void reload()
		throws 	IOException,
			MarshalException, 
			ValidationException
	{
		m_singleton = null;
		m_loaded    = false;

		init();
	}

	/**
	 * <p>Return the singleton instance of this factory</p>
	 *
	 * @return The current factory instance.
	 *
	 * @throws java.lang.IllegalStateException Thrown if the factory
	 * 	has not yet been initialized.
	 */
	public static synchronized PollOutagesConfigFactory getInstance()
	{
		if(!m_loaded)
			throw new IllegalStateException("The factory has not been initialized");

		return m_singleton;
	}

	/**
	 * <p>Return the outages configured</p>
	 *
	 * @return the outages configured
	 */
	public synchronized Outage[] getOutages()
	{
		return m_config.getOutage();
	}

	/**
	 * <p>Return the specified outage</p>
	 *
	 * @param name	the outage that is to be looked up
	 *
	 * @return the specified outage, null if not found
	 */
	public synchronized Outage getOutage(String name)
	{
		Enumeration enum = m_config.enumerateOutage();
		while(enum.hasMoreElements())
		{
			Outage out = (Outage)enum.nextElement();
			if (out.getName().equals(name))
			{
				return out;
			}
		}
		
		return null;
	}

	/**
	 * <p>Return the type for specified outage</p>
	 *
	 * @param name	the outage that is to be looked up
	 *
	 * @return the type for the specified outage, null if not found
	 */
	public synchronized String getOutageType(String name)
	{
		Outage out = getOutage(name);
		if (out == null)
			return null;
		else
			return out.getType();
	}

	/**
	 * <p>Return the outage times for specified outage</p>
	 *
	 * @param name	the outage that is to be looked up
	 *
	 * @return the  outage times for the specified outage, null if not found
	 */
	public synchronized Time[] getOutageTimes(String name)
	{
		Outage out = getOutage(name);
		if (out == null)
			return null;
		else
			return out.getTime();
	}

	/**
	 * <p>Return the interfaces for specified outage</p>
	 *
	 * @param name	the outage that is to be looked up
	 *
	 * @return the interfaces for the specified outage, null if not found
	 */
	public synchronized Interface[] getInterfaces(String name)
	{
		Outage out = getOutage(name);
		if (out == null)
			return null;
		else
			return out.getInterface();
	}

	/**
	 * <p>Return if interfaces is part of specified outage</p>
	 *
	 * @param linterface	the interface to be looked up
	 * @param outName	the outage name
	 *
	 * @return the interface is part of the specified outage
	 */
	public synchronized boolean isInterfaceInOutage(String linterface, String outName)
	{
		Outage out = getOutage(outName);
		if (out == null)
			return false;

		return isInterfaceInOutage(linterface, out);
	}

	/**
	 * <p>Return if interfaces is part of specified outage</p>
	 *
	 * @param linterface	the interface to be looked up
	 * @param outName	the outage 
	 *
	 * @return the interface is part of the specified outage
	 */
	public synchronized boolean isInterfaceInOutage(String linterface, Outage out)
	{
		if (out == null)
			return false;

		Enumeration enum = out.enumerateInterface();
		while(enum.hasMoreElements())
		{
			Interface ointerface = (Interface)enum.nextElement();
			if (ointerface.getAddress().equals(linterface))
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * <p>Return if time is part of specified outage</p>
	 *
	 * @param cal		the calendar to lookup
	 * @param outName	the outage name
	 *
	 * @return true if time is in outage
	 */
	public synchronized boolean isTimeInOutage(Calendar cal, String outName)
	{
		Outage out = getOutage(outName);
		if (out == null)
			return false;

		return isTimeInOutage(cal, out);
	}

	/**
	 * <p>Return if time is part of specified outage</p>
	 *
	 * @param cal		the calendar to lookup
	 * @param out		the outage
	 *
	 * @return true if time is in outage
	 */
	public synchronized boolean isTimeInOutage(Calendar cal, Outage out)
	{
		Category log = ThreadCategory.getInstance(getClass());
		
		if (log.isDebugEnabled())
			log.debug("isTimeInOutage: checking for time '" + cal.getTime() + "' in outage '" + out.getName() + "'");
		
		if (out == null)
			return false;

		long curCalTime = cal.getTime().getTime();

		// check if day is part of outage
		boolean inOutage = false;

		// flag indicating that day(which is optional) was not specified in the time

		Enumeration enum = out.enumerateTime();
		while(enum.hasMoreElements() && !inOutage)
		{
			Calendar outCalBegin = new GregorianCalendar();
			Calendar outCalEnd = new GregorianCalendar();

			Time oTime = (Time)enum.nextElement();

			String oTimeDay = oTime.getDay();
			String begins = oTime.getBegins();
			String ends = oTime.getEnds();

			if (oTimeDay != null)
			{
				// see if outage time was specified as sunday/monday..
				Integer dayInMap = (Integer)m_dayOfWeekMap.get(oTimeDay);
				if (dayInMap != null)
				{
					// check if value specified matches current date
					if (cal.get(Calendar.DAY_OF_WEEK) == dayInMap.intValue())
						inOutage = true;

					outCalBegin.set(Calendar.DAY_OF_WEEK, dayInMap.intValue());
					outCalEnd.set(Calendar.DAY_OF_WEEK, dayInMap.intValue());
				}
				// else see if outage time was specified as day of month
				else
				{
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
			long outCalBeginTime = outCalBegin.getTime().getTime();
			long outCalEndTime = outCalEnd.getTime().getTime();
			
			if (curCalTime >= outCalBeginTime && curCalTime <= outCalEndTime)
				inOutage = true;
			else
				inOutage = false;
		}

		return inOutage;

	}

	/**
	 * <p>Return if current time is part of specified outage</p>
	 *
	 * @param outName	the outage name
	 *
	 * @return true if current time is in outage
	 */
	public synchronized boolean isCurTimeInOutage(String outName)
	{
		// get current time
		Calendar cal = new GregorianCalendar();

		return isTimeInOutage(cal, outName);
	}
	/**
	 * <p>Return if current time is part of specified outage</p>
	 *
	 * @param out	the outage
	 *
	 * @return true if current time is in outage
	 */
	public synchronized boolean isCurTimeInOutage(Outage out)
	{
		// get current time
		Calendar cal = new GregorianCalendar();

		return isTimeInOutage(cal, out);
	}
}
