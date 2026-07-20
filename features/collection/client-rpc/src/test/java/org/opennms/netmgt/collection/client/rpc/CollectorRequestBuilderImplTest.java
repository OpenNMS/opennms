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
package org.opennms.netmgt.collection.client.rpc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionException;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.CollectionStatus;
import org.opennms.netmgt.collection.api.CollectorAdaptor;

/**
 * Validates that adaptors are notified on both success and failure paths
 * of a collection request. The failure path matters for token-auth: when
 * the collector throws on a 401, {@code TokenAuthCollectorAdaptor} still
 * needs to see a FAILED result so it can invalidate the stale token.
 */
public class CollectorRequestBuilderImplTest {

    private final CollectionAgent agent = mock(CollectionAgent.class);
    private final Map<String, Object> attrs = new HashMap<>();

    @Test
    public void successPath_runsAdaptorsOnRealResult() {
        final CollectionSet successSet = mock(CollectionSet.class);
        final CollectorAdaptor adaptor = mock(CollectorAdaptor.class);
        when(adaptor.handleCollectionResult(eq(agent), any(), eq(successSet))).thenReturn(successSet);

        final CollectionSet out = CollectorRequestBuilderImpl.applyAdaptorsAndPropagate(
                agent, attrs, Arrays.asList(adaptor), successSet, null);

        assertSame(successSet, out);
        verify(adaptor, times(1)).handleCollectionResult(eq(agent), any(), eq(successSet));
    }

    @Test
    public void failurePath_runsAdaptorsOnSyntheticFailedSetThenRethrows() {
        final CollectorAdaptor adaptor = mock(CollectorAdaptor.class);
        final ArgumentCaptor<CollectionSet> seen = ArgumentCaptor.forClass(CollectionSet.class);
        when(adaptor.handleCollectionResult(eq(agent), any(), seen.capture()))
                .thenAnswer(inv -> inv.getArgument(2));

        final RuntimeException boom = new RuntimeException("collector threw");

        try {
            CollectorRequestBuilderImpl.applyAdaptorsAndPropagate(
                    agent, attrs, Arrays.asList(adaptor), null, boom);
            fail("expected the original exception to be rethrown");
        } catch (RuntimeException re) {
            assertSame("original exception should propagate", boom, re);
        }

        verify(adaptor, times(1)).handleCollectionResult(eq(agent), any(), any(CollectionSet.class));
        final CollectionSet supplied = seen.getValue();
        assertNotNull("adaptor must receive a non-null CollectionSet", supplied);
        assertEquals("adaptor must see FAILED status",
                CollectionStatus.FAILED, supplied.getStatus());
    }

    @Test
    public void failurePath_unwrapsCompletionExceptionCause() {
        final IOException ioex = new IOException("HTTP 401");
        final CompletionException wrapper = new CompletionException(ioex);
        final CollectorAdaptor adaptor = mock(CollectorAdaptor.class);
        when(adaptor.handleCollectionResult(eq(agent), any(), any())).thenAnswer(inv -> inv.getArgument(2));

        try {
            CollectorRequestBuilderImpl.applyAdaptorsAndPropagate(
                    agent, attrs, Arrays.asList(adaptor), null, wrapper);
            fail("expected an exception");
        } catch (CompletionException ce) {
            assertSame("cause should be the original IOException", ioex, ce.getCause());
        }
    }

    @Test
    public void noAdaptors_failurePathStillRethrows() {
        final RuntimeException boom = new RuntimeException("collector threw");
        try {
            CollectorRequestBuilderImpl.applyAdaptorsAndPropagate(
                    agent, attrs, Collections.<CollectorAdaptor>emptyList(), null, boom);
            fail();
        } catch (RuntimeException re) {
            assertSame(boom, re);
        }
    }

    @Test
    public void successPath_emptyAdaptorListReturnsOriginal() {
        final CollectionSet set = mock(CollectionSet.class);
        final List<CollectorAdaptor> none = Collections.<CollectorAdaptor>emptyList();

        final CollectionSet out = CollectorRequestBuilderImpl.applyAdaptorsAndPropagate(
                agent, attrs, none, set, null);

        assertSame(set, out);
    }

    @Test
    public void failurePath_setsAuthFailureMarker_whenCauseMessageContainsPrefix() {
        final IOException auth = new IOException("Can't retrieve /foo because auth failure: HTTP 401");
        final CollectorAdaptor adaptor = mock(CollectorAdaptor.class);
        when(adaptor.handleCollectionResult(eq(agent), any(), any())).thenAnswer(inv -> inv.getArgument(2));

        try {
            CollectorRequestBuilderImpl.applyAdaptorsAndPropagate(
                    agent, attrs, Arrays.asList(adaptor), null, auth);
            fail();
        } catch (CompletionException expected) {
            // checked exception gets wrapped on rethrow
        }

        assertEquals(Boolean.TRUE, attrs.get(CollectorAdaptor.AUTH_FAILURE_PARAM));
    }

    @Test
    public void failurePath_setsAuthFailureMarker_whenNestedCauseMatches() {
        final IOException inner = new IOException("auth failure: HTTP 403 from https://example/data");
        final RuntimeException outer = new RuntimeException("wrapper", inner);
        final CollectorAdaptor adaptor = mock(CollectorAdaptor.class);
        when(adaptor.handleCollectionResult(eq(agent), any(), any())).thenAnswer(inv -> inv.getArgument(2));

        try {
            CollectorRequestBuilderImpl.applyAdaptorsAndPropagate(
                    agent, attrs, Arrays.asList(adaptor), null, outer);
            fail();
        } catch (RuntimeException expected) {
            // rethrown
        }

        assertEquals(Boolean.TRUE, attrs.get(CollectorAdaptor.AUTH_FAILURE_PARAM));
    }

    @Test
    public void failurePath_doesNotSetAuthFailureMarker_forNonAuthError() {
        final IOException nonAuth = new IOException("HTTP 500 from https://example/data");
        final CollectorAdaptor adaptor = mock(CollectorAdaptor.class);
        when(adaptor.handleCollectionResult(eq(agent), any(), any())).thenAnswer(inv -> inv.getArgument(2));

        try {
            CollectorRequestBuilderImpl.applyAdaptorsAndPropagate(
                    agent, attrs, Arrays.asList(adaptor), null, nonAuth);
            fail();
        } catch (CompletionException expected) {
            // rethrown
        }

        assertEquals(null, attrs.get(CollectorAdaptor.AUTH_FAILURE_PARAM));
    }

    @Test
    public void successPath_lastAdaptorReturnValueWins() {
        final CollectionSet first = mock(CollectionSet.class);
        final CollectionSet rewritten = mock(CollectionSet.class);
        final CollectorAdaptor a1 = mock(CollectorAdaptor.class);
        final CollectorAdaptor a2 = mock(CollectorAdaptor.class);
        when(a1.handleCollectionResult(eq(agent), any(), eq(first))).thenReturn(rewritten);
        when(a2.handleCollectionResult(eq(agent), any(), eq(rewritten))).thenReturn(rewritten);

        final CollectionSet out = CollectorRequestBuilderImpl.applyAdaptorsAndPropagate(
                agent, attrs, Arrays.asList(a1, a2), first, null);

        assertSame(rewritten, out);
        verify(a1, never()).handleCollectionResult(eq(agent), any(), eq(rewritten));
    }
}
