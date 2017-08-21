/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.icmp.commands;

import java.net.InetAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opennms.netmgt.icmp.PingConstants;
import org.opennms.netmgt.icmp.proxy.LocationAwarePingClient;
import org.opennms.netmgt.icmp.proxy.PingSweepSummary;

@Command(scope = "ping", name = "sweep", description = "Ping-Sweep")
public class PingSweepCommand extends OsgiCommandSupport {

    private LocationAwarePingClient locationAwarePingClient;

    @Option(name = "-l", aliases = "--location", description = "location")
    String m_location;

    @Option(name = "-s", aliases = "--system-id", description = "System ID")
    String m_systemId;

    @Option(name = "-r", aliases = "--retries", description = "number of retries")
    int m_retries = PingConstants.DEFAULT_RETRIES;

    @Option(name = "-t", aliases = "--timeout", description = "timeout in msec")
    int m_timeout = PingConstants.DEFAULT_TIMEOUT;

    @Option(name = "-p", aliases = "--packetsize", description = "packet size")
    int m_packetsize = PingConstants.DEFAULT_PACKET_SIZE;

    @Option(name = "-s", aliases = "--pps", description = "packer per second")
    double m_packetsPerSecond = PingConstants.DEFAULT_PACKETS_PER_SECOND;

    @Argument(index = 0, name = "begin", description = "begin address of the IP range to be pinged", required = true, multiValued = false)
    String m_begin;

    @Argument(index = 1, name = "end", description = "end address of the IP range to be pinged", required = true, multiValued = false)
    String m_end;

    @Override
    protected Object doExecute() throws Exception {
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

    public void setLocationAwarePingClient(LocationAwarePingClient locationAwarePingClient) {
        this.locationAwarePingClient = locationAwarePingClient;
    }

}
