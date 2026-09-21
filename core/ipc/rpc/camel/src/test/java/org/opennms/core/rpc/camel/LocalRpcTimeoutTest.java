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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Test;
import org.opennms.core.rpc.api.RpcModule;
import org.opennms.core.rpc.api.RpcRequest;
import org.opennms.core.rpc.api.RpcResponse;

import io.opentracing.Span;

/**
 * NMS-20006 — local (same-location) RPC execution is bounded by a fixed,
 * configurable timeout instead of the request TTL.
 */
public class LocalRpcTimeoutTest {

    private static final String LOCAL_LOCATION = "Default";

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @After
    public void tearDown() {
        System.clearProperty(CamelRpcClientFactory.LOCAL_EXEC_TIMEOUT_PROPERTY);
        scheduler.shutdownNow();
    }

    /**
     * Regression guard for NMS-20006: a local operation that completes after its
     * request TTL has expired must still return its result. Enforcing the TTL on
     * local execution is what broke slow-but-healthy SNMP collections in 36.0.2 /
     * Meridian 2024.3.11 — this test fails if that behavior is ever reintroduced.
     */
    @Test
    public void slowLocalExecutionCompletesDespiteExpiredTtl() throws Exception {
        final CompletableFuture<StubResponse> completesLate = new CompletableFuture<>();
        scheduler.schedule(() -> completesLate.complete(new StubResponse(null)), 1_500, TimeUnit.MILLISECONDS);
        final RpcModule<StubRequest, StubResponse> slowModule = stubModule(completesLate);

        final CamelRpcClientFactory factory = new CamelRpcClientFactory();
        factory.setLocation(LOCAL_LOCATION);

        // TTL far below the module's completion time.
        final StubRequest request = new StubRequest(LOCAL_LOCATION, 200L);

        final StubResponse response = factory.getClient(slowModule).execute(request).get(10, TimeUnit.SECONDS);
        assertNull(response.getErrorMessage());
    }

    /**
     * A custom timeout is honored: with a 5 ms timeout configured, a module future
     * that never completes fails promptly with a TimeoutException instead of
     * wedging the caller.
     */
    @Test
    public void customLocalTimeoutIsHonored() throws Exception {
        System.setProperty(CamelRpcClientFactory.LOCAL_EXEC_TIMEOUT_PROPERTY, "5");
        final CompletableFuture<StubResponse> neverCompletes = new CompletableFuture<>();
        final RpcModule<StubRequest, StubResponse> hangingModule = stubModule(neverCompletes);

        final CamelRpcClientFactory factory = new CamelRpcClientFactory();
        factory.setLocation(LOCAL_LOCATION); // start() intentionally NOT called — the local branch needs no Camel.

        final StubRequest request = new StubRequest(LOCAL_LOCATION, null);

        try {
            factory.getClient(hangingModule).execute(request).get(10, TimeUnit.SECONDS);
            fail("Expected the local timeout to fail the hung execution");
        } catch (ExecutionException e) {
            assertTrue("expected a TimeoutException cause but was: " + e.getCause(),
                    e.getCause() instanceof TimeoutException);
        }
    }

    // -----------------------------------------------------------------------
    // Minimal stubs (no Camel/Spring/Mockito needed for the local branch).
    // -----------------------------------------------------------------------

    private static RpcModule<StubRequest, StubResponse> stubModule(final CompletableFuture<StubResponse> future) {
        return new RpcModule<StubRequest, StubResponse>() {
            @Override public CompletableFuture<StubResponse> execute(StubRequest request) { return future; }
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
