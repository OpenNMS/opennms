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
import org.opennms.netmgt.icmp.PingConstants;
import org.opennms.netmgt.icmp.proxy.LocationAwarePingClient;
import org.opennms.netmgt.icmp.proxy.PingSweepSummary;

@Command(scope = "opennms", name = "ping-sweep", description = "ICMP Ping Sweep")
@Service
public class PingSweepCommand implements Action {

    @Reference
    public LocationAwarePingClient locationAwarePingClient;

    @Option(name = "-l", aliases = "--location", description = "Location")
    String m_location;

    @Option(name = "-s", aliases = "--system-id", description = "System ID")
    String m_systemId;

    @Option(name = "-r", aliases = "--retries", description = "Number of retries")
    int m_retries = PingConstants.DEFAULT_RETRIES;

    @Option(name = "-t", aliases = "--timeout", description = "Timeout in milliseconds")
    int m_timeout = PingConstants.DEFAULT_TIMEOUT;

    @Option(name = "-p", aliases = "--packetsize", description = "Packet size")
    int m_packetsize = PingConstants.DEFAULT_PACKET_SIZE;

    @Option(name = "--pps", description = "packets per second")
    double m_packetsPerSecond = PingConstants.DEFAULT_PACKETS_PER_SECOND;

    @Argument(index = 0, name = "begin", description = "First address of the IP range to be pinged", required = true, multiValued = false)
    String m_begin;

    @Argument(index = 1, name = "end", description = "Last address of the IP range to be pinged", required = true, multiValued = false)
    String m_end;

    @Override
    public Object execute() throws Exception {
        final InetAddress begin = InetAddress.getByName(m_begin);
        final InetAddress end = InetAddress.getByName(m_end);

        System.out.printf("Pinging hosts from %s to %s with:\n", begin.getHostAddress(), end.getHostAddress());
        if (m_location != null) {
            System.out.printf("\tLocation: %s\n", m_location);
        }
        System.out.printf("\tRetries: %d\n", m_retries);
        System.out.printf("\tTimeout: %d\n", m_timeout);
        System.out.printf("\tPacket size: %d\n", m_packetsize);
        System.out.printf("\tPackets per second: %f\n", m_packetsPerSecond);

        final CompletableFuture<PingSweepSummary> future = locationAwarePingClient.sweep()
                .withLocation(m_location)
                .withSystemId(m_systemId)
                .withRange(begin, end, m_retries, m_timeout, TimeUnit.MILLISECONDS)
                .withPacketSize(m_packetsize)
                .withPacketsPerSecond(m_packetsPerSecond)
                .execute();

        while (true) {
            try {
                try {
                    PingSweepSummary summary = future.get(1, TimeUnit.SECONDS);
                    if (summary.getResponses().isEmpty()) {
                        System.out.printf("\n\nNone of the IP addresses responsed to our pings.\n");
                    } else {
                        System.out.printf("\n\nIP Address\tRound-trip time\n");
                        summary.getResponses().forEach((address, rtt) -> {
                            System.out.printf("%s\t%.3f ms\n", address.getHostAddress(), rtt);
                        });
                    }
                } catch (InterruptedException e) {
                    System.out.println("\n\nInterrupted.");
                } catch (ExecutionException e) {
                    System.out.printf("\n\nPing Sweep failed with: %s\n", e);
                }
                break;
            } catch (TimeoutException e) {
                // pass
            }
            System.out.print(".");
        }
        return null;
    }
}
