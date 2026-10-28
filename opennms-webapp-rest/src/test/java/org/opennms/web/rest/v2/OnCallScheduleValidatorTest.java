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
package org.opennms.web.rest.v2;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.web.rest.v2.model.OnCallScheduleDto;
import org.opennms.web.rest.v2.model.OnCallTimeDto;

public class OnCallScheduleValidatorTest {

    private Locale m_locale;

    @Before
    public void pinLocale() {
        m_locale = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
    }

    @After
    public void restoreLocale() {
        Locale.setDefault(m_locale);
    }

    private static OnCallScheduleDto schedule(final String type, final String day, final String begins, final String ends) {
        final OnCallTimeDto time = new OnCallTimeDto();
        time.setDay(day);
        time.setBegins(begins);
        time.setEnds(ends);
        final OnCallScheduleDto dto = new OnCallScheduleDto();
        dto.setUser("bob");
        dto.setType(type);
        dto.setTimes(Arrays.asList(time));
        return dto;
    }

    private static void assertRejected(final OnCallScheduleDto dto, final String... mentions) {
        try {
            OnCallScheduleValidator.validate(dto);
            fail("expected a rejection");
        } catch (final UserMgmtValidationException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("bob"));
            for (final String mention : mentions) {
                assertTrue(e.getMessage(), e.getMessage().contains(mention));
            }
        }
    }

    @Test
    public void testSpecificTimeInADaylightSavingGap() {
        final TimeZone zone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));
        try {
            // the clocks go from 02:00 to 03:00 on 8 March 2026
            assertRejected(schedule("specific", null, "08-Mar-2026 02:30:00", "08-Mar-2026 04:00:00"),
                    "does not exist in the server's time zone (America/New_York)");
            OnCallScheduleValidator.validate(schedule("specific", null, "08-Mar-2026 03:30:00", "08-Mar-2026 04:00:00"));
            // a malformed time still gets the format message
            assertRejected(schedule("specific", null, "08-Mar-2026 2:30", "08-Mar-2026 04:00:00"), "must be a time like");
        } finally {
            TimeZone.setDefault(zone);
        }
    }

    @Test
    public void testSpecific() {
        OnCallScheduleValidator.validate(schedule("specific", null, "21-Oct-2026 08:00:00", "21-Oct-2026 17:00:00"));
        assertRejected(schedule("specific", null, "08:00:00", "17:00:00"), "begins");
        assertRejected(schedule("specific", "monday", "21-Oct-2026 08:00:00", "21-Oct-2026 17:00:00"), "day");
        assertRejected(schedule("specific", null, "21-Oct-2026 08:00:00", "21-Oct-2026 25:00:00"), "ends");
    }

    @Test
    public void testDaily() {
        OnCallScheduleValidator.validate(schedule("daily", null, "08:00:00", "17:00:00"));
        assertRejected(schedule("daily", null, "8:00:00", "17:00:00"), "begins");
        assertRejected(schedule("daily", "monday", "08:00:00", "17:00:00"), "day");
        assertRejected(schedule("daily", null, "08:00:00", "21-Oct-2026 17:00:00"), "ends");
    }

    @Test
    public void testWeekly() {
        OnCallScheduleValidator.validate(schedule("weekly", "monday", "08:00:00", "17:00:00"));
        OnCallScheduleValidator.validate(schedule("weekly", "Sunday", "00:00:00", "23:59:59"));
        assertRejected(schedule("weekly", null, "08:00:00", "17:00:00"), "day");
        assertRejected(schedule("weekly", "funday", "08:00:00", "17:00:00"), "day");
        assertRejected(schedule("weekly", "3", "08:00:00", "17:00:00"), "day");
        assertRejected(schedule("weekly", "monday", "08:00", "17:00:00"), "begins");
    }

    @Test
    public void testMonthly() {
        OnCallScheduleValidator.validate(schedule("monthly", "1", "08:00:00", "17:00:00"));
        OnCallScheduleValidator.validate(schedule("monthly", "31", "08:00:00", "17:00:00"));
        assertRejected(schedule("monthly", "0", "08:00:00", "17:00:00"), "day");
        assertRejected(schedule("monthly", "32", "08:00:00", "17:00:00"), "day");
        assertRejected(schedule("monthly", "monday", "08:00:00", "17:00:00"), "day");
        assertRejected(schedule("monthly", null, "08:00:00", "17:00:00"), "day");
        assertRejected(schedule("monthly", "5", "08:00:00", "25:00:00"), "ends");
    }

    @Test
    public void testBeginsMustPrecedeEnds() {
        assertRejected(schedule("daily", null, "17:00:00", "08:00:00"), "before");
        assertRejected(schedule("daily", null, "08:00:00", "08:00:00"), "before");
        assertRejected(schedule("specific", null, "22-Oct-2026 08:00:00", "21-Oct-2026 17:00:00"), "before");
    }

    @Test
    public void testTypeAndTimesRequired() {
        assertRejected(schedule("hourly", null, "08:00:00", "17:00:00"), "type");
        assertRejected(schedule(null, null, "08:00:00", "17:00:00"), "type");
        final OnCallScheduleDto none = schedule("daily", null, "08:00:00", "17:00:00");
        none.setTimes(java.util.Collections.emptyList());
        assertRejected(none, "time");
        none.setTimes(null);
        assertRejected(none, "time");
        final OnCallScheduleDto missingBegins = schedule("daily", null, null, "17:00:00");
        assertRejected(missingBegins, "begins");
    }

    @Test
    public void testNameRules() {
        for (final String good : List.of("Night shift", "a.b", "Night-shift (2)", "rota_1", "7x24", "Bereitschaft Süd", "夜勤", "Cafe\u0301")) {
            assertNull("'" + good + "' should be accepted", OnCallScheduleValidator.validateName(good));
        }
        for (final String bad : List.of(" x", "x ", "a/b", "<b>", "..", ".", "a%b", "a?b", "a#b", "a\\b", "a&b", "a\"b", "a'b", "a`b", "a>b", "",
                // lookalikes: other whitespace, invisible characters, and double spaces
                "Ops\u00A0", "Ops\u2003", "a\nb", "a\tb", "a  b", "a\u200Bb", "-x", "a@b")) {
            assertNotNull("'" + bad + "' should be rejected", OnCallScheduleValidator.validateName(bad));
        }
        assertNull(OnCallScheduleValidator.validateName("x".repeat(256)));
        assertNotNull(OnCallScheduleValidator.validateName("x".repeat(257)));
    }
}
