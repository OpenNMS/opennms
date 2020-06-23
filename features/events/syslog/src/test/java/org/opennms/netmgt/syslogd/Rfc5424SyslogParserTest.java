/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
 *     http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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