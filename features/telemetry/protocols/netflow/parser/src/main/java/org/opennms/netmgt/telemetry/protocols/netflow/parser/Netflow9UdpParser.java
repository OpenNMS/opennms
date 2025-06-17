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
package org.opennms.netmgt.telemetry.protocols.netflow.parser;

import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.slice;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint16;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;

import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.distributed.core.api.Identity;
import org.opennms.netmgt.dnsresolver.api.DnsResolver;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.telemetry.listeners.Dispatchable;
import org.opennms.netmgt.telemetry.api.receiver.TelemetryMessage;
import org.opennms.netmgt.telemetry.listeners.UdpParser;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.RecordProvider;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.Value;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow9.proto.Header;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow9.proto.Packet;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.Session;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.UdpSessionManager;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.Netflow9MessageBuilder;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import io.netty.buffer.ByteBuf;

public class Netflow9UdpParser extends UdpParserBase implements UdpParser, Dispatchable {

    private final Netflow9MessageBuilder messageBuilder = new Netflow9MessageBuilder();

    public Netflow9UdpParser(final String name,
                             final AsyncDispatcher<TelemetryMessage> dispatcher,
                             final EventForwarder eventForwarder,
                             final Identity identity,
                             final DnsResolver dnsResolver,
                             final MetricRegistry metricRegistry) {
        super(Protocol.NETFLOW9, name, dispatcher, eventForwarder, identity, dnsResolver, metricRegistry);
    }

    public Netflow9MessageBuilder getMessageBuilder() {
        return this.messageBuilder;
    }

    @Override
    protected RecordProvider parse(Session session, ByteBuf buffer) throws Exception {
        final Header header = new Header(slice(buffer, Header.SIZE));
        final Packet packet = new Packet(session, header, buffer);

        detectClockSkew(header.unixSecs * 1000L, session.getRemoteAddress());

        return packet;
    }

    @Override
    public boolean handles(final ByteBuf buffer) {
        return uint16(buffer) == Header.VERSION;
    }

    @Override
    protected UdpSessionManager.SessionKey buildSessionKey(final InetSocketAddress remoteAddress, final InetSocketAddress localAddress) {
        return new SessionKey(remoteAddress.getAddress(), localAddress);
    }

    public static class SessionKey implements UdpSessionManager.SessionKey {
        private final InetAddress remoteAddress;
        private final InetSocketAddress localAddress;

        public SessionKey(final InetAddress remoteAddress, final InetSocketAddress localAddress) {
            this.remoteAddress = remoteAddress;
            this.localAddress = localAddress;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final SessionKey that = (SessionKey) o;
            return Objects.equal(this.localAddress, that.localAddress) &&
                    Objects.equal(this.remoteAddress, that.remoteAddress);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(this.localAddress, this.remoteAddress);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("remoteAddress", remoteAddress)
                    .add("localAddress", localAddress)
                    .toString();
        }

        @Override
        public String getDescription() {
            return InetAddressUtils.str(this.remoteAddress);
        }

        @Override
        public InetAddress getRemoteAddress() {
            return this.remoteAddress;
        }
    }

    public Long getFlowActiveTimeoutFallback() {
        return this.messageBuilder.getFlowActiveTimeoutFallback();
    }

    public void setFlowActiveTimeoutFallback(final Long flowActiveTimeoutFallback) {
        this.messageBuilder.setFlowActiveTimeoutFallback(flowActiveTimeoutFallback);
    }

    public Long getFlowInactiveTimeoutFallback() {
        return this.messageBuilder.getFlowInactiveTimeoutFallback();
    }

    public void setFlowInactiveTimeoutFallback(final Long flowInactiveTimeoutFallback) {
        this.messageBuilder.setFlowInactiveTimeoutFallback(flowInactiveTimeoutFallback);
    }

    public Long getFlowSamplingIntervalFallback() {
        return this.messageBuilder.getFlowSamplingIntervalFallback();
    }

    public void setFlowSamplingIntervalFallback(final Long flowSamplingIntervalFallback) {
        this.messageBuilder.setFlowSamplingIntervalFallback(flowSamplingIntervalFallback);
    }
}
