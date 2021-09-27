/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
