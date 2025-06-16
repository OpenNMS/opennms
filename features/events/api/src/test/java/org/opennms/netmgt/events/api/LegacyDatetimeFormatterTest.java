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
package org.opennms.netmgt.events.api;

import static org.junit.Assert.assertEquals;

import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opennms.test.LocaleProviderUtils;

public class LegacyDatetimeFormatterTest {
    private TimeZone oldZone;

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
        oldZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("Etc/UTC")));
    }

    @After
    public void tearDown() {
        TimeZone.setDefault(oldZone);
    }

    @Test
    public void testFormat() throws Exception {
        final LegacyDatetimeFormatter formatter = new LegacyDatetimeFormatter();
        final String date = formatter.format(new Date(0));
        assertEquals("1970-01-01T00:00:00Z", date);
    }

    @Test
    public void testParse() throws Exception {
        final LegacyDatetimeFormatter formatter = new LegacyDatetimeFormatter();

        // "default" format (DateFormat.FULL + DateFormat.LONG)
        Date date = formatter.parse("Thursday, January 1, 1970 12:00:03 AM GMT");
        assertEquals(new Date(3000), date);

        // "custom" format (old XML format)
        date = formatter.parse("Thursday, 1 January 1970 00:00:03 o'clock GMT");
        assertEquals(new Date(3000), date);

        // "full" format
        date = formatter.parse("Thursday, January 1, 1970 12:00:03 AM GMT");
        assertEquals(new Date(3000), date);

        date = formatter.parse("1970-01-01T00:00:03+00:00");
        assertEquals(new Date(3000), date);
    }
}
