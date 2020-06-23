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

/**
 * Provides the ability to execute the RPCs and defines how the requests/responses will
 * be marshaled/unmarshaled over the wire.
 *
 * @author jwhite
 */
public interface RpcModule<S extends RpcRequest, T extends RpcResponse> extends RpcClient<S,T> {

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
