/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
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

package org.opennms.core.ipc.sink.aggregation;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.ipc.sink.api.AggregationPolicy;
import org.opennms.core.ipc.sink.api.AsyncPolicy;
import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.ipc.sink.api.MessageDispatcherFactory;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.ipc.sink.api.SyncDispatcher;
import org.opennms.core.ipc.sink.common.AbstractMessageDispatcherFactory;
import org.opennms.core.test.MockLogAppender;

import com.google.common.util.concurrent.RateLimiter;

public class AggregationTest {

    private static final int COMPLETION_SIZE = 10;
    private static final int COMPLETION_INTERVAL_MS = 500;

    private final InetAddress localhost = InetAddress.getLoopbackAddress();

    private final List<Object> dispatchedMessages = new ArrayList<>();

    private final MessageDispatcherFactory capturingMessageDispatcherFactory = new AbstractMessageDispatcherFactory<Void>() {
        @Override
        public <S extends Message, T extends Message> void dispatch(SinkModule<S, T> module, Void metadata, T message) {
            dispatchedMessages.add(message);
        }
    };

    @Before
    public void setUp() {
        MockLogAppender.setupLogging();
    }

    @After
    public void tearDown() {
        MockLogAppender.assertNoWarningsOrGreater();
    }

    @Test
    public void noAggregateUsingIdentityFunction() throws Exception {
        SinkModuleWithIdentityAggregate aggregatingSinkModule = new SinkModuleWithIdentityAggregate();
        try(SyncDispatcher<UDPPacketLog> dispatcher = capturingMessageDispatcherFactory.createSyncDispatcher(aggregatingSinkModule)) {
            for (byte i = 0; i < 10; i++) {
                UDPPacket packet = new UDPPacket(localhost, ByteBuffer.wrap(new byte[]{i}));
                UDPPacketLog packetLog = new UDPPacketLog(packet);
                dispatcher.send(packetLog);
                // The same message should have been immediately dispatched
                assertEquals(dispatchedMessages.get(i), packetLog);
            }
        }
    }

    @Test
    public void noAggregateUsingMapFunction() throws Exception {
        SinkModuleWithMappingAggregate aggregatingSinkModule = new SinkModuleWithMappingAggregate();
        try(SyncDispatcher<UDPPacket> dispatcher = capturingMessageDispatcherFactory.createSyncDispatcher(aggregatingSinkModule)) {
            for (byte i = 0; i < 10; i++) {
                UDPPacket packet = new UDPPacket(localhost, ByteBuffer.wrap(new byte[]{i}));
                dispatcher.send(packet);
                // The message should have been wrapped in a UDPPacketLog
                // and immediately dispatched
                UDPPacketLog packetLog = new UDPPacketLog(packet);
                assertEquals(dispatchedMessages.get(i), packetLog);
            }
        }
    }

    @Test
    public void aggregateWithoutInterval() throws Exception {
        SinkModuleWithAggregateNoInterval aggregatingSinkModule = new SinkModuleWithAggregateNoInterval();
        try(SyncDispatcher<UDPPacket> dispatcher = capturingMessageDispatcherFactory.createSyncDispatcher(aggregatingSinkModule)) {
            for (byte i = 0; i < 10 * COMPLETION_SIZE; i++) {
                UDPPacket packet = new UDPPacket(localhost, ByteBuffer.wrap(new byte[]{(byte)i}));
                dispatcher.send(packet);
            }
            // The message should have been aggregated
            assertEquals(10, dispatchedMessages.size());
        }
    }

    @Test
    public void aggregateWithInterval() throws Exception {
        SinkModuleWithAggregateAndInterval aggregatingSinkModule = new SinkModuleWithAggregateAndInterval();
        try(SyncDispatcher<UDPPacket> dispatcher = capturingMessageDispatcherFactory.createSyncDispatcher(aggregatingSinkModule)) {
            // Send less than COMPLETION_SIZE packets
            assertThat(COMPLETION_SIZE, greaterThan(1));
            UDPPacket packet = new UDPPacket(localhost, ByteBuffer.wrap(new byte[]{(byte)42}));
            dispatcher.send(packet);

            // Now wait until the aggregate is dispatched
            await().atMost(4 * COMPLETION_INTERVAL_MS, MILLISECONDS)
                .pollDelay(50, MILLISECONDS)
                .until(() -> dispatchedMessages, hasSize(1));
        }
    }

