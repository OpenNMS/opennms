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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.opennms.core.rpc.api.RpcModule;
import org.opennms.core.rpc.api.RpcRequest;
import org.opennms.core.rpc.api.RpcResponse;

/**
 * Used to group the {@link RpcRequest} and associated {@link RpcModule}.
 *
 * These objects are used by the {@link CamelRpcClientPreProcessor}.
 *
 * @author jwhite
 */
public class CamelRpcRequest<S extends RpcRequest, T extends RpcResponse> {
    private final RpcModule<S,T> module;
    private final S request;
    private Map<String, String> tracingInfo = new HashMap<>();

    public CamelRpcRequest(RpcModule<S,T> module, S request, Map<String, String> tracingInfo) {
        this.module = Objects.requireNonNull(module);
        this.request = Objects.requireNonNull(request);
        this.tracingInfo.putAll(tracingInfo);
    }

    public RpcModule<S,T> getModule() {
        return module;
    }

    public S getRequest() {
        return request;
    }

    public Map<String, String> getTracingInfo() {
        return this.tracingInfo;
    }
}
