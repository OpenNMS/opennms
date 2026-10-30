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
import static org.opennms.netmgt.events.api.EventConstants.OPENNMS_STOPPING_UEI;

import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.sysprops.SystemProperties;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventIpcManagerFactory;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds and sends the events that mark OpenNMS finishing its startup and beginning its shutdown.
 */
final class LifecycleEvents {
    private static final Logger LOG = LoggerFactory.getLogger(LifecycleEvents.class);

    static final String SOURCE = "OpenNMS.Manager";

    static final String PARM_VERSION = "version";
    static final String PARM_STARTUP_SECONDS = "startupSeconds";
    static final String PARM_PREVIOUS_SHUTDOWN = "previousShutdown";
    static final String PARM_UPTIME_SECONDS = "uptimeSeconds";
    static final String PARM_REASON = "reason";

    static final String CLEAN = "clean";
    static final String UNCLEAN = "unclean";
    static final String UNKNOWN = "unknown";

    static final String REASON_SIGNAL = "shutdown signal";
    static final String REASON_REQUESTED = "JMX stop request";
    static final String REASON_STARTUP_FAILED = "service startup failed";

    static final long STOPPING_TIMEOUT_MS = SystemProperties.getLong("org.opennms.lifecycle.stoppingEventTimeoutMs", 5_000L);

    private static final String LAST_LIFECYCLE_EVENT =
            "SELECT eventuei FROM events WHERE eventuei IN (?, ?) ORDER BY eventtime DESC, eventid DESC LIMIT 1";

    private LifecycleEvents() {
    }

    /**
     * How the previous run ended, judged by the last lifecycle event it stored: a stopping event means
     * it shut down cleanly, a started event without one after it means it did not.
     */
    static String previousShutdown(final DataSource dataSource) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(LAST_LIFECYCLE_EVENT)) {
            statement.setString(1, OPENNMS_STARTED_UEI);
            statement.setString(2, OPENNMS_STOPPING_UEI);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return UNKNOWN;
                }
                return OPENNMS_STOPPING_UEI.equals(resultSet.getString(1)) ? CLEAN : UNCLEAN;
            }
        } catch (SQLException e) {
            LOG.warn("Unable to determine how the previous run ended", e);
            return UNKNOWN;
        }
    }

    static Event startedEvent(final String version, final long startupMillis, final String previousShutdown) {
        return new EventBuilder(OPENNMS_STARTED_UEI, SOURCE)
                .addParam(PARM_VERSION, version)
                .addParam(PARM_STARTUP_SECONDS, Math.round(startupMillis / 1000d))
                .addParam(PARM_PREVIOUS_SHUTDOWN, previousShutdown)
                .getEvent();
    }

    static Event stoppingEvent(final String version, final long uptimeMillis, final String reason) {
        return new EventBuilder(OPENNMS_STOPPING_UEI, SOURCE)
                .addParam(PARM_VERSION, version)
                .addParam(PARM_UPTIME_SECONDS, Math.round(uptimeMillis / 1000d))
                .addParam(PARM_REASON, reason)
                .getEvent();
    }

    static void sendStarted() {
        try {
            final Event event = startedEvent(version(), jvmUptimeMillis(), previousShutdown(DataSourceFactory.getInstance()));
            EventIpcManagerFactory.getIpcManager().sendNow(event);
        } catch (RuntimeException e) {
            LOG.warn("Unable to send the {} event", OPENNMS_STARTED_UEI, e);
        }
    }

    /**
     * Sent synchronously so the event is stored before Eventd itself is stopped; the next start reads it
     * to tell a clean shutdown from a crash.
     */
    static void sendStopping(final String reason) {
        final EventIpcManager eventIpcManager;
        try {
            eventIpcManager = EventIpcManagerFactory.getIpcManager();
        } catch (IllegalStateException e) {
            // Eventd never started, e.g. an HA standby stopping before promotion
            LOG.debug("Eventd is not running; not sending the {} event", OPENNMS_STOPPING_UEI);
            return;
        }
        sendStopping(eventIpcManager, stoppingEvent(version(), jvmUptimeMillis(), reason), STOPPING_TIMEOUT_MS);
    }

    /**
     * Waits at most {@code timeoutMillis} for the event to be stored; with the database down the write could
     * block on the connection pool and hold up the shutdown until the service manager kills the process.
     */
    static boolean sendStopping(final EventIpcManager eventIpcManager, final Event event, final long timeoutMillis) {
        final Thread sender = new Thread(() -> {
            try {
                eventIpcManager.sendNowSync(event);
            } catch (RuntimeException e) {
                LOG.warn("Unable to send the {} event", OPENNMS_STOPPING_UEI, e);
            }
        }, "lifecycle-stopping-event");
        sender.setDaemon(true);
        sender.start();
        try {
            sender.join(timeoutMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (sender.isAlive()) {
            LOG.warn("The {} event was not stored within {} ms; continuing the shutdown without it", OPENNMS_STOPPING_UEI, timeoutMillis);
            return false;
        }
        return true;
    }

    private static String version() {
        return System.getProperty("version.display", "unknown");
    }

    private static long jvmUptimeMillis() {
        return ManagementFactory.getRuntimeMXBean().getUptime();
    }
}
