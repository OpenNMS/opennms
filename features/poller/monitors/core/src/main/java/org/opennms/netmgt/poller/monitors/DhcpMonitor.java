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
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.support.AbstractServiceMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        return (transaction != null && transaction.isSuccess()) ?
                PollStatus.available((double) transaction.getResponseTime()) :
                PollStatus.unavailable("DHCP service unavailable: " +
                        "No response received from " + svc.getIpAddr() + " within " + timeout + " and " + retries + " attempt(s). " +
                        "Relay Mode: " + relayMode + ", Extended Mode " + extendedMode + ", Relay IP address: " + myAddress +
                        ", Request IP address: " + requestIpAddress + ", MAC address: " + macAddress + ". ");
    }
}
