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