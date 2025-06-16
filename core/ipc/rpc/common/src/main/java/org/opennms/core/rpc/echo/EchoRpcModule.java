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
package org.opennms.core.rpc.echo;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;

import org.opennms.core.rpc.xml.AbstractXmlRpcModule;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

public class EchoRpcModule extends AbstractXmlRpcModule<EchoRequest, EchoResponse> {

    public static final EchoRpcModule INSTANCE = new EchoRpcModule();

    public static final String RPC_MODULE_ID = "Echo";

    private static final Supplier<Timer> TIMER_SUPPLIER = Suppliers.memoize(() -> new Timer("EchoRpcModule"));

    public EchoRpcModule() {
        super(EchoRequest.class, EchoResponse.class);
    }

    public void beforeRun() { }

    @Override
    public CompletableFuture<EchoResponse> execute(final EchoRequest request) {
        final CompletableFuture<EchoResponse> future = new CompletableFuture<>();
        if (request.getDelay() != null) {
            TIMER_SUPPLIER.get().schedule(new TimerTask() {
                @Override
                public void run() {
                    processRequest(request, future);
                }
            }, request.getDelay());
        } else {
            processRequest(request, future);
        }
        return future;
    }

    public void processRequest(EchoRequest request, CompletableFuture<EchoResponse> future) {
        beforeRun();
        if (request.shouldThrow()) {
            future.completeExceptionally(new MyEchoException(request.getMessage()));
        } else {
            EchoResponse response = new EchoResponse();
            response.setId(request.getId());
            response.setMessage(request.getMessage());
            response.setBody(request.getBody());
            future.complete(response);
        }
    }

    @Override
    public String getId() {
        return RPC_MODULE_ID;
    }

    @Override
    public EchoResponse createResponseWithException(Throwable ex) {
        return new EchoResponse(ex);
    }
}
