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

package org.opennms.core.ipc.grpc.common;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;

import io.grpc.stub.ClientCalls;

/**
 * <pre>
 * service definitions of IPC between Minion and OpenNMS
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.24.0)",
    comments = "Source: ipc.proto")
public final class OnmsIpcGrpc {

  private OnmsIpcGrpc() {}

  public static final String SERVICE_NAME = "OnmsIpc";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<org.opennms.core.ipc.grpc.common.RpcMessage,
      org.opennms.core.ipc.grpc.common.RpcMessage> getRpcStreamingMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "RpcStreaming",
      requestType = org.opennms.core.ipc.grpc.common.RpcMessage.class,
      responseType = org.opennms.core.ipc.grpc.common.RpcMessage.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<org.opennms.core.ipc.grpc.common.RpcMessage,
      org.opennms.core.ipc.grpc.common.RpcMessage> getRpcStreamingMethod() {
    io.grpc.MethodDescriptor<org.opennms.core.ipc.grpc.common.RpcMessage, org.opennms.core.ipc.grpc.common.RpcMessage> getRpcStreamingMethod;
    if ((getRpcStreamingMethod = OnmsIpcGrpc.getRpcStreamingMethod) == null) {
      synchronized (OnmsIpcGrpc.class) {
        if ((getRpcStreamingMethod = OnmsIpcGrpc.getRpcStreamingMethod) == null) {
          OnmsIpcGrpc.getRpcStreamingMethod = getRpcStreamingMethod =
              io.grpc.MethodDescriptor.<org.opennms.core.ipc.grpc.common.RpcMessage, org.opennms.core.ipc.grpc.common.RpcMessage>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "RpcStreaming"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.opennms.core.ipc.grpc.common.RpcMessage.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.opennms.core.ipc.grpc.common.RpcMessage.getDefaultInstance()))
              .setSchemaDescriptor(new OnmsIpcMethodDescriptorSupplier("RpcStreaming"))
              .build();
        }
      }
    }
    return getRpcStreamingMethod;
  }

  private static volatile io.grpc.MethodDescriptor<SinkMessage,
      Empty> getSinkStreamingMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SinkStreaming",
      requestType = SinkMessage.class,
      responseType = Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
  public static io.grpc.MethodDescriptor<SinkMessage,
      Empty> getSinkStreamingMethod() {
    io.grpc.MethodDescriptor<SinkMessage, Empty> getSinkStreamingMethod;
    if ((getSinkStreamingMethod = OnmsIpcGrpc.getSinkStreamingMethod) == null) {
      synchronized (OnmsIpcGrpc.class) {
        if ((getSinkStreamingMethod = OnmsIpcGrpc.getSinkStreamingMethod) == null) {
          OnmsIpcGrpc.getSinkStreamingMethod = getSinkStreamingMethod =
              io.grpc.MethodDescriptor.<SinkMessage, Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SinkStreaming"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  SinkMessage.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  Empty.getDefaultInstance()))
              .setSchemaDescriptor(new OnmsIpcMethodDescriptorSupplier("SinkStreaming"))
              .build();
        }
      }
    }
    return getSinkStreamingMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static OnmsIpcStub newStub(io.grpc.Channel channel) {
    return new OnmsIpcStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static OnmsIpcBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new OnmsIpcBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static OnmsIpcFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new OnmsIpcFutureStub(channel);
  }

  /**
   * <pre>
   * service definitions of IPC between Minion and OpenNMS
   * </pre>
   */
  public static abstract class OnmsIpcImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * Streams RPC messages between OpenNMS and Minion.
     * </pre>
     */
    public io.grpc.stub.StreamObserver<org.opennms.core.ipc.grpc.common.RpcMessage> rpcStreaming(
        io.grpc.stub.StreamObserver<org.opennms.core.ipc.grpc.common.RpcMessage> responseObserver) {
      return asyncUnimplementedStreamingCall(getRpcStreamingMethod(), responseObserver);
    }

    /**
     * <pre>
     * Streams Sink messages from Minion to OpenNMS
     * </pre>
     */
    public io.grpc.stub.StreamObserver<SinkMessage> sinkStreaming(
        io.grpc.stub.StreamObserver<Empty> responseObserver) {
      return asyncUnimplementedStreamingCall(getSinkStreamingMethod(), responseObserver);
    }

    @Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getRpcStreamingMethod(),
            asyncBidiStreamingCall(
              new MethodHandlers<
                org.opennms.core.ipc.grpc.common.RpcMessage,
                org.opennms.core.ipc.grpc.common.RpcMessage>(
                  this, METHODID_RPC_STREAMING)))
          .addMethod(
            getSinkStreamingMethod(),
            asyncClientStreamingCall(
              new MethodHandlers<
                SinkMessage,
                Empty>(
                  this, METHODID_SINK_STREAMING)))
          .build();
    }
  }

  /**
   * <pre>
   * service definitions of IPC between Minion and OpenNMS
   * </pre>
   */
  public static final class OnmsIpcStub extends io.grpc.stub.AbstractStub<OnmsIpcStub> {
    private OnmsIpcStub(io.grpc.Channel channel) {
      super(channel);
    }

    private OnmsIpcStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected OnmsIpcStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new OnmsIpcStub(channel, callOptions);
    }

    /**
     * <pre>
     * Streams RPC messages between OpenNMS and Minion.
     * </pre>
     */
    public io.grpc.stub.StreamObserver<org.opennms.core.ipc.grpc.common.RpcMessage> rpcStreaming(
        io.grpc.stub.StreamObserver<org.opennms.core.ipc.grpc.common.RpcMessage> responseObserver) {
      return ClientCalls.asyncBidiStreamingCall(
          getChannel().newCall(getRpcStreamingMethod(), getCallOptions()), responseObserver);
    }

    /**
     * <pre>
     * Streams Sink messages from Minion to OpenNMS
     * </pre>
     */
    public io.grpc.stub.StreamObserver<SinkMessage> sinkStreaming(
        io.grpc.stub.StreamObserver<Empty> responseObserver) {
      return ClientCalls.asyncClientStreamingCall(
          getChannel().newCall(getSinkStreamingMethod(), getCallOptions()), responseObserver);
    }
  }

  /**
   * <pre>
   * service definitions of IPC between Minion and OpenNMS
   * </pre>
   */
  public static final class OnmsIpcBlockingStub extends io.grpc.stub.AbstractStub<OnmsIpcBlockingStub> {
    private OnmsIpcBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private OnmsIpcBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected OnmsIpcBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new OnmsIpcBlockingStub(channel, callOptions);
    }
  }

  /**
   * <pre>
   * service definitions of IPC between Minion and OpenNMS
   * </pre>
   */
  public static final class OnmsIpcFutureStub extends io.grpc.stub.AbstractStub<OnmsIpcFutureStub> {
    private OnmsIpcFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private OnmsIpcFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected OnmsIpcFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new OnmsIpcFutureStub(channel, callOptions);
    }
  }

  private static final int METHODID_RPC_STREAMING = 0;
  private static final int METHODID_SINK_STREAMING = 1;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final OnmsIpcImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(OnmsIpcImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }

    @Override
    @SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_RPC_STREAMING:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.rpcStreaming(
              (io.grpc.stub.StreamObserver<org.opennms.core.ipc.grpc.common.RpcMessage>) responseObserver);
        case METHODID_SINK_STREAMING:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.sinkStreaming(
              (io.grpc.stub.StreamObserver<Empty>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class OnmsIpcBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    OnmsIpcBaseDescriptorSupplier() {}

    @Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return MinionIpc.getDescriptor();
    }

    @Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("OnmsIpc");
    }
  }

  private static final class OnmsIpcFileDescriptorSupplier
      extends OnmsIpcBaseDescriptorSupplier {
    OnmsIpcFileDescriptorSupplier() {}
  }

  private static final class OnmsIpcMethodDescriptorSupplier
      extends OnmsIpcBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    OnmsIpcMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (OnmsIpcGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new OnmsIpcFileDescriptorSupplier())
              .addMethod(getRpcStreamingMethod())
              .addMethod(getSinkStreamingMethod())
              .build();
        }
      }
    }
    return result;
  }
}
