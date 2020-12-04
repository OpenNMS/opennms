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
