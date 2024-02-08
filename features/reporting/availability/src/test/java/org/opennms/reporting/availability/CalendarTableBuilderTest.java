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
package org.opennms.reporting.availability;

/*
 * @author jsartin
 * 
 * Test cases for building calendar table used in availability reports
 * 
 */

import java.util.Calendar;
import java.util.Locale;

import junit.framework.TestCase;

public class CalendarTableBuilderTest extends TestCase {

        @Override
	protected void setUp() throws Exception {
		super.setUp();
	}

        @Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testLocaleFrench() {
		Locale.setDefault(Locale.FRENCH);
		CalendarTableBuilder builder = new CalendarTableBuilder(2005,05);
		CalendarTable section = builder.getTable();
		DaysOfWeek days = section.getDaysOfWeek();
		assertEquals("dim.", days.getDayName(0));
		}
	public void testLocaleEnUs() {
		Locale.setDefault(Locale.US);
		CalendarTableBuilder builder = new CalendarTableBuilder(2005,05);
		CalendarTable section = builder.getTable();
		DaysOfWeek days = section.getDaysOfWeek();
		assertEquals("Sun", days.getDayName(0));
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
		@SuppressWarnings("unused")
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
