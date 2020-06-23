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

package org.opennms.reporting.availability;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * <p>CalendarTableBuilder class.</p>
 *
 * @author jsartin
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CalendarTableBuilder {
		
		public Day[] m_days;
		public CalendarTable m_calTable;
		private int m_firstDay;
		Calendar m_workingCalendar;
			
		Locale m_locale;
		
		/*
		 * Construct a calendar section for epoch time
		 * 
		 * @param endTime
		 * 		epoch time for the calendar month
		 * 
		 */

		/**
		 * <p>Constructor for CalendarTableBuilder.</p>
		 *
		 * @param endTime a long.
		 */
		public CalendarTableBuilder(long endTime) {
			
			m_locale = Locale.getDefault();
			m_workingCalendar = Calendar.getInstance(m_locale);
			m_workingCalendar.setTimeInMillis(endTime);
			int month = m_workingCalendar.get(Calendar.MONTH);
			calendarTableInit(month);
		}
		
		/*
		 * Construct a calendar section for year and month
		 * 
		 * @param year
		 * 		year for calendar
		 * @param month
		 * 		month for calendar (Jaunary = 0)
		 */
		
		/**
		 * <p>Constructor for CalendarTableBuilder.</p>
		 *
		 * @param year a int.
		 * @param month a int.
		 */
		public CalendarTableBuilder(int year, int month) {
		
			m_locale = Locale.getDefault();
			m_workingCalendar = Calendar.getInstance(m_locale);
			m_workingCalendar.set(Calendar.MONTH, month);
			m_workingCalendar.set(Calendar.YEAR,year);
			calendarTableInit(month);
			
		}
		
		private void calendarTableInit(int month) {
			
			m_calTable = new CalendarTable();
						
		   	m_days = new Day[42];
			
			int dayInLastMonth; 
			int dayInThisMonth;
			int dayInNextMonth;
			int firstDayOfWeek;
					   	
		   	String[] monthNames = new DateFormatSymbols(m_locale).getMonths();
		   	m_calTable.setMonth(monthNames[month]);
		   	
			String[] dayNames = new DateFormatSymbols(m_locale).getShortWeekdays();
		   	DaysOfWeek titleDays = new DaysOfWeek();
			
		    int dayOfWeek;
		
		    /* SetUp Title days for calendar */
					    
		    firstDayOfWeek = m_workingCalendar.getFirstDayOfWeek();
			for (int i = 0; i < 7; i++) {
				dayOfWeek = (firstDayOfWeek + i) < 8 ? (firstDayOfWeek + i) : 1;
				titleDays.addDayName(dayNames[dayOfWeek]);
				}
			m_calTable.setDaysOfWeek(titleDays);
		    			
		    m_workingCalendar.set(Calendar.DAY_OF_MONTH, 1);
		                                                                                                                             
		    m_firstDay = m_workingCalendar.get(Calendar.DAY_OF_WEEK) - firstDayOfWeek;
		    
		    /**
		     * if first day of the week is before the first day in the month, then
			 * there will be some invisible days
		     */
		
			if (m_firstDay < 0) {
		        m_firstDay += 7;
		    }
			
			
		    for (dayInLastMonth = 0; dayInLastMonth < m_firstDay; dayInLastMonth++) {
		    	m_days[dayInLastMonth] = new Day();
		    	m_days[dayInLastMonth].setVisible(false);
				m_days[dayInLastMonth].setPctValue(0.0);
			}
			
	        /**
	         * get the first day in the next month
	         */ 
			
		    m_workingCalendar.add(Calendar.MONTH, 1);
	        Date firstDayInNextMonth = m_workingCalendar.getTime();
	        m_workingCalendar.add(Calendar.MONTH, -1);

	        Date day = m_workingCalendar.getTime();
	        dayInThisMonth = dayInLastMonth;
			
			int date = 1;
	        while (day.before(firstDayInNextMonth)) {
	            m_days[dayInThisMonth] = new Day();
	        	m_days[dayInThisMonth].setDate(date);
	        	m_days[dayInThisMonth].setVisible(true);
				m_days[dayInThisMonth].setPctValue(0.0);
				dayInThisMonth++;
				date++;
	            m_workingCalendar.add(Calendar.DATE, 1);
	            day = m_workingCalendar.getTime();
	        }
	        
	        /**
	         * And set the remainder invisible too....
	         */
	                   
			// TODO: Is the number 42 correct?
	        for (dayInNextMonth = dayInThisMonth; dayInNextMonth < 42; dayInNextMonth++) {
	        	m_days[dayInNextMonth] =  new Day();
	            m_days[dayInNextMonth].setVisible(false);
				m_days[dayInNextMonth].setPctValue(0.0);
			}
	        
	        
		}
	        
	    /**
	     * <p>print</p>
	     */
	    public void print () {
	    	
	    	for (int y = 0; y < 6; y++) {
	    		for (int x = 0; x < 7; x++) {
	    			int index = x + (7 * y);
	    			System.out.println("index: " + index + "visible: " +
							m_days[index].getVisible() + "date: " + 
							m_days[index].getDate() + " value " + 
							m_days[index].getPctValue());
	            }
	    	}
	  
		}
		
		/*
		 * Return completed calendar section
		 */
	    
	    /**
	     * <p>getTable</p>
	     *
	     * @return a {@link org.opennms.reporting.availability.CalendarTable} object.
	     */
	    public CalendarTable getTable() {
	    	
			/* Build CalendarSection here */
			
	    	Week week = null;
	    	
	    	for (int y = 0; y < 6; y++) {
	    		week = new Week();
	    		m_calTable.addWeek(y,week);
	    		for (int x = 0; x < 7; x++) {
	    			int index = x + (7 * y);
	    			week.addDay(x,m_days[index]);
	    		}
	    	}
			
			return m_calTable;
	    				    		    	
	    }
		
	   //TODO: Make this method aware of the last day in the month. Add illegal argument exception?
		
		/*
		 * Set value at given date
		 * 
		 * @param sDate
		 * 		date to set value
		 * @param value
		 * 		value (typically percent availability)
		 */
		
		/**
		 * <p>setPctValue</p>
		 *
		 * @param sDate a int.
		 * @param value a double.
		 */
		public void setPctValue (int sDate, double value) {
			m_days[sDate + m_firstDay -1].setPctValue(value);
		}
		


}
