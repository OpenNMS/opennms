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

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
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
}