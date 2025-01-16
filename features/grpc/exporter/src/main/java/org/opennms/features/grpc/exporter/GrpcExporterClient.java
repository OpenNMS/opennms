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

package org.opennms.features.grpc.exporter;

import com.google.protobuf.Empty;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.opennms.plugin.grpc.proto.services.InventoryUpdateList;
import org.opennms.plugin.grpc.proto.services.ServiceSyncGrpc;
import org.opennms.plugin.grpc.proto.services.StateUpdateList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.io.File;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


public class GrpcExporterClient {
    private static final Logger LOG = LoggerFactory.getLogger(GrpcExporterClient.class);

    public static final String FOREIGN_TYPE = "OpenNMS";

    private final String host;
    private final Integer port;
    private final String tlsCertPath;

    private final boolean tlsEnabled;
    private ManagedChannel channel;

    private ServiceSyncGrpc.ServiceSyncStub monitoredServiceSyncStub;

    private StreamObserver<InventoryUpdateList> inventoryUpdateStream;
    private StreamObserver<StateUpdateList> stateUpdateStream;
    private ScheduledExecutorService scheduler;
    private final AtomicBoolean reconnecting = new AtomicBoolean(false);
    private final AtomicBoolean stopped = new AtomicBoolean(false);
    private Callback inventoryCallback;

    public GrpcExporterClient(final String host,
                              final Integer port,
                              final String tlsCertPath,
                              final boolean tlsEnabled) {
        this.host = Objects.requireNonNull(host);
        this.port = Objects.requireNonNull(port);
        this.tlsCertPath = tlsCertPath;
        this.tlsEnabled = tlsEnabled;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("grpc-exporter-connect"));
    }

    public void start() throws SSLException {
        final NettyChannelBuilder channelBuilder;
        if (this.port > 0) {
            channelBuilder = NettyChannelBuilder.forAddress(this.host, this.port)
                    .keepAliveWithoutCalls(true);
        } else {
            channelBuilder = NettyChannelBuilder.forTarget(this.host)
                    .keepAliveWithoutCalls(true);
        }

        if (tlsEnabled && tlsCertPath != null && !tlsCertPath.isBlank()) {
            channel = channelBuilder.useTransportSecurity()
                    .sslContext(GrpcSslContexts.forClient()
                            .trustManager(new File(tlsCertPath)).build())
                    .build();
            LOG.info("TLS enabled with cert at {}", tlsCertPath);
        } else if (tlsEnabled) {
            // Use system store specified in javax.net.ssl.trustStore
            channel = channelBuilder.useTransportSecurity()
                    .build();
            LOG.info("TLS enabled with certs from system store");
        } else {
            channel = channelBuilder.usePlaintext()
                    .build();
            LOG.info("TLS disabled, using plain text");
        }

        this.monitoredServiceSyncStub = ServiceSyncGrpc.newStub(this.channel);
        connectStreams();
        LOG.info("GrpcExporterClient started connection to {}:{}", this.host, this.port);
    }

    public void stop() {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
        if (channel != null) {
            channel.shutdownNow();
        }
        stopped.set(true);
        LOG.info("GrpcExporterClient stopped");
    }

    public void setInventoryCallback(Callback inventoryCallback) {
        this.inventoryCallback = inventoryCallback;
    }

    private synchronized void initializeStreams() {
        if (getChannelState().equals(ConnectivityState.READY)) {
            try {
                this.inventoryUpdateStream =
                        this.monitoredServiceSyncStub.inventoryUpdate(new LoggingAckReceiver("monitored_service_inventory_update", this));
                this.stateUpdateStream =
                        this.monitoredServiceSyncStub.stateUpdate(new LoggingAckReceiver("monitored_service_state_update", this));
                this.scheduler.shutdown();
                this.scheduler = null;
                LOG.info("Streams initialized successfully.");
                reconnecting.set(false);
                // While connecting, reconnecting, send callback to inventory service.
                if (inventoryCallback != null) {
                    inventoryCallback.sendInventorySnapShot();
                }
            } catch (Exception e) {
                LOG.error("Failed to initialize streams", e);
            }
        } else {
            LOG.info("Channel state is not READY, retrying...");
        }
    }

    private void connectStreams() {
        // Schedule connect streams immediately and 30 secs thereafter.
        scheduler.scheduleAtFixedRate(this::initializeStreams, 30, 30, TimeUnit.SECONDS);
    }

    private synchronized void reconnectStreams() {
        // Multiple streams may try to reconnect but should end up using one scheduler which schedules connection after a delay of 30 secs.
        if (reconnecting.compareAndSet(false, true) && !stopped.get()) {
            if (scheduler == null || scheduler.isShutdown()) {
                scheduler = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("grpc-exporter-reconnect"));
                scheduler.scheduleAtFixedRate(this::initializeStreams, 30, 30, TimeUnit.SECONDS);
            }
        }
    }

    ConnectivityState getChannelState() {
        return channel.getState(true);
    }

    public void sendMonitoredServicesInventoryUpdate(final InventoryUpdateList inventoryUpdates) {
        if (inventoryUpdateStream != null) {
            this.inventoryUpdateStream.onNext(inventoryUpdates);
            LOG.info("Sent an monitored service inventory update with {} services", inventoryUpdates.getServicesCount());
        } else {
            LOG.warn("Unable to send inventory snapshot since channel is not ready yet");
        }
    }

    public void sendMonitoredServicesStatusUpdate(final StateUpdateList stateUpdates) {
        if (this.stateUpdateStream != null) {
            this.stateUpdateStream.onNext(stateUpdates);
            LOG.info("Sent an monitored service state update with {} services", stateUpdates.getUpdatesCount());
        } else {
            LOG.warn("Unable to send monitored service status update since channel is not ready yet");
        }
    }

    private static class LoggingAckReceiver implements StreamObserver<Empty> {

        private final String type;

        private final GrpcExporterClient client;

        private LoggingAckReceiver(final String type, GrpcExporterClient client) {
            this.type = Objects.requireNonNull(type);
            this.client = client;
        }

        @Override
        public void onNext(final Empty value) {
            LOG.debug("Received ACK {}", this.type);
        }

        @Override
        public void onError(final Throwable t) {
            LOG.error("Received error {}", this.type, t);
            client.reconnectStreams();
        }

        @Override
        public void onCompleted() {
            LOG.info("Completed {}", this.type);
            client.reconnectStreams();
        }
    }
}
