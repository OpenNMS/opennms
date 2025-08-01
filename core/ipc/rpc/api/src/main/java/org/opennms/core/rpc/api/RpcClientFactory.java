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
package org.opennms.core.rpc.api;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

/**
 * Creates a {@link RpcClient} that can be used to invoke RPCs against the given {@link RpcModule}.
 *
 * @author jwhite
 */
public interface RpcClientFactory {

    String LOG_PREFIX = "ipc";
    // RPC Metrics related constants.
    String JMX_DOMAIN_RPC = "org.opennms.core.ipc.rpc";
    String RPC_REQUEST_SENT = "requestSent";
    String RPC_REQUESTS_RECEIVED = "requestsReceived";
    String RPC_FAILED = "requestFailed";
    String RPC_DURATION = "duration";
    String RPC_REQUEST_SIZE = "requestSize";
    String RPC_RESPONSE_SIZE = "responseSize";

    <R extends RpcRequest, S extends RpcResponse> RpcClient<R, S> getClient(RpcModule<R, S> module);


    static void updateDuration(MetricRegistry metricRegistry, String location, String moduleId, long duration) {
        Histogram histogram = metricRegistry.histogram(MetricRegistry.name(location, moduleId, RPC_DURATION));
        histogram.update(duration);
    }

    static void markRpcCount(MetricRegistry metricRegistry, String location, String moduleId) {
        Meter rpcCount = metricRegistry.meter(MetricRegistry.name(location, moduleId, RPC_REQUEST_SENT));
        rpcCount.mark();
    }

    static void updateRequestSize(MetricRegistry metricRegistry, String location, String moduleId, int requestSize) {
        Histogram histogram = metricRegistry.histogram(MetricRegistry.name(location, moduleId, RPC_REQUEST_SIZE));
        histogram.update(requestSize);
    }

    static void updateResponseSize(MetricRegistry metricRegistry, String location, String moduleId, int responseSize) {
        Histogram histogram = metricRegistry.histogram(MetricRegistry.name(location, moduleId, RPC_RESPONSE_SIZE));
        histogram.update(responseSize);
    }

    static void markFailed(MetricRegistry metricRegistry, String location, String moduleId) {
        Meter failed = metricRegistry.meter(MetricRegistry.name(location, moduleId, RPC_FAILED));
        failed.mark();
    }

}