    /**
     * NMS-9114: Test concurrency with the timer thread.
     */
    @Test
    public void aggregateWithAggressiveFlushInterval() throws Exception {
        final int DISPATCHES_PER_SECOND = 5000;
        final int DISPATCHES_PER_ITERATION = 1000;

        SinkModuleWithAggregateAndAggressiveInterval aggregatingSinkModule = new SinkModuleWithAggregateAndAggressiveInterval();
        try(SyncDispatcher<UDPPacket> dispatcher = capturingMessageDispatcherFactory.createSyncDispatcher(aggregatingSinkModule)) {
            // Create a thread that will perform a rate-limited dispatch until interrupted
            final Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    final RateLimiter rateLimiter = RateLimiter.create(DISPATCHES_PER_SECOND);
                    while(true) {
                        rateLimiter.acquire(DISPATCHES_PER_ITERATION);
                        for (int i = 0; i < DISPATCHES_PER_ITERATION; i++) {
                            UDPPacket packet = new UDPPacket(localhost, ByteBuffer.wrap(new byte[]{(byte)42}));
                            dispatcher.send(packet);
                        }
                        if (Thread.interrupted()) {
                            return;
                        }
                    }
                }
            });
            t.start();

            await().atMost(10, SECONDS)
                .pollDelay(1, SECONDS)
                .until(() -> dispatchedMessages, hasSize(greaterThan(5 * DISPATCHES_PER_SECOND)));
            t.interrupt();

            // We successfully dispatched many messages.
            // The MockLogAppender in this tests tearDown() function will
            // validate that no errors or warning (i.e. NPEs) we logged
            // during this time
        }
    }

    public static class UDPPacket implements Message {
        private final InetAddress source;
        private final ByteBuffer bytes;

        public UDPPacket(InetAddress source, ByteBuffer bytes) {
            this.source = Objects.requireNonNull(source);
            this.bytes = Objects.requireNonNull(bytes);
        }

        public InetAddress getSource() {
            return source;
        }

        public ByteBuffer getBytes() {
            return bytes;
        }

        @Override
        public int hashCode() {
            return Objects.hash(bytes, source);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else if (obj == null) {
                return false;
            } else if (!(obj instanceof UDPPacket)) {
                return false;
            }
            UDPPacket other = (UDPPacket) obj;
            return Objects.equals(this.bytes, other.bytes) &&
                    Objects.equals(this.source, other.source);
        }
    }

    public static class UDPPacketLog implements Message {
        private final List<UDPPacket> packets;

        public UDPPacketLog(UDPPacket... packets) {
            this.packets = new ArrayList<>(Arrays.asList(packets));
        }

        public List<UDPPacket> getPackets() {
            return packets;
        }

        @Override
        public int hashCode() {
            return Objects.hash(packets);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else if (obj == null) {
                return false;
            } else if (!(obj instanceof UDPPacketLog)) {
                return false;
            }
            UDPPacketLog other = (UDPPacketLog) obj;
            return Objects.equals(this.packets, other.packets);
        }
    }

    public static class SinkModuleWithIdentityAggregate extends AbstractSinkModule<UDPPacketLog, UDPPacketLog> {
        @Override
        public AggregationPolicy<UDPPacketLog, UDPPacketLog, UDPPacketLog> getAggregationPolicy() {
            return new IdentityAggregationPolicy<>();
        }
    }

    private static class SinkModuleWithMappingAggregate extends AbstractSinkModule<UDPPacket, UDPPacketLog> {
        @Override
        public AggregationPolicy<UDPPacket, UDPPacketLog, UDPPacketLog> getAggregationPolicy() {
            return new MappingAggregationPolicy<UDPPacket, UDPPacketLog>() {
                @Override
                public UDPPacketLog map(UDPPacket message) {
                    return new UDPPacketLog(message);
                }
            };
        }
    }

    private static class SinkModuleWithAggregateNoInterval extends AbstractSinkModule<UDPPacket, UDPPacketLog> {
        @Override
        public AggregationPolicy<UDPPacket, UDPPacketLog, UDPPacketLog> getAggregationPolicy() {
            return new AggregationPolicy<UDPPacket, UDPPacketLog, UDPPacketLog>() {
                @Override
                public int getCompletionSize() {
                    return COMPLETION_SIZE;
                }

                @Override
                public int getCompletionIntervalMs() {
                    return 0;
                }

                @Override
                public Object key(UDPPacket message) {
                    // Key by the source address
                    return message.getSource();
                }

                @Override
                public UDPPacketLog aggregate(UDPPacketLog oldPacketLog, UDPPacket newPacket) {
                    if (oldPacketLog == null) {
                        return new UDPPacketLog(newPacket);
                    } else {
                        oldPacketLog.getPackets().add(newPacket);
                        return oldPacketLog;
                    }
                }

                @Override
                public UDPPacketLog build(UDPPacketLog accumulator) {
                    return accumulator;
                }
            };
        }
    }

    private static class SinkModuleWithAggregateAndInterval extends AbstractSinkModule<UDPPacket, UDPPacketLog> {
        @Override
        public AggregationPolicy<UDPPacket, UDPPacketLog, UDPPacketLog> getAggregationPolicy() {
            return new AggregationPolicy<UDPPacket, UDPPacketLog, UDPPacketLog>() {
                @Override
                public int getCompletionSize() {
                    return COMPLETION_SIZE;
                }

                @Override
                public int getCompletionIntervalMs() {
                    return COMPLETION_INTERVAL_MS;
                }

                @Override
                public Object key(UDPPacket message) {
                    // Key by the source address
                    return message.getSource();
                }

                @Override
                public UDPPacketLog aggregate(UDPPacketLog oldPacketLog, UDPPacket newPacket) {
                    if (oldPacketLog == null) {
                        return new UDPPacketLog(newPacket);
                    } else {
                        oldPacketLog.getPackets().add(newPacket);
                        return oldPacketLog;
                    }
                }

                @Override
                public UDPPacketLog build(UDPPacketLog accumulator) {
                    return accumulator;
                }
            };
        }
    }

    private static class SinkModuleWithAggregateAndAggressiveInterval extends AbstractSinkModule<UDPPacket, UDPPacketLog> {
        @Override
        public AggregationPolicy<UDPPacket, UDPPacketLog, UDPPacketLog> getAggregationPolicy() {
            return new AggregationPolicy<UDPPacket, UDPPacketLog, UDPPacketLog>() {
                @Override
                public int getCompletionSize() {
                    return 1;
                }

                @Override
                public int getCompletionIntervalMs() {
                    // Run the timer thread frequently
                    return 1;
                }

                @Override
                public Object key(UDPPacket message) {
                    // Key by the source address
                    return message.getSource();
                }

                @Override
                public UDPPacketLog aggregate(UDPPacketLog oldPacketLog, UDPPacket newPacket) {
                    if (oldPacketLog == null) {
                        return new UDPPacketLog(newPacket);
                    } else {
                        oldPacketLog.getPackets().add(newPacket);
                        return oldPacketLog;
                    }
                }

                @Override
                public UDPPacketLog build(UDPPacketLog accumulator) {
                    return accumulator;
                }
            };
        }
    }

    private static abstract class AbstractSinkModule<S extends Message, T extends Message> implements SinkModule<S, T> {

        @Override
        public String getId() {
            return getClass().getCanonicalName();
        }

        @Override
        public int getNumConsumerThreads() {
            return 1;
        }

        @Override
        public byte[] marshal(T message) {
            return null;
        }

        @Override
        public T unmarshal(byte[] bytes) {
            return null;
        }

        @Override
        public AsyncPolicy getAsyncPolicy() {
            return null;
        }
    }
}
