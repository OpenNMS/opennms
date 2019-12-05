/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.core.ipc.osgi;

import java.util.concurrent.CompletableFuture;

import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.ipc.sink.api.MessageConsumer;
import org.opennms.core.ipc.sink.api.MessageConsumerManager;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.ipc.sink.common.AbstractMessageConsumerManager;
import org.opennms.core.rpc.api.RpcClient;
import org.opennms.core.rpc.api.RpcClientFactory;
import org.opennms.core.rpc.api.RpcModule;
import org.opennms.core.rpc.api.RpcRequest;
import org.opennms.core.rpc.api.RpcResponse;
import org.opennms.core.soa.lookup.ServiceLookup;
import org.opennms.core.soa.lookup.ServiceLookupBuilder;
import org.opennms.core.soa.lookup.ServiceRegistryLookup;
import org.opennms.core.soa.support.DefaultServiceRegistry;

public class OsgiIpcManager extends AbstractMessageConsumerManager implements RpcClientFactory {


    private final ServiceLookup<Class<?>, String> serviceLookup;

    private final ServiceLookup<Class<?>, String> blockingServiceLookup;

    private RpcClientFactory rpcClientFactory;


    public OsgiIpcManager() {
        serviceLookup = new ServiceLookupBuilder(new ServiceRegistryLookup(DefaultServiceRegistry.INSTANCE))
                .build();

        blockingServiceLookup = new ServiceLookupBuilder(new ServiceRegistryLookup(DefaultServiceRegistry.INSTANCE))
                .blocking()
                .build();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R extends RpcRequest, S extends RpcResponse> RpcClient<R, S> getClient(RpcModule<R, S> module) {
        if (rpcClientFactory == null) {
            rpcClientFactory = getRpcClientFactory(false);
        }
        if (rpcClientFactory != null) {
            return rpcClientFactory.getClient(module);
        }
        // No RPC client factory found yet, create RpcClient here and
        // delegate execute call to registered client factory
        return new RpcClient<R, S>() {
            @Override
            public CompletableFuture<S> execute(R request) {
                RpcClient rpcClient = getRpcClient(module);
                if (rpcClient != null) {
                    return rpcClient.execute(request);
                } else {
                    CompletableFuture<S> future = new CompletableFuture();
                    future.completeExceptionally(new RuntimeException("No RPC client found"));
                    return future;
                }
            }
        };
    }


    private RpcClientFactory getRpcClientFactory(boolean blocking) {
        if (blocking) {
            return blockingServiceLookup.lookup(RpcClientFactory.class, "(!(strategy=delegate))");
        }
        return serviceLookup.lookup(RpcClientFactory.class, "(!(strategy=delegate))");
    }


    private MessageConsumerManager getConsumerManager() {
        return blockingServiceLookup.lookup(MessageConsumerManager.class, "(!(strategy=delegate))");
    }


    private <R extends RpcRequest, S extends RpcResponse> RpcClient getRpcClient(RpcModule<R, S> module) {
        RpcClientFactory clientFactory = getRpcClientFactory(true);
        if (clientFactory != null) {
            return clientFactory.getClient(module);
        }
        return null;
    }


    @Override
    public <S extends Message, T extends Message> void dispatch(SinkModule<S, T> module, T message) {
        MessageConsumerManager consumerManagerDelegate = getConsumerManager();
        if (consumerManagerDelegate != null) {
            consumerManagerDelegate.dispatch(module, message);
        }
    }


    @Override
    protected void startConsumingForModule(SinkModule<?, Message> module) throws Exception {
        // delegate handles this, pass
    }

    @Override
    protected void stopConsumingForModule(SinkModule<?, Message> module) throws Exception {
        // delegate handles this, pass
    }

    @Override
    public <S extends Message, T extends Message> void registerConsumer(MessageConsumer<S, T> consumer)
            throws Exception {
        // Don't block registering consumer.
        new Thread(() -> registerConsumerRunner(consumer)).start();
    }

    private <S extends Message, T extends Message> void registerConsumerRunner(MessageConsumer<S, T> consumer) {
        MessageConsumerManager consumerManagerDelegate = getConsumerManager();
        if (consumerManagerDelegate != null) {
            try {
                consumerManagerDelegate.registerConsumer(consumer);
            } catch (Exception e) {
                //pass
            }
        }
    }


    @Override
    public <S extends Message, T extends Message> void unregisterConsumer(MessageConsumer<S, T> consumer) throws Exception {
        MessageConsumerManager consumerManagerDelegate = getConsumerManager();
        if (consumerManagerDelegate != null) {
            consumerManagerDelegate.unregisterConsumer(consumer);
        }
    }

}
