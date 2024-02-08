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
package org.opennms.netmgt.syslogd;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneId;

import org.junit.Test;
import org.opennms.netmgt.config.SyslogdConfigFactory;

public class RadixTreeSyslogParserTest {

    @Test
    public void shouldHonorTimezoneSettings() throws Exception {
        // we expect the following behaviour:

        // 1.) log with time zone => time zone of log:
        checkWithTimeZoneInLogMessage(ZoneId.of("Europe/Berlin"), "timezone=\"CET\" ");

        // 2.) log without time zone + with configured time zone => configured time zone
        checkWithoutTimeZoneInLogMessage(ZoneId.of("CET"), "timezone=\"CET\" ");

        // 3.) log without time zone + no configured time zone => JVM time zone:
        checkWithoutTimeZoneInLogMessage(ZoneId.systemDefault(), "");
    }

    private void checkWithTimeZoneInLogMessage(ZoneId expectedTimeZoneId, String timezoneProperty) throws Exception {
        String log = "<1> 2018-10-12T10:34:43"+expectedTimeZoneId.getId()+" localhost logmessage";
        check(expectedTimeZoneId, timezoneProperty, log);
    }

    private void checkWithoutTimeZoneInLogMessage(ZoneId expectedTimeZoneId, String timezoneProperty) throws Exception {
        String log = "<1> 2018-10-12T10:34:43 localhost logmessage";
        check(expectedTimeZoneId, timezoneProperty, log);
    }

    private void check(ZoneId expectedTimeZoneId, String timezoneProperty, String log) throws Exception {

        String configuration = "<syslogd-configuration>" +
                "<configuration " +
                "syslog-port=\"10514\" " +
                timezoneProperty+
                "/></syslogd-configuration>";

        final InputStream stream = new ByteArrayInputStream((configuration).getBytes());
        final SyslogdConfigFactory config = new SyslogdConfigFactory(stream);
        final RadixTreeSyslogParser parser = new RadixTreeSyslogParser(config, SyslogdTestUtils.toByteBuffer(log));
        SyslogMessage message = parser.parse();
        assertEquals(expectedTimeZoneId, message.getZoneId());
    }

    private SyslogMessage parseSyslogMessage(final String log) throws IOException {
        final String configuration = "<syslogd-configuration><configuration syslog-port=\"10514\"/></syslogd-configuration>";

        final InputStream stream = new ByteArrayInputStream((configuration).getBytes());
        final SyslogdConfigFactory config = new SyslogdConfigFactory(stream);
        final RadixTreeSyslogParser parser = new RadixTreeSyslogParser(config, SyslogdTestUtils.toByteBuffer(log));
        return parser.parse();
    }

    @Test
    public void test_HZN_1504_Case1() throws IOException {
        final String log = "<187>2765: .Jan  7 12:36:39: %LINK-3-UPDOWN: Interface GigabitEthernet0, changed state to up";
        assertEquals(Integer.valueOf(1), parseSyslogMessage(log).getMonth());
    }

    @Test
    public void test_HZN_1504_Case2() throws IOException {
        final String log = "<189>338: *Jan 17 17:05:36.608: %SYS-5-CONFIG_I: Configured from console by console";
        assertEquals(Integer.valueOf(1), parseSyslogMessage(log).getMonth());
    }
}