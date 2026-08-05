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

package org.opennms.netmgt.flows.victorialogs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;

import java.util.concurrent.atomic.AtomicInteger;
import com.google.gson.JsonParser;
import org.junit.Test;
import org.opennms.integration.api.v1.flows.Flow;

import com.google.gson.JsonObject;

public class FlowJsonSerializerTest {

    private final FlowJsonSerializer serializer = new FlowJsonSerializer();

    /**
     * The dotted key names are the contract shared with the Elasticsearch flow document. If one of
     * them is renamed here the two backends stop being comparable, so pin them.
     */
    @Test
    public void emitsDottedElasticsearchFieldNames() {
        final JsonObject doc = serializer.toJsonObject(TestFlow.full());

        assertTrue(doc.has("netflow.bytes"));
        assertTrue(doc.has("netflow.src_addr"));
        assertTrue(doc.has("netflow.dst_addr"));
        assertTrue(doc.has("netflow.delta_switched"));
        assertTrue(doc.has("netflow.last_switched"));
        assertTrue(doc.has("netflow.sampling_interval"));
        assertTrue(doc.has("netflow.convo_key"));
        assertTrue(doc.has("node_exporter"));

        // Dots must stay in the key, not become nested objects.
        assertFalse("netflow must not be nested", doc.has("netflow"));
    }

    /**
     * proportional attribution downstream depends on these three, so a regression here would be
     * expensive and quiet.
     */
    @Test
    public void preservesTimestampsAsEpochMillis() {
        final JsonObject doc = serializer.toJsonObject(TestFlow.full());

        assertEquals(1_600_000_000_000L, doc.get("@timestamp").getAsLong());
        assertEquals(1_600_000_000_000L, doc.get("netflow.delta_switched").getAsLong());
        assertEquals(1_600_000_060_000L, doc.get("netflow.last_switched").getAsLong());
    }

    /**
     * A bare epoch number is ambiguous between seconds, millis and nanos, so the serializer also
     * emits an explicit RFC3339 field and that is what _time_field points at.
     */
    @Test
    public void emitsUnambiguousRfc3339TimeField() {
        final JsonObject doc = serializer.toJsonObject(TestFlow.full());

        assertEquals("2020-09-13T12:26:40Z", doc.get(FlowJsonSerializer.TIME_FIELD).getAsString());
        assertEquals(FlowJsonSerializer.TIME_FIELD, VictoriaLogsClientConfig.DEFAULT_TIME_FIELD);
    }

    /** Enum wire values differ from the Java constant names in several cases. */
    @Test
    public void usesElasticsearchEnumWireValues() {
        assertEquals("ingress", FlowJsonSerializer.direction(Flow.Direction.INGRESS));
        assertEquals("private", FlowJsonSerializer.locality(Flow.Locality.PRIVATE));
        assertEquals("Netflow v9", FlowJsonSerializer.netflowVersion(Flow.NetflowVersion.V9));
        assertEquals("IPFIX", FlowJsonSerializer.netflowVersion(Flow.NetflowVersion.IPFIX));
        // Note the lower-case "o": the wire value is not the constant name.
        assertEquals("RandomNoutOfNSampling",
                FlowJsonSerializer.samplingAlgorithm(Flow.SamplingAlgorithm.RandomNOutOfNSampling));
    }

    /** Absent values must be omitted, not written as JSON null, or every flow carries dead columns. */
    @Test
    public void omitsNullFields() {
        final JsonObject doc = serializer.toJsonObject(TestFlow.minimal());

        assertFalse(doc.has("netflow.application"));
        assertFalse(doc.has("netflow.src_addr_hostname"));
        assertFalse(doc.has("node_exporter"));
        assertFalse(doc.has("hosts"));
    }

    @Test
    public void rendersVlanUnsignedAsString() {
        // isString(), not getAsString(): a numeric primitive also answers getAsString(), so the
        // "as String" half of this test's name was previously unverifiable by its own assertion.
        final JsonObject doc = new FlowJsonSerializer().toJsonObject(TestFlow.full());
        assertTrue("vlan must be a JSON string, not a number",
                doc.get("netflow.vlan").getAsJsonPrimitive().isString());
        assertEquals("100", doc.get("netflow.vlan").getAsString());
    }


