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

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.camel.CamelContext;
import org.apache.camel.Route;
import org.apache.camel.builder.RouteBuilder;
import org.opennms.core.rpc.api.RpcModule;
import org.opennms.core.rpc.api.RpcRequest;
import org.opennms.core.rpc.api.RpcResponse;
import org.opennms.minion.core.api.MinionIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dynamically creates and deletes Camel routes to process RPC requests
 * for all registered {@link RpcModule} services.
 *
 * @author jwhite
 */
public abstract class CamelRpcServerRouteManager {

    private static final Logger LOG = LoggerFactory.getLogger(CamelRpcServerRouteManager.class);

    private final CamelContext context;

    private final MinionIdentity identity;

    private final Map<RpcModule<RpcRequest,RpcResponse>, String> routeIdsByModule = new ConcurrentHashMap<>();

    public CamelRpcServerRouteManager(CamelContext context, MinionIdentity identity) {
        this.context = Objects.requireNonNull(context);
        this.identity = Objects.requireNonNull(identity);
    }

    public static String getRouteId(RpcModule<?,?> module) {
        return "RPC.Server." + module.getId();
    }

    public abstract RouteBuilder getRouteBuilder(CamelContext context, MinionIdentity identity, RpcModule<RpcRequest,RpcResponse> module);

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void bind(RpcModule module) throws Exception {
        if (module != null) {
            final RpcModule<RpcRequest,RpcResponse> rpcModule = (RpcModule<RpcRequest,RpcResponse>)module;
            final String routeId = getRouteId(rpcModule);
            final Route existingRoute = context.getRoute(routeId);
            if (routeIdsByModule.containsKey(rpcModule)) {
                if (existingRoute == null) {
                    LOG.error("RpcModule {} ({}) was marked as registered but its route {} cannot be found in the Camel context", 
                        rpcModule.getId(),
                        Integer.toHexString(rpcModule.hashCode()),
                        routeId
                    );
                } else {
                    LOG.warn("RpcModule {} ({}) was already registered on route {}: {}",
                        rpcModule.getId(),
                        Integer.toHexString(rpcModule.hashCode()),
                        routeId,
                        existingRoute
                    );
                }
            } else {
                if (existingRoute == null) {
                    final RouteBuilder routeBuilder = getRouteBuilder(context, identity, module);
                    context.addRoutes(routeBuilder);
                    routeIdsByModule.put(rpcModule, routeId);
                    LOG.info("Registered RpcModule {} ({}) on route {} with builder {}",
                        rpcModule.getId(),
                        Integer.toHexString(rpcModule.hashCode()),
                        routeId,
                        routeBuilder
                    );
                } else {
                    LOG.warn("RpcModule {} ({}) cannot be registered, route {} is already present: {}",
                        rpcModule.getId(),
                        Integer.toHexString(rpcModule.hashCode()),
                        routeId,
                        existingRoute
                    );
                }
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void unbind(RpcModule module) throws Exception {
        if (module != null) {
            final RpcModule<RpcRequest,RpcResponse> rpcModule = (RpcModule<RpcRequest,RpcResponse>)module;
            final String routeId = routeIdsByModule.remove(rpcModule);
            if (routeId != null) {
                context.stopRoute(routeId);
                context.removeRoute(routeId);
                LOG.info("Deregistered RpcModule {} ({})", rpcModule.getId(), Integer.toHexString(rpcModule.hashCode()));
            } else {
                LOG.warn("Could not determine route ID for RpcModule {} ({})", rpcModule.getId(), Integer.toHexString(rpcModule.hashCode()));
            }
        }
    }

}
