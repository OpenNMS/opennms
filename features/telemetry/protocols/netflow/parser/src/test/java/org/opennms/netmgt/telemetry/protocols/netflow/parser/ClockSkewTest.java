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

    private ParserBase parserBase = new ParserBase(Protocol.NETFLOW5, "name", new AsyncDispatcher<TelemetryMessage>() {
        @Override
        public CompletableFuture<TelemetryMessage> send(TelemetryMessage message) {
            return null;
        }

        @Override
        public int getQueueSize() {
            return 0;
        }

        @Override
        public void close() {

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
}
