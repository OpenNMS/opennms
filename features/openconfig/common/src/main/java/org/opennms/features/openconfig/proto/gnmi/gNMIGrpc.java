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


package org.opennms.features.openconfig.proto.gnmi;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.24.0)",
    comments = "Source: gnmi.proto")
public final class gNMIGrpc {

  private gNMIGrpc() {}

  public static final String SERVICE_NAME = "gnmi.gNMI";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<org.opennms.features.openconfig.proto.gnmi.Gnmi.CapabilityRequest,
      org.opennms.features.openconfig.proto.gnmi.Gnmi.CapabilityResponse> getCapabilitiesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Capabilities",
      requestType = org.opennms.features.openconfig.proto.gnmi.Gnmi.CapabilityRequest.class,
      responseType = org.opennms.features.openconfig.proto.gnmi.Gnmi.CapabilityResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.opennms.features.openconfig.proto.gnmi.Gnmi.CapabilityRequest,
      org.opennms.features.openconfig.proto.gnmi.Gnmi.CapabilityResponse> getCapabilitiesMethod() {
    io.grpc.MethodDescriptor<org.opennms.features.openconfig.proto.gnmi.Gnmi.CapabilityRequest, org.opennms.features.openconfig.proto.gnmi.Gnmi.CapabilityResponse> getCapabilitiesMethod;
    if ((getCapabilitiesMethod = gNMIGrpc.getCapabilitiesMethod) == null) {
      synchronized (gNMIGrpc.class) {
        if ((getCapabilitiesMethod = gNMIGrpc.getCapabilitiesMethod) == null) {
          gNMIGrpc.getCapabilitiesMethod = getCapabilitiesMethod =
              io.grpc.MethodDescriptor.<org.opennms.features.openconfig.proto.gnmi.Gnmi.CapabilityRequest, org.opennms.features.openconfig.proto.gnmi.Gnmi.CapabilityResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Capabilities"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.opennms.features.openconfig.proto.gnmi.Gnmi.CapabilityRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.opennms.features.openconfig.proto.gnmi.Gnmi.CapabilityResponse.getDefaultInstance()))
              .setSchemaDescriptor(new gNMIMethodDescriptorSupplier("Capabilities"))
              .build();
        }
      }
    }
    return getCapabilitiesMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.opennms.features.openconfig.proto.gnmi.Gnmi.GetRequest,
      org.opennms.features.openconfig.proto.gnmi.Gnmi.GetResponse> getGetMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Get",
      requestType = org.opennms.features.openconfig.proto.gnmi.Gnmi.GetRequest.class,
      responseType = org.opennms.features.openconfig.proto.gnmi.Gnmi.GetResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.opennms.features.openconfig.proto.gnmi.Gnmi.GetRequest,
      org.opennms.features.openconfig.proto.gnmi.Gnmi.GetResponse> getGetMethod() {
    io.grpc.MethodDescriptor<org.opennms.features.openconfig.proto.gnmi.Gnmi.GetRequest, org.opennms.features.openconfig.proto.gnmi.Gnmi.GetResponse> getGetMethod;
    if ((getGetMethod = gNMIGrpc.getGetMethod) == null) {
      synchronized (gNMIGrpc.class) {
        if ((getGetMethod = gNMIGrpc.getGetMethod) == null) {
          gNMIGrpc.getGetMethod = getGetMethod =
              io.grpc.MethodDescriptor.<org.opennms.features.openconfig.proto.gnmi.Gnmi.GetRequest, org.opennms.features.openconfig.proto.gnmi.Gnmi.GetResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Get"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.opennms.features.openconfig.proto.gnmi.Gnmi.GetRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.opennms.features.openconfig.proto.gnmi.Gnmi.GetResponse.getDefaultInstance()))
              .setSchemaDescriptor(new gNMIMethodDescriptorSupplier("Get"))
              .build();
        }
      }
    }
    return getGetMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.opennms.features.openconfig.proto.gnmi.Gnmi.SetRequest,
      org.opennms.features.openconfig.proto.gnmi.Gnmi.SetResponse> getSetMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Set",
      requestType = org.opennms.features.openconfig.proto.gnmi.Gnmi.SetRequest.class,
      responseType = org.opennms.features.openconfig.proto.gnmi.Gnmi.SetResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.opennms.features.openconfig.proto.gnmi.Gnmi.SetRequest,
      org.opennms.features.openconfig.proto.gnmi.Gnmi.SetResponse> getSetMethod() {
    io.grpc.MethodDescriptor<org.opennms.features.openconfig.proto.gnmi.Gnmi.SetRequest, org.opennms.features.openconfig.proto.gnmi.Gnmi.SetResponse> getSetMethod;
    if ((getSetMethod = gNMIGrpc.getSetMethod) == null) {
      synchronized (gNMIGrpc.class) {
        if ((getSetMethod = gNMIGrpc.getSetMethod) == null) {
          gNMIGrpc.getSetMethod = getSetMethod =
              io.grpc.MethodDescriptor.<org.opennms.features.openconfig.proto.gnmi.Gnmi.SetRequest, org.opennms.features.openconfig.proto.gnmi.Gnmi.SetResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Set"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.opennms.features.openconfig.proto.gnmi.Gnmi.SetRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.opennms.features.openconfig.proto.gnmi.Gnmi.SetResponse.getDefaultInstance()))
              .setSchemaDescriptor(new gNMIMethodDescriptorSupplier("Set"))
              .build();
        }
      }
    }
    return getSetMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.opennms.features.openconfig.proto.gnmi.Gnmi.SubscribeRequest,
      org.opennms.features.openconfig.proto.gnmi.Gnmi.SubscribeResponse> getSubscribeMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Subscribe",
      requestType = org.opennms.features.openconfig.proto.gnmi.Gnmi.SubscribeRequest.class,
      responseType = org.opennms.features.openconfig.proto.gnmi.Gnmi.SubscribeResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<org.opennms.features.openconfig.proto.gnmi.Gnmi.SubscribeRequest,
      org.opennms.features.openconfig.proto.gnmi.Gnmi.SubscribeResponse> getSubscribeMethod() {
    io.grpc.MethodDescriptor<org.opennms.features.openconfig.proto.gnmi.Gnmi.SubscribeRequest, org.opennms.features.openconfig.proto.gnmi.Gnmi.SubscribeResponse> getSubscribeMethod;
    if ((getSubscribeMethod = gNMIGrpc.getSubscribeMethod) == null) {
      synchronized (gNMIGrpc.class) {
        if ((getSubscribeMethod = gNMIGrpc.getSubscribeMethod) == null) {
          gNMIGrpc.getSubscribeMethod = getSubscribeMethod =
              io.grpc.MethodDescriptor.<org.opennms.features.openconfig.proto.gnmi.Gnmi.SubscribeRequest, org.opennms.features.openconfig.proto.gnmi.Gnmi.SubscribeResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Subscribe"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.opennms.features.openconfig.proto.gnmi.Gnmi.SubscribeRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.opennms.features.openconfig.proto.gnmi.Gnmi.SubscribeResponse.getDefaultInstance()))
              .setSchemaDescriptor(new gNMIMethodDescriptorSupplier("Subscribe"))
              .build();
        }
      }
    }
    return getSubscribeMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static gNMIStub newStub(io.grpc.Channel channel) {
    return new gNMIStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static gNMIBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new gNMIBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static gNMIFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new gNMIFutureStub(channel);
  }

  /**
   */
  public static abstract class gNMIImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * Capabilities allows the client to retrieve the set of capabilities that
     * is supported by the target. This allows the target to validate the
     * service version that is implemented and retrieve the set of models that
     * the target supports. The models can then be specified in subsequent RPCs
     * to restrict the set of data that is utilized.
     * Reference: gNMI Specification Section 3.2
     * </pre>
     */
    public void capabilities(org.opennms.features.openconfig.proto.gnmi.Gnmi.CapabilityRequest request,
        io.grpc.stub.StreamObserver<org.opennms.features.openconfig.proto.gnmi.Gnmi.CapabilityResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getCapabilitiesMethod(), responseObserver);
    }

    /**
     * <pre>
     * Retrieve a snapshot of data from the target. A Get RPC requests that the
     * target snapshots a subset of the data tree as specified by the paths
     * included in the message and serializes this to be returned to the
     * client using the specified encoding.
     * Reference: gNMI Specification Section 3.3
     * </pre>
     */
    public void get(org.opennms.features.openconfig.proto.gnmi.Gnmi.GetRequest request,
        io.grpc.stub.StreamObserver<org.opennms.features.openconfig.proto.gnmi.Gnmi.GetResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getGetMethod(), responseObserver);
    }

    /**
     * <pre>
     * Set allows the client to modify the state of data on the target. The
     * paths to modified along with the new values that the client wishes
     * to set the value to.
     * Reference: gNMI Specification Section 3.4
     * </pre>
     */
    public void set(org.opennms.features.openconfig.proto.gnmi.Gnmi.SetRequest request,
        io.grpc.stub.StreamObserver<org.opennms.features.openconfig.proto.gnmi.Gnmi.SetResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getSetMethod(), responseObserver);
    }

    /**
     * <pre>
     * Subscribe allows a client to request the target to send it values
     * of particular paths within the data tree. These values may be streamed
     * at a particular cadence (STREAM), sent one off on a long-lived channel
     * (POLL), or sent as a one-off retrieval (ONCE).
     * Reference: gNMI Specification Section 3.5
     * </pre>
     */
    public io.grpc.stub.StreamObserver<org.opennms.features.openconfig.proto.gnmi.Gnmi.SubscribeRequest> subscribe(
        io.grpc.stub.StreamObserver<org.opennms.features.openconfig.proto.gnmi.Gnmi.SubscribeResponse> responseObserver) {
      return asyncUnimplementedStreamingCall(getSubscribeMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getCapabilitiesMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.opennms.features.openconfig.proto.gnmi.Gnmi.CapabilityRequest,
                org.opennms.features.openconfig.proto.gnmi.Gnmi.CapabilityResponse>(
                  this, METHODID_CAPABILITIES)))
          .addMethod(
            getGetMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.opennms.features.openconfig.proto.gnmi.Gnmi.GetRequest,
                org.opennms.features.openconfig.proto.gnmi.Gnmi.GetResponse>(
                  this, METHODID_GET)))
          .addMethod(
            getSetMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.opennms.features.openconfig.proto.gnmi.Gnmi.SetRequest,
                org.opennms.features.openconfig.proto.gnmi.Gnmi.SetResponse>(
                  this, METHODID_SET)))
          .addMethod(
            getSubscribeMethod(),
            asyncBidiStreamingCall(
              new MethodHandlers<
                org.opennms.features.openconfig.proto.gnmi.Gnmi.SubscribeRequest,
                org.opennms.features.openconfig.proto.gnmi.Gnmi.SubscribeResponse>(
                  this, METHODID_SUBSCRIBE)))
          .build();
    }
  }

  /**
   */
  public static final class gNMIStub extends io.grpc.stub.AbstractStub<gNMIStub> {
    private gNMIStub(io.grpc.Channel channel) {
      super(channel);
    }

    private gNMIStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected gNMIStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new gNMIStub(channel, callOptions);
    }

    /**
     * <pre>
     * Capabilities allows the client to retrieve the set of capabilities that
     * is supported by the target. This allows the target to validate the
     * service version that is implemented and retrieve the set of models that
     * the target supports. The models can then be specified in subsequent RPCs
     * to restrict the set of data that is utilized.
     * Reference: gNMI Specification Section 3.2
     * </pre>
     */
    public void capabilities(org.opennms.features.openconfig.proto.gnmi.Gnmi.CapabilityRequest request,
        io.grpc.stub.StreamObserver<org.opennms.features.openconfig.proto.gnmi.Gnmi.CapabilityResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getCapabilitiesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Retrieve a snapshot of data from the target. A Get RPC requests that the
     * target snapshots a subset of the data tree as specified by the paths
     * included in the message and serializes this to be returned to the
     * client using the specified encoding.
     * Reference: gNMI Specification Section 3.3
     * </pre>
     */
    public void get(org.opennms.features.openconfig.proto.gnmi.Gnmi.GetRequest request,
        io.grpc.stub.StreamObserver<org.opennms.features.openconfig.proto.gnmi.Gnmi.GetResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Set allows the client to modify the state of data on the target. The
     * paths to modified along with the new values that the client wishes
     * to set the value to.
     * Reference: gNMI Specification Section 3.4
     * </pre>
     */
    public void set(org.opennms.features.openconfig.proto.gnmi.Gnmi.SetRequest request,
        io.grpc.stub.StreamObserver<org.opennms.features.openconfig.proto.gnmi.Gnmi.SetResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getSetMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Subscribe allows a client to request the target to send it values
     * of particular paths within the data tree. These values may be streamed
     * at a particular cadence (STREAM), sent one off on a long-lived channel
     * (POLL), or sent as a one-off retrieval (ONCE).
     * Reference: gNMI Specification Section 3.5
     * </pre>
     */
    public io.grpc.stub.StreamObserver<org.opennms.features.openconfig.proto.gnmi.Gnmi.SubscribeRequest> subscribe(
        io.grpc.stub.StreamObserver<org.opennms.features.openconfig.proto.gnmi.Gnmi.SubscribeResponse> responseObserver) {
      return asyncBidiStreamingCall(
          getChannel().newCall(getSubscribeMethod(), getCallOptions()), responseObserver);
    }
  }

  /**
   */
  public static final class gNMIBlockingStub extends io.grpc.stub.AbstractStub<gNMIBlockingStub> {
    private gNMIBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private gNMIBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected gNMIBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new gNMIBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Capabilities allows the client to retrieve the set of capabilities that
     * is supported by the target. This allows the target to validate the
     * service version that is implemented and retrieve the set of models that
     * the target supports. The models can then be specified in subsequent RPCs
     * to restrict the set of data that is utilized.
     * Reference: gNMI Specification Section 3.2
     * </pre>
     */
    public org.opennms.features.openconfig.proto.gnmi.Gnmi.CapabilityResponse capabilities(org.opennms.features.openconfig.proto.gnmi.Gnmi.CapabilityRequest request) {
      return blockingUnaryCall(
          getChannel(), getCapabilitiesMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Retrieve a snapshot of data from the target. A Get RPC requests that the
     * target snapshots a subset of the data tree as specified by the paths
     * included in the message and serializes this to be returned to the
     * client using the specified encoding.
     * Reference: gNMI Specification Section 3.3
     * </pre>
     */
    public org.opennms.features.openconfig.proto.gnmi.Gnmi.GetResponse get(org.opennms.features.openconfig.proto.gnmi.Gnmi.GetRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Set allows the client to modify the state of data on the target. The
     * paths to modified along with the new values that the client wishes
     * to set the value to.
     * Reference: gNMI Specification Section 3.4
     * </pre>
     */
    public org.opennms.features.openconfig.proto.gnmi.Gnmi.SetResponse set(org.opennms.features.openconfig.proto.gnmi.Gnmi.SetRequest request) {
      return blockingUnaryCall(
          getChannel(), getSetMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class gNMIFutureStub extends io.grpc.stub.AbstractStub<gNMIFutureStub> {
    private gNMIFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private gNMIFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected gNMIFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new gNMIFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Capabilities allows the client to retrieve the set of capabilities that
     * is supported by the target. This allows the target to validate the
     * service version that is implemented and retrieve the set of models that
     * the target supports. The models can then be specified in subsequent RPCs
     * to restrict the set of data that is utilized.
     * Reference: gNMI Specification Section 3.2
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<org.opennms.features.openconfig.proto.gnmi.Gnmi.CapabilityResponse> capabilities(
        org.opennms.features.openconfig.proto.gnmi.Gnmi.CapabilityRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getCapabilitiesMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Retrieve a snapshot of data from the target. A Get RPC requests that the
     * target snapshots a subset of the data tree as specified by the paths
     * included in the message and serializes this to be returned to the
     * client using the specified encoding.
     * Reference: gNMI Specification Section 3.3
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<org.opennms.features.openconfig.proto.gnmi.Gnmi.GetResponse> get(
        org.opennms.features.openconfig.proto.gnmi.Gnmi.GetRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Set allows the client to modify the state of data on the target. The
     * paths to modified along with the new values that the client wishes
     * to set the value to.
     * Reference: gNMI Specification Section 3.4
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<org.opennms.features.openconfig.proto.gnmi.Gnmi.SetResponse> set(
        org.opennms.features.openconfig.proto.gnmi.Gnmi.SetRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getSetMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_CAPABILITIES = 0;
  private static final int METHODID_GET = 1;
  private static final int METHODID_SET = 2;
  private static final int METHODID_SUBSCRIBE = 3;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final gNMIImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(gNMIImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_CAPABILITIES:
          serviceImpl.capabilities((org.opennms.features.openconfig.proto.gnmi.Gnmi.CapabilityRequest) request,
              (io.grpc.stub.StreamObserver<org.opennms.features.openconfig.proto.gnmi.Gnmi.CapabilityResponse>) responseObserver);
          break;
        case METHODID_GET:
          serviceImpl.get((org.opennms.features.openconfig.proto.gnmi.Gnmi.GetRequest) request,
              (io.grpc.stub.StreamObserver<org.opennms.features.openconfig.proto.gnmi.Gnmi.GetResponse>) responseObserver);
          break;
        case METHODID_SET:
          serviceImpl.set((org.opennms.features.openconfig.proto.gnmi.Gnmi.SetRequest) request,
              (io.grpc.stub.StreamObserver<org.opennms.features.openconfig.proto.gnmi.Gnmi.SetResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_SUBSCRIBE:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.subscribe(
              (io.grpc.stub.StreamObserver<org.opennms.features.openconfig.proto.gnmi.Gnmi.SubscribeResponse>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class gNMIBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    gNMIBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return org.opennms.features.openconfig.proto.gnmi.Gnmi.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("gNMI");
    }
  }

  private static final class gNMIFileDescriptorSupplier
      extends gNMIBaseDescriptorSupplier {
    gNMIFileDescriptorSupplier() {}
  }

  private static final class gNMIMethodDescriptorSupplier
      extends gNMIBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    gNMIMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (gNMIGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new gNMIFileDescriptorSupplier())
              .addMethod(getCapabilitiesMethod())
              .addMethod(getGetMethod())
              .addMethod(getSetMethod())
              .addMethod(getSubscribeMethod())
              .build();
        }
      }
    }
    return result;
  }
}
