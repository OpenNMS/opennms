/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.controller;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * Mainly tests the calculation of start/end times, so we're sure we're getting
 * them right, particularly with the more complex "AT-TIME style" definitions, 
 * as defined for rrdfetch (http://oss.oetiker.ch/rrdtool/doc/rrdfetch.en.html)
 * 
 * Many of the comparisons are a little loose, and allow the times to be up to 1
 * second out, when comparing the calculated value to "now".
 * 
 * In practice this will usually be unnecessary or only 1-2 ms out, but let's
 * not fail the tests because of stupid internal timing issues.
 * 
 * NB: These tests largely mirror those in the JRobin TimeParserTest, with a few
 * minor exceptions.  This is so we can be sure that while we're using JRobin code 
 * for the parsing, we've passed values correctly.  And then if we rewrite or 
 * replace the parsing code with something else, we know what we expected should work.  
 * 
 * @author cmiskell
 * 
 */
public class RrdGraphControllerTest {

	private final static int ONE_HOUR_IN_MILLIS=60*60*1000;
	private final static int ONE_DAY_IN_MILLIS=24 * ONE_HOUR_IN_MILLIS;
	
	private RrdGraphController m_controller;
	private MockHttpServletRequest m_request;

	@Before
	public void setUp() throws InterruptedException {
		m_controller = new RrdGraphController();
		m_request = new MockHttpServletRequest();

		//Yeah, this looks weird, but there's method to my madness.  
		// Many of these tests create Calendars based on the current time, then use the 
		// parsing code which will assume something about when 'now' is, often the current day/week
		// If we're running around midnight and the Calendar gets created on one day and the parsing
		// happens on the next, you'll get spurious failures, which would be really annoying and hard
		// to replicate.  Initial tests show that most tests take ~100ms, so if it's within 10 seconds
		// of midnight, wait for 30 seconds (and print a message saing why)
		Calendar now = new GregorianCalendar();
		if(now.get(Calendar.HOUR_OF_DAY) == 23 && now.get(Calendar.MINUTE) > 59 && now.get(Calendar.SECOND) > 50) {
			Thread.sleep(30000);
		}


	}

	/**
	 * Set all fields smaller than an hour to 0.
	 * @param now
	 */
	private void setSubHourFieldsZero(Calendar cal) {
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
	}

	/**
	 * Set all fields smaller than a day to 0 (rounding down to midnight effectively)
	 * @param now
	 */
	private void setSubDayFieldsZero(Calendar cal) {
		setSubHourFieldsZero(cal);
		cal.set(Calendar.HOUR_OF_DAY,0);
	}
	
	/**
	 * Test a simple and explicit specification of start/end times. This is the
	 * most basic it can be.
	 */
	@Test
	public void testExplicitTimeSpec() {
		m_request.addParameter("start", "123456789");
		m_request.addParameter("end", "123456790");
		long[] result = m_controller.parseTimes(m_request);
		assertEquals(123456789, result[0]);
		assertEquals(123456790, result[1]);
	}

	/**
	 * Kinda like the JUnit asserts for doubles, which allows an "epsilon"
	 * But this is for integers, and with a specific description in the assert
	 * just for timestamps.
	 * 
	 * All times are expected in milliseconds
	 * 
	 * @param expected - expected value
	 * @param actual - actual value
	 * @param epsilon - the maximum difference
	 * @param desc - some description of the time.  Usually "start" or "end", could be others
	 */
	private void assertTimestampsEqualWithEpsilon(long expected, long actual, int epsilon, String desc) {
		assertTrue("Expecting the calculated "+desc+" time " + actual + " ("+ new Date(actual).toString() +")"
				+ " to be within "+epsilon+"ms of " + expected +" ("+new Date(expected)+")",
				Math.abs(actual - expected) < epsilon);
	}
	
	/**
	 * Test the defaults, when no start/end time has been specified at all. Per
	 * RRDTool, should give us 1 day ago until now
	 */
	@Test
	public void testDefaultStartEndTimeSpec() {
		long now = new Date().getTime();
		long oneDayAgo = now - (24 * 60 * 60 * 1000);
		long[] result = m_controller.parseTimes(m_request);
		
		long start = result[0];
		long end = result[1];
		assertTimestampsEqualWithEpsilon(oneDayAgo, start, 1000, "start");
		assertTimestampsEqualWithEpsilon(now, end, 1000, "end");
	
		assertTrue("Expecting the calculated end time " + end
				+ " to be within 1000ms of now " + now,
				Math.abs(end - now) < 1000);
	}


	/**
	 * Test the defaults if someone managed to pass through an empty string.  A bit paranoid, but hey, why not
	 */
	@Test
	public void testEmptyStringStartEndTimeSpec() {
		long now = new Date().getTime();
		long oneDayAgo = now - (24 * 60 * 60 * 1000);
		
		m_request.addParameter("end", "");
		m_request.addParameter("start", "");

		long[] result = m_controller.parseTimes(m_request);
		long start = result[0];
		long end = result[1];
		
		assertTimestampsEqualWithEpsilon(oneDayAgo, start, 1000, "start");
		assertTimestampsEqualWithEpsilon(now, end, 1000, "end");

	}
	
	/**
	 * Test an explicit setting of end to "now". Expect the same as for the
	 * default (1 day ago -> now)
	 */
	@Test
	public void testNowEnd() {
		long now = new Date().getTime();
		long oneDayAgo = now - (24 * 60 * 60 * 1000);

		m_request.addParameter("end", "now");

		long[] result = m_controller.parseTimes(m_request);
		long start = result[0];
		long end = result[1];
				
		assertTimestampsEqualWithEpsilon(oneDayAgo, start, 1000, "start");
		assertTimestampsEqualWithEpsilon(now, end, 1000, "end");

	}
	
	/**
	 * Test that we can use "n" for "now" in the end parameter.
	 */
	@Test
	public void testAbbreviatedNowForEnd() {
		long now = new Date().getTime();
		long oneDayAgo = now - (24 * 60 * 60 * 1000);

		m_request.addParameter("end", "n");

		long[] result = m_controller.parseTimes(m_request);
		long start = result[0];
		long end = result[1];
	
		assertTimestampsEqualWithEpsilon(oneDayAgo, start, 1000, "start");
		assertTimestampsEqualWithEpsilon(now, end, 1000, "end");

	}
	
	/**
	 * Test the specification of just an "hour", with "today" as the day, for
	 * both start and end, using the 24hour clock
	 */
	@Test
	public void test24HourClockHourTodayStartEndTime() {
		Calendar now = new GregorianCalendar();
		now.set(Calendar.HOUR_OF_DAY, 8); //8am
		now.set(Calendar.MINUTE, 0);
		now.set(Calendar.SECOND, 0);
		now.set(Calendar.MILLISECOND, 0);
		Date startDate = now.getTime();
		
		now.set(Calendar.HOUR_OF_DAY, 16); //4pm
		now.set(Calendar.MINUTE, 0);
		now.set(Calendar.SECOND, 0);
		now.set(Calendar.MILLISECOND, 0);
		Date endDate = now.getTime();
		
		m_request.addParameter("start", "08:00");
		m_request.addParameter("end", "16:00");
		
		long[] result = m_controller.parseTimes(m_request);
		long start = result[0];
		long end = result[1];

		assertEquals(startDate.getTime(), start);
		assertEquals(endDate.getTime(), end);

	}
	
	/**
	 * Test the specification of just an "hour", with "today" as the day, for
	 * both start and end, using am/pm designators
	 */
	//Will fail until JRobin 1.5.13 comes out with my fix to TimeParser (not yet committed)
	/*@Test
	public void testAMPMClockHourTodayStartEndTime() {
		Calendar now = new GregorianCalendar();
		now.set(Calendar.HOUR_OF_DAY, 8); //8am
		now.set(Calendar.MINUTE, 0);
		now.set(Calendar.SECOND, 0);
		now.set(Calendar.MILLISECOND, 0);
		Date startDate = now.getTime();
		now.set(Calendar.HOUR_OF_DAY, 16); //4pm
		now.set(Calendar.MINUTE, 0);
		now.set(Calendar.SECOND, 0);
		now.set(Calendar.MILLISECOND, 0);
		Date endDate = now.getTime();
		
		m_request.addParameter("start", "today 8am");
		m_request.addParameter("end", "today 4pm");
		
		long[] result = m_controller.parseTimes(m_request);
		long start = result[0];
		long end = result[1];

		assertEquals(startDate.getTime(), start);
		assertEquals(endDate.getTime(), end);

	}*/
	
	/**
	 * Test the specification of just an "hour", with "yesterday" as the day, for
	 * both start and end, using am/pm designators
	 */
	//Will fail until JRobin 1.5.13 comes out with my fix to TimeParser (not yet committed)
	/*@Test
	public void testAMPMClockHourTodayStartEndTime() {
		Calendar now = new GregorianCalendar();
		now.set(Calendar.HOUR_OF_DAY, 8); //8am
		now.set(Calendar.MINUTE, 0);
		now.set(Calendar.SECOND, 0);
		now.set(Calendar.MILLISECOND, 0);
		Date startDate = new Date(now.getTimeInMillis()-(24 * 60 * 60 * 1000));

		now.set(Calendar.HOUR_OF_DAY, 16); //4pm
		now.set(Calendar.MINUTE, 0);
		now.set(Calendar.SECOND, 0);
		now.set(Calendar.MILLISECOND, 0);
		Date endDate = new Date(now.getTimeInMillis()-(24 * 60 * 60 * 1000));

		
		m_request.addParameter("start", "yesterday 8am");
		m_request.addParameter("end", "yesterday 4pm");
		
		long[] result = m_controller.parseTimes(m_request);
		long start = result[0];
		long end = result[1];

		assertEquals(startDate.getTime(), start);
		assertEquals(endDate.getTime(), end);

	}*/

	/**
	 * Tests a simple negative hour offset
	 * Test the simple start=-1h example from rrdfetch man page.  
	 * 
	 */
	@Test
	public void testSimpleNegativeOffset() {
		Calendar now = new GregorianCalendar();
		Date endDate = now.getTime();
		Date startDate = new Date(now.getTimeInMillis()-(60 * 60 * 1000));
		
		m_request.addParameter("start", "-1h");
		
		long[] result = m_controller.parseTimes(m_request);
		long start = result[0];
		long end = result[1];

		assertTimestampsEqualWithEpsilon(endDate.getTime(), end, 1000, "end");
		assertTimestampsEqualWithEpsilon(startDate.getTime(), start, 1000, "start");
	}
	
	/**
	 * Test a start relative to an end that isn't now
	 */
	@Test
	public void testRelativeStartOffsetEnd() {
		Calendar now = new GregorianCalendar();
		Date endDate = new Date(now.getTimeInMillis()-ONE_HOUR_IN_MILLIS);
		Date startDate = new Date(now.getTimeInMillis()-(3*ONE_HOUR_IN_MILLIS));

		//End is 1 hour ago; start is 2 hours before that
		m_request.addParameter("end", "-1h");
		m_request.addParameter("start", "end-2h");
		long[] result = m_controller.parseTimes(m_request);
		long start = result[0];
		long end = result[1];

		assertTimestampsEqualWithEpsilon(endDate.getTime(), end, 1000, "end");
		assertTimestampsEqualWithEpsilon(startDate.getTime(), start, 1000, "start");

	}

	/**
	 * Test a start relative to an end that isn't now
	 */
	@Test
	public void testRelativeStartOffsetEndAbbreviatedEnd() {
		Calendar now = new GregorianCalendar();
		Date endDate = new Date(now.getTimeInMillis()-ONE_HOUR_IN_MILLIS);
		Date startDate = new Date(now.getTimeInMillis()-(3*ONE_HOUR_IN_MILLIS));

		//End is 1 hour ago; start is 2 hours before that
		m_request.addParameter("start", "e-2h");
		m_request.addParameter("end", "-1h");
		long[] result = m_controller.parseTimes(m_request);
		long start = result[0];
		long end = result[1];

		assertTimestampsEqualWithEpsilon(endDate.getTime(), end, 1000, "end");
		assertTimestampsEqualWithEpsilon(startDate.getTime(), start, 1000, "start");

	}

	/**
	 * Test an end relative to a start that isn't now
	 */
	@Test
	public void testRelativeEndOffsetStart() {
		Calendar now = new GregorianCalendar();
		Date endDate = new Date(now.getTimeInMillis()-(2*ONE_HOUR_IN_MILLIS));
		Date startDate = new Date(now.getTimeInMillis()-(4*ONE_HOUR_IN_MILLIS));

		m_request.addParameter("end", "start+2h");
		m_request.addParameter("start", "-4h");
		long[] result = m_controller.parseTimes(m_request);
		long start = result[0];
		long end = result[1];

		assertTimestampsEqualWithEpsilon(endDate.getTime(), end, 1000, "end");
		assertTimestampsEqualWithEpsilon(startDate.getTime(), start, 1000, "start");

	}

	/**
	 * Test an end relative to a start that isn't now - abbreviated start (s)
	 */
	@Test
	public void testRelativeEndOffsetStartAbbreviatedStart() {
		Calendar now = new GregorianCalendar();
		Date endDate = new Date(now.getTimeInMillis()-(2*ONE_HOUR_IN_MILLIS));
		Date startDate = new Date(now.getTimeInMillis()-(4*ONE_HOUR_IN_MILLIS));

		m_request.addParameter("end", "s+2h");
		m_request.addParameter("start", "-4h");
		long[] result = m_controller.parseTimes(m_request);
		long start = result[0];
		long end = result[1];

		assertTimestampsEqualWithEpsilon(endDate.getTime(), end, 1000, "end");
		assertTimestampsEqualWithEpsilon(startDate.getTime(), start, 1000, "start");

	}

	/**
	 * Test hour:min, and hour.min syntaxes
	 */
	@Test
	public void testHourMinuteSyntax() {
		Calendar now = new GregorianCalendar();
		setSubHourFieldsZero(now);
		now.set(Calendar.HOUR_OF_DAY, 8); 
		now.set(Calendar.MINUTE, 30);
		
		Date startDate = now.getTime();
		now.set(Calendar.HOUR_OF_DAY, 16); 
		now.set(Calendar.MINUTE, 45);
		Date endDate = now.getTime();
		
		//Mixed syntaxes FTW; two tests in one
		//This also exercises the test of the order of parsing (time then day).  If
		// that order is wrong, 8.30 could be (and was at one point) interpreted as a day.month 
		m_request.addParameter("end", "16:45");
		m_request.addParameter("start", "8.30");
		long[] result = m_controller.parseTimes(m_request);
		long start = result[0];
		long end = result[1];

		assertEquals(startDate.getTime(), start);
		assertEquals(endDate.getTime(), end);

	}
	
	/**
	 * Test a plain date specified as DD.MM.YYYY
	 */
	@Test
	public void testDateWithDots() {
		//Remember: 0-based month
		Calendar cal = new GregorianCalendar(1980,0,1);
		Date startDate = cal.getTime();
		
		cal = new GregorianCalendar(1980,11,15);
		Date endDate = cal.getTime();
		
		//Start is a simple one; end ensures we have our days/months around the right way.
		m_request.addParameter("end", "00:00 15.12.1980");
		m_request.addParameter("start", "00:00 01.01.1980");
		long[] result = m_controller.parseTimes(m_request);
		long start = result[0];
		long end = result[1];

		assertEquals(startDate.getTime(), start);
		assertEquals(endDate.getTime(), end);

	}

	/**
	 * Test a plain date specified as DD/MM/YYYY
	 */
	@Test
	public void testDateWithSlashes() {
		//Remember: 0-based month
		Calendar cal = new GregorianCalendar(1980,0,1);
		Date startDate = cal.getTime();
		
		cal = new GregorianCalendar(1980,11,15);
		Date endDate = cal.getTime();
		
		//Start is a simple one; end ensures we have our days/months around the right way.
		m_request.addParameter("end", "00:00 12/15/1980");
		m_request.addParameter("start", "00:00 01/01/1980");
		long[] result = m_controller.parseTimes(m_request);
		long start = result[0];
		long end = result[1];

		assertEquals(startDate.getTime(), start);
		assertEquals(endDate.getTime(), end);

	}
	
	/**
	 * Test a plain date specified as YYYYMMDD
	 */
	@Test
	public void testDateWithNoDelimiters() {
		//Remember: 0-based month
		Calendar cal = new GregorianCalendar(1980,0,1);
		Date startDate = cal.getTime();
		
		cal = new GregorianCalendar(1980,11,15);
		Date endDate = cal.getTime();
		
		//Start is a simple one; end ensures we have our days/months around the right way.
		m_request.addParameter("end", "00:00 19801215");
		m_request.addParameter("start", "00:00 19800101");
		long[] result = m_controller.parseTimes(m_request);
		long start = result[0];
		long end = result[1];

		assertEquals(startDate.getTime(), start);
		assertEquals(endDate.getTime(), end);

	}
	
	/**
	 * Test named month dates with no year
	 * 
	 * NB: Seems silly to test all, just test that an arbitrary one works, in both short and long form  
	 * 
	 * If we find actual problems with specific months, we can add more tests
	 */
	@Test
	public void testNamedMonthsNoYear() {
		//Remember: 0-based month -- 2 = March
		Calendar cal = new GregorianCalendar();
		cal.set(Calendar.MONTH, 2); 
		cal.set(Calendar.DAY_OF_MONTH, 1);
		this.setSubDayFieldsZero(cal);
		Date startDate = cal.getTime();
		
		//10 = November 
		cal = new GregorianCalendar();
		cal.set(Calendar.MONTH, 10); 
		cal.set(Calendar.DAY_OF_MONTH, 15);
		this.setSubDayFieldsZero(cal);
		Date endDate = cal.getTime();
		
		//one short, one long month name
		m_request.addParameter("end", "00:00 November 15");
		m_request.addParameter("start", "00:00 Mar 1 ");
		long[] result = m_controller.parseTimes(m_request);
		long start = result[0];
		long end = result[1];

		assertEquals(startDate.getTime(), start);
		assertEquals(endDate.getTime(), end);
		
	}

	/**
	 * Test named month dates with 2 digit years
	 * 
	 * NB: Seems silly to test all, just test that an arbitrary one works, in both short and long form
	 * If we find actual problems with specific months, we can add more tests
	 */
	@Test
	public void testNamedMonthsTwoDigitYear() {
		//Remember: 0-based month -- 1 = Feb
		Calendar cal = new GregorianCalendar(1980,1,2);
		Date startDate = cal.getTime();
		
		//9 = October 
		cal = new GregorianCalendar(1980,9,16);
		Date endDate = cal.getTime();
		
		m_request.addParameter("end", "00:00 October 16 80");
		m_request.addParameter("start", "00:00 Feb 2 80");
		long[] result = m_controller.parseTimes(m_request);
		long start = result[0];
		long end = result[1];

		assertEquals(startDate.getTime(), start);
		assertEquals(endDate.getTime(), end);
		
	}
	
	/**
	 * Test named month dates with 4 digit years
	 * 
	 * NB: Seems silly to test all, just test that an arbitrary one works, in both short and long form
	 * 
	 * If we find actual problems with specific months, we can add more tests
	 */
	@Test
	public void testNamedMonthsFourDigitYear() {
		//Remember: 0-based month -- 3 = April
		Calendar cal = new GregorianCalendar(1980,3,6);
		Date startDate = cal.getTime();
		
		//8 = Sept 
		cal = new GregorianCalendar(1980,8,17);
		Date endDate = cal.getTime();
		
		m_request.addParameter("end", "00:00 September 17 1980");
		m_request.addParameter("start", "00:00 Apr 6 1980");
		long[] result = m_controller.parseTimes(m_request);
		long start = result[0];
		long end = result[1];

		assertEquals(startDate.getTime(), start);
		assertEquals(endDate.getTime(), end);
		
	}
	
	/**
	 * Test day of week specification.  The expected behaviour is annoyingly murky; if these tests start failing
	 * give serious consideration to fixing the tests rather than the underlying code.
	 * 
	 */
	@Test
	public void testDayOfWeekTimeSpec() {
		Calendar cal = new GregorianCalendar();
		cal.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
		cal.set(Calendar.HOUR_OF_DAY, 12);
		this.setSubHourFieldsZero(cal);
		Date startDate = cal.getTime();

		cal = new GregorianCalendar();
		cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
		cal.set(Calendar.HOUR_OF_DAY, 18);
		this.setSubHourFieldsZero(cal);
		Date endDate = cal.getTime();

		
		m_request.addParameter("end", "6pm Friday");
		m_request.addParameter("start", "noon Thursday");
		long[] result = m_controller.parseTimes(m_request);
		long start = result[0];
		long end = result[1];

		assertEquals(startDate.getTime(), start);
		assertEquals(endDate.getTime(), end);
		
	}
	
	/**
	 * Test some basic time offsets
	 */
	@Test
	public void testTimeOffsets1() {
		Calendar cal = new GregorianCalendar();
		cal.setTimeInMillis(cal.getTimeInMillis()-60000);
		Date startDate = cal.getTime();
		
		cal = new GregorianCalendar();
		cal.setTimeInMillis(cal.getTimeInMillis()-10000);
		Date endDate = cal.getTime();
		
		m_request.addParameter("end", "now-10 seconds");
		m_request.addParameter("start", "now - 1minute");
		long[] result = m_controller.parseTimes(m_request);
		long start = result[0];
		long end = result[1];

		assertTimestampsEqualWithEpsilon(startDate.getTime(), start, 1000, "start");
		assertTimestampsEqualWithEpsilon(endDate.getTime(), end, 1000, "end");

	}
	
	/**
	 * Test some basic time offsets
	 * NB: Due to it's use of a "day" offset, this may fail around daylight savings time.
	 * Maybe (Depends how the parsing code constructs it's dates)
	 */
	@Test
	public void testTimeOffsets2() {
		Calendar cal = new GregorianCalendar();
		cal.setTimeInMillis(cal.getTimeInMillis()-ONE_DAY_IN_MILLIS);
		Date startDate = cal.getTime();
		
		cal = new GregorianCalendar();
		cal.setTimeInMillis(cal.getTimeInMillis()-(3*ONE_HOUR_IN_MILLIS));
		Date endDate = cal.getTime();
		
		m_request.addParameter("end", "now-3 hours");
		m_request.addParameter("start", "now - 1 day");
		long[] result = m_controller.parseTimes(m_request);
		long start = result[0];
		long end = result[1];

		assertTimestampsEqualWithEpsilon(startDate.getTime(), start, 1000, "start");
		assertTimestampsEqualWithEpsilon(endDate.getTime(), end, 1000, "end");

	}

	/**
	 * Test some basic time offsets
	 */
	@Test
	public void testTimeOffsets3() {
		Calendar cal = new GregorianCalendar();
		cal.set(Calendar.MONTH, 6);
		cal.set(Calendar.DAY_OF_MONTH, 12);
		cal.add(Calendar.MONTH, -1);
		Date startDate = cal.getTime();
		
		cal = new GregorianCalendar();
		cal.set(Calendar.MONTH, 6);
		cal.set(Calendar.DAY_OF_MONTH, 12);
		cal.add(Calendar.WEEK_OF_YEAR, -3);
		Date endDate = cal.getTime();
		
		m_request.addParameter("end", "Jul 12 - 3 weeks");
		m_request.addParameter("start", "Jul 12 - 1 month");
		long[] result = m_controller.parseTimes(m_request);
		long start = result[0];
		long end = result[1];

		assertTimestampsEqualWithEpsilon(startDate.getTime(), start, 1000, "start");
		assertTimestampsEqualWithEpsilon(endDate.getTime(), end, 1000, "end");

	}

	/**
	 * Test another basic time offset
	 */
	@Test
	public void testTimeOffsets4() {
		//Month 6 = July (0 offset)
		Calendar cal = new GregorianCalendar(1980, 6, 12);
		Date endDate = cal.getTime();
		
		cal = new GregorianCalendar(1979, 6, 12);
		Date startDate = cal.getTime();
		
		m_request.addParameter("end", "00:00 12.07.1980");
		m_request.addParameter("start", "end - 1 year");
		long[] result = m_controller.parseTimes(m_request);
		long start = result[0];
		long end = result[1];

		assertTimestampsEqualWithEpsilon(startDate.getTime(), start, 1000, "start");
		assertTimestampsEqualWithEpsilon(endDate.getTime(), end, 1000, "end");
	}
	
	/**
	 * Test some complex offset examples (per the rrdfetch man page)
	 */
	@Test
	public void complexTest1() {
		
		Calendar cal = new GregorianCalendar();
		cal.set(Calendar.HOUR_OF_DAY, 9);
		cal.add(Calendar.DAY_OF_YEAR, -1);
		setSubHourFieldsZero(cal);
		Date startDate = cal.getTime();
		

		m_request.addParameter("end", "now");
		m_request.addParameter("start", "noon yesterday -3hours");
		long[] result = m_controller.parseTimes(m_request);
		long start = result[0];

		assertEquals(startDate.getTime(), start);
	}
	
	/**
	 * Test some more complex offset examples
	 */
	@Test
	public void complexTest2() {
		
		Calendar cal = new GregorianCalendar();
		cal.add(Calendar.HOUR, -5);
		cal.add(Calendar.MINUTE, -45);
		Date startDate = cal.getTime();

		m_request.addParameter("end", "now");
		m_request.addParameter("start", "-5h45min");
		long[] result = m_controller.parseTimes(m_request);
		long start = result[0];

		assertTimestampsEqualWithEpsilon(startDate.getTime(), start, 1000, "start");
	}

	/**
	 * Test some more complex offset examples
	 */
	@Test
	public void complexTest3() {
		
		Calendar cal = new GregorianCalendar();
		cal.add(Calendar.MONTH, -5);
		cal.add(Calendar.WEEK_OF_YEAR, -1);
		cal.add(Calendar.DAY_OF_YEAR, -2);
		Date startDate = cal.getTime();

		m_request.addParameter("end", "now");
		m_request.addParameter("start", "-5mon1w2d");
		long[] result = m_controller.parseTimes(m_request);
		long start = result[0];

		assertTimestampsEqualWithEpsilon(startDate.getTime(), start, 1000, "start");
	}

}
