/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.Callable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.LegacyDatetimeFormatter;

@RunWith(Parameterized.class)
public class EventConstantsTest {

    // Test Parameters
    private final Locale m_testLocale;
    private final TimeZone m_testTimeZone;
    private final String m_gmtText;
    private final String m_zoneText;
    private final String m_legacyGmtText;
    private final String m_legacyZoneText;
    private final String m_legacyOldXmlGmtText;
    private final String m_legacyOldXmlZoneText;
    private final String m_legacyFullGmtText;
    private final String m_legacyFullZoneText;
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
                                "Thursday, March 10, 2011 10:40:37 PM GMT",
                                "Thursday, March 10, 2011 11:40:37 PM CET",
                                "Thursday, 10 March 2011 22:40:37 o'clock GMT",
                                "Thursday, 10 March 2011 23:40:37 o'clock CET",
                                "Thursday, March 10, 2011 10:40:37 PM GMT",
                                "Thursday, March 10, 2011 11:40:37 PM CET",
                                Long.valueOf(1299796837 * 1000L)
                            },
                            {
                                new Locale("it", "IT"),
                                TimeZone.getTimeZone("CET"),
                                "2011-03-10T23:40:37+01:00",
                                "2011-03-10T23:40:37+01:00",
                                "giovedì 10 marzo 2011 22.40.37 GMT",
                                "giovedì 10 marzo 2011 23.40.37 CET",
                                "Thursday, 10 March 2011 22:40:37 o'clock GMT",
                                "Thursday, 10 March 2011 23:40:37 o'clock CET",
                                "giovedì 10 marzo 2011 22.40.37 GMT",
                                "giovedì 10 marzo 2011 23.40.37 CET",
                                Long.valueOf(1299796837 * 1000L)
                            },
                            {
                                new Locale("fr", "FR"),
                                TimeZone.getTimeZone("CET"),
                                "2011-03-10T23:40:37+01:00",
                                "2011-03-10T23:40:37+01:00",
                                "jeudi 10 mars 2011 22:40:37 GMT",
                                "jeudi 10 mars 2011 23:40:37 CET",
                                "Thursday, 10 March 2011 22:40:37 o'clock GMT",
                                "Thursday, 10 March 2011 23:40:37 o'clock CET",
                                "jeudi 10 mars 2011 22:40:37 GMT",
                                "jeudi 10 mars 2011 23:40:37 CET",
                                Long.valueOf(1299796837 * 1000L)
                            },
                            {
                                new Locale("fr", "CA"),
                                TimeZone.getTimeZone("CET"),
                                "2011-03-10T23:40:37+01:00",
                                "2011-03-10T23:40:37+01:00",
                                "jeudi 10 mars 2011 22:40:37 GMT",
                                "jeudi 10 mars 2011 23:40:37 CET",
                                "Thursday, 10 March 2011 22:40:37 o'clock GMT",
                                "Thursday, 10 March 2011 23:40:37 o'clock CET",
                                "jeudi 10 mars 2011 22:40:37 GMT",
                                "jeudi 10 mars 2011 23:40:37 CET",
                                Long.valueOf(1299796837 * 1000L)
                            },
                            {
                                new Locale("de", "DE"),
                                TimeZone.getTimeZone("CET"),
                                "2011-03-10T23:40:37+01:00",
                                "2011-03-10T23:40:37+01:00",
                                "Donnerstag, 10. März 2011 22:40:37 GMT",
                                "Donnerstag, 10. März 2011 23:40:37 MEZ",
                                "Thursday, 10 March 2011 22:40:37 o'clock GMT",
                                "Thursday, 10 March 2011 23:40:37 o'clock CET",
                                "Donnerstag, 10. März 2011 22:40:37 GMT",
                                "Donnerstag, 10. März 2011 23:40:37 MEZ",
                                Long.valueOf(1299796837 * 1000L)
                            }
            });
    }

    public EventConstantsTest(
        final Locale locale,
        final TimeZone timeZone,
        final String gmtText,
        final String zoneText,
        final String legacyGmtText,
        final String legacyZoneText,
        final String legacyOldXmlGmtText,
        final String legacyOldXmlZoneText,
        final String legacyFullGmtText,
        final String legacyFullZoneText,
        final Long timestamp
    ) {
        m_testLocale = locale;
        m_testTimeZone = timeZone;
        m_gmtText = gmtText;
        m_zoneText = zoneText;
        m_legacyGmtText = legacyGmtText;
        m_legacyZoneText = legacyZoneText;
        m_legacyOldXmlGmtText = legacyOldXmlGmtText;
        m_legacyOldXmlZoneText = legacyOldXmlZoneText;
        m_legacyFullGmtText = legacyFullGmtText;
        m_legacyFullZoneText = legacyFullZoneText;
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
        System.clearProperty("org.opennms.events.legacyFormatter");

        LegacyDatetimeFormatter.FORMATTER_FULL.remove();
        LegacyDatetimeFormatter.FORMATTER_LONG.remove();
        LegacyDatetimeFormatter.FORMATTER_LONG_GMT.remove();
        LegacyDatetimeFormatter.FORMATTER_CUSTOM.remove();
        LegacyDatetimeFormatter.FORMATTER_DEFAULT.remove();
    }

    @After
    public void tearDown() {
        Locale.setDefault(m_defaultLocale);
        TimeZone.setDefault(m_defaultTimeZone);
        System.clearProperty("org.opennms.events.legacyFormatter");
    }

    @Test
    public void testNms12261() throws Exception {
        final Date date = EventConstants.getEventDatetimeFormatter().parse("2019-08-27T07:13:53+00:00");
        assertEquals(1566890033000l, date.getTime());
    }

    @Test
    public void testEventDateParse() throws Exception {
        // default (implicit legacyFormatter=false)
        Date date = EventConstants.getEventDatetimeFormatter().parse(m_gmtText);
        assertEquals(m_testLocale + ": time should equal " + m_timestamp, m_timestamp.longValue(), date.getTime());

        date = EventConstants.getEventDatetimeFormatter().parse(m_zoneText);
        assertEquals(m_testLocale + ": time should equal " + m_timestamp, m_timestamp.longValue(), date.getTime());

        // disable legacy formatter
        System.setProperty("org.opennms.events.legacyFormatter", "false");
        assertThrows(() -> {
            return EventConstants.getEventDatetimeFormatter().parse(m_legacyGmtText);
        });
        assertThrows(() -> {
            return EventConstants.getEventDatetimeFormatter().parse(m_legacyZoneText);
        });
        assertThrows(() -> {
            return EventConstants.getEventDatetimeFormatter().parse(m_legacyOldXmlGmtText);
        });
        assertThrows(() -> {
            return EventConstants.getEventDatetimeFormatter().parse(m_legacyOldXmlZoneText);
        });
        assertThrows(() -> {
            return EventConstants.getEventDatetimeFormatter().parse(m_legacyFullGmtText);
        });
        assertThrows(() -> {
            return EventConstants.getEventDatetimeFormatter().parse(m_legacyFullZoneText);
        });

        // enable legacy formatter
        System.setProperty("org.opennms.events.legacyFormatter", "true");

        date = EventConstants.getEventDatetimeFormatter().parse(m_gmtText);
        assertEquals(m_testLocale + ": time should equal " + m_timestamp, m_timestamp.longValue(), date.getTime());

        date = EventConstants.getEventDatetimeFormatter().parse(m_zoneText);
        assertEquals(m_testLocale + ": time should equal " + m_timestamp, m_timestamp.longValue(), date.getTime());

        date = EventConstants.getEventDatetimeFormatter().parse(m_legacyGmtText);
        assertEquals(m_testLocale + ": time should equal " + m_timestamp, m_timestamp.longValue(), date.getTime());

        date = EventConstants.getEventDatetimeFormatter().parse(m_legacyZoneText);
        assertEquals(m_testLocale + ": time should equal " + m_timestamp, m_timestamp.longValue(), date.getTime());

        date = EventConstants.getEventDatetimeFormatter().parse(m_legacyOldXmlGmtText);
        assertEquals(m_testLocale + ": time should equal " + m_timestamp, m_timestamp.longValue(), date.getTime());

        date = EventConstants.getEventDatetimeFormatter().parse(m_legacyOldXmlZoneText);
        assertEquals(m_testLocale + ": time should equal " + m_timestamp, m_timestamp.longValue(), date.getTime());

        date = EventConstants.getEventDatetimeFormatter().parse(m_legacyFullGmtText);
        assertEquals(m_testLocale + ": time should equal " + m_timestamp, m_timestamp.longValue(), date.getTime());

        date = EventConstants.getEventDatetimeFormatter().parse(m_legacyFullZoneText);
        assertEquals(m_testLocale + ": time should equal " + m_timestamp, m_timestamp.longValue(), date.getTime());
    }

    @Test
    public void testFormatToString() throws Exception {
        String formatted = EventConstants.getEventDatetimeFormatter().format(new Date(m_timestamp));
        assertEquals(m_testLocale + ": formatted string should equal " + m_gmtText, m_gmtText, formatted);

        System.setProperty("org.opennms.events.legacyFormatter", "false");
        formatted = EventConstants.getEventDatetimeFormatter().format(new Date(m_timestamp));
        assertEquals(m_testLocale + ": formatted string should equal " + m_gmtText, m_gmtText, formatted);

        System.setProperty("org.opennms.events.legacyFormatter", "true");
        formatted = EventConstants.getEventDatetimeFormatter().format(new Date(m_timestamp));
        assertEquals(m_testLocale + ": formatted string should equal " + m_legacyGmtText, m_legacyGmtText, formatted);
    }

    private void assertThrows(Callable<?> c) {
        Exception caught = null;
        try {
            c.call();
        } catch (final Exception e) {
            caught = e;
        }
        assertNotNull(caught);
    }
}
