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

package org.opennms.features.grpc.exporter.nmsinventory;

import com.google.protobuf.Empty;
import io.grpc.ClientInterceptor;
import io.grpc.ConnectivityState;
import io.grpc.stub.StreamObserver;
import org.opennms.features.grpc.exporter.Callback;
import org.opennms.features.grpc.exporter.GrpcExporter;
import org.opennms.features.grpc.exporter.NamedThreadFactory;
import org.opennms.plugin.grpc.proto.services.AlarmUpdateList;
import org.opennms.plugin.grpc.proto.services.NmsInventoryServiceSyncGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.plugin.grpc.proto.services.NmsInventoryUpdateList;
import org.opennms.plugin.grpc.proto.services.EventUpdateList;
import javax.net.ssl.SSLException;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class NmsInventoryGrpcClient extends GrpcExporter {
    private static final Logger LOG = LoggerFactory.getLogger(NmsInventoryGrpcClient.class);

    private final String threadName = "alarm-inventory-grpc-connect";
    private final AtomicBoolean reconnecting = new AtomicBoolean(false);
    private ScheduledExecutorService scheduler;
    private NmsInventoryServiceSyncGrpc.NmsInventoryServiceSyncStub nmsSyncStub;
    private StreamObserver<AlarmUpdateList> alarmsUpdateStream;
    private StreamObserver<NmsInventoryUpdateList> nmsInventoryUpdateStream;
    private StreamObserver<EventUpdateList> eventUpdateStream;

    private Callback inventoryCallback;

    public NmsInventoryGrpcClient(final String host,
                                  final String tlsCertPath,
                                  final boolean tlsEnabled,
                                  final ClientInterceptor clientInterceptor) {
        super(host, tlsCertPath, tlsEnabled, clientInterceptor);
        this.scheduler = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory(threadName));

    }

    public void start() throws SSLException{
        super.startGrpcConnection();
        this.nmsSyncStub = NmsInventoryServiceSyncGrpc.newStub(super.getChannel());
        connectStreams();
    }
    private void connectStreams() {
        // Schedule connect streams immediately and 30 secs thereafter.
        scheduler.scheduleAtFixedRate(this::initializeStreams, 30, 30, TimeUnit.SECONDS);
    }

    private synchronized void initializeStreams() {

        if (super.getChannelState().equals(ConnectivityState.READY)) {
            try {
                this.alarmsUpdateStream =
                        this.nmsSyncStub.alarmUpdate(new LoggingAckReceiver("nms_alarm_update", this));
                this.nmsInventoryUpdateStream =
                        this.nmsSyncStub.inventoryUpdate(new LoggingAckReceiver("nms_inventory_update", this));
                this.eventUpdateStream =
                        this.nmsSyncStub.eventUpdate(new LoggingAckReceiver("events_update", this));

                this.scheduler.shutdown();
                this.scheduler = null;
                LOG.info("NMS Streams initialized successfully.");
                reconnecting.set(false);
                // While connecting, reconnecting, send callback to inventory service.
                if (inventoryCallback != null) {
                    inventoryCallback.sendInventorySnapShot();
                }
            } catch (Exception e) {
                LOG.error("Failed to initialize NMS streams", e);
            }
        } else {
            LOG.info("GRPC-Channel state is not READY, retrying... {}", super.getHost());
        }
    }

    public void stop() {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
        super.stopGrpcConnection();
    }

    private synchronized void reconnectStreams() {
        // Multiple streams may try to reconnect but should end up using one scheduler which schedules connection after a delay of 30 secs.
        if (reconnecting.compareAndSet(false, true) && !super.getStopped()) {
            if (scheduler == null || scheduler.isShutdown()) {
                scheduler = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory(threadName));
                scheduler.scheduleAtFixedRate(this::initializeStreams, 30, 30, TimeUnit.SECONDS);
            }
        }
    }


    public void setInventoryCallback(Callback inventoryCallback) {
        this.inventoryCallback = inventoryCallback;
    }

    public void sendAlarmUpdate(final AlarmUpdateList alarmUpdates) {
        if (this.alarmsUpdateStream != null) {
            this.alarmsUpdateStream.onNext(alarmUpdates);
            LOG.info("Client-Sent an alarm-Updates with {} count", alarmUpdates.getAlarmsCount());
        } else {
            LOG.warn("Client-Unable to send alarm-Updates since channel is not ready yet .. {} ", super.getHost());
        }
    }

    public void sendNmsInventoryUpdate(final NmsInventoryUpdateList updates) {
        if (this.nmsInventoryUpdateStream != null) {
            this.nmsInventoryUpdateStream.onNext(updates);
        } else {
            LOG.warn("Client-Unable to send Inventory-Updates since channel is not ready yet .. {} ", super.getHost());
        }
    }

    public void sendEventUpdate(final EventUpdateList updates) {
        if (this.eventUpdateStream != null) {
            this.eventUpdateStream.onNext(updates);
            LOG.info("Client-Sent an Event-Update with {} count", updates.getEventCount());
        } else {
            LOG.warn("Client-Unable to send Event-Updates since channel is not ready yet .. {} ", super.getHost());
        }
    }

    private static class LoggingAckReceiver implements StreamObserver<Empty> {

        private final String type;
        private final NmsInventoryGrpcClient client;

        private LoggingAckReceiver(final String type, NmsInventoryGrpcClient client) {
            this.type = Objects.requireNonNull(type);
            this.client = client;
        }

        @Override
        public void onNext(final Empty value) {
            LOG.debug("AlarmInventory-Received ACK {}", this.type);
        }

        @Override
        public void onError(final Throwable t) {
            LOG.error("AlarmInventory-Received error {}", this.type, t);
            client.reconnectStreams();
        }

        @Override
        public void onCompleted() {
            LOG.info("AlarmInventory-Completed {}", this.type);
            client.reconnectStreams();
        }
    }
}