/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

import java.io.IOException;
import java.util.Map;

import org.opennms.core.soa.lookup.ServiceLookup;
import org.opennms.core.soa.lookup.ServiceLookupBuilder;
import org.opennms.core.soa.lookup.ServiceRegistryLookup;
import org.opennms.core.soa.support.DefaultServiceRegistry;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.TimeoutTracker;
import org.opennms.features.dhcpd.Dhcpd;
import org.opennms.features.dhcpd.Transaction;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.support.AbstractServiceMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Distributable(DistributionContext.DAEMON)
public final class DhcpMonitor extends AbstractServiceMonitor {
    public static final int DEFAULT_RETRIES = 0;
    public static final int DEFAULT_TIMEOUT = 3000;
    public static final String DEFAULT_MAC_ADDRESS = "00:06:0D:BE:9C:B2";
    private static final Logger LOG = LoggerFactory.getLogger(DhcpMonitor.class);

    private final ServiceLookup<Class<?>, String> SERVICE_LOOKUP = new ServiceLookupBuilder(new ServiceRegistryLookup(DefaultServiceRegistry.INSTANCE))
            .blocking()
            .build();

    private Dhcpd dhcpd;

    public void setDhcpd(final Dhcpd dhcpd) {
        this.dhcpd = dhcpd;
    }

    @Override
    public PollStatus poll(final MonitoredService svc, final Map<String, Object> parameters) {
        if (dhcpd == null) {
            dhcpd = SERVICE_LOOKUP.lookup(Dhcpd.class, null);
        }

        // common parameters
        final int retries = ParameterMap.getKeyedInteger(parameters, "retry", DEFAULT_RETRIES);
        final int timeout = ParameterMap.getKeyedInteger(parameters, "timeout", DEFAULT_TIMEOUT);

        // DHCP-specific parameters
        final String macAddress = ParameterMap.getKeyedString(parameters, "macAddress", DEFAULT_MAC_ADDRESS);
        final boolean relayMode = ParameterMap.getKeyedBoolean(parameters, "relayMode", false);
        final boolean extendedMode = ParameterMap.getKeyedBoolean(parameters, "extendedMode", false);
        final String myAddress = ParameterMap.getKeyedString(parameters, "myIpAddress", "127.0.0.1");
        final String requestIpAddress = ParameterMap.getKeyedString(parameters, "requestIpAddress", "127.0.0.1");

        final TimeoutTracker tracker = new TimeoutTracker(parameters, retries, timeout);

        Transaction transaction = null;

        tracker.reset();

        while (tracker.shouldRetry() && (transaction == null || !transaction.isSuccess())) {
            try {
                transaction = dhcpd.executeTransaction(svc.getIpAddr(), macAddress, relayMode, myAddress, extendedMode, requestIpAddress, timeout);
            } catch (IOException e) {
                LOG.error("An unexpected exception occurred during DHCP polling", e);
                return PollStatus.unavailable("An unexpected exception occurred during DHCP polling: " + e.getMessage());
            }
            tracker.nextAttempt();
        }

        return transaction.isSuccess() ?
                PollStatus.available((double) transaction.getResponseTime()) :
                PollStatus.unavailable("DHCP service unavailable: " +
                        "No response received from " + svc.getIpAddr() + " within " + timeout + " and " + retries + " attempt(s). " +
                        "Relay Mode: " + relayMode + ", Extended Mode " + extendedMode + ", Relay IP address: " + myAddress +
                        ", Request IP address: " + requestIpAddress + ", MAC address: " + macAddress + ". ");
    }
}
