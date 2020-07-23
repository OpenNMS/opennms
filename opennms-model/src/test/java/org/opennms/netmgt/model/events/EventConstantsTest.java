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

package org.opennms.netmgt.model.events;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.events.api.EventConstants;

@RunWith(Parameterized.class)
public class EventConstantsTest {

    // Test Parameters
    private final Locale m_testLocale;
    private final TimeZone m_testTimeZone;
    private final String m_gmtText;
    private final String m_zoneText;
    private final Long m_timestamp;
    private final Calendar m_timestampCalendar;

    @Parameters
    public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][] {
                            {
                                new Locale("en", "US"),
                                TimeZone.getTimeZone("CET"),
                                "2011-03-10T23:40:37+01:00",
                                "2011-03-10T23:40:37+01:00",
                                Long.valueOf(1299796837 * 1000L)
                            },
                            {
                                new Locale("it", "IT"),
                                TimeZone.getTimeZone("CET"),
                                "2011-03-10T23:40:37+01:00",
                                "2011-03-10T23:40:37+01:00",
                                Long.valueOf(1299796837 * 1000L)
                            },
                            {
                                new Locale("fr", "FR"),
                                TimeZone.getTimeZone("CET"),
                                "2011-03-10T23:40:37+01:00",
                                "2011-03-10T23:40:37+01:00",
                                Long.valueOf(1299796837 * 1000L)
                            },
                            {
                                new Locale("fr", "CA"),
                                TimeZone.getTimeZone("CET"),
                                "2011-03-10T23:40:37+01:00",
                                "2011-03-10T23:40:37+01:00",
                                Long.valueOf(1299796837 * 1000L)
                            },
                            {
                                new Locale("de", "DE"),
                                TimeZone.getTimeZone("CET"),
                                "2011-03-10T23:40:37+01:00",
                                "2011-03-10T23:40:37+01:00",
                                Long.valueOf(1299796837 * 1000L)
                            }
            });
    }

    public EventConstantsTest(final Locale locale, final TimeZone timeZone, final String gmtText, final String zoneText, final Long timestamp) {
        m_testLocale = locale;
        m_testTimeZone = timeZone;
        m_gmtText = gmtText;
        m_zoneText = zoneText;
        m_timestamp = timestamp;
        m_timestampCalendar = new GregorianCalendar();
        m_timestampCalendar.setTime(new Date(m_timestamp));
    }

    // Initialized Inside the Tests
    private Locale m_defaultLocale;
    private TimeZone m_defaultTimeZone;

    @Before
    public void setUp() {
        MockLogAppender.setupLogging();

        m_defaultLocale = Locale.getDefault();
        m_defaultTimeZone = TimeZone.getDefault();
        Locale.setDefault(m_testLocale);
        TimeZone.setDefault(m_testTimeZone);
    }

    @After
    public void tearDown() {
        Locale.setDefault(m_defaultLocale);
        TimeZone.setDefault(m_defaultTimeZone);
    }

    @Test
    public void testNms12261() throws Exception {
        final Date date = EventConstants.parseToDate("2019-08-27T07:13:53+00:00");
        assertEquals(1566890033000l, date.getTime());
    }

    @Test
    public void testEventDateParse() throws Exception {
        final Date date = EventConstants.parseToDate(m_zoneText);
        assertEquals(m_testLocale + ": time should equal " + m_timestamp, m_timestamp.longValue(), date.getTime());
    }

    @Test
    public void testFormatToString() throws Exception {
        final String formatted = EventConstants.formatToString(new Date(m_timestamp));
        assertEquals(m_testLocale + ": formatted string should equal " + m_gmtText, m_gmtText, formatted);
    }
}
