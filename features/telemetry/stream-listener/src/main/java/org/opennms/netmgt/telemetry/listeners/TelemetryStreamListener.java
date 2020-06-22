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

package org.opennms.netmgt.telemetry.listeners;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.features.openconfig.api.Handler;
import org.opennms.features.openconfig.api.HostConfig;
import org.opennms.features.openconfig.api.OpenConfigClient;
import org.opennms.features.openconfig.api.OpenConfigClientFactory;
import org.opennms.features.openconfig.api.StreamConfig;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.events.api.EventSubscriptionService;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.telemetry.api.receiver.StreamListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Strings;

import io.netty.buffer.Unpooled;
import io.netty.util.internal.SocketUtils;

public class TelemetryStreamListener implements StreamListener, EventListener {

    private static final Logger LOG = LoggerFactory.getLogger(TelemetryStreamListener.class);

    private static final int DEFAULT_PORT = 50051;
    private static final int DEFAULT_FREQUENCY = 300000; //5minutes
    private final String name;

    private final StreamParser parser;

    private final MetricRegistry metrics;

    private final OpenConfigClientFactory openConfigClientFactory;

    private final EventSubscriptionService eventSubscriptionService;

    private Map<String, OpenConfigClient> ocClientByIpAddress = new ConcurrentHashMap<>();

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1000);

    public TelemetryStreamListener(final String name,
                                   final StreamParser parser,
                                   final OpenConfigClientFactory clientFactory,
                                   final EventSubscriptionService eventSubscriptionService,
                                   final MetricRegistry metrics) {
        this.name = Objects.requireNonNull(name);
        this.openConfigClientFactory = clientFactory;
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
            Integer nodeId = event.getNodeid().intValue();
            String ipAddress = InetAddressUtils.toIpAddrString(event.getInterfaceAddress());
            subscribe(nodeId, ipAddress);
        } else if (event.getUei().equals(EventConstants.INTERFACE_DELETED_EVENT_UEI)) {
            String ipAddress = InetAddressUtils.toIpAddrString(event.getInterfaceAddress());
            unsubscribe(ipAddress);
        }
    }

    @Override
    public void start() throws InterruptedException {
        eventSubscriptionService.addEventListener(this);
        List<Config> matchingConfig = parser.getMatchingConfig();
        matchingConfig.forEach(this::subscribeToTelemetry);
    }

    @Override
    public void stop() throws InterruptedException {
        ocClientByIpAddress.forEach((ipAddress, openConfigClient) -> {
            openConfigClient.close();
        });
    }

    private void subscribe(Integer nodeId, String ipAddress) {
        // Since this will be invoked in event consumer thread, invoke it in different thread to not block event consumer thread.
        Executors.newSingleThreadExecutor().execute(() -> {
            Config config = parser.getConfig(nodeId, ipAddress);
            subscribeToTelemetry(config);
        });
    }

    private boolean subscribeToTelemetry(Config config) {

        if(!config.getParams().isEmpty()) {
            Integer port = getInteger(config.getParams().get("port"), DEFAULT_PORT);
            Integer frequency = getInteger(config.getParams().get("frequency"), DEFAULT_FREQUENCY);
            String pathString = config.getParams().get("paths");
            String mode = config.getParams().get("mode");
            List<String> paths = pathString != null ? Arrays.asList(pathString.split(",", -1)) : new ArrayList<>();
            Map<String, String> tlsFilePaths = config.getParams().entrySet().stream()
                    .filter(configuration -> configuration.getKey().contains("tls"))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            try {
                HostConfig hostConfig = new HostConfig(config.getIpAddress(), port, tlsFilePaths);
                OpenConfigClient openConfigClient = openConfigClientFactory.createClient(hostConfig);
                StreamConfig streamConfig = new StreamConfig(paths, frequency, mode);
                boolean succeeded = openConfigClient.subscribe(streamConfig, new StreamHandler(config, port));
                if (succeeded) {
                    ocClientByIpAddress.put(config.getIpAddress(), openConfigClient);
                    return true;
                } else {
                    openConfigClient.close();
                }
            } catch (IOException e) {
                LOG.info("Couldn't get connection to telemetry stream", e);
            }
        }
        return false;
    }

    private void unsubscribe(String ipAddress) {
        if (!Strings.isNullOrEmpty(ipAddress)) {
            OpenConfigClient openConfigClient = ocClientByIpAddress.get(ipAddress);
            if (openConfigClient != null) {
                openConfigClient.close();
            }
        }
    }

    private static Integer getInteger(String value, Integer defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private void scheduleSubscription(Config config) {
        ScheduledFuture<Boolean> future = scheduledExecutorService.schedule(() -> subscribeToTelemetry(config), 300, TimeUnit.SECONDS);
        try {
            boolean succeeded = future.get();
            if(succeeded) {
                return;
            }
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Exception while subscribing to openconfig telemetry at {}", config.getIpAddress(), e);
        }
        scheduleSubscription(config);
    }

    private class StreamHandler implements Handler {

        private final Config config;
        private final InetSocketAddress remoteAddress;

        public StreamHandler(Config config, Integer port) {
            this.config = config;
            this.remoteAddress = SocketUtils.socketAddress(config.getIpAddress(), port);
        }

        @Override
        public void accept(byte[] response) {
            try {
                parser.parse(Unpooled.wrappedBuffer(response), remoteAddress);
            } catch (Exception e) {
                LOG.warn("Exception while parsing telemetry data at address = {}", remoteAddress);
            }
        }

        @Override
        public void onError(String error) {
            OpenConfigClient openConfigClient = ocClientByIpAddress.remove(config.getIpAddress());
            if (openConfigClient != null) {
                openConfigClient.close();
            }
            scheduleSubscription(config);
        }
    }

}
