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

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.flows.filter.api.Filter;
import org.opennms.netmgt.flows.filter.api.TimeRangeFilter;

/**
 * The parts of the query service that can be settled without a server.
 *
 * <p>Almost everything this class does is only meaningful against real data, which is what
 * {@code ReferenceComparisonIT} is for. What belongs here is the handful of decisions taken
 * <em>before</em> any query is built — those are invisible to the reference comparison precisely
 * because they result in no query at all.
 */
public class VictoriaLogsFlowQueryServiceTest {

    private VictoriaLogsClient client;
    private VictoriaLogsFlowQueryService service;

    @Before
    public void setUp() {
        client = mock(VictoriaLogsClient.class);
        service = new VictoriaLogsFlowQueryService(client);
    }

    @org.junit.After
    public void tearDown() {
        if (service != null) {
            service.stop();
        }
    }

    private static List<Filter> window() {
        return Collections.singletonList(new TimeRangeFilter(0, 100));
    }

    /**
     * Asking for no entities must answer with nothing — even when "Other" was requested.
     *
     * <p>The complement of nothing is everything, so a series that honoured {@code includeOther}
     * here would answer a request naming no applications with a chart of all the traffic there is,
     * labelled "Other". {@code RawFlowQueryService.getSeriesFor} short-circuits before it looks at
     * the flag for the same reason.
     *
     * <p>Verifying that the client is never touched is the real assertion: an empty table could also
     * come back from a query that ran and found nothing, and that is a different behaviour with a
     * different cost.
     */
    @Test
    public void anEmptyEntitySetIsAnsweredWithoutQueryingEvenWhenOtherIsRequested() throws Exception {
        assertTrue("applications", service
                .getApplicationSeries(Collections.emptySet(), 10, true, window()).get().isEmpty());
        assertTrue("hosts", service
                .getHostSeries(Collections.emptySet(), 10, true, window()).get().isEmpty());
        assertTrue("conversations", service
                .getConversationSeries(Collections.emptySet(), 10, true, window()).get().isEmpty());

        verifyNoInteractions(client);
    }

    /**
     * The Top-N methods must <em>not</em> short-circuit on N=0 with "Other".
     *
     * <p>That request is coherent — it asks for the complement of an empty top list, which is all
     * traffic — and the recorded Elasticsearch answers serve it rather than returning nothing. The
     * guard added for the explicit-set methods above must not have leaked into this path, so this
     * asserts the query is attempted at all.
     */
    @Test
    public void topNWithZeroStillAsksForOther() throws Exception {
        final org.mockito.ArgumentCaptor<String> asked =
                org.mockito.ArgumentCaptor.forClass(String.class);
        org.mockito.Mockito.when(client.query(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(java.util.Collections.emptyList());

        service.getTopNApplicationSeries(0, 10, true, window()).get();

        org.mockito.Mockito.verify(client, org.mockito.Mockito.atLeastOnce())
                .query(asked.capture());
        // Not merely "some query happened". With an empty top list the complement is everything, so
        // the query must NOT narrow to a set of applications -- which is exactly what it would do if
        // the empty-set short-circuit added for the explicit-set methods had leaked into this path.
        assertTrue("the Other query must select the complement of nothing: " + asked.getAllValues(),
                asked.getAllValues().stream().anyMatch(q -> !q.contains("netflow.application\":=\"")));
    }

    /** A time range is required to total anything; without one there is nothing to take a share of. */
    @Test(expected = IllegalArgumentException.class)
    public void aSummaryWithoutATimeRangeIsRejected() throws Throwable {
        try {
            service.getApplicationSummaries(Set.of("http"), false, Collections.emptyList()).get();
        } catch (final java.util.concurrent.ExecutionException e) {
            throw e.getCause();
        }
    }

    /**
     * A query discarded by shutdown must fail, not hang.
     *
     * <p>{@code shutdownNow()} throws away whatever is still queued, and a discarded task never runs
     * its body — so its future would be neither completed nor failed. {@code
     * FlowRestServiceImpl.waitForFuture} calls {@code get()} with no timeout, so each abandoned
     * query would hold a request thread for the life of the process. Nothing exercised this: the
     * other tests here call {@code stop()} only from teardown, with nothing outstanding.
     */
    @Test
    public void shutdownFailsQueuedQueriesRatherThanLeavingThemUnsettled() throws Exception {
        final java.util.concurrent.CountDownLatch running = new java.util.concurrent.CountDownLatch(1);
        final java.util.concurrent.CountDownLatch release = new java.util.concurrent.CountDownLatch(1);
        final java.util.concurrent.ExecutorService single =
                java.util.concurrent.Executors.newSingleThreadExecutor();
        final VictoriaLogsFlowQueryService blocking =
                new VictoriaLogsFlowQueryService(client, 120_000L, single);
        org.mockito.Mockito.when(client.query(org.mockito.ArgumentMatchers.anyString()))
                .thenAnswer(invocation -> {
                    running.countDown();
                    release.await();
                    return Collections.emptyList();
                });
        try {
            // Occupies the single worker...
            blocking.getFlowCount(window());
            assertTrue("the first query should be running", running.await(5, SECONDS));
            // ...so this one can only be sitting in the queue.
            final java.util.concurrent.CompletableFuture<Long> queued = blocking.getFlowCount(window());

            blocking.stop();

            try {
                queued.get(5, SECONDS);
                org.junit.Assert.fail("a discarded query must fail rather than never settle");
            } catch (final java.util.concurrent.ExecutionException expected) {
                assertTrue(String.valueOf(expected.getCause()),
                        expected.getCause() instanceof VictoriaLogsException);
            }
        } finally {
            release.countDown();
            single.shutdownNow();
        }
    }
}
