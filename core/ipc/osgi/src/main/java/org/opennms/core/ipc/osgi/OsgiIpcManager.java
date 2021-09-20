/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class OsgiIpcManager extends AbstractMessageConsumerManager implements RpcClientFactory, DisposableBean {

    private static final Logger LOG = LoggerFactory.getLogger(OsgiIpcManager.class);

    private final ThreadFactory sinkRegisterConsumerThreadFactory = new ThreadFactoryBuilder()
            .setNameFormat("sink-register-consumer-delegate-%d")
            .build();
    private final ExecutorService sinkRegisterConsumerExecutor = Executors.newCachedThreadPool(sinkRegisterConsumerThreadFactory);

    private final ServiceLookup<Class<?>, String> blockingServiceLookup;

    private MessageConsumerManager consumerManagerDelegate;


    public OsgiIpcManager() {
        blockingServiceLookup = new ServiceLookupBuilder(new ServiceRegistryLookup(DefaultServiceRegistry.INSTANCE))
                .blocking()
                .build();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R extends RpcRequest, S extends RpcResponse> RpcClient<R, S> getClient(RpcModule<R, S> module) {

        // create RpcClient here and postpone calling delegate to execute call.
        return new RpcClientDelegate(module);
    }


    private RpcClientFactory getRpcClientFactory() {
        return blockingServiceLookup.lookup(RpcClientFactory.class, "(!(strategy=delegate))");
    }


    private MessageConsumerManager getConsumerManager() {
        consumerManagerDelegate = blockingServiceLookup.lookup(MessageConsumerManager.class, "(!(strategy=delegate))");
        return consumerManagerDelegate;
    }


    private <R extends RpcRequest, S extends RpcResponse> RpcClient getRpcClient(RpcModule<R, S> module) {
        RpcClientFactory clientFactory = getRpcClientFactory();
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
        // Register consumer in a separate thread.
        sinkRegisterConsumerExecutor.execute(()-> loadManagerAndRegisterConsumer(consumer));
    }

    private <S extends Message, T extends Message> void loadManagerAndRegisterConsumer(MessageConsumer<S, T> consumer) {

        MessageConsumerManager consumerManagerDelegate = getConsumerManager();
        if (consumerManagerDelegate != null) {
            try {
                consumerManagerDelegate.registerConsumer(consumer);
            } catch (Exception e) {
                LOG.error("Exception while registering consumer for module {}", consumer.getModule(), e);
            }
        }
    }


    @Override
    public <S extends Message, T extends Message> void unregisterConsumer(MessageConsumer<S, T> consumer) throws Exception {
        if (consumerManagerDelegate != null) {
            consumerManagerDelegate.unregisterConsumer(consumer);
        }
    }

    @Override
    public void destroy() throws Exception {
        sinkRegisterConsumerExecutor.shutdownNow();
    }

    private class RpcClientDelegate<R extends RpcRequest, S extends RpcResponse> implements RpcClient {

        private RpcModule<R, S> module;

        private RpcClient delegate;

        private RpcClientDelegate(RpcModule<R, S> module) {
            this.module = module;
        }

        @SuppressWarnings("unchecked")
        @Override
        public CompletableFuture execute(RpcRequest request) {
            if (delegate == null) {
                delegate = getRpcClient(module);
            }
            if (delegate != null) {
                return delegate.execute(request);
            } else {
                CompletableFuture<S> future = new CompletableFuture();
                future.completeExceptionally(new RuntimeException("No RPC client found"));
                return future;
            }
        }
    }
}
