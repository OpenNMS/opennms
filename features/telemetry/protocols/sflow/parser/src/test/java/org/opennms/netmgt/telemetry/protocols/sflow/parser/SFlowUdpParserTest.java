package org.opennms.netmgt.telemetry.protocols.sflow.parser;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.netmgt.dnsresolver.api.DnsResolver;
import org.opennms.netmgt.telemetry.api.receiver.TelemetryMessage;

import java.net.InetAddress;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class SFlowUdpParserTest {

    private final DnsResolver dnsResolver = new DnsResolver() {
        @Override
        public CompletableFuture<Optional<InetAddress>> lookup(String hostname) {
            return CompletableFuture.completedFuture(Optional.of(InetAddress.getLoopbackAddress()));
        }

        @Override
        public CompletableFuture<Optional<String>> reverseLookup(InetAddress inetAddress) {
            return CompletableFuture.completedFuture(Optional.of("resolved-hostname"));
        }
    };

    private final AsyncDispatcher<TelemetryMessage> dispatcher = new AsyncDispatcher<TelemetryMessage>() {
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
    };

    @Test
    public void testDnsLookupsEnabled() {
        SFlowUdpParser sFlowUdpParser = new SFlowUdpParser("name", dispatcher, dnsResolver);
        sFlowUdpParser.start(new ScheduledThreadPoolExecutor(1));
        Assert.assertTrue(sFlowUdpParser.getEnricher().isDnsLookupsEnabled());
    }

    @Test
    public void testDnsLookupsDisabled() {
        SFlowUdpParser sFlowUdpParser = new SFlowUdpParser("name", dispatcher, dnsResolver);
        sFlowUdpParser.setDnsLookupsEnabled(false);
        sFlowUdpParser.start(new ScheduledThreadPoolExecutor(1));
        Assert.assertFalse(sFlowUdpParser.getEnricher().isDnsLookupsEnabled());
    }
}
