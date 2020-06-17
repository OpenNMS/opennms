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
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.opennms.features.openconfig.api.Config;
import org.opennms.features.openconfig.api.TelemetryClient;
import org.opennms.features.openconfig.api.TelemetryClientFactory;
import org.opennms.netmgt.telemetry.api.receiver.StreamListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Strings;

import io.netty.buffer.Unpooled;
import io.netty.util.internal.SocketUtils;

public class TelemetryStreamListener implements StreamListener {

    private static final Logger LOG = LoggerFactory.getLogger(TelemetryStreamListener.class);

    private static final int DEFAULT_PORT = 50051;
    private static final int DEFAULT_FREQUENCY = 300000; //5minutes
    private final String name;

    private final UdpParser parser;

    private final MetricRegistry metrics;

    private final TelemetryClientFactory telemetryClientFactory;

    private Map<String, TelemetryClient> telemetryClientMap = new ConcurrentHashMap<>();

    public TelemetryStreamListener(final String name,
                                   final List<UdpParser> parsers,
                                   final MetricRegistry metrics,
                                   final TelemetryClientFactory clientFactory) {
        this.name = Objects.requireNonNull(name);
        Objects.requireNonNull(parsers);
        this.metrics = Objects.requireNonNull(metrics);
        this.telemetryClientFactory = clientFactory;

        if (parsers.isEmpty()) {
            throw new IllegalArgumentException("At least 1 parsers must be defined");
        }

        if (parsers.size() > 1) {
            throw new IllegalArgumentException("Forward parser is only expected once");
        }
        parser = parsers.get(0);
    }


    @Override
    public String getName() {
        return name;
    }

    @Override
    public void start() throws InterruptedException {
    }

    @Override
    public void stop() throws InterruptedException {
        telemetryClientMap.forEach((ipAddress, telemetryClient) -> {
            telemetryClient.stop();
        });
    }

    public void subscribe(Integer nodeId, String ipAddress, Map<String, String> config) {
        // Since this will be invoked in event consumer thread, invoke it in different thread to not block event consumer thread.
        Executors.newSingleThreadExecutor().execute(() -> createAndSubscribeToTelemetryClient(nodeId, ipAddress, config));
    }

    private void createAndSubscribeToTelemetryClient(Integer nodeId, String ipAddress, Map<String, String> config) {
        Integer port = getInteger(config.get("port"), DEFAULT_PORT);
        Integer frequency = getInteger(config.get("frequency"), DEFAULT_FREQUENCY);
        String pathString = config.get("paths");
        String mode = config.get("mode");
        List<String> paths = pathString != null ? Arrays.asList(pathString.split(",", -1)) : new ArrayList<>();
        Map<String, String> tlsFilePaths = config.entrySet().stream()
                .filter(configuration -> configuration.getKey().contains("tls"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        InetSocketAddress remoteAddress = SocketUtils.socketAddress(ipAddress, port);

        try {
            TelemetryClient telemetryClient = telemetryClientFactory.createClient(ipAddress,
                    port,
                    tlsFilePaths);
            Config streamConfig = new Config(paths, frequency);
            boolean subscribed = telemetryClient.subscribe(streamConfig, (response) -> forwardToParser(response, remoteAddress));
            if (subscribed) {
                telemetryClientMap.put(ipAddress, telemetryClient);
            } else {
                telemetryClient.stop();
            }
        } catch (IOException e) {
            LOG.info("Couldn't get connection to telemetry stream", e);
            return;
        }
    }

    public void unsubscribe(String ipAddress) {
        if (!Strings.isNullOrEmpty(ipAddress)) {
            TelemetryClient telemetryClient = telemetryClientMap.get(ipAddress);
            if (telemetryClient != null) {
                telemetryClient.stop();
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

    private void forwardToParser(byte[] response, InetSocketAddress remoteAddress) {
        try {
            parser.parse(Unpooled.wrappedBuffer(response), remoteAddress, null);
        } catch (Exception e) {
            LOG.warn("Exception while parsing telemetry data at address = {}", remoteAddress);
        }
    }

}
