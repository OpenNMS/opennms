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
package org.opennms.netmgt.vmmgr;

import static org.opennms.netmgt.events.api.EventConstants.OPENNMS_STARTED_UEI;

import java.lang.management.ManagementFactory;

import org.opennms.netmgt.events.api.EventIpcManagerFactory;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds and sends the event that marks OpenNMS finishing its startup.
 */
final class LifecycleEvents {
    private static final Logger LOG = LoggerFactory.getLogger(LifecycleEvents.class);

    static final String SOURCE = "OpenNMS.Manager";

    static final String PARM_VERSION = "version";
    static final String PARM_STARTUP_SECONDS = "startupSeconds";

    private LifecycleEvents() {
    }

    static Event startedEvent(final String version, final long startupMillis) {
        return new EventBuilder(OPENNMS_STARTED_UEI, SOURCE)
                .addParam(PARM_VERSION, version)
                .addParam(PARM_STARTUP_SECONDS, Math.round(startupMillis / 1000d))
                .getEvent();
    }

    static void sendStarted() {
        try {
            final Event event = startedEvent(version(), jvmUptimeMillis());
            EventIpcManagerFactory.getIpcManager().sendNow(event);
        } catch (RuntimeException e) {
            LOG.warn("Unable to send the {} event", OPENNMS_STARTED_UEI, e);
        }
    }

    private static String version() {
        return System.getProperty("version.display", "unknown");
    }

    private static long jvmUptimeMillis() {
        return ManagementFactory.getRuntimeMXBean().getUptime();
    }
}
