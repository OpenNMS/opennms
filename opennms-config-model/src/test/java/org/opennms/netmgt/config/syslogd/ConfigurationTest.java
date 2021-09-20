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

package org.opennms.netmgt.config.syslogd;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.zone.ZoneRulesException;
import java.util.TimeZone;

import org.junit.Test;
import org.opennms.core.xml.JaxbUtils;

public class ConfigurationTest {

    @Test
    public void validTimeZoneShouldReturned() throws IOException {
        assertEquals("GMT", readTimeZone("timezone=\"GMT\" ").getID());
    }

    @Test
    public void emptyTimeZoneAttributeShouldReturnNull() throws IOException {
        assertNull(readTimeZone("timezone=\"\""));
    }

    @Test(expected = ZoneRulesException.class)
    public void invalidTimeZoneShouldBeReported() throws IOException {
        readTimeZone("timezone=\"NotATimeZone\" ");
    }

    private TimeZone readTimeZone(String timezoneProperty) throws IOException {
        String configuration = "<syslogd-configuration>" +
                "<configuration " +
                "syslog-port=\"10514\" " +
                timezoneProperty +
                "/></syslogd-configuration>";
        final InputStream stream = new ByteArrayInputStream((configuration).getBytes());
        SyslogdConfiguration config;
        try (final Reader reader = new InputStreamReader(stream)) {
            config = JaxbUtils.unmarshal(SyslogdConfiguration.class, reader);
        }
        return config.getConfiguration().getTimeZone().orElse(null);
    }
}