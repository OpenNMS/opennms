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
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.opennms.integration.api.v1.flows.Flow;

import com.google.gson.JsonObject;

/**
 * Ties what {@link FlowJsonSerializer} writes to what {@link VictoriaLogsFlowQueryService} reads.
 *
 * <p><strong>Why this is not covered by the acceptance test.</strong> {@code ReferenceComparisonIT}
 * ingests the Elasticsearch documents recorded in {@code flow-query-reference.json} directly, adding
 * only {@code _time}. That makes it a faithful check of the query translation — it asks the recorded
 * questions of the recorded data — but it means the class that actually writes flows in production is
 * never on the path. A disagreement between writer and reader would leave that whole suite green
 * while a real deployment returned nothing.
 *
 * <p>The direction spellings below are the sharpest instance. {@code summarize()} decides inbound
 * versus outbound with {@code "ingress".equals(...)}; if the serializer emitted {@code "INGRESS"}
 * every byte in the system would be reported as outbound, and nothing else in the suite would notice
 * — {@code FlowJsonSerializerTest} pins the emitted key set, not the values.
 */
public class WriterReaderContractTest {

    /**
     * Fields the query path reads by name.
     *
     * <p>Spelled out here rather than referenced from the query service so that the two sides are
     * pinned independently: a constant renamed on both sides at once would still be a wire-format
     * change, and this test is what makes that visible.
     */
    private static final List<String> QUERY_CRITICAL_FIELDS = Arrays.asList(
            "netflow.application",
            "netflow.bytes",
            "netflow.convo_key",
            "netflow.delta_switched",
            "netflow.direction",
            "netflow.dscp",
            "netflow.dst_addr",
            "netflow.ecn",
            "netflow.input_snmp",
            "netflow.last_switched",
            "netflow.output_snmp",
            "netflow.src_addr");

    /**
     * The exact strings {@code summarize()} and {@code ProportionalSeriesService} compare against.
     *
     * <p>Lower case, and not the enum's {@code name()} — that is the whole point.
     */
    @Test
    public void directionIsWrittenInTheSpellingTheQueryPathCompares() {
        assertEquals("ingress", FlowJsonSerializer.direction(Flow.Direction.INGRESS));
        assertEquals("egress", FlowJsonSerializer.direction(Flow.Direction.EGRESS));
        assertEquals("unknown", FlowJsonSerializer.direction(Flow.Direction.UNKNOWN));
    }

    /** Every field a query reads must be one the writer actually emits, under that exact name. */
    @Test
    public void writerEmitsEveryFieldTheQueryPathReads() {
        final JsonObject document = new FlowJsonSerializer().toJsonObject(TestFlow.full());
        for (final String field : QUERY_CRITICAL_FIELDS) {
            assertTrue("the query path reads " + field + " but the writer never emits it; "
                    + "queries would silently return nothing for it", document.has(field));
        }
    }

    /**
     * A flow without hostnames omits the hostname fields rather than writing empty ones.
     *
     * <p>This is the half of the contract the reader has to cope with: {@code collectHostnames}
     * groups an address with its hostname and skips empty values, which is only correct because the
     * writer leaves the field out entirely instead of emitting {@code ""}. Were that to change, every
     * address lacking a hostname would map to the empty string and shadow a real one.
     */
    @Test
    public void writerOmitsAbsentHostnamesRatherThanEmittingBlanks() {
        final JsonObject document = new FlowJsonSerializer().toJsonObject(TestFlow.full());

        assertTrue("addresses are always emitted", document.has("netflow.src_addr"));
        assertTrue("addresses are always emitted", document.has("netflow.dst_addr"));
        assertEquals("an absent hostname must be an absent field, not an empty one",
                false, document.has("netflow.src_addr_hostname"));
        assertEquals("an absent hostname must be an absent field, not an empty one",
                false, document.has("netflow.dst_addr_hostname"));
    }
}
