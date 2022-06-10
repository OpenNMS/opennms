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


package org.opennms.core.ipc.twin.grpc.common;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 * <pre>
 * service definitions of Twin IPC between Minion and OpenNMS
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.30.0)",
    comments = "Source: twin-grpc.proto")
public final class OpenNMSTwinIpcGrpc {

  private OpenNMSTwinIpcGrpc() {}

  public static final String SERVICE_NAME = "OpenNMSTwinIpc";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<org.opennms.core.ipc.twin.model.TwinRequestProto,
      org.opennms.core.ipc.twin.model.TwinResponseProto> getRpcStreamingMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "RpcStreaming",
      requestType = org.opennms.core.ipc.twin.model.TwinRequestProto.class,
      responseType = org.opennms.core.ipc.twin.model.TwinResponseProto.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<org.opennms.core.ipc.twin.model.TwinRequestProto,
      org.opennms.core.ipc.twin.model.TwinResponseProto> getRpcStreamingMethod() {
    io.grpc.MethodDescriptor<org.opennms.core.ipc.twin.model.TwinRequestProto, org.opennms.core.ipc.twin.model.TwinResponseProto> getRpcStreamingMethod;
    if ((getRpcStreamingMethod = OpenNMSTwinIpcGrpc.getRpcStreamingMethod) == null) {
      synchronized (OpenNMSTwinIpcGrpc.class) {
        if ((getRpcStreamingMethod = OpenNMSTwinIpcGrpc.getRpcStreamingMethod) == null) {
          OpenNMSTwinIpcGrpc.getRpcStreamingMethod = getRpcStreamingMethod =
              io.grpc.MethodDescriptor.<org.opennms.core.ipc.twin.model.TwinRequestProto, org.opennms.core.ipc.twin.model.TwinResponseProto>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "RpcStreaming"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.opennms.core.ipc.twin.model.TwinRequestProto.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.opennms.core.ipc.twin.model.TwinResponseProto.getDefaultInstance()))
              .setSchemaDescriptor(new OpenNMSTwinIpcMethodDescriptorSupplier("RpcStreaming"))
              .build();
        }
      }
    }
    return getRpcStreamingMethod;
  }

  private static volatile io.grpc.MethodDescriptor<MinionHeader,
      org.opennms.core.ipc.twin.model.TwinResponseProto> getSinkStreamingMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SinkStreaming",
      requestType = MinionHeader.class,
      responseType = org.opennms.core.ipc.twin.model.TwinResponseProto.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<MinionHeader,
      org.opennms.core.ipc.twin.model.TwinResponseProto> getSinkStreamingMethod() {
    io.grpc.MethodDescriptor<MinionHeader, org.opennms.core.ipc.twin.model.TwinResponseProto> getSinkStreamingMethod;
    if ((getSinkStreamingMethod = OpenNMSTwinIpcGrpc.getSinkStreamingMethod) == null) {
      synchronized (OpenNMSTwinIpcGrpc.class) {
        if ((getSinkStreamingMethod = OpenNMSTwinIpcGrpc.getSinkStreamingMethod) == null) {
          OpenNMSTwinIpcGrpc.getSinkStreamingMethod = getSinkStreamingMethod =
              io.grpc.MethodDescriptor.<MinionHeader, org.opennms.core.ipc.twin.model.TwinResponseProto>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SinkStreaming"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  MinionHeader.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.opennms.core.ipc.twin.model.TwinResponseProto.getDefaultInstance()))
              .setSchemaDescriptor(new OpenNMSTwinIpcMethodDescriptorSupplier("SinkStreaming"))
              .build();
        }
      }
    }
    return getSinkStreamingMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static OpenNMSTwinIpcStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<OpenNMSTwinIpcStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<OpenNMSTwinIpcStub>() {
        @Override
        public OpenNMSTwinIpcStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new OpenNMSTwinIpcStub(channel, callOptions);
        }
      };
    return OpenNMSTwinIpcStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static OpenNMSTwinIpcBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<OpenNMSTwinIpcBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<OpenNMSTwinIpcBlockingStub>() {
        @Override
        public OpenNMSTwinIpcBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new OpenNMSTwinIpcBlockingStub(channel, callOptions);
        }
      };
    return OpenNMSTwinIpcBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static OpenNMSTwinIpcFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<OpenNMSTwinIpcFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<OpenNMSTwinIpcFutureStub>() {
        @Override
        public OpenNMSTwinIpcFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new OpenNMSTwinIpcFutureStub(channel, callOptions);
        }
      };
    return OpenNMSTwinIpcFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * service definitions of Twin IPC between Minion and OpenNMS
   * </pre>
   */
  public static abstract class OpenNMSTwinIpcImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * Streams Twin request/Response from Minion to OpenNMS
     * </pre>
     */
    public io.grpc.stub.StreamObserver<org.opennms.core.ipc.twin.model.TwinRequestProto> rpcStreaming(
        io.grpc.stub.StreamObserver<org.opennms.core.ipc.twin.model.TwinResponseProto> responseObserver) {
      return asyncUnimplementedStreamingCall(getRpcStreamingMethod(), responseObserver);
    }

    /**
     * <pre>
     * Stream Twin updates from OpenNMS to Minion.
     * </pre>
     */
    public void sinkStreaming(MinionHeader request,
                              io.grpc.stub.StreamObserver<org.opennms.core.ipc.twin.model.TwinResponseProto> responseObserver) {
      asyncUnimplementedUnaryCall(getSinkStreamingMethod(), responseObserver);
    }

    @Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getRpcStreamingMethod(),
            asyncBidiStreamingCall(
              new MethodHandlers<
                org.opennms.core.ipc.twin.model.TwinRequestProto,
                org.opennms.core.ipc.twin.model.TwinResponseProto>(
                  this, METHODID_RPC_STREAMING)))
          .addMethod(
            getSinkStreamingMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                MinionHeader,
                org.opennms.core.ipc.twin.model.TwinResponseProto>(
                  this, METHODID_SINK_STREAMING)))
          .build();
    }
  }

  /**
   * <pre>
   * service definitions of Twin IPC between Minion and OpenNMS
   * </pre>
   */
  public static final class OpenNMSTwinIpcStub extends io.grpc.stub.AbstractAsyncStub<OpenNMSTwinIpcStub> {
    private OpenNMSTwinIpcStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected OpenNMSTwinIpcStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new OpenNMSTwinIpcStub(channel, callOptions);
    }

    /**
     * <pre>
     * Streams Twin request/Response from Minion to OpenNMS
     * </pre>
     */
    public io.grpc.stub.StreamObserver<org.opennms.core.ipc.twin.model.TwinRequestProto> rpcStreaming(
        io.grpc.stub.StreamObserver<org.opennms.core.ipc.twin.model.TwinResponseProto> responseObserver) {
      return asyncBidiStreamingCall(
          getChannel().newCall(getRpcStreamingMethod(), getCallOptions()), responseObserver);
    }

    /**
     * <pre>
     * Stream Twin updates from OpenNMS to Minion.
     * </pre>
     */
    public void sinkStreaming(MinionHeader request,
                              io.grpc.stub.StreamObserver<org.opennms.core.ipc.twin.model.TwinResponseProto> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getSinkStreamingMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * <pre>
   * service definitions of Twin IPC between Minion and OpenNMS
   * </pre>
   */
  public static final class OpenNMSTwinIpcBlockingStub extends io.grpc.stub.AbstractBlockingStub<OpenNMSTwinIpcBlockingStub> {
    private OpenNMSTwinIpcBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected OpenNMSTwinIpcBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new OpenNMSTwinIpcBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Stream Twin updates from OpenNMS to Minion.
     * </pre>
     */
    public java.util.Iterator<org.opennms.core.ipc.twin.model.TwinResponseProto> sinkStreaming(
        MinionHeader request) {
      return blockingServerStreamingCall(
          getChannel(), getSinkStreamingMethod(), getCallOptions(), request);
    }
  }

  /**
   * <pre>
   * service definitions of Twin IPC between Minion and OpenNMS
   * </pre>
   */
  public static final class OpenNMSTwinIpcFutureStub extends io.grpc.stub.AbstractFutureStub<OpenNMSTwinIpcFutureStub> {
    private OpenNMSTwinIpcFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected OpenNMSTwinIpcFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new OpenNMSTwinIpcFutureStub(channel, callOptions);
    }
  }

  private static final int METHODID_SINK_STREAMING = 0;
  private static final int METHODID_RPC_STREAMING = 1;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final OpenNMSTwinIpcImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(OpenNMSTwinIpcImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_SINK_STREAMING:
          serviceImpl.sinkStreaming((MinionHeader) request,
              (io.grpc.stub.StreamObserver<org.opennms.core.ipc.twin.model.TwinResponseProto>) responseObserver);
          break;
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
              (io.grpc.stub.StreamObserver<org.opennms.core.ipc.twin.model.TwinResponseProto>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class OpenNMSTwinIpcBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    OpenNMSTwinIpcBaseDescriptorSupplier() {}

    @Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return TwinGrpcIpc.getDescriptor();
    }

    @Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("OpenNMSTwinIpc");
    }
  }

  private static final class OpenNMSTwinIpcFileDescriptorSupplier
      extends OpenNMSTwinIpcBaseDescriptorSupplier {
    OpenNMSTwinIpcFileDescriptorSupplier() {}
  }

  private static final class OpenNMSTwinIpcMethodDescriptorSupplier
      extends OpenNMSTwinIpcBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    OpenNMSTwinIpcMethodDescriptorSupplier(String methodName) {
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
      synchronized (OpenNMSTwinIpcGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new OpenNMSTwinIpcFileDescriptorSupplier())
              .addMethod(getRpcStreamingMethod())
              .addMethod(getSinkStreamingMethod())
              .build();
        }
      }
    }
    return result;
  }
}
