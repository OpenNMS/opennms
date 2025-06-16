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
package org.opennms.netmgt.icmp.commands;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.icmp.proxy.LocationAwarePingClient;
import org.opennms.netmgt.icmp.proxy.PingRequestBuilder;
import org.opennms.netmgt.icmp.proxy.PingStringUtils;
import org.opennms.netmgt.icmp.proxy.PingSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "opennms", name = "ping", description = "ICMP Ping")
@Service
public class PingCommand implements Action {

    private static final Logger LOG = LoggerFactory.getLogger(PingCommand.class);

    @Reference
    public LocationAwarePingClient locationAwarePingClient;

    @Option(name = "-l", aliases = "--location", description = "Location", required = false, multiValued = false)
    String m_location;

    @Option(name = "-s", aliases = "--system-id", description = "System ID")
    String m_systemId;

    @Option (name="-c", aliases = "--count", description="Number of requests")
    int m_count = 1;

    @Argument(index = 0, name = "host", description = "Hostname or IP Address of the system to walk", required = true, multiValued = false)
    String m_host;

    @Override
    public Object execute() {
        LOG.debug("opennms:ping {} {}", m_location != null ? "-l " + m_location : "", m_host);
        InetAddress byName;
        try {
            byName = InetAddress.getByName(m_host);
        } catch (UnknownHostException uhe) {
            System.out.printf("PING %s: Unknown host%n", uhe);
            return null;
        }
        final PingRequestBuilder.Callback callback = (newSequence, summary) -> {
            if (m_count > 1) {
                if (summary.getSequences().size() == 0) {
                    System.out.println(PingStringUtils.renderHeader(summary));
                }
                if (newSequence != null) {
                    System.out.println(PingStringUtils.renderSequence(summary.getRequest(), newSequence));
                }
                if (summary.isComplete()) {
                    System.out.println(PingStringUtils.renderSummary(summary));
                }
            }
        };

        final CompletableFuture<PingSummary> future = locationAwarePingClient.ping(byName)
            .withLocation(m_location)
            .withSystemId(m_systemId)
            .withNumberOfRequests(m_count)
            .withProgressCallback(callback).execute();

        while (true) {
            try {
                PingSummary summary = future.get(1, TimeUnit.SECONDS);
                // If count is one, we do not wrap the execution in another Thead, therefore the "onCompletion" is
                // executed by the Pinger-Thread which output is piped to "output.log" instead of "karaf.log" resulting
                // in an empty return. Therefore we print manually.
                // In case of m_count > 1, the callback takes care of the "karaf.log" output.
                if (m_count == 1) {
                    System.out.println(String.format("PING: %s %.3f ms", byName, summary.getSequence(0).getResponse().getRtt()));
                }
                break;
            } catch (TimeoutException e) {
                // pass
            } catch (InterruptedException | ExecutionException e) {
                if (m_count == 1) {
                    System.out.println(String.format("PING: %s %s", byName, e.getCause().getClass().getName()));
                }
                break;
            }
            System.out.print(".");
        }
        return null;
    }
}