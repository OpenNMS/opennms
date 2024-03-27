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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.junit.Test;
import org.opennms.netmgt.dnsresolver.api.DnsResolver;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow5.proto.Header;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow5.proto.Packet;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow5.proto.Record;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class RecordEnricherTest {

    /**
     * Test flow enrichment by mocking the {@link DnsResolver}.
     */
    @Test
    public void canEnrichFlow() throws InvalidPacketException, ExecutionException, InterruptedException, UnknownHostException {
        enrichFlow(CompletableFuture.completedFuture(Optional.of("test")), Optional.of("test"), true);
        enrichFlow(CompletableFuture.completedFuture(Optional.empty()), Optional.empty(), true);

        CompletableFuture exceptionalFuture = new CompletableFuture();
        exceptionalFuture.completeExceptionally(new RuntimeException());
        enrichFlow(exceptionalFuture, Optional.empty(), true);
    }

    @Test
    public void canDisableEnrichFlow() throws InvalidPacketException, ExecutionException, InterruptedException, UnknownHostException {
        enrichFlow(CompletableFuture.completedFuture(Optional.of("test")), Optional.empty(), false);

        CompletableFuture exceptionalFuture = new CompletableFuture();
        exceptionalFuture.completeExceptionally(new RuntimeException());
        enrichFlow(exceptionalFuture, Optional.empty(), false);
    }

    private void enrichFlow(CompletableFuture reverseLookupFuture, Optional<String> expectedValue, boolean dnsLookupsEnabled) throws InvalidPacketException, ExecutionException, InterruptedException, UnknownHostException {
        DnsResolver dnsResolver = mock(DnsResolver.class);
        when(dnsResolver.reverseLookup(any())).thenReturn(reverseLookupFuture);

        RecordEnricher enricher = new RecordEnricher(dnsResolver, dnsLookupsEnabled);

        final Packet packet = getSampleNf5Packet();
        final List<CompletableFuture<RecordEnrichment>> enrichmentFutures = packet.getRecords().map(enricher::enrich)
                .collect(Collectors.toList());

        CompletableFuture.allOf(enrichmentFutures.toArray(new CompletableFuture[]{}));

        for (CompletableFuture<RecordEnrichment> future : enrichmentFutures) {
            assertThat(future.isCompletedExceptionally(), equalTo(false));

            RecordEnrichment enrichment = future.get();

            assertThat(enrichment.getHostnameFor(InetAddress.getByName("255.255.255.255")), equalTo(expectedValue));
        }
    }

    private static Packet getSampleNf5Packet() throws InvalidPacketException {
        // Generate minimal Netflow v5 packet with 1 record
        byte[] bytes = new byte[Header.SIZE + Record.SIZE];
        Arrays.fill(bytes, (byte) 0xFF);
        bytes[0] = 0x00;
        bytes[1] = 0x05;
        bytes[2] = 0x00;
        bytes[3] = 0x01;

        final ByteBuf buffer = Unpooled.wrappedBuffer(bytes);
        final Header header = new Header(buffer);
        return new Packet(header, buffer);
    }

}
