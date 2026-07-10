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
package org.opennms.core.rpc.camel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;
import org.opennms.core.rpc.api.RpcClient;
import org.opennms.core.rpc.api.RpcModule;
import org.opennms.core.rpc.api.RpcRequest;
import org.opennms.core.rpc.api.RpcResponse;

import io.opentracing.Span;

/**
 * NMS-19951 — regression test for the local-execution timeout backstop.
 *
 * <p>{@link CamelRpcClientFactory#getClient(RpcModule)}'s {@code execute()} short-circuits a request
 * for the current location directly to the module. Previously that direct path applied no timeout,
 * so a local {@link RpcModule} whose future never completed (e.g. a detector/SNMP async op whose
 * completion callback is lost) returned a future that hung forever — the mechanism behind the
 * provisiond scan wedge. The fix bounds the direct path to the request TTL (or the default RPC
 * timeout when none is set), mirroring the remote/Minion branch.</p>
 *
 * <p>These tests assert the fixed behavior: a local request with a TTL whose module never completes
 * is completed exceptionally with a {@link TimeoutException} at ~TTL, and a module that does complete
 * still returns its result.</p>
 */
public class LocalRpcTimeoutTest {

    private static final String LOCAL_LOCATION = "Default";

    /**
     * The fix: a local request whose module future never completes is bounded by the TTL and
     * completes exceptionally with a {@link TimeoutException}, rather than hanging forever.
     */
    @Test
    public void localExecutionAppliesTtlTimeoutWhenModuleNeverCompletes() throws Exception {
        // A module whose execute() returns a future that is never completed — models a local
        // detector/SNMP async op whose completion callback is lost.
        final CompletableFuture<StubResponse> neverCompletes = new CompletableFuture<>();
        final RpcModule<StubRequest, StubResponse> hangingModule = stubModule(req -> neverCompletes);

        final CamelRpcClientFactory factory = new CamelRpcClientFactory();
        factory.setLocation(LOCAL_LOCATION); // NB: start() intentionally NOT called — the local branch needs no Camel.

        // Request targets the local location and carries a 1s TTL.
        final StubRequest request = new StubRequest(LOCAL_LOCATION, 1_000L);

        final long start = System.currentTimeMillis();
        final CompletableFuture<StubResponse> future = factory.getClient(hangingModule).execute(request);

        try {
            // Generous ceiling; the ~1s TTL bound should fire well before this.
            future.get(10, TimeUnit.SECONDS);
            fail("Expected the local future to be bounded by the TTL and time out");
        } catch (ExecutionException expected) {
            assertTrue("expected a TimeoutException cause but was: " + expected.getCause(),
                    expected.getCause() instanceof TimeoutException);
        }
        final long elapsedMs = System.currentTimeMillis() - start;
        assertTrue("timeout should fire near the 1s TTL, but took " + elapsedMs + "ms", elapsedMs < 5_000L);
    }

    /**
     * Control: when the module's future DOES complete, the local path returns it (a null location is
     * also treated as local by the factory).
     */
    @Test
    public void localExecutionReturnsModuleResultWhenItCompletes() throws Exception {
        final RpcModule<StubRequest, StubResponse> okModule =
                stubModule(req -> CompletableFuture.completedFuture(new StubResponse(null)));

        final CamelRpcClientFactory factory = new CamelRpcClientFactory();
        factory.setLocation(LOCAL_LOCATION);

        final StubRequest request = new StubRequest(null, 1_000L);

        final StubResponse response = factory.getClient(okModule).execute(request).get(5, TimeUnit.SECONDS);
        assertEquals(null, response.getErrorMessage());
    }

    // -----------------------------------------------------------------------
    // Minimal stubs (no Camel/Spring/Mockito needed for the local branch).
    // -----------------------------------------------------------------------

    private interface Exec {
        CompletableFuture<StubResponse> execute(StubRequest request);
    }

    private static RpcModule<StubRequest, StubResponse> stubModule(final Exec exec) {
        return new RpcModule<StubRequest, StubResponse>() {
            @Override public CompletableFuture<StubResponse> execute(StubRequest request) { return exec.execute(request); }
            @Override public String getId() { return "stub"; }
            @Override public String marshalRequest(StubRequest request) { return ""; }
            @Override public StubRequest unmarshalRequest(String request) { return new StubRequest(LOCAL_LOCATION, null); }
            @Override public String marshalResponse(StubResponse response) { return ""; }
            @Override public StubResponse unmarshalResponse(String response) { return new StubResponse(null); }
            @Override public StubResponse createResponseWithException(Throwable ex) { return new StubResponse(ex.getMessage()); }
        };
    }

    private static final class StubRequest implements RpcRequest {
        private final String location;
        private final Long ttlMs;
        StubRequest(String location, Long ttlMs) { this.location = location; this.ttlMs = ttlMs; }
        @Override public String getLocation() { return location; }
        @Override public String getSystemId() { return null; }
        @Override public Long getTimeToLiveMs() { return ttlMs; }
        @Override public Map<String, String> getTracingInfo() { return Collections.emptyMap(); }
        @Override public Span getSpan() { return null; }
    }

    private static final class StubResponse implements RpcResponse {
        private final String errorMessage;
        StubResponse(String errorMessage) { this.errorMessage = errorMessage; }
        @Override public String getErrorMessage() { return errorMessage; }
    }
}
