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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.Test;
import org.opennms.core.rpc.api.RpcClient;
import org.opennms.core.rpc.api.RpcClientFactory;
import org.opennms.core.rpc.echo.EchoRequest;
import org.opennms.core.rpc.echo.EchoResponse;
import org.opennms.core.rpc.echo.EchoRpcModule;
import org.opennms.core.test.camel.CamelBlueprintTest;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.test.ThreadLocker;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Used to verify and validate the thread profiles of the RPC
 * server via the EchoRpcModule.
 *
 * @author jwhite
 */
public abstract class EchoRpcThreadIT extends CamelBlueprintTest {

    public static final int NTHREADS = 100;

    public static final String REMOTE_LOCATION_NAME = "remote";

    @Autowired
    private OnmsDistPoller identity;

    @Autowired
    private RpcClientFactory rpcClientFactory;

    public ThreadLockingEchoRpcModule lockingRpcModule = new ThreadLockingEchoRpcModule();

    @Test(timeout=60000)
    public void canProcessManyRequestsAsynchronously() throws Exception {
        // Execute a request via a remote location
        assertNotEquals(REMOTE_LOCATION_NAME, identity.getLocation());

        // Lock the run method in our RPC module, we want to validate
        // the number of threads that are "running" the module
        CompletableFuture<Integer> runLockedFuture = lockingRpcModule.getRunLocker().waitForThreads(NTHREADS);

        // Fire off NTHREADS request
        ThreadLockingEchoClient client = new ThreadLockingEchoClient(rpcClientFactory, lockingRpcModule);
        List<CompletableFuture<EchoResponse>> futures = new ArrayList<>();
        for (int i = 0; i < NTHREADS; i++) {
            EchoRequest request = new EchoRequest("ping");
            request.setTimeToLiveMs(30000L);
            request.setLocation(REMOTE_LOCATION_NAME);
            futures.add(client.execute(request));
        }

        // Wait for all the threads calling run() to be locked
        runLockedFuture.get();

        // Release and verify that all the futures return
        lockingRpcModule.getRunLocker().release();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[NTHREADS])).get();
    }

    public static class ThreadLockingEchoClient implements RpcClient<EchoRequest, EchoResponse> {

        private RpcClient<EchoRequest, EchoResponse> m_delegate;

        public ThreadLockingEchoClient(RpcClientFactory rpcClientFactory, ThreadLockingEchoRpcModule threadLockingEchoRpcModule) {
            m_delegate = rpcClientFactory.getClient(threadLockingEchoRpcModule);
        }

        @Override
        public CompletableFuture<EchoResponse> execute(EchoRequest request) {
            return m_delegate.execute(request);
        }
    }

    public static class ThreadLockingEchoRpcModule extends EchoRpcModule {
        private final ThreadLocker runLocker = new ThreadLocker();

        @Override
        public void beforeRun() {
            runLocker.park();
        }

        @Override
        public String getId() {
            return "Locking" + RPC_MODULE_ID;
        }

        @Override
        public CompletableFuture<EchoResponse> execute(final EchoRequest request) {
            final CompletableFuture<EchoResponse> future = new CompletableFuture<>();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (request.getDelay() != null) {
                        try {
                            Thread.sleep(request.getDelay());
                            processRequest(request, future);
                        } catch (InterruptedException e) {
                            future.completeExceptionally(e);
                        }
                    } else {
                        processRequest(request, future);
                    }
                }
            }).start();
            return future;
        }

        public ThreadLocker getRunLocker() {
            return runLocker;
        }
    }
}