    /**
     * Destination first, matching the order {@code FlowDocument.from()} produces via its setter
     * call order. Reversing this would silently break byte-comparability with the Elasticsearch
     * document, which is the whole basis of the A/B comparison.
     */
    @Test
    public void collectsHostsInElasticsearchOrderDestinationFirst() {
        final JsonObject doc = serializer.toJsonObject(TestFlow.full());
        assertEquals(2, doc.getAsJsonArray("hosts").size());
        assertEquals("10.0.0.1", doc.getAsJsonArray("hosts").get(0).getAsString());
        assertEquals("192.168.1.1", doc.getAsJsonArray("hosts").get(1).getAsString());
    }

    @Test
    public void ndJsonIsOneLinePerFlowAndNewlineTerminated() {
        final String body = serializer.toNdJson(Arrays.asList(TestFlow.full(), TestFlow.minimal()));

        assertTrue(body.endsWith("\n"));
        assertEquals(2, body.split("\n").length);
        // A stray newline inside a document would silently split it into two unparseable lines.
        // Asserted by the line count above: a substring taken up to the first newline cannot contain
        // one, so the obvious-looking check on it holds for every input, including the broken one.
        assertEquals("each line must be a complete document", 2,
                body.chars().filter(c -> c == '\n').count());
    }

    @Test
    public void emptyBatchProducesEmptyBody() {
        assertEquals("", serializer.toNdJson(Collections.emptyList()));
    }

    /**
     * A non-finite double is not JSON, and Gson will happily write it anyway.
     *
     * <p>{@code JsonElement.toString()} goes through a lenient writer, so a NaN sampling interval
     * would emit the bare token {@code NaN}. VictoriaLogs cannot parse that line, skips it, and still
     * answers 2xx — losing the flow silently while the repository counts it as sent. Omitting the
     * field keeps the line parseable and the rest of the flow intact.
     */
    @Test
    public void nonFiniteNumbersAreOmittedRatherThanEmittedAsBareTokens() {
        for (final Double notFinite : new Double[]{Double.NaN,
                Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            final String line = new FlowJsonSerializer()
                    .toJsonLine(TestFlow.full().withSamplingInterval(notFinite));
            assertFalse("must not emit " + notFinite + ": " + line,
                    line.contains("NaN") || line.contains("Infinity"));
            assertFalse("the field is omitted entirely",
                    line.contains("netflow.sampling_interval"));
            // Still valid JSON, and the rest of the flow survived.
            final JsonObject reparsed = JsonParser.parseString(line).getAsJsonObject();
            assertTrue(reparsed.has("netflow.bytes"));
        }
    }

    /** A finite sampling interval is still emitted; the guard must not swallow ordinary values. */
    @Test
    public void finiteSamplingIntervalsAreStillEmitted() {
        final String line = new FlowJsonSerializer()
                .toJsonLine(TestFlow.full().withSamplingInterval(2.5d));
        assertTrue(line, line.contains("netflow.sampling_interval"));
    }

    /**
     * A flow that cannot be rendered must cost only itself.
     *
     * <p>The wire format is one independent line per flow, so aborting the batch for one bad record
     * would lose the other 999 in a default-size chunk.
     */
    @Test
    public void anUnrenderableFlowIsSkippedWithoutLosingTheBatch() {
        // Built from full(), not from a bare TestFlow: a default-constructed one is unrenderable
        // for several reasons besides the thrown exception, so the test would have passed even with
        // the intended path unreachable.
        final Flow poison = new TestFlow() {
            @Override
            public java.time.Instant getTimestamp() {
                throw new IllegalStateException("this flow cannot be read");
            }
        }.withAddresses("10.0.0.1", "10.0.0.2");
        final AtomicInteger skipped = new AtomicInteger();
        final String body = new FlowJsonSerializer()
                .toNdJson(Arrays.asList(TestFlow.full(), poison, TestFlow.full()), skipped);

        assertEquals("the bad flow is counted", 1, skipped.get());
        assertEquals("and the good ones still went out", 2,
                body.lines().filter(l -> !l.isEmpty()).count());
    }
}
