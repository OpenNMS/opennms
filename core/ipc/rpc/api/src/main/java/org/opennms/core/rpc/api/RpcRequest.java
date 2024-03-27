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

import java.util.Map;

import io.opentracing.Span;

/**
 * The request of an RPC call.
 *
 * @author jwhite
 */
public interface RpcRequest {

    public static final String TAG_NODE_ID = "nodeId";
    public static final String TAG_NODE_LABEL = "nodeLabel";
    public static final String TAG_CLASS_NAME = "className";
    public static final String TAG_IP_ADDRESS = "ipAddress";
    public static final String TAG_DESCRIPTION = "description";
    /**
     * Used to route the request to the appropriate location.
     */
    String getLocation();

    /**
     * Used to route the request to a particular system at the given location.
     */
    String getSystemId();

    /**
     * When using JMS, the request will fail if no response was received in this
     * many milliseconds.
     */
    Long getTimeToLiveMs();

    /**
     * RPC clients expose tracing info as tags there by giving more context to each RPC trace.
     * Implementations should add tags defined above if they are available.
     */
    Map<String, String> getTracingInfo();

    Span getSpan();
}
