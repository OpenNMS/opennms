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

package org.opennms.core.ipc.grpc.client;

import static org.opennms.core.ipc.grpc.client.GrpcClientConstants.CLIENT_CERTIFICATE_FILE_PATH;
import static org.opennms.core.ipc.grpc.client.GrpcClientConstants.CLIENT_PRIVATE_KEY_FILE_PATH;
import static org.opennms.core.ipc.grpc.client.GrpcClientConstants.DEFAULT_GRPC_HOST;
import static org.opennms.core.ipc.grpc.client.GrpcClientConstants.DEFAULT_GRPC_PORT;
import static org.opennms.core.ipc.grpc.client.GrpcClientConstants.DEFAULT_MESSAGE_SIZE;
import static org.opennms.core.ipc.grpc.client.GrpcClientConstants.GRPC_CLIENT_PID;
import static org.opennms.core.ipc.grpc.client.GrpcClientConstants.GRPC_HOST;
import static org.opennms.core.ipc.grpc.client.GrpcClientConstants.GRPC_MAX_INBOUND_SIZE;
import static org.opennms.core.ipc.grpc.client.GrpcClientConstants.GRPC_PORT;
import static org.opennms.core.ipc.grpc.client.GrpcClientConstants.TLS_ENABLED;
import static org.opennms.core.ipc.grpc.client.GrpcClientConstants.TRUST_CERTIFICATE_FILE_PATH;
import static org.opennms.core.ipc.sink.api.Message.SINK_METRIC_PRODUCER_DOMAIN;
import static org.opennms.core.ipc.sink.api.SinkModule.HEARTBEAT_MODULE_ID;
import static org.opennms.core.rpc.api.RpcModule.MINION_HEADERS;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.SSLException;

import org.opennms.core.ipc.grpc.common.ConfigUtils;
import org.opennms.core.ipc.grpc.common.OnmsIpcGrpc;
import org.opennms.core.ipc.grpc.common.RpcMessage;
import org.opennms.core.ipc.grpc.common.SinkMessage;
import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.ipc.sink.api.MessageConsumerManager;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.ipc.sink.common.AbstractMessageDispatcherFactory;
import org.opennms.core.logging.Logging;
import org.opennms.core.rpc.api.RpcModule;
import org.opennms.core.rpc.api.RpcRequest;
import org.opennms.core.rpc.api.RpcResponse;
import org.opennms.core.utils.PropertiesUtils;
import org.opennms.distributed.core.api.MinionIdentity;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ByteString;

import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NegotiationType;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.stub.StreamObserver;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;

public class MinionGrpcClient extends AbstractMessageDispatcherFactory<String> {

    private static final Logger LOG = LoggerFactory.getLogger(MinionGrpcClient.class);
    private ManagedChannel channel;
    private OnmsIpcGrpc.OnmsIpcStub asyncStub;
    private Properties properties;
    private BundleContext bundleContext;
    private MinionIdentity minionIdentity;
    private ConfigurationAdmin configAdmin;
    private StreamObserver<RpcMessage> rpcStream;
    private StreamObserver<SinkMessage> sinkStream;
    private ConnectivityState currentChannelState;
    private final Map<String, RpcModule<RpcRequest, RpcResponse>> registerdModules = new ConcurrentHashMap<>();


    public MinionGrpcClient(MinionIdentity identity, ConfigurationAdmin configAdmin) {
        this.minionIdentity = identity;
        this.configAdmin = configAdmin;
    }


    public void start() throws IOException {
        properties = ConfigUtils.getPropertiesFromConfig(configAdmin, GRPC_CLIENT_PID);
        String host = PropertiesUtils.getProperty(properties, GRPC_HOST, DEFAULT_GRPC_HOST);
        int port = PropertiesUtils.getProperty(properties, GRPC_PORT, DEFAULT_GRPC_PORT);
        boolean tlsEnabled = PropertiesUtils.getProperty(properties, TLS_ENABLED, false);
        int maxInboundMessageSize = PropertiesUtils.getProperty(properties, GRPC_MAX_INBOUND_SIZE, DEFAULT_MESSAGE_SIZE);

        NettyChannelBuilder channelBuilder = NettyChannelBuilder.forAddress(host, port)
                .keepAliveWithoutCalls(true)
                .maxInboundMessageSize(maxInboundMessageSize);

        if (tlsEnabled) {
            channel = channelBuilder
                    .negotiationType(NegotiationType.TLS)
                    .sslContext(buildSslContext().build())
                    .build();
            LOG.info("TLS enabled for gRPC");
        } else {
            channel = channelBuilder.usePlaintext().build();
        }

        asyncStub = OnmsIpcGrpc.newStub(channel);
        initializeRpcStub();
        initializeSinkStub();
        LOG.info("Minion at location {} with systemId {} started", minionIdentity.getLocation(), minionIdentity.getId());

    }

