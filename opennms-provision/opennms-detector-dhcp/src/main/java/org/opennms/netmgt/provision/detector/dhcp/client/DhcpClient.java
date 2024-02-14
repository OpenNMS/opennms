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

        if (transaction == null) {
            return new DhcpResponse(-1);
        }
        return new DhcpResponse(transaction.getResponseTime());
    }

    @Override
    public DhcpResponse sendRequest(final DhcpRequest request) throws IOException, Exception {
        return null;
    }
}
