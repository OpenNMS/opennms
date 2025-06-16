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