    private SslContextBuilder buildSslContext() throws SSLException {
        SslContextBuilder builder = GrpcSslContexts.forClient();
        String clientCertChainFilePath = properties.getProperty(CLIENT_CERTIFICATE_FILE_PATH);
        String clientPrivateKeyFilePath = properties.getProperty(CLIENT_PRIVATE_KEY_FILE_PATH);
        String trustCertCollectionFilePath = properties.getProperty(TRUST_CERTIFICATE_FILE_PATH);

        if (trustCertCollectionFilePath != null) {
            builder.trustManager(new File(trustCertCollectionFilePath));
        }
        if (clientCertChainFilePath != null && clientPrivateKeyFilePath != null) {
            builder.keyManager(new File(clientCertChainFilePath), new File(clientPrivateKeyFilePath));
        }
        return builder;
    }

    private void initializeRpcStub() {
        if (getChannelState().equals(ConnectivityState.READY)) {
            rpcStream = asyncStub.rpcStreaming(new RpcMessageHandler());
            // Need to send minion headers to gRPC server in order to register.
            sendMinionHeaders();
            LOG.info("Initialized RPC stub");
        } else {
            LOG.warn("gRPC server is not in ready state");
        }
    }

    private void initializeSinkStub() {
        if (getChannelState().equals(ConnectivityState.READY)) {
            sinkStream = asyncStub.sinkStreaming(new EmptyMessageReceiver());
        } else {
            LOG.warn("gRPC server is not in ready state");
        }
    }


