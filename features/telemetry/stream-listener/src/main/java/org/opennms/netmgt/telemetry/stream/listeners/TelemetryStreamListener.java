/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.stream.listeners;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.events.api.EventSubscriptionService;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.telemetry.api.receiver.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;

public class TelemetryStreamListener implements EventListener, Listener {

    private static final Logger LOG = LoggerFactory.getLogger(TelemetryStreamListener.class);
    private final String name;

    private final StreamParser parser;

    private final MetricRegistry metrics;

    private final EventSubscriptionService eventSubscriptionService;

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(100);

    private Map<String, Connection> connectionsMap = new ConcurrentHashMap<>();

    public TelemetryStreamListener(final String name,
                                   final StreamParser parser,
                                   final EventSubscriptionService eventSubscriptionService,
                                   final MetricRegistry metrics) {
        this.name = Objects.requireNonNull(name);
        this.eventSubscriptionService = eventSubscriptionService;
        this.parser = Objects.requireNonNull(parser);
        this.metrics = Objects.requireNonNull(metrics);
    }


    @Override
    public String getName() {
        return name;
    }

    @Override
    public void onEvent(IEvent event) {
        if (event.getUei().equals(EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI)) {
            Integer nodeId = event.getNodeid() != null ? event.getNodeid().intValue() : null;
            String ipAddress = InetAddressUtils.toIpAddrString(event.getInterfaceAddress());
            // No need to block event consumer, run this asynchronously.
            scheduledExecutorService.execute(() -> {
                  connect(nodeId, ipAddress);
            });
        } else if (event.getUei().equals(EventConstants.INTERFACE_DELETED_EVENT_UEI)) {
            String ipAddress = InetAddressUtils.toIpAddrString(event.getInterfaceAddress());
            Integer nodeId = event.getNodeid() != null ? event.getNodeid().intValue() : null;
            closeConnection(nodeId, ipAddress);
        }
    }

    @Override
    public void start() throws InterruptedException {
        eventSubscriptionService.addEventListener(this);
        parser.start(scheduledExecutorService);
    }

    @Override
    public void stop() throws InterruptedException {
        parser.stop();
        scheduledExecutorService.shutdown();
    }

    private void connect(Integer nodeId, String ipAddress) {
        try (Connection connection = parser.connect(nodeId, ipAddress)) {
            connectionsMap.put(ipAddress, connection);
        } catch (Exception e) {
            LOG.error("Exception while connecting to stream connection for NodeId = {}, IpAddress {}", ipAddress, e);
        }
    }

    private void closeConnection(Integer nodeId, String ipAddress) {
        parser.closeConnection(nodeId, ipAddress);
    }

}
