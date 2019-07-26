/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.netflow.adapter.common;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

import com.google.common.collect.Maps;

public class SlopeAvoidanceThing {

    private final HashMap<Key, Lookback> table = Maps.newHashMap();

    private Optional<Lookback> advance(final String sessionKey,
                                      final Conversation conversation,
                                      final UpdatingFlow.Timeout timeout,
                                      final long timestamp,
                                      final long bytes,
                                      final long packets) {
        final Key key = new Key(sessionKey, conversation);

        // TODO fooker: TTL

        // Replace the last record with the current one
        final Lookback lookback = this.table.put(key, new Lookback(timestamp, bytes, packets));

        return Optional.ofNullable(lookback);
    }

    public Session session(final String key) {
        return new Session(key);
    }

    public class Session {
        private final String sessionKey;

        private Session(final String sessionKey) {
            this.sessionKey = Objects.requireNonNull(sessionKey);
        }

        public Optional<Lookback> advance(final Conversation conversation,
                                           final UpdatingFlow.Timeout timeout,
                                           final long timestamp,
                                           final long bytes,
                                           final long packets) {
           return SlopeAvoidanceThing.this.advance(
                   this.sessionKey,
                   conversation,
                   timeout,
                   timestamp,
                   bytes,
                   packets);
        }
    }

    public static class Lookback {
        private final long timestamp;

        private final long bytes;
        private final long packets;

        public Lookback(final long timestamp,
                        final long bytes,
                        final long packets) {
            this.timestamp = timestamp;
            this.bytes = bytes;
            this.packets = packets;
        }

        public long getTimestamp() {
            return this.timestamp;
        }

        public long getBytes() {
            return this.bytes;
        }

        public long getPackets() {
            return this.packets;
        }
    }

//    public static class Exporter {
//        private final String location;
//        private final String address;
//        private final int port;
//        private final int systemId;
//
//        public Exporter(final String location,
//                        final String address,
//                        int port,
//                        int systemId) {
//            this.location = Objects.requireNonNull(location);
//            this.address = Objects.requireNonNull(address);
//            this.port = port;
//            this.systemId = systemId;
//        }
//
//        @Override
//        public boolean equals(final Object o) {
//            if (this == o) {
//                return true;
//            }
//            if (!(o instanceof Exporter)) {
//                return false;
//            }
//
//            final Exporter that = (Exporter) o;
//            return this.port == that.port &&
//                    this.systemId == that.systemId &&
//                    Objects.equals(this.location, that.location) &&
//                    Objects.equals(this.address, that.address);
//        }
//
//        @Override
//        public int hashCode() {
//            return Objects.hash(this.location, this.address, this.port, this.systemId);
//        }
//    }

    public static class Conversation {
        private final long observationDomainId;
        private final int proto;
        private final String srcAddr;
        private final String dstAddr;
        private final int srcPort;
        private final int dstPort;

        public Conversation(final long observationDomainId,
                            final int proto,
                            final String srcAddr,
                            final String dstAddr,
                            final int srcPort,
                            final int dstPort) {
            this.observationDomainId = observationDomainId;
            this.proto = proto;
            this.srcAddr = Objects.requireNonNull(srcAddr);
            this.dstAddr = Objects.requireNonNull(dstAddr);
            this.srcPort = srcPort;
            this.dstPort = dstPort;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Conversation)) {
                return false;
            }

            final Conversation that = (Conversation) o;
            return this.observationDomainId == that.observationDomainId &&
                    this.proto == that.proto &&
                    this.srcPort == that.srcPort &&
                    this.dstPort == that.dstPort &&
                    Objects.equals(this.srcAddr, that.srcAddr) &&
                    Objects.equals(this.dstAddr, that.dstAddr);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.observationDomainId, this.proto, this.srcAddr, this.dstAddr, this.srcPort, this.dstPort);
        }
    }

    public static class Key {
        private final String sessionKey;
        private final Conversation conversation;

        public Key(final String sessionKey,
                   final Conversation conversation) {
            this.sessionKey = Objects.requireNonNull(sessionKey);
            this.conversation = Objects.requireNonNull(conversation);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Key)) {
                return false;
            }

            final Key that = (Key) o;
            return Objects.equals(this.sessionKey, that.sessionKey) &&
                    Objects.equals(this.conversation, that.conversation);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.sessionKey, this.conversation);
        }
    }


}
