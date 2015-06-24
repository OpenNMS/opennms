/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.MockLogAppender;

@RunWith(Parameterized.class)
public class DateFormatLocaleTest {

    // Test Parameters
    private final Locale m_testLocale;

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {Locale.CANADA},
                // Doesn't work because fr_CA drops the second digit in FULL format
                // {Locale.CANADA_FRENCH},
                {Locale.CHINA},
                // Doesn't work because fr_FR drops the second digit in FULL format
                // {Locale.FRANCE},
                // Doesn't work because de_DE drops the second digit in FULL format
                // {Locale.GERMANY},
                {Locale.ITALY},
                {Locale.JAPAN},
                {Locale.KOREA},
                {Locale.PRC},
                {Locale.TAIWAN},
                {Locale.UK},
                {Locale.US},
                {new Locale("no_NN")},
                {new Locale("no_NB")},
                {new Locale("no_NO")}
        });
    }

    public DateFormatLocaleTest(final Locale locale) {
        m_testLocale = locale;
    }

    @Before
    public void setUp() {
        MockLogAppender.setupLogging();
    }

    @Test
    public void testFullDateTimeParsing() throws Exception {
        long currentMillis = System.currentTimeMillis();
        // Remove the millisecond digits from the epoch timestamp value
        Date date = new Date(currentMillis - (currentMillis % 1000));
        DateFormat format = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, m_testLocale);
        String formattedDate = format.format(date);
        Date newDate = format.parse(formattedDate);
        assertEquals(m_testLocale.toString(), date, newDate);
    }

    @Test
    public void testLongDateTimeParsing() throws Exception {
        long currentMillis = System.currentTimeMillis();
        // Remove the millisecond digits from the epoch timestamp value
        Date date = new Date(currentMillis - (currentMillis % 1000));
        DateFormat format = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.LONG, m_testLocale);
        String formattedDate = format.format(date);
        Date newDate = format.parse(formattedDate);
        assertEquals(m_testLocale.toString(), date, newDate);
    }
}
