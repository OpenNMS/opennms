/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2025 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2025 The OpenNMS Group, Inc.
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
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.poller.monitors;

import com.google.common.base.Strings;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.TimeoutTracker;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.snmp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.Map;
import java.util.TreeMap;

public class PtpMonitor extends SnmpMonitorStrategy {
    private static final Logger LOG = LoggerFactory.getLogger(PtpMonitor.class);

    private final SnmpObjId CLOCKPORT_OID = SnmpObjId.get(".1.3.6.1.2.1.241.1.2.9.1.5.90");
    private final SnmpObjId PORTSTATE_OID = SnmpObjId.get(".1.3.6.1.2.1.241.1.2.9.1.6.90");

    private static final int DEFAULT_RETRY = 0;
    public static final int DEFAULT_TIMEOUT = 3000;

    private enum State {
        initializing(1),
        faulty(2),
        disabled(3),
        listening(4),
        preMaster(5),
        master(6),
        passive(7),
        uncalibrated(8),
        slave(9);

        private final int value;

        State(final int value) {
            this.value = value;
        }

        public static State get(final int value) {
            for (final State state : State.values()) {
                if (value == state.value) {
                    return state;
                }
            }
            return null;
        }
    }

    @Override
    public PollStatus poll(final MonitoredService svc, final Map<String, Object> parameters) {
        final TimeoutTracker tracker = new TimeoutTracker(parameters, DEFAULT_RETRY, DEFAULT_TIMEOUT);
        PollStatus ps = PollStatus.unavailable();

        final InetAddress inetAddress = svc.getAddress();
        final SnmpAgentConfig snmpAgentConfig = getAgentConfig(svc, parameters);
        final String hostAddress = InetAddressUtils.str(inetAddress);

        final String watchedClockPort = ParameterMap.getKeyedString(parameters, "clock-port", "");
        final String desiredPortStateString = ParameterMap.getKeyedString(parameters, "port-state", "").toLowerCase();

        if (Strings.isNullOrEmpty(watchedClockPort)) {
            LOG.debug("Missing required parameter: clock-port");
            return PollStatus.unknown("Missing required parameter: clock-port");
        }

        if (Strings.isNullOrEmpty(desiredPortStateString)) {
            LOG.debug("Missing required parameter: port-state");
            return PollStatus.unknown("Missing required parameter: port-state");
        }

        final State desiredPortState;

        try {
            desiredPortState = State.valueOf(desiredPortStateString);
        } catch (final IllegalArgumentException e) {
            LOG.debug("Unknown port state: " + desiredPortStateString, e);
            return PollStatus.unknown("Unknown port state: " + desiredPortStateString);
        }

        for (tracker.reset(); tracker.shouldRetry() && !ps.isAvailable(); tracker.nextAttempt()) {

            tracker.startAttempt();

            final Map<String, State> portStates = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            final RowCallback rowCallback = new RowCallback() {
                @Override
                public void rowCompleted(final SnmpRowResult result) {
                    final SnmpValue clockPort = result.getValue(CLOCKPORT_OID);
                    final SnmpValue portState = result.getValue(PORTSTATE_OID);

                    if (clockPort != null && portState != null) {
                        portStates.put(clockPort.toDisplayString(), State.get(portState.toInt()));
                    }
                }
            };

            final TableTracker tableTracker = new TableTracker(rowCallback, CLOCKPORT_OID, PORTSTATE_OID);

            try {
                try (final SnmpWalker snmpWalker = SnmpUtils.createWalker(snmpAgentConfig, "ptpPortState", tableTracker)) {
                    snmpWalker.start();
                    snmpWalker.waitFor();
                    final String errorMessage = snmpWalker.getErrorMessage();
                    if (errorMessage != null && !errorMessage.trim().isEmpty()) {
                        ps = PollStatus.unavailable(errorMessage);
                        continue;
                    }
                }

                if (!portStates.containsKey(watchedClockPort)) {
                    ps = PollStatus.unknown("Unknown clock-port: " + watchedClockPort);
                    continue;
                }

                final State reportedPortState = portStates.get(watchedClockPort);

                if (reportedPortState == null) {
                    ps = PollStatus.unknown("Cannot determine port-state for given clock-port");
                    continue;
                }

                if (reportedPortState.equals(desiredPortState)) {
                    return PollStatus.up(tracker.elapsedTimeInMillis());
                } else {
                    ps = PollStatus.down("State '" + reportedPortState + "' for clock-port " + watchedClockPort + " does not match state '" + desiredPortState + "'");
                }
            } catch (final Throwable t) {
                final String reason = "Unexpected exception during SNMP poll of interface " + hostAddress;
                LOG.debug(reason, t);
                ps = PollStatus.down(reason);
            }
        }

        return ps;
    }
}
