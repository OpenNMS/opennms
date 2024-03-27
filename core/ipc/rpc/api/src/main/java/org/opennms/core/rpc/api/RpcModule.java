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

/**
 * Provides the ability to execute the RPCs and defines how the requests/responses will
 * be marshaled/unmarshaled over the wire.
 *
 * @author jwhite
 */
public interface RpcModule<S extends RpcRequest, T extends RpcResponse> extends RpcClient<S,T> {

    public static final String MINION_HEADERS_MODULE = "MINION_HEADERS";

    /**
     * Used to route the request/responses to the appropriate module.
     * 
     * This ID should be unique for every RpcModule implementation.
     */
    String getId();

    /**
     * Marshals the request to a string.
     */
    String marshalRequest(S request);

    /**
     * Unmarshals the request from a string.
     */
    S unmarshalRequest(String request);

    /**
     * Marshals the response to a string.
     */
    String marshalResponse(T response);

    /**
     * Unmarshals the response from a string.
     */
    T unmarshalResponse(String response);

    /**
     * Called when the {@link RpcModule} throws an exception while executing a request.
     *
     * This function should return a new {@link RpcResponse} that stores a string-based representation
     * of the exception that occurred and make this available via {@link RpcResponse#getErrorMessage()}
     * once un-marshaled.
     *
     * @param ex the exception that occurred
     * @return a {@link RpcResponse} that stores the exception
     */
    T createResponseWithException(Throwable ex);

}
