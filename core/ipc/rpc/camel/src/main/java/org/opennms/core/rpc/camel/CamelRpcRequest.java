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

package org.opennms.core.rpc.camel;

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

    public CamelRpcRequest(RpcModule<S,T> module, S request) {
        this.module = Objects.requireNonNull(module);
        this.request = Objects.requireNonNull(request);
    }

    public RpcModule<S,T> getModule() {
        return module;
    }

    public S getRequest() {
        return request;
    }
}
