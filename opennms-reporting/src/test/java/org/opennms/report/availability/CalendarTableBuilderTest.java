package org.opennms.report.availability;

/*
 * @author jsartin
 */

import java.util.Calendar;
import java.util.Locale;

import junit.framework.TestCase;

public class CalendarTableBuilderTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testLocaleFrench() {
		Locale.setDefault(Locale.FRENCH);
		CalendarTableBuilder builder = new CalendarTableBuilder(2005,05);
		CalendarTable section = builder.getTable();
		DaysOfWeek days = section.getDaysOfWeek();
		assertEquals(days.getDayName(0),"lun.");
		}
	public void testLocaleEnUs() {
		Locale.setDefault(Locale.US);
		CalendarTableBuilder builder = new CalendarTableBuilder(2005,05);
		CalendarTable section = builder.getTable();
		DaysOfWeek days = section.getDaysOfWeek();
		assertEquals(days.getDayName(0),"Sun");
		}
	public void testVisibleDays() {
		testVisibleDays(Locale.US, 2004, Calendar.FEBRUARY, 1);
	}

	private void testVisibleDays(Locale locale, int year, int month, int dayOfMonth) {
		Locale.setDefault(locale);
		CalendarTableBuilder builder = new CalendarTableBuilder(year,month);
		for (int i= 0; i < builder.m_days.length; i++) {
			System.out.println("day " + i + " " + builder.m_days[i].getVisible());
			
		}
		int firstWeekDay = getDayOfWeek(locale, year, month, dayOfMonth);
		int daysInMonth = getDaysInMonth(locale, year, month);
		int numWeeks = (firstWeekDay + daysInMonth + 6) / 7 ;
		int displayDays = numWeeks * 7;

	}
	
	public void testGetDaysInMonth() {
		assertEquals(29, getDaysInMonth(Locale.US, 2004, Calendar.FEBRUARY));
		assertEquals(28, getDaysInMonth(Locale.US, 2005, Calendar.FEBRUARY));
	}
	
	
	private int getDaysInMonth(Locale locale, int year, int month) {
		Calendar cal = Calendar.getInstance(locale);
		cal.set(year, month, 1);
		return cal.getActualMaximum(Calendar.DAY_OF_MONTH);
	}

	private int getDayOfWeek(Locale locale, int year, int month, int dayOfMonth) {
		Calendar cal = Calendar.getInstance(locale);
		cal.set(year, month, dayOfMonth);
		return cal.get(Calendar.DAY_OF_WEEK);
	}
}
