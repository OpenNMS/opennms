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

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Test;
import org.opennms.netmgt.config.SyslogdConfigFactory;

public class Rfc5424SyslogParserTest {
    @Test
    public void shouldHonorTimezoneWithConfiguredDefault() throws IOException {
        checkDateParserWith(TimeZone.getTimeZone("CET"), "timezone=\"CET\" ");
    }

    @Test
    public void shouldHonorTimezoneWithoutConfiguredDefault() throws IOException {
        checkDateParserWith(TimeZone.getTimeZone("GMT"), "");
    }

    private void checkDateParserWith(TimeZone expectedTimeZone, String timezoneProperty) throws IOException {
        String configuration = "<syslogd-configuration>" +
                "<configuration " +
                "syslog-port=\"10514\" " +
                timezoneProperty+
                "/></syslogd-configuration>";

        final InputStream stream = new ByteArrayInputStream((configuration).getBytes());
        final SyslogdConfigFactory config = new SyslogdConfigFactory(stream);
        final SyslogParser parser = new Rfc5424SyslogParser(config, SyslogdTestUtils.toByteBuffer("something"));
        checkDateParserWith(expectedTimeZone, "yyyy-MM-dd'T'HH:mm:ss'Z'", parser);
        checkDateParserWith(expectedTimeZone, "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", parser);
    }

    private void checkDateParserWith(TimeZone expectedTimeZone, String pattern, SyslogParser parser) throws IOException {
        LocalDateTime expectedLocalDateTime = LocalDateTime.of(2017, 2, 3, 12, 21, 20);
        ZonedDateTime expectedDateTime = ZonedDateTime.of(expectedLocalDateTime, expectedTimeZone.toZoneId());
        Date parsedDate = parser.parseDate(DateTimeFormatter.ofPattern(pattern).format(expectedDateTime));
        assertEquals(expectedDateTime.toInstant(), parsedDate.toInstant());
    }

}