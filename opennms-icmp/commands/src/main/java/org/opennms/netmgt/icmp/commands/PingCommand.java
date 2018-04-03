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

@Command(scope = "ping", name = "ping", description = "Ping")
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
    public Object execute() throws Exception {
        LOG.debug("ping:ping {} {}", m_location != null ? "-l " + m_location : "", m_host);
        final InetAddress byName = InetAddress.getByName(m_host);
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
            }
            System.out.print(".");
        }
        return null;
    }
}