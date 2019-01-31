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

    private int m_retries;
    private int m_timeout;
    private InetAddress m_address;
    private final String m_macAddress;
    private final String m_myIpAddress;
    private final boolean m_extendedMode;
    private final boolean m_relayMode;
    private final String m_requestIpAddress;

    private Dhcpd m_dhcpd;

    public DhcpClient(final String macAddress, final boolean relayMode, final String myIpAddress, final boolean extendedMode, final String requestIpAddress, final int timeout, final int retries, final Dhcpd dhcpd) {
        this.m_macAddress = macAddress;
        this.m_relayMode = relayMode;
        this.m_myIpAddress = myIpAddress;
        this.m_extendedMode = extendedMode;
        this.m_requestIpAddress = requestIpAddress;
        this.m_timeout = timeout;
        this.m_retries = retries;
        this.m_dhcpd = dhcpd;
    }

    @Override
    public void close() {
    }

    @Override
    public void connect(InetAddress address, int port, int timeout) {
        m_address = address;
        m_timeout = timeout;
    }

    @Override
    public DhcpResponse receiveBanner() {
        final TimeoutTracker tracker = new TimeoutTracker(Collections.emptyMap(), m_retries, m_timeout);
        final Transaction transaction = new Transaction(m_address.getHostAddress(), m_macAddress, m_relayMode, m_myIpAddress, m_extendedMode, m_requestIpAddress, m_timeout);
        for (tracker.reset(); tracker.shouldRetry() && !transaction.isSuccess(); tracker.nextAttempt()) {
            try {
                LOG.error("Checking for Dhcp: {}", transaction);
                m_dhcpd.addTransaction(transaction);
            } catch (IOException e) {
                LOG.error("An unexpected exception occurred during DHCP detection", e);
                return new DhcpResponse(-1);
            }
        }

        return new DhcpResponse(transaction.getResponseTime());
    }

    @Override
    public DhcpResponse sendRequest(DhcpRequest request) throws IOException, Exception {
        return null;
    }
}
