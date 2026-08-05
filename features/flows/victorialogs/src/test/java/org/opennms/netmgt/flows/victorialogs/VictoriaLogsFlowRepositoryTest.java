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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.codahale.metrics.MetricRegistry;

public class VictoriaLogsFlowRepositoryTest {

    private VictoriaLogsClient client;
    private MetricRegistry metrics;
    private VictoriaLogsFlowRepository repository;

    @Before
    public void setUp() {
        client = mock(VictoriaLogsClient.class);
        metrics = new MetricRegistry();
        repository = new VictoriaLogsFlowRepository(metrics, client);
        // Not setBulkFlushMs(0), which the setter refuses as non-positive and silently ignores.
        // These tests drive sending through flushNow() and never start the scheduler, so the flush
        // period is irrelevant -- saying so beats a call that looks load-bearing and does nothing.
    }

    /**
     * The important one. {@code PipelineImpl} persists to every registered repository in an
     * unguarded loop, so an exception escaping here would abort that loop and could stop the
     * Elasticsearch repository being called at all — a VictoriaLogs outage would become a total
     * flow-persistence outage.
     */
    @Test
    public void backendFailureNeverPropagates() throws Exception {
        doThrow(new VictoriaLogsException("backend is down")).when(client).ingest(anyString());

        repository.setBulkSize(1);
        repository.persist(flows(5));
        repository.stop();

        assertEquals("failed flows must be counted as dropped",
                5, metrics.meter("flowsDropped").getCount());
        assertEquals("and never as persisted",
                0, metrics.meter("flowsPersisted").getCount());
    }

    /** Flows lost to a failed send must show up on the meters, not vanish. */
    @Test
    public void everyFlowIsEitherPersistedOrDropped() throws Exception {
        repository.setBulkSize(10);
        // persist() only buffers -- sending is the flush thread's job, so each batch is flushed
        // explicitly here rather than waiting on a timer.
        repository.persist(flows(10));
        repository.flushNow();

        doThrow(new VictoriaLogsException("backend is down")).when(client).ingest(anyString());
        repository.persist(flows(10));
        repository.flushNow();
        repository.stop();

        final long persisted = metrics.meter("flowsPersisted").getCount();
        final long dropped = metrics.meter("flowsDropped").getCount();
        assertEquals("20 flows in, 20 accounted for", 20, persisted + dropped);
        assertEquals(10, persisted);
        assertEquals(10, dropped);
    }

    /**
     * A full buffer must not be serialized as one enormous request. After an outage the buffer can
     * hold {@code maxBufferedFlows} entries, and sending them in a single body would hold the
     * NDJSON string, its UTF-8 bytes and the gzipped copy in memory at once.
     */
    @Test
    public void flushChunksTheBufferAtBulkSize() throws Exception {
        final List<Integer> lineCounts = new ArrayList<>();
        doAnswer(invocation -> {
            lineCounts.add(invocation.<String>getArgument(0).split("\n").length);
            return null;
        }).when(client).ingest(anyString());

        // 250 flows at a bulk size of 100: persist() buffers all of them and the shutdown flush sends
        // them as 100, 100 and 50 -- three requests, none exceeding the bulk size.
        repository.setBulkSize(100);
        repository.persist(flows(250));
        repository.stop();

        verify(client, atLeast(3)).ingest(anyString());
        for (final Integer count : lineCounts) {
            assertTrue("a request carried " + count + " flows, exceeding bulkSize", count <= 100);
        }
        assertEquals(250, lineCounts.stream().mapToInt(Integer::intValue).sum());
    }

    /** Beyond maxBufferedFlows the excess is dropped and counted rather than growing the heap. */
    @Test
    public void buffersAreBoundedAndOverflowIsCounted() {
        repository.setBulkSize(Integer.MAX_VALUE);
        repository.setMaxBufferedFlows(10);

        repository.persist(flows(25));

        assertEquals("15 flows beyond the cap must be counted as dropped",
                15, metrics.meter("flowsDropped").getCount());
    }

