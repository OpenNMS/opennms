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
package org.opennms.netmgt.model.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.Callable;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.LegacyDatetimeFormatter;
import org.opennms.test.LocaleProviderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NOTE: if you run this test in Eclipse, it doesn't properly inherit the
 * <code>java.locale.providers=CLDR,COMPAT</code> property from the
 * top-level <code>pom.xml</code> file. You will need to set it manually
 * by specifying <code>java.locale.providers=CLDR,COMPAT</code> in your JUnit
 * run configuration.
 */
@RunWith(Parameterized.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EventConstantsTest {
    private static final Logger LOG = LoggerFactory.getLogger(EventConstantsTest.class);

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
                                TimeZone.getTimeZone("UTC"),
                                "2011-03-10T22:40:37Z",
                                "2011-03-10T22:40:37Z",
                                "Thursday, March 10, 2011 10:40:37 PM GMT",
                                "Thursday, March 10, 2011 10:40:37 PM UTC",
                                "Thursday, 10 March 2011 22:40:37 o'clock GMT",
                                "Thursday, 10 March 2011 22:40:37 o'clock UTC",
                                "Thursday, March 10, 2011 10:40:37 PM GMT",
                                "Thursday, March 10, 2011 10:40:37 PM UTC",
                                Long.valueOf(1299796837 * 1000L)
                            },
                            {
                                new Locale("en", "US"),
                                TimeZone.getTimeZone("CET"),
                                "2011-03-10T23:40:37+01:00",
                                "2011-03-10T23:40:37+01:00",
                                "Thursday, March 10, 2011 10:40:37 PM GMT",
                                "Thursday, March 10, 2011 05:40:37 PM EST",
                                "Thursday, 10 March 2011 22:40:37 o'clock GMT",
                                "Thursday, 10 March 2011 17:40:37 o'clock EST",
                                "Thursday, March 10, 2011 10:40:37 PM GMT",
                                "Thursday, March 10, 2011 05:40:37 PM EST",
                                Long.valueOf(1299796837 * 1000L)
                            },
                            {
                                new Locale("en", "US"),
                                TimeZone.getTimeZone("CST"),
                                "2011-03-10T16:40:37-06:00",
                                "2011-03-10T16:40:37-06:00",
                                "Thursday, March 10, 2011 10:40:37 PM GMT",
                                "Thursday, March 10, 2011 04:40:37 PM CST",
                                "Thursday, 10 March 2011 22:40:37 o'clock GMT",
                                "Thursday, 10 March 2011 16:40:37 o'clock CST",
                                "Thursday, March 10, 2011 10:40:37 PM GMT",
                                "Thursday, March 10, 2011 04:40:37 PM CST",
                                Long.valueOf(1299796837 * 1000L)
                            },
                            {
                                new Locale("en", "US"),
                                TimeZone.getTimeZone("MST"),
                                "2011-03-10T15:40:37-07:00",
                                "2011-03-10T15:40:37-07:00",
                                "Thursday, March 10, 2011 10:40:37 PM GMT",
                                "Thursday, March 10, 2011 03:40:37 PM MST",
                                "Thursday, 10 March 2011 22:40:37 o'clock GMT",
                                "Thursday, 10 March 2011 15:40:37 o'clock MST",
                                "Thursday, March 10, 2011 10:40:37 PM GMT",
                                "Thursday, March 10, 2011 03:40:37 PM MST",
                                Long.valueOf(1299796837 * 1000L)
                            },
                            {
                                new Locale("en", "US"),
                                TimeZone.getTimeZone("PST"),
                                "2011-03-10T14:40:37-08:00",
                                "2011-03-10T14:40:37-08:00",
                                "Thursday, March 10, 2011 10:40:37 PM GMT",
                                "Thursday, March 10, 2011 02:40:37 PM PST",
                                "Thursday, 10 March 2011 22:40:37 o'clock GMT",
                                "Thursday, 10 March 2011 14:40:37 o'clock PST",
                                "Thursday, March 10, 2011 10:40:37 PM GMT",
                                "Thursday, March 10, 2011 02:40:37 PM PST",
                                Long.valueOf(1299796837 * 1000L)
                            },
                            /* this changed subtly in JDK11 and frankly, it's not worth figuring out how exactly
                            {
                                new Locale("it", "IT"),
                                TimeZone.getTimeZone("CET"),
                                "2011-03-10T23:40:37+01:00",
                                "2011-03-10T23:40:37+01:00",
                                "giovedì 10 marzo 2011 22:40:37 GMT",
                                "giovedì 10 marzo 2011 17:40:37 EST",
                                "Thursday, 10 March 2011 22:40:37 o'clock GMT",
                                "Thursday, 10 March 2011 17:40:37 o'clock EST",
                                "giovedì 10 marzo 2011 22:40:37 GMT",
                                "giovedì 10 marzo 2011 17:40:37 EST",
                                Long.valueOf(1299796837 * 1000L)
                            },
                            */
                            {
                                new Locale("fr", "FR"),
                                TimeZone.getTimeZone("CET"),
                                "2011-03-10T23:40:37+01:00",
                                "2011-03-10T23:40:37+01:00",
                                "jeudi 10 mars 2011 22:40:37 GMT",
                                "jeudi 10 mars 2011 17:40:37 EST",
                                "Thursday, 10 March 2011 22:40:37 o'clock GMT",
                                "Thursday, 10 March 2011 17:40:37 o'clock EST",
                                "jeudi 10 mars 2011 22:40:37 GMT",
                                "jeudi 10 mars 2011 17:40:37 EST",
                                Long.valueOf(1299796837 * 1000L)
                            },
                            {
                                new Locale("fr", "CA"),
                                TimeZone.getTimeZone("CET"),
                                "2011-03-10T23:40:37+01:00",
                                "2011-03-10T23:40:37+01:00",
                                "jeudi 10 mars 2011 22:40:37 GMT",
                                "jeudi 10 mars 2011 17:40:37 EST",
                                "Thursday, 10 March 2011 22:40:37 o'clock GMT",
                                "Thursday, 10 March 2011 17:40:37 o'clock EST",
                                "jeudi 10 mars 2011 22:40:37 GMT",
                                "jeudi 10 mars 2011 17:40:37 EST",
                                Long.valueOf(1299796837 * 1000L)
                            },
                            {
                                new Locale("de", "DE"),
                                TimeZone.getTimeZone("CET"),
                                "2011-03-10T23:40:37+01:00",
                                "2011-03-10T23:40:37+01:00",
                                "Donnerstag, 10. März 2011 22:40:37 GMT",
                                "Donnerstag, 10. März 2011 17:40:37 EST",
                                "Thursday, 10 March 2011 22:40:37 o'clock GMT",
                                "Thursday, 10 March 2011 17:40:37 o'clock EST",
                                "Donnerstag, 10. März 2011 22:40:37 GMT",
                                "Donnerstag, 10. März 2011 17:40:37 EST",
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

    @BeforeClass
    public static void beforeClass() throws Exception {
        LocaleProviderUtils.compat();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        LocaleProviderUtils.reset();
    }

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
    public void testEventGmtDateParse() throws Exception {
        System.setProperty("org.opennms.events.legacyFormatter", "false");
        parseText(m_gmtText);
    }

    @Test
    public void testEventZoneDateParse() throws Exception {
        System.setProperty("org.opennms.events.legacyFormatter", "false");
        parseText(m_zoneText);
    }

    @Test
    public void testEventLegacyGmtDateParse() throws Exception {
        System.setProperty("org.opennms.events.legacyFormatter", "false");
        assertThrows(() -> {
            return EventConstants.getEventDatetimeFormatter().parse(m_legacyGmtText);
        });
    }

    @Test
    public void testEventLegacyZoneDateParse() throws Exception {
        System.setProperty("org.opennms.events.legacyFormatter", "false");
        assertThrows(() -> {
            return EventConstants.getEventDatetimeFormatter().parse(m_legacyZoneText);
        });
    }

    @Test
    public void testEventLegacyOldXmlGmtDateParse() throws Exception {
        System.setProperty("org.opennms.events.legacyFormatter", "false");
        assertThrows(() -> {
            return EventConstants.getEventDatetimeFormatter().parse(m_legacyOldXmlGmtText);
        });
    }

    @Test
    public void testEventLegacyOldXmlZoneDateParse() throws Exception {
        System.setProperty("org.opennms.events.legacyFormatter", "false");
        assertThrows(() -> {
            return EventConstants.getEventDatetimeFormatter().parse(m_legacyOldXmlZoneText);
        });
    }

    @Test
    public void testEventLegacyFullGmtDateParse() throws Exception {
        System.setProperty("org.opennms.events.legacyFormatter", "false");
        assertThrows(() -> {
            return EventConstants.getEventDatetimeFormatter().parse(m_legacyFullGmtText);
        });
    }

    @Test
    public void testEventLegacyFullZoneDateParse() throws Exception {
        System.setProperty("org.opennms.events.legacyFormatter", "false");
        assertThrows(() -> {
            return EventConstants.getEventDatetimeFormatter().parse(m_legacyFullZoneText);
        });
    }

    @Test
    public void testEventGmtDateParseLegacyFormatter() throws Exception {
        System.setProperty("org.opennms.events.legacyFormatter", "true");
        parseText(m_gmtText);
    }

    @Test
    public void testEventZoneDateParseLegacyFormatter() throws Exception {
        System.setProperty("org.opennms.events.legacyFormatter", "true");
        parseText(m_zoneText);
    }

    @Test
    public void testEventLegacyGmtDateParseLegacyFormatter() throws Exception {
        System.setProperty("org.opennms.events.legacyFormatter", "true");
        parseText(m_legacyGmtText);
    }

    @Test
    public void testEventLegacyZoneDateParseLegacyFormatter() throws Exception {
        System.setProperty("org.opennms.events.legacyFormatter", "true");
        parseText(m_legacyZoneText);
    }

    @Test
    public void testEventLegacyOldXmlGmtDateParseLegacyFormatter() throws Exception {
        System.setProperty("org.opennms.events.legacyFormatter", "true");
        parseText(m_legacyOldXmlGmtText);
    }

    @Test
    public void testEventLegacyOldXmlZoneDateParseLegacyFormatter() throws Exception {
        System.setProperty("org.opennms.events.legacyFormatter", "true");
        parseText(m_legacyOldXmlZoneText);
    }

    @Test
    public void testEventLegacyFullGmtDateParseLegacyFormatter() throws Exception {
        System.setProperty("org.opennms.events.legacyFormatter", "true");
        parseText(m_legacyFullGmtText);
    }

    @Test
    public void testEventLegacyFullZoneDateParseLegacyFormatter() throws Exception {
        System.setProperty("org.opennms.events.legacyFormatter", "true");
        parseText(m_legacyFullZoneText);
    }

    @Test
    public void testFormatToString() throws Exception {
        System.setProperty("org.opennms.events.legacyFormatter", "false");
        String formatted = EventConstants.getEventDatetimeFormatter().format(new Date(m_timestamp));
        LOG.debug("formatted date as " + m_testLocale.getDisplayLanguage() + ", " + m_testTimeZone.getID() + ": " + formatted);
        assertEquals(m_testLocale + ": formatted string should equal " + m_gmtText, m_gmtText, formatted);
    }

    @Test
    public void testFormatToStringLegacyFormatter() throws Exception {
        System.setProperty("org.opennms.events.legacyFormatter", "true");
        String formatted = EventConstants.getEventDatetimeFormatter().format(new Date(m_timestamp));
        LOG.debug("formatted date as " + m_testLocale.getDisplayLanguage() + ", " + m_testTimeZone.getID() + ": " + formatted + " (legacy)");
        assertEquals(m_testLocale + ": formatted string should equal " + m_gmtText, m_gmtText, formatted);
    }

    private void parseText(final String text) throws ParseException {
        LOG.debug("parsing text as " + m_testLocale.getDisplayLanguage() + ", " + m_testTimeZone.getID() + ": " + text);
        try {
            Date date = EventConstants.getEventDatetimeFormatter().parse(text);
            assertEquals(m_testLocale + ": time should equal " + m_timestamp, m_timestamp.longValue(), date.getTime());
        } catch (final ParseException e) {
            e.printStackTrace();
            throw e;
        }
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
