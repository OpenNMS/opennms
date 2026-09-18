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
package org.opennms.netmgt.wsman.eventlog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.wsman.eventlog.Log;
import org.opennms.netmgt.config.wsman.eventlog.Package;
import org.opennms.netmgt.config.wsman.eventlog.WsmanEventlogConfiguration;

public class WsmanEventlogConfigurationTest {

    private static final File SHIPPED = new File("../../opennms-base-assembly/src/main/filtered/etc/wsman-eventlog-configuration.xml");

    @Test
    public void readsTheShippedConfiguration() {
        final WsmanEventlogConfiguration config = JaxbUtils.unmarshal(WsmanEventlogConfiguration.class, SHIPPED);
        assertEquals(4, config.getThreads());
        assertEquals(1, config.getRetries());
        assertEquals("5m", config.getTargetRefreshInterval());
        assertEquals(1, config.getPackages().size());

        final Package pkg = config.getPackages().get(0);
        assertEquals("windows-servers", pkg.getName());
        assertEquals("IPADDR != '0.0.0.0'", pkg.getFilter());
        assertEquals(3, pkg.getLogs().size());
        final Log system = pkg.getLogs().get(0);
        assertEquals("System", system.getName());
        assertTrue(system.isEnabled());
        assertEquals(60_000L, system.getInterval());
        assertEquals(500, system.getMaxRecords());
        assertEquals("1h", system.getLookback());
        assertEquals("Error,Warning", system.getLevels());
        assertFalse(pkg.getLogs().get(2).isEnabled());

        assertEquals(2, pkg.getEventMappings().size());
        assertEquals(6008, pkg.getEventMappings().get(0).getEventId());
        assertEquals("uei.opennms.org/wsman/eventlog/unexpectedShutdown", pkg.getEventMappings().get(0).getUei());
        assertEquals("Major", pkg.getEventMappings().get(0).getSeverity());
    }

    @Test
    public void defaultsApplyToAMinimalLog() {
        final WsmanEventlogConfiguration config = JaxbUtils.unmarshal(WsmanEventlogConfiguration.class,
                "<wsman-eventlog-configuration xmlns=\"http://xmlns.opennms.org/xsd/config/wsman-eventlog\">"
                + "<package name=\"p\"><filter>IPADDR != '0.0.0.0'</filter><log name=\"System\"/></package>"
                + "</wsman-eventlog-configuration>");
        final Log log = config.getPackages().get(0).getLogs().get(0);
        assertTrue(log.isEnabled());
        assertEquals(60_000L, log.getInterval());
        assertEquals(500, log.getMaxRecords());
        assertEquals("1h", log.getLookback());
        assertEquals(4, config.getThreads());
    }

    @Test
    public void parsesDurationsAndLevels() {
        assertEquals(90_000L, Durations.parse("90s").toMillis());
        assertEquals(15 * 60_000L, Durations.parse("15m").toMillis());
        assertEquals(3_600_000L, Durations.parse("1h").toMillis());
        assertEquals(2 * 86_400_000L, Durations.parse("2d").toMillis());
        assertEquals(500L, Durations.parse("500").toMillis());
        assertEquals(java.util.List.of(1, 2, 5), EventLogLevel.parseEventTypes("Error, Warning,AuditFailure"));
        assertTrue(EventLogLevel.parseEventTypes(null).isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsAnUnknownLevel() {
        EventLogLevel.parseEventTypes("Critical");
    }
}
