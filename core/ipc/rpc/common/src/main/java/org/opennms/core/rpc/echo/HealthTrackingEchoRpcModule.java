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

import static org.opennms.core.rpc.echo.EchoRpcModule.RPC_MODULE_ID;

import java.util.concurrent.CompletableFuture;

import org.opennms.core.health.api.HealthCheckResponseCache;
import org.opennms.core.health.api.Response;
import org.opennms.core.rpc.xml.AbstractXmlRpcModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An echo rpc module that informs a {@link HealthCheckResponseCache} about the success / failure of service calls.
 */
public class HealthTrackingEchoRpcModule extends AbstractXmlRpcModule<EchoRequest, EchoResponse> {

    private static final Logger LOG = LoggerFactory.getLogger(HealthTrackingEchoRpcModule.class);

    private final EchoRpcModule delegate;
    private final HealthCheckResponseCache healthCheckResponseCache;

    public HealthTrackingEchoRpcModule(
            EchoRpcModule delegate,
            HealthCheckResponseCache healthCheckResponseCache
    ) {
        super(EchoRequest.class, EchoResponse.class);
        this.delegate = delegate;
        this.healthCheckResponseCache = healthCheckResponseCache;
    }

    @Override
    public CompletableFuture<EchoResponse> execute(EchoRequest request) {
        LOG.debug("Received echo request - receivedTime: {}", request.getId());
        if (healthCheckResponseCache != null) {
            healthCheckResponseCache.setResponse(Response.SUCCESS);
        }
        return delegate.execute(request);
    }

    @Override
    public String getId() {
        return RPC_MODULE_ID;
    }

    @Override
    public EchoResponse createResponseWithException(Throwable ex) {
        return delegate.createResponseWithException(ex);
    }

}
