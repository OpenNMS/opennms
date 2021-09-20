/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.detector.dhcp.client;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collections;

import org.opennms.core.utils.TimeoutTracker;

import org.opennms.features.dhcpd.Dhcpd;
import org.opennms.features.dhcpd.Transaction;
import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.detector.dhcp.request.DhcpRequest;
import org.opennms.netmgt.provision.detector.dhcp.response.DhcpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DhcpClient implements Client<DhcpRequest, DhcpResponse> {
    private static final Logger LOG = LoggerFactory.getLogger(DhcpClient.class);

    private int retries;
    private int timeout;
    private InetAddress address;
    private final String macAddress;
    private final String myIpAddress;
    private final boolean extendedMode;
    private final boolean relayMode;
    private final String requestIpAddress;

    private Dhcpd dhcpd;

    public DhcpClient(final String macAddress, final boolean relayMode, final String myIpAddress, final boolean extendedMode, final String requestIpAddress, final int timeout, final int retries, final Dhcpd dhcpd) {
        this.macAddress = macAddress;
        this.relayMode = relayMode;
        this.myIpAddress = myIpAddress;
        this.extendedMode = extendedMode;
        this.requestIpAddress = requestIpAddress;
        this.timeout = timeout;
        this.retries = retries;
        this.dhcpd = dhcpd;
    }

    @Override
    public void close() {
    }

    @Override
    public void connect(final InetAddress address, final int port, final int timeout) {
        this.address = address;
        this.timeout = timeout;
    }

    @Override
    public DhcpResponse receiveBanner() {
        final TimeoutTracker tracker = new TimeoutTracker(Collections.emptyMap(), retries, timeout);
        Transaction transaction = null;

        tracker.reset();

        while (tracker.shouldRetry() && (transaction == null || !transaction.isSuccess())) {
            try {
                LOG.error("Checking for Dhcp: {}", transaction);
                transaction = dhcpd.executeTransaction(address.getHostAddress(), macAddress, relayMode, myIpAddress, extendedMode, requestIpAddress, timeout);
            } catch (IOException e) {
                LOG.error("An unexpected exception occurred during DHCP detection", e);
                return new DhcpResponse(-1);
            }
            tracker.nextAttempt();
        }

        return new DhcpResponse(transaction.getResponseTime());
    }

    @Override
    public DhcpResponse sendRequest(final DhcpRequest request) throws IOException, Exception {
        return null;
    }
}
