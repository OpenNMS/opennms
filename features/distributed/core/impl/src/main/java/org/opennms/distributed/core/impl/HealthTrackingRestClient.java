/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.distributed.core.impl;

import java.util.concurrent.Callable;

import org.opennms.core.health.api.CachingHealthCheck;
import org.opennms.core.health.api.HealthCheckResponseCache;
import org.opennms.core.health.api.Response;
import org.opennms.distributed.core.api.RestClient;

/**
 * A rest client that informs a {@link HealthCheckResponseCache} about the success / failure of service calls.
 */
public class HealthTrackingRestClient implements RestClient {
    private final RestClient delegate;
    private final HealthCheckResponseCache healthCheckResponseCache;

    public HealthTrackingRestClient(RestClient delegate, HealthCheckResponseCache healthCheckResponseCache) {
        this.delegate = delegate;
        this.healthCheckResponseCache = healthCheckResponseCache;
    }

    private <T> T callAndInformCachingHealthCheck(Callable<T> callable) throws Exception {
        try {
            var v = callable.call();
            if (healthCheckResponseCache != null) {
                healthCheckResponseCache.setResponse(Response.SUCCESS);
            }
            return v;
        } catch (Throwable e) {
            if (healthCheckResponseCache != null) {
                healthCheckResponseCache.setResponse(new Response(e));
            }
            throw e;
        }

    }
    @Override
    public String getVersion() throws Exception {
        return callAndInformCachingHealthCheck(delegate::getVersion);
    }

    @Override
    public void ping() throws Exception {
        callAndInformCachingHealthCheck(() -> { delegate.ping(); return null; });
    }

    @Override
    public String getSnmpV3Users() throws Exception {
        return callAndInformCachingHealthCheck(delegate::getSnmpV3Users);
    }
}
