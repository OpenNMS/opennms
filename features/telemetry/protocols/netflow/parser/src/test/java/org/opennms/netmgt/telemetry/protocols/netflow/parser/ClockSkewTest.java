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

import java.net.InetAddress;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.distributed.core.api.Identity;
import org.opennms.netmgt.dnsresolver.api.DnsResolver;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.telemetry.api.receiver.TelemetryMessage;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.Value;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.MessageBuilder;
import org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;

import com.codahale.metrics.MetricRegistry;

public class ClockSkewTest {
    private int eventCount = 0;

    private EventForwarder eventForwarder = new EventForwarder() {

        @Override
        public void sendNow(Event event) {
            System.out.println("Sending event: " + event);
            eventCount++;
        }

        @Override
        public void sendNow(Log eventLog) {
            Assert.fail();
        }

        @Override
        public void sendNowSync(Event event) {
            Assert.fail();
        }

        @Override
        public void sendNowSync(Log eventLog) {
            Assert.fail();
        }
    };

    private Identity identity = new Identity() {
        @Override
        public String getId() {
            return "myId";
        }

        @Override
        public String getLocation() {
            return "myLocation";
        }

        @Override
        public String getType() {
            return "MINION";
        }
    };

    private DnsResolver dnsResolver = new DnsResolver() {

        @Override
        public CompletableFuture<Optional<InetAddress>> lookup(final String hostname) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        @Override
        public CompletableFuture<Optional<String>> reverseLookup(InetAddress inetAddress) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
    };

    private ParserBase parserBase = new ParserBaseExt(Protocol.NETFLOW5, "name", new AsyncDispatcher<TelemetryMessage>() {
        @Override
        public CompletableFuture<DispatchStatus> send(TelemetryMessage message) {
            return null;
        }

        @Override
        public int getQueueSize() {
            return 0;
        }

        @Override
        public void close() throws Exception {

        }
    }, eventForwarder, identity, dnsResolver, new MetricRegistry());

    @Before
    public void reset() {
        this.eventCount = 0;
    }

    @Test
    public void testClockSkewEventSentOnlyOnce() {
        long current = System.currentTimeMillis();

        parserBase.setMaxClockSkew(300);
        parserBase.setClockSkewEventRate(3600);
        parserBase.detectClockSkew(current - 299000, InetAddress.getLoopbackAddress());
        Assert.assertEquals(0, eventCount);

        parserBase.detectClockSkew(current - 301000, InetAddress.getLoopbackAddress());
        Assert.assertEquals(1, eventCount);

        parserBase.detectClockSkew(current - 301000, InetAddress.getLoopbackAddress());
        Assert.assertEquals(1, eventCount);
    }

    @Test
    public void testClockSkewEventRate() throws Exception {
        long current = System.currentTimeMillis();

        parserBase.setMaxClockSkew(300);
        parserBase.setClockSkewEventRate(1);
        parserBase.detectClockSkew(current - 299000, InetAddress.getLoopbackAddress());
        Assert.assertEquals(0, eventCount);

        parserBase.detectClockSkew(current - 301000, InetAddress.getLoopbackAddress());
        Assert.assertEquals(1, eventCount);

        parserBase.detectClockSkew(current - 301000, InetAddress.getLoopbackAddress());
        Assert.assertEquals(1, eventCount);

        parserBase.detectClockSkew(current - 301000, InetAddress.getLoopbackAddress());
        Assert.assertEquals(1, eventCount);

        Thread.sleep(1000);

        parserBase.detectClockSkew(current - 301000, InetAddress.getLoopbackAddress());
        Assert.assertEquals(2, eventCount);
    }

    private class ParserBaseExt extends ParserBase {

        public ParserBaseExt(Protocol protocol, String name, AsyncDispatcher<TelemetryMessage> dispatcher, EventForwarder eventForwarder, Identity identity, DnsResolver dnsResolver, MetricRegistry metricRegistry) {
            super(protocol, name, dispatcher, eventForwarder, identity, dnsResolver, metricRegistry);
        }

        @Override
        protected MessageBuilder getMessageBuilder() {
            return new MessageBuilder() {
                @Override
                public FlowMessage.Builder buildMessage(final Iterable<Value<?>> values, final RecordEnrichment enrichment) {
                    return FlowMessage.newBuilder();
                }
            };
        }

        @Override
        public Object dumpInternalState() {
            return null;
        }
    }
}
