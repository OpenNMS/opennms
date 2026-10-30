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
package org.opennms.web.alarm;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

public class AlarmQueryServletTest {

    private static final String PREFIX = "afterfirsteventtime";

    @Test
    public void testMidnight() throws Exception {
        assertEquals(LocalDateTime.of(2026, 10, 29, 0, 5), parse("12", "05", "am"));
    }

    @Test
    public void testNoon() throws Exception {
        assertEquals(LocalDateTime.of(2026, 10, 29, 12, 5), parse("12", "05", "pm"));
    }

    @Test
    public void testOtherHours() throws Exception {
        assertEquals(LocalDateTime.of(2026, 10, 29, 1, 0), parse("1", "00", "am"));
        assertEquals(LocalDateTime.of(2026, 10, 29, 11, 59), parse("11", "59", "am"));
        assertEquals(LocalDateTime.of(2026, 10, 29, 13, 0), parse("1", "00", "pm"));
        assertEquals(LocalDateTime.of(2026, 10, 29, 23, 59), parse("11", "59", "pm"));
    }

    private static LocalDateTime parse(String hour, String minute, String ampm) throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(PREFIX + "hour", hour);
        request.addParameter(PREFIX + "minute", minute);
        request.addParameter(PREFIX + "ampm", ampm);
        request.addParameter(PREFIX + "month", "9");
        request.addParameter(PREFIX + "date", "29");
        request.addParameter(PREFIX + "year", "2026");
        final Date date = new AlarmQueryServlet().getDateFromRequest(request, PREFIX);
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }
}
