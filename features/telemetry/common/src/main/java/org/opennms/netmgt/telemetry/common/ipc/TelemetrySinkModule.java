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
package org.opennms.netmgt.telemetry.common.ipc;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import org.opennms.core.ipc.sink.api.AggregationPolicy;
import org.opennms.core.ipc.sink.api.AsyncPolicy;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.telemetry.config.api.QueueDefinition;
import org.opennms.netmgt.telemetry.api.receiver.TelemetryMessage;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

public class TelemetrySinkModule implements SinkModule<TelemetryMessage, TelemetryProtos.TelemetryMessageLog> {
    private static final String MODULE_ID_PREFIX = "Telemetry-";

    private static final int DEFAULT_NUM_THREADS = Runtime.getRuntime().availableProcessors() * 2;
    private static final int DEFAULT_BATCH_SIZE = 1000;
    private static final int DEFAULT_BATCH_INTERVAL_MS = 500;
    private static final int DEFAULT_QUEUE_SIZE = 10000;

    @Autowired
    private DistPollerDao distPollerDao;

    private final QueueDefinition queueConfig;

    private final String moduleId;

    public TelemetrySinkModule(final QueueDefinition queueConfig) {
        this.queueConfig = Objects.requireNonNull(queueConfig);
        this.moduleId = MODULE_ID_PREFIX + this.queueConfig.getName();
    }

    @Override
    public String getId() {
        return moduleId;
    }

    @Override
    public int getNumConsumerThreads() {
        return queueConfig.getNumThreads().orElse(DEFAULT_NUM_THREADS);
    }

    @Override
    public byte[] marshal(TelemetryProtos.TelemetryMessageLog message) {
        return message.toByteArray();
    }

    @Override
    public TelemetryProtos.TelemetryMessageLog unmarshal(byte[] bytes) {
        try {
            return TelemetryProtos.TelemetryMessageLog.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] marshalSingleMessage(TelemetryMessage message) {
        return marshal(getAggregationPolicy().aggregate(null, message).build());
    }

    @Override
    public TelemetryMessage unmarshalSingleMessage(byte[] message) {
        TelemetryProtos.TelemetryMessageLog messageLog = unmarshal(message);
        InetSocketAddress inetSocketAddress = new InetSocketAddress(messageLog.getSourceAddress(), messageLog.getSourcePort());
        return new TelemetryMessage(inetSocketAddress,
                ByteBuffer.wrap(messageLog.getMessage(0).getByteArray()),
                new Date(messageLog.getMessage(0).getTimestamp()));
    }

    @Override
    public AggregationPolicy<TelemetryMessage, TelemetryProtos.TelemetryMessageLog, TelemetryProtos.TelemetryMessageLog.Builder> getAggregationPolicy() {
        final String systemId = distPollerDao.whoami().getId();
        final String systemLocation = distPollerDao.whoami().getLocation();
        return new AggregationPolicy<TelemetryMessage, TelemetryProtos.TelemetryMessageLog, TelemetryProtos.TelemetryMessageLog.Builder>() {
            @Override
            public int getCompletionSize() {
                return TelemetrySinkModule.this.queueConfig.getBatchSize().orElse(DEFAULT_BATCH_SIZE);
            }

            @Override
            public int getCompletionIntervalMs() {
                return TelemetrySinkModule.this.queueConfig.getBatchIntervalMs().orElse(DEFAULT_BATCH_INTERVAL_MS);
            }

            @Override
            public Object key(TelemetryMessage telemetryMessage) {
                return telemetryMessage.getSource();
            }

            @Override
            public TelemetryProtos.TelemetryMessageLog.Builder aggregate(TelemetryProtos.TelemetryMessageLog.Builder accumulator, TelemetryMessage message) {
                if (accumulator == null) {
                    accumulator = TelemetryProtos.TelemetryMessageLog.newBuilder()
                            .setLocation(systemLocation)
                            .setSystemId(systemId)
                            .setSourceAddress(message.getSource().getHostString())
                            .setSourcePort(message.getSource().getPort());
                }
                final TelemetryProtos.TelemetryMessage messageDto = TelemetryProtos.TelemetryMessage.newBuilder()
                        .setTimestamp(message.getReceivedAt().getTime())
                        .setBytes(ByteString.copyFrom(message.getBuffer()))
                        .build();
                // Append
                accumulator.addMessage(messageDto);
                return accumulator;
            }

            @Override
            public TelemetryProtos.TelemetryMessageLog build(TelemetryProtos.TelemetryMessageLog.Builder accumulator) {
                return accumulator.build();
            }
        };
    }

    @Override
    public AsyncPolicy getAsyncPolicy() {
        return new AsyncPolicy() {
            @Override
            public int getQueueSize() {
                return TelemetrySinkModule.this.queueConfig.getQueueSize().orElse(DEFAULT_QUEUE_SIZE);
            }

            @Override
            public int getNumThreads() {
                return TelemetrySinkModule.this.queueConfig.getNumThreads().orElse(DEFAULT_NUM_THREADS);
            }

            @Override
            public boolean isBlockWhenFull() {
                // Always block when full
                return true;
            }
        };
    }

    @Override
    public Optional<String> getRoutingKey(final TelemetryProtos.TelemetryMessageLog message) {
        // Allow the queue configuration to drive whether or not the routing key is used. Default to true.
        if (queueConfig.getUseRoutingKey().orElse(true)) {
            return Optional.of(String.format("%s@%s:%d", message.getLocation(), message.getSourceAddress(), message.getSourcePort()));
        }
        // We've been configured to ommit the routing key.
        return Optional.empty();
    }

    public DistPollerDao getDistPollerDao() {
        return distPollerDao;
    }

    public void setDistPollerDao(DistPollerDao distPollerDao) {
        this.distPollerDao = distPollerDao;
    }
}
