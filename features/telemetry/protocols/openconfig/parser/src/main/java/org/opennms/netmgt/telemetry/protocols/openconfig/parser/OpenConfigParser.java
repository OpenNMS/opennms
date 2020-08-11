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

package org.opennms.netmgt.telemetry.protocols.openconfig.parser;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.core.rpc.utils.mate.ContextKey;
import org.opennms.core.rpc.utils.mate.EntityScopeProvider;
import org.opennms.core.rpc.utils.mate.FallbackScope;
import org.opennms.core.rpc.utils.mate.Interpolator;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.telemetry.api.receiver.TelemetryMessage;
import org.opennms.netmgt.telemetry.stream.listeners.Config;
import org.opennms.netmgt.telemetry.stream.listeners.Connection;
import org.opennms.netmgt.telemetry.stream.listeners.ConnectionFactory;
import org.opennms.netmgt.telemetry.stream.listeners.StreamParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.internal.SocketUtils;

public class OpenConfigParser implements StreamParser {

    private static final Logger LOG = LoggerFactory.getLogger(OpenConfigParser.class);
    private static final String PARAM_ENABLED = "enabled";
    private final String name;
    private final AsyncDispatcher<TelemetryMessage> dispatcher;
    private final EntityScopeProvider entityScopeProvider;
    private final Map<String, String> parameters;
    private final IpInterfaceDao ipInterfaceDao;
    private final ConnectionFactory connectionFactory;
    private ScheduledExecutorService scheduledExecutorService;
    private Map<NodeIdAndIpAddressKey, Connection> connectionMap = new ConcurrentHashMap<>();

    public OpenConfigParser(String name, Map<String, String> parameters,
                            AsyncDispatcher<TelemetryMessage> dispatcher,
                            EntityScopeProvider entityScopeProvider,
                            IpInterfaceDao ipInterfaceDao,
                            ConnectionFactory connectionFactory) {
        this.name = name;
        this.parameters = parameters;
        this.dispatcher = dispatcher;
        this.entityScopeProvider = entityScopeProvider;
        this.ipInterfaceDao = ipInterfaceDao;
        this.connectionFactory = connectionFactory;
    }

    private Config getConfig(Integer nodeId, String ipAddress) {

        Map<String, String> params = Interpolator.interpolateStrings(parameters, new FallbackScope(
                entityScopeProvider.getScopeForNode(nodeId),
                entityScopeProvider.getScopeForInterface(nodeId, ipAddress)
        ));

        return new Config(nodeId, ipAddress, params);
    }

    private List<Config> getMatchingConfig() {
        List<Config> configList = new ArrayList<>();
        String enabledContext = parameters.get(PARAM_ENABLED);
        if (!Strings.isNullOrEmpty(enabledContext)) {
            try {
                Optional<ContextKey> contextKey = Interpolator.getContextKeyFromMateData(enabledContext);
                contextKey.ifPresent(ctxkey -> {
                    List<OnmsIpInterface> interfaceList = ipInterfaceDao.findInterfacesWithMetadata(ctxkey.getContext(), ctxkey.getKey(), "true");
                    interfaceList.forEach(onmsIpInterface -> {
                        Config config = getConfig(onmsIpInterface.getNodeId(), InetAddressUtils.toIpAddrString(onmsIpInterface.getIpAddress()));
                        configList.add(config);
                    });
                });

            } catch (Exception e) {
                LOG.error("Exception while getting metadata for openconfig parser", e);
            }
        }
        return configList;
    }


    @Override
    public Connection connect(int nodeId, String ipAddress) throws Exception {
        Config config = getConfig(nodeId, ipAddress);
        Connection connection = connectionFactory.getConnection(config);
        connection.subscribe(new StreamHandler(config));
        connectionMap.put(new NodeIdAndIpAddressKey(nodeId, ipAddress), connection);
        return connection;
    }

    @Override
    public void closeConnection(int nodeId, String ipAddress) {
        NodeIdAndIpAddressKey key = new NodeIdAndIpAddressKey(nodeId, ipAddress);
        if (connectionMap.containsKey(key)) {
            Connection connection = connectionMap.remove(key);
            try {
                connection.close();
            } catch (IOException e) {
                LOG.error("Exception while closing the stream connection for NodeId = {} and IpAddress {}", nodeId, ipAddress, e);
            }
        }

    }

    @Override
    public CompletableFuture<?> parse(ByteBuf buffer, InetSocketAddress remoteAddress) throws Exception {
        final TelemetryMessage msg = new TelemetryMessage(remoteAddress, buffer.nioBuffer());
        return dispatcher.send(msg);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void start(ScheduledExecutorService executorService) {
        scheduledExecutorService = executorService;
        getMatchingConfig().forEach(config -> {
            try {
                connect(config.getNodeId(), config.getIpAddress());
            } catch (Exception e) {
                LOG.error("Exception while connecting to stream at NodeId = {} and IpAddress {}",
                        config.getNodeId(), config.getIpAddress(), e);
            }
        });
    }

    @Override
    public void stop() {
        connectionMap.forEach((key, connection) -> {
            try {
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private class StreamHandler implements Connection.Handler {

        private final Config config;
        private final InetSocketAddress remoteAddress;

        public StreamHandler(Config config) {
            this.config = config;
            Integer port = Objects.requireNonNull(getInteger(config.getParams().get("port")));
            this.remoteAddress = SocketUtils.socketAddress(config.getIpAddress(), port);
        }

        @Override
        public void accept(byte[] response) {
            try {
                parse(Unpooled.wrappedBuffer(response), remoteAddress);
            } catch (Exception e) {
                LOG.warn("Exception while parsing telemetry data at address = {}", remoteAddress, e);
            }
        }

        @Override
        public void onError(String error) {
            scheduleSubscription(config);
        }
    }


    private void scheduleSubscription(Config config) {
        if (scheduledExecutorService == null) {
            return;
        }
        if (!connectionMap.containsKey(new NodeIdAndIpAddressKey(config.getNodeId(), config.getIpAddress()))) {
            return;
        }
        ScheduledFuture<Boolean> future = scheduledExecutorService.schedule(() -> subscribeToTelemetry(config), 300, TimeUnit.SECONDS);
        try {
            boolean succeeded = future.get();
            if (succeeded) {
                return;
            }
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Exception while connecting to stream at NodeId = {} and IpAddress {}",
                    config.getNodeId(), config.getIpAddress(), e);
        }
        // Try to connect again.
        scheduleSubscription(config);
    }


    private boolean subscribeToTelemetry(Config config) {
        try (Connection connection = connectionFactory.getConnection(config)) {
            connection.subscribe(new StreamHandler(config));
            return true;
        } catch (Exception e) {
            LOG.error("Exception while connecting to stream at NodeId = {} and IpAddress {}",
                    config.getNodeId(), config.getIpAddress(), e);
        }
        return false;
    }


    private static Integer getInteger(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private class NodeIdAndIpAddressKey {

        private final Integer nodeId;
        private final String ipAddress;

        public NodeIdAndIpAddressKey(Integer nodeId, String ipAddress) {
            this.nodeId = nodeId;
            this.ipAddress = ipAddress;
        }

        public Integer getNodeId() {
            return nodeId;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NodeIdAndIpAddressKey that = (NodeIdAndIpAddressKey) o;
            return Objects.equals(nodeId, that.nodeId) &&
                    Objects.equals(ipAddress, that.ipAddress);
        }

        @Override
        public int hashCode() {
            return Objects.hash(nodeId, ipAddress);
        }
    }
}
