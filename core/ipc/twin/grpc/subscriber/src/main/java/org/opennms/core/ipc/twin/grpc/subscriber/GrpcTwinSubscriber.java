/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.core.ipc.twin.grpc.subscriber;


import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.*;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.opennms.core.grpc.common.GrpcIpcUtils;
import org.opennms.core.ipc.twin.common.AbstractTwinSubscriber;
import org.opennms.core.ipc.twin.common.TwinRequestBean;
import org.opennms.core.ipc.twin.common.TwinResponseBean;
import org.opennms.core.ipc.twin.grpc.common.*;
import org.opennms.core.utils.PropertiesUtils;
import org.opennms.distributed.core.api.MinionIdentity;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NegotiationType;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;

import static org.opennms.core.grpc.common.GrpcIpcUtils.GRPC_HOST;

public class GrpcTwinSubscriber extends AbstractTwinSubscriber {

    private static final Logger LOG = LoggerFactory.getLogger(GrpcTwinSubscriber.class);
    private static final long RETRIEVAL_TIMEOUT = 3000;
    private static final int TWIN_REQUEST_POOL_SIZE = 100;
    private final int port;
    private final ConfigurationAdmin configAdmin;
    private ManagedChannel channel;
    private Properties clientProperties;
    private OpenNMSTwinIpcGrpc.OpenNMSTwinIpcStub asyncStub;
    private StreamObserver<TwinRequestProto> rpcStream;
    private final ThreadFactory twinRequestSenderThreadFactory = new ThreadFactoryBuilder()
            .setNameFormat("twin-request-sender-%d")
            .build();
    private final ScheduledExecutorService twinRequestSenderExecutor = Executors.newScheduledThreadPool(TWIN_REQUEST_POOL_SIZE,
            twinRequestSenderThreadFactory);

    public GrpcTwinSubscriber(MinionIdentity minionIdentity, ConfigurationAdmin configAdmin, int port) {
        super(minionIdentity);
        this.configAdmin = configAdmin;
        this.port = port;
    }

    public void start() throws IOException {
        // Twin inherits all properties from ipc client except for port.
        clientProperties = GrpcIpcUtils.getPropertiesFromConfig(configAdmin, GrpcIpcUtils.GRPC_CLIENT_PID);

        channel = GrpcIpcUtils.getChannel(clientProperties, this.port);

        asyncStub = OpenNMSTwinIpcGrpc.newStub(channel);
        LOG.info("Started Twin gRPC Subscriber at location {} with systemId {}", getMinionIdentity().getLocation(), getMinionIdentity().getId());

    }

    private StreamObserver<TwinRequestProto> getRpcStream(ConnectivityState currentChannelState) {
        if (currentChannelState.equals(ConnectivityState.READY) && this.rpcStream == null) {
            ResponseHandler responseHandler = new ResponseHandler();
            this.rpcStream = asyncStub.rpcStreaming(responseHandler);
            MinionHeader minionHeader = MinionHeader.newBuilder().setLocation(getMinionIdentity().getLocation())
                    .setSystemId(getMinionIdentity().getId()).build();
            asyncStub.sinkStreaming(minionHeader, responseHandler);
        }
        return this.rpcStream;
    }

    public void shutdown() {
        if (channel != null) {
            channel.shutdown();
        }
        twinRequestSenderExecutor.shutdown();
    }

    @Override
    protected void sendRpcRequest(TwinRequestBean twinRequest) {
        TwinRequestProto twinRequestProto = mapTwinRequest(twinRequest);
        // Send RPC Request asynchronously.
        CompletableFuture.runAsync(() -> retrySendRpcRequest(twinRequestProto), twinRequestSenderExecutor);
    }

    private void retrySendRpcRequest(TwinRequestProto twinRequestProto) {
        // It may be possible that TwinPublisher is Offline. Retry sending request till it succeeds.
        ScheduledFuture<Boolean> future = twinRequestSenderExecutor.schedule(
                () -> sendTwinRequest(twinRequestProto), RETRIEVAL_TIMEOUT, TimeUnit.MILLISECONDS);
        try {
            boolean succeeded = future.get();
            if (succeeded) {
                return;
            }
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Error while attempting to send Twin Request with key {}", twinRequestProto.getConsumerKey(), e);
        }
        retrySendRpcRequest(twinRequestProto);
    }

    private synchronized boolean sendTwinRequest(TwinRequestProto twinRequestProto) {
        ConnectivityState currentChannelState = channel.getState(true);
        StreamObserver<TwinRequestProto> requestSender = getRpcStream(currentChannelState);
        if (requestSender != null && currentChannelState.equals(ConnectivityState.READY)) {
            requestSender.onNext(twinRequestProto);
            return true;
        } else {
            return false;
        }
    }

    private TwinRequestProto mapTwinRequest(TwinRequestBean twinRequest) {
        TwinRequestProto.Builder builder = TwinRequestProto.newBuilder();
        builder.setConsumerKey(twinRequest.getKey()).setLocation(getMinionIdentity().getLocation())
                .setSystemId(getMinionIdentity().getId());
        return builder.build();
    }

    private class ResponseHandler implements StreamObserver<TwinResponseProto> {

        @Override
        public void onNext(TwinResponseProto twinResponseProto) {
            TwinResponseBean twinResponseBean = mapTwinResponseProto(twinResponseProto);
            accept(twinResponseBean);
        }

        @Override
        public void onError(Throwable throwable) {
            LOG.error("Error in Twin streaming", throwable);
            rpcStream = null;
        }

        @Override
        public void onCompleted() {
            LOG.error("Closing Twin Response Handler");
            rpcStream = null;
        }

        private TwinResponseBean mapTwinResponseProto(TwinResponseProto twinResponseProto) {
            TwinResponseBean twinResponseBean = new TwinResponseBean();
            if (!Strings.isNullOrEmpty(twinResponseProto.getLocation())) {
                twinResponseBean.setLocation(twinResponseProto.getLocation());
            }
            twinResponseBean.setKey(twinResponseProto.getConsumerKey());
            if (twinResponseProto.getTwinObject() != null) {
                twinResponseBean.setObject(twinResponseProto.getTwinObject().toByteArray());
            }
            return twinResponseBean;
        }
    }


}