    private static List<TestFlow> flows(final int count) {
        final List<TestFlow> flows = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            flows.add(TestFlow.full().withConvoKey("convo-" + i));
        }
        return flows;
    }

    /**
     * Zero reads like "unlimited" and would mean the opposite.
     *
     * <p>With a non-positive bound the headroom check fails for every call, so every flow is dropped
     * and the only symptom is a rate-limited warning blaming VictoriaLogs for being unreachable —
     * which it is not. The two setters either side of this one already refuse bad values.
     */
    @Test
    public void aNonPositiveBufferBoundIsRefusedRatherThanTurningPersistenceOff() {
        final int original = repository.getMaxBufferedFlows();

        repository.setMaxBufferedFlows(0);
        assertEquals("zero is ignored", original, repository.getMaxBufferedFlows());
        repository.setMaxBufferedFlows(-1);
        assertEquals("so is negative", original, repository.getMaxBufferedFlows());

        repository.persist(List.of(TestFlow.full()));
        assertEquals("and flows are still accepted", 0, metrics.meter("flowsDropped").getCount());

        repository.setMaxBufferedFlows(5);
        assertEquals("a positive value still applies", 5, repository.getMaxBufferedFlows());
    }

    /**
     * An Error must not end flushing for the life of the process.
     *
     * <p>{@code scheduleWithFixedDelay} cancels a task that throws, and serializing a chunk holds the
     * string, its bytes and the gzipped copy at once — so OutOfMemoryError is a real outcome here,
     * not a hypothetical. Catching only Exception would let it past and leave a repository that
     * accepts flows forever and sends none, with nothing logged.
     */
    @Test
    public void anErrorFromTheBackendDoesNotEndFlushing() throws Exception {
        doThrow(new OutOfMemoryError("simulated"))
                .doNothing()
                .when(client).ingest(anyString());

        repository.persist(List.of(TestFlow.full()));
        repository.flushNow();          // must not propagate the Error

        repository.persist(List.of(TestFlow.full()));
        repository.flushNow();          // and flushing must still work afterwards

        verify(client, atLeast(2)).ingest(anyString());
    }

    /**
     * The scheduled flush path keeps working after an Error during a send.
     *
     * <p>This is the only test that drives {@code start()}, so it is the only one that exercises the
     * scheduler, {@code requestFlush}, the {@code flushQueued} handshake and {@code flushSafely} at
     * all — the sibling tests all call {@code flushNow()}, which bypasses every one of them.
     *
     * <p>Be precise about what it does <em>not</em> pin: {@code sendQuietly} catches {@link
     * Throwable} itself, so an Error raised by the send never reaches {@code flushSafely}'s own
     * catch. That catch is a backstop for anything thrown outside the send — draining, or the loop
     * itself — and no test reaches it, because a mocked client gives no way to raise an Error there.
     * Narrowing {@code flushSafely} to {@code Exception} alone would therefore still pass this. What
     * is genuinely pinned is that an Error during a send does not stop later scheduled flushes,
     * which was untested in any form before.
     */
    @Test
    public void anErrorOnTheScheduledPathDoesNotCancelTheSchedule() throws Exception {
        final java.util.concurrent.atomic.AtomicInteger calls =
                new java.util.concurrent.atomic.AtomicInteger();
        final java.util.concurrent.CountDownLatch sends =
                new java.util.concurrent.CountDownLatch(2);
        doAnswer(invocation -> {
            sends.countDown();
            if (calls.incrementAndGet() == 1) {
                throw new OutOfMemoryError("simulated");
            }
            return null;
        }).when(client).ingest(anyString());

        repository.setDisabled(false);
        repository.setBulkSize(1);
        repository.setBulkFlushMs(50);
        repository.start();
        try {
            repository.persist(List.of(TestFlow.full()));
            repository.persist(List.of(TestFlow.full()));

            assertTrue("the flush schedule must still be running after an Error killed one flush",
                    sends.await(15, java.util.concurrent.TimeUnit.SECONDS));
        } finally {
            repository.stop();
        }
    }
}