    @SuppressWarnings({"rawtypes", "unchecked"})
    public void bind(RpcModule module) throws Exception {
        if (module != null) {
            final RpcModule<RpcRequest, RpcResponse> rpcModule = (RpcModule<RpcRequest, RpcResponse>) module;
            if (registerdModules.containsKey(rpcModule.getId())) {
                LOG.warn(" {} module is already registered", rpcModule.getId());
            } else {
                registerdModules.put(rpcModule.getId(), rpcModule);
                LOG.info("Registered module {} with gRPC client", rpcModule.getId());
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void unbind(RpcModule module) throws Exception {
        if (module != null) {
            final RpcModule<RpcRequest, RpcResponse> rpcModule = (RpcModule<RpcRequest, RpcResponse>) module;
            registerdModules.remove(rpcModule.getId());
            LOG.info("Removing module {} from gRPC client.", rpcModule.getId());
        }
    }

    private boolean hasChangedToReadyState() {
        ConnectivityState prevState = currentChannelState;
        return !prevState.equals(ConnectivityState.READY) && getChannelState().equals(ConnectivityState.READY);
    }

    public void shutdown() {
        if (rpcStream != null) {
            rpcStream.onCompleted();
        }
        channel.shutdown();
        LOG.info("Minion at location {} with systemId {} stopped", minionIdentity.getLocation(), minionIdentity.getId());
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }


    @Override
    public String getMetricDomain() {
        return SINK_METRIC_PRODUCER_DOMAIN;
    }

    @Override
    public BundleContext getBundleContext() {
        return bundleContext;
    }

    @Override
    public Tracer getTracer() {
        return GlobalTracer.get();
    }

    public ConnectivityState getChannelState() {
        return currentChannelState = channel.getState(true);
    }


    @Override
    public <S extends Message, T extends Message> void dispatch(SinkModule<S, T> module, String metadata, T message) {

        try (Logging.MDCCloseable mdc = Logging.withPrefixCloseable(MessageConsumerManager.LOG_PREFIX)) {
            byte[] sinkMessageContent = module.marshal(message);
            String messageId = UUID.randomUUID().toString();
            SinkMessage.Builder sinkMessageBuilder = SinkMessage.newBuilder()
                    .setMessageId(messageId)
                    .setLocation(minionIdentity.getLocation())
                    .setModuleId(module.getId())
                    .setContent(ByteString.copyFrom(sinkMessageContent));

            if (module.getId().equals(HEARTBEAT_MODULE_ID)) {
                if (rpcStream == null || sinkStream == null || hasChangedToReadyState()) {
                    initializeSinkStub();
                    initializeRpcStub();
                }
            }
            if (getChannelState().equals(ConnectivityState.READY)) {
                sendSinkMessage(sinkMessageBuilder.build());
            } else {
                LOG.info("gRPC server is not in ready state");
            }
        }
    }

    private synchronized void sendSinkMessage(SinkMessage sinkMessage) {
        sinkStream.onNext(sinkMessage);
    }


    private void sendMinionHeaders() {

        RpcMessage rpcMessage = RpcMessage.newBuilder()
                .setLocation(minionIdentity.getLocation())
                .setSystemId(minionIdentity.getId())
                .setModuleId(MINION_HEADERS)
                .setRpcId(minionIdentity.getId())
                .build();
        sendRpcMessage(rpcMessage);
        LOG.info("Sending Minion Headers to gRPC server");

    }


    private synchronized void sendRpcMessage(RpcMessage rpcMessage) {
        if (rpcStream != null) {
            rpcStream.onNext(rpcMessage);
        }
    }

    private class RpcMessageHandler implements StreamObserver<RpcMessage> {

        @Override
        public void onNext(RpcMessage rpcMessage) {

            try {
                sendAck(rpcMessage);
                processRpcMessage(rpcMessage);
            } catch (Throwable e) {
                LOG.error("Error while processing the RPC Request", e);
            }
        }

        @Override
        public void onError(Throwable throwable) {
            LOG.error("Error in RPC streaming", throwable);
            rpcStream = null;
        }

        @Override
        public void onCompleted() {
            LOG.error("Closing RPC message handler");
            rpcStream = null;
        }

        private void processRpcMessage(RpcMessage request) {
            String moduleId = request.getModuleId();
            if (LOG.isTraceEnabled()) {
                LOG.trace("Received rpc message for module {} with Id {}, message {}", moduleId, request.getRpcId(),
                        request.getRpcContent().toStringUtf8());
            }
            RpcModule<RpcRequest, RpcResponse> rpcModule = registerdModules.get(moduleId);
            if (rpcModule == null) {
                return;
            }
            RpcRequest rpcRequest = rpcModule.unmarshalRequest(request.getRpcContent().toStringUtf8());
            CompletableFuture<RpcResponse> future = rpcModule.execute(rpcRequest);
            future.whenComplete((res, ex) -> {
                final RpcResponse rpcResponse;
                if (ex != null) {
                    // An exception occurred, store the exception in a new response
                    LOG.warn("An error occured while executing a call in {}.", rpcModule.getId(), ex);
                    rpcResponse = rpcModule.createResponseWithException(ex);
                } else {
                    // No exception occurred, use the given response
                    rpcResponse = res;
                }
                // Construct response using the same rpcId;
                String responseAsString = rpcModule.marshalResponse(rpcResponse);
                RpcMessage response = RpcMessage.newBuilder()
                        .setRpcId(request.getRpcId())
                        .setSystemId(minionIdentity.getId())
                        .setLocation(request.getLocation())
                        .setModuleId(request.getModuleId())
                        .setRpcContent(ByteString.copyFrom(responseAsString.getBytes()))
                        .build();
                if (getChannelState().equals(ConnectivityState.READY)) {
                    try {
                        sendRpcMessage(response);
                        if (LOG.isTraceEnabled()) {
                            LOG.trace("Response sent for module {} with Id {} and response = {}", moduleId, request.getRpcId(), responseAsString);
                        }
                    } catch (Throwable e) {
                        LOG.error("Error while sending response {}", responseAsString, e);
                    }
                }
            });
        }


        private void sendAck(RpcMessage request) {

            RpcMessage response = RpcMessage.newBuilder()
                    .setLocation(request.getLocation())
                    .setModuleId(MINION_HEADERS)
                    .setRpcId(request.getRpcId())
                    .setSystemId(minionIdentity.getId())
                    .build();

            if (getChannelState().equals(ConnectivityState.READY)) {
                sendRpcMessage(response);
                LOG.trace("Sending Ack for rpcId {}", request.getRpcId());

            } else {
                LOG.debug("gRPC server is not in ready state");
            }

        }
    }


}
