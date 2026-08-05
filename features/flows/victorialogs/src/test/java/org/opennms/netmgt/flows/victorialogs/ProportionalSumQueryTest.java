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

import java.util.Collections;

import org.junit.Test;

public class ProportionalSumQueryTest {

    private static final long MINUTE = 60_000L;
    private static final long MAX_FLOW_DURATION = 2 * MINUTE;

    /**
     * The fan-out must depend only on how long a flow can last, never on how wide the query is.
     * Indexing buckets from the window start instead would make a day-long query at five-minute
     * resolution expand every flow into 288 rows before discarding almost all of them.
     */
    @Test
    public void fanOutDependsOnFlowDurationNotWindowWidth() {
        assertEquals("a 2-minute flow touches at most 2 five-minute buckets",
                2, ProportionalSumQuery.bucketsPerFlow(5 * MINUTE, MAX_FLOW_DURATION));

        final String oneHour = ProportionalSumQuery.build("*", Collections.emptyList(),
                0, 60 * MINUTE, 5 * MINUTE, MAX_FLOW_DURATION);
        final String oneDay = ProportionalSumQuery.build("*", Collections.emptyList(),
                0, 24 * 60 * MINUTE, 5 * MINUTE, MAX_FLOW_DURATION);

        assertTrue(oneHour.contains("format \"[0,1]\""));
        assertTrue("a 24x wider window must not widen the fan-out",
                oneDay.contains("format \"[0,1]\""));
    }

    @Test
    public void bucketCountGrowsWithFinerSteps() {
        // A flow spanning exactly one bucket still straddles two when unaligned.
        assertEquals(3, ProportionalSumQuery.bucketsPerFlow(MINUTE, MAX_FLOW_DURATION));
        assertEquals(13, ProportionalSumQuery.bucketsPerFlow(10_000L, MAX_FLOW_DURATION));
        assertEquals(2, ProportionalSumQuery.bucketsPerFlow(10 * MINUTE, MAX_FLOW_DURATION));
    }

    /** A pathological step/duration ratio must not generate an unbounded literal array. */
    @Test
    public void bucketCountIsCapped() {
        assertEquals(ProportionalSumQuery.MAX_BUCKETS_PER_FLOW,
                ProportionalSumQuery.bucketsPerFlow(1L, Long.MAX_VALUE / 2));
        // The cap's magnitude, not just that one is applied. Comparing the result to the constant
        // alone would keep passing if the constant grew to a million -- which is the unbounded
        // literal array this guard exists to prevent.
        assertTrue("the cap must stay small enough that the unrolled array is sane: "
                        + ProportionalSumQuery.MAX_BUCKETS_PER_FLOW,
                ProportionalSumQuery.MAX_BUCKETS_PER_FLOW <= 4096);
    }

    /** Flow field names contain dots, which LogsQL would otherwise parse as syntax. */
    @Test
    public void fieldNamesAreQuoted() {
        final String q = ProportionalSumQuery.build("*", Collections.singletonList("netflow.application"),
                0, 60 * MINUTE, 5 * MINUTE, MAX_FLOW_DURATION);

        assertTrue(q.contains("\"netflow.bytes\""));
        assertTrue(q.contains("\"netflow.delta_switched\""));
        assertTrue(q.contains("stats by (bstart, \"netflow.application\")"));
    }

    @Test
    public void zeroDurationFlowsAreGivenAMinimumSpan() {
        final String q = ProportionalSumQuery.build("*", Collections.emptyList(),
                0, 60 * MINUTE, 5 * MINUTE, MAX_FLOW_DURATION);
        // Without this a zero-duration flow would divide by zero and be dropped entirely, whereas
        // Elasticsearch attributes it wholly to the bucket containing its start.
        assertTrue(q.contains("max(_rawdur, 1) as _dur"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNonPositiveStep() {
        ProportionalSumQuery.build("*", Collections.emptyList(), 0, MINUTE, 0, MAX_FLOW_DURATION);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsInvertedWindow() {
        ProportionalSumQuery.build("*", Collections.emptyList(), MINUTE, 0, MINUTE, MAX_FLOW_DURATION);
    }

    /**
     * A quoted LogsQL token has no room for a raw control character.
     *
     * <p>These strings are not all ours — a conversation key arrives from the REST layer and a
     * hostname from a PTR record — so a stray newline must become an escape rather than reaching the
     * server, where it would be rejected and fail the whole query instead of matching nothing.
     */
    @Test
    public void quoteEscapesControlCharacters() {
        assertEquals("\"a\\nb\"", ProportionalSumQuery.quote("a\nb"));
        assertEquals("\"a\\rb\"", ProportionalSumQuery.quote("a\rb"));
        assertEquals("\"a\\tb\"", ProportionalSumQuery.quote("a\tb"));
        assertEquals("\"a\\u0000b\"", ProportionalSumQuery.quote("a\u0000b"));
        assertEquals("\"a\\u007fb\"", ProportionalSumQuery.quote("a\u007fb"));
    }

    /** Backslash and quote were always escaped; that must not have regressed. */
    @Test
    public void quoteStillEscapesBackslashAndQuote() {
        assertEquals("\"a\\\\b\"", ProportionalSumQuery.quote("a\\b"));
        assertEquals("\"a\\\"b\"", ProportionalSumQuery.quote("a\"b"));
        assertEquals("\"netflow.bytes\"", ProportionalSumQuery.quote("netflow.bytes"));
    }

    /** Printable non-ASCII is data, not something to escape. */
    @Test
    public void quoteLeavesPrintableUnicodeAlone() {
        assertEquals("\"caf\u00e9\"", ProportionalSumQuery.quote("caf\u00e9"));
    }
}
