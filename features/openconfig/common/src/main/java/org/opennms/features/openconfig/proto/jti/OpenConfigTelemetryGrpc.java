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

package org.opennms.features.openconfig.proto.jti;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 * <pre>
 * Interface exported by Agent
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.24.0)",
    comments = "Source: telemetry.proto")
public final class OpenConfigTelemetryGrpc {

  private OpenConfigTelemetryGrpc() {}

  public static final String SERVICE_NAME = "telemetry.OpenConfigTelemetry";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<org.opennms.features.openconfig.proto.jti.Telemetry.SubscriptionRequest,
      org.opennms.features.openconfig.proto.jti.Telemetry.OpenConfigData> getTelemetrySubscribeMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "telemetrySubscribe",
      requestType = org.opennms.features.openconfig.proto.jti.Telemetry.SubscriptionRequest.class,
      responseType = org.opennms.features.openconfig.proto.jti.Telemetry.OpenConfigData.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<org.opennms.features.openconfig.proto.jti.Telemetry.SubscriptionRequest,
      org.opennms.features.openconfig.proto.jti.Telemetry.OpenConfigData> getTelemetrySubscribeMethod() {
    io.grpc.MethodDescriptor<org.opennms.features.openconfig.proto.jti.Telemetry.SubscriptionRequest, org.opennms.features.openconfig.proto.jti.Telemetry.OpenConfigData> getTelemetrySubscribeMethod;
    if ((getTelemetrySubscribeMethod = OpenConfigTelemetryGrpc.getTelemetrySubscribeMethod) == null) {
      synchronized (OpenConfigTelemetryGrpc.class) {
        if ((getTelemetrySubscribeMethod = OpenConfigTelemetryGrpc.getTelemetrySubscribeMethod) == null) {
          OpenConfigTelemetryGrpc.getTelemetrySubscribeMethod = getTelemetrySubscribeMethod =
              io.grpc.MethodDescriptor.<org.opennms.features.openconfig.proto.jti.Telemetry.SubscriptionRequest, org.opennms.features.openconfig.proto.jti.Telemetry.OpenConfigData>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "telemetrySubscribe"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.opennms.features.openconfig.proto.jti.Telemetry.SubscriptionRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.opennms.features.openconfig.proto.jti.Telemetry.OpenConfigData.getDefaultInstance()))
              .setSchemaDescriptor(new OpenConfigTelemetryMethodDescriptorSupplier("telemetrySubscribe"))
              .build();
        }
      }
    }
    return getTelemetrySubscribeMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.opennms.features.openconfig.proto.jti.Telemetry.CancelSubscriptionRequest,
      org.opennms.features.openconfig.proto.jti.Telemetry.CancelSubscriptionReply> getCancelTelemetrySubscriptionMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "cancelTelemetrySubscription",
      requestType = org.opennms.features.openconfig.proto.jti.Telemetry.CancelSubscriptionRequest.class,
      responseType = org.opennms.features.openconfig.proto.jti.Telemetry.CancelSubscriptionReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.opennms.features.openconfig.proto.jti.Telemetry.CancelSubscriptionRequest,
      org.opennms.features.openconfig.proto.jti.Telemetry.CancelSubscriptionReply> getCancelTelemetrySubscriptionMethod() {
    io.grpc.MethodDescriptor<org.opennms.features.openconfig.proto.jti.Telemetry.CancelSubscriptionRequest, org.opennms.features.openconfig.proto.jti.Telemetry.CancelSubscriptionReply> getCancelTelemetrySubscriptionMethod;
    if ((getCancelTelemetrySubscriptionMethod = OpenConfigTelemetryGrpc.getCancelTelemetrySubscriptionMethod) == null) {
      synchronized (OpenConfigTelemetryGrpc.class) {
        if ((getCancelTelemetrySubscriptionMethod = OpenConfigTelemetryGrpc.getCancelTelemetrySubscriptionMethod) == null) {
          OpenConfigTelemetryGrpc.getCancelTelemetrySubscriptionMethod = getCancelTelemetrySubscriptionMethod =
              io.grpc.MethodDescriptor.<org.opennms.features.openconfig.proto.jti.Telemetry.CancelSubscriptionRequest, org.opennms.features.openconfig.proto.jti.Telemetry.CancelSubscriptionReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "cancelTelemetrySubscription"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.opennms.features.openconfig.proto.jti.Telemetry.CancelSubscriptionRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.opennms.features.openconfig.proto.jti.Telemetry.CancelSubscriptionReply.getDefaultInstance()))
              .setSchemaDescriptor(new OpenConfigTelemetryMethodDescriptorSupplier("cancelTelemetrySubscription"))
              .build();
        }
      }
    }
    return getCancelTelemetrySubscriptionMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.opennms.features.openconfig.proto.jti.Telemetry.GetSubscriptionsRequest,
      org.opennms.features.openconfig.proto.jti.Telemetry.GetSubscriptionsReply> getGetTelemetrySubscriptionsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "getTelemetrySubscriptions",
      requestType = org.opennms.features.openconfig.proto.jti.Telemetry.GetSubscriptionsRequest.class,
      responseType = org.opennms.features.openconfig.proto.jti.Telemetry.GetSubscriptionsReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.opennms.features.openconfig.proto.jti.Telemetry.GetSubscriptionsRequest,
      org.opennms.features.openconfig.proto.jti.Telemetry.GetSubscriptionsReply> getGetTelemetrySubscriptionsMethod() {
    io.grpc.MethodDescriptor<org.opennms.features.openconfig.proto.jti.Telemetry.GetSubscriptionsRequest, org.opennms.features.openconfig.proto.jti.Telemetry.GetSubscriptionsReply> getGetTelemetrySubscriptionsMethod;
    if ((getGetTelemetrySubscriptionsMethod = OpenConfigTelemetryGrpc.getGetTelemetrySubscriptionsMethod) == null) {
      synchronized (OpenConfigTelemetryGrpc.class) {
        if ((getGetTelemetrySubscriptionsMethod = OpenConfigTelemetryGrpc.getGetTelemetrySubscriptionsMethod) == null) {
          OpenConfigTelemetryGrpc.getGetTelemetrySubscriptionsMethod = getGetTelemetrySubscriptionsMethod =
              io.grpc.MethodDescriptor.<org.opennms.features.openconfig.proto.jti.Telemetry.GetSubscriptionsRequest, org.opennms.features.openconfig.proto.jti.Telemetry.GetSubscriptionsReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "getTelemetrySubscriptions"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.opennms.features.openconfig.proto.jti.Telemetry.GetSubscriptionsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.opennms.features.openconfig.proto.jti.Telemetry.GetSubscriptionsReply.getDefaultInstance()))
              .setSchemaDescriptor(new OpenConfigTelemetryMethodDescriptorSupplier("getTelemetrySubscriptions"))
              .build();
        }
      }
    }
    return getGetTelemetrySubscriptionsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.opennms.features.openconfig.proto.jti.Telemetry.GetOperationalStateRequest,
      org.opennms.features.openconfig.proto.jti.Telemetry.GetOperationalStateReply> getGetTelemetryOperationalStateMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "getTelemetryOperationalState",
      requestType = org.opennms.features.openconfig.proto.jti.Telemetry.GetOperationalStateRequest.class,
      responseType = org.opennms.features.openconfig.proto.jti.Telemetry.GetOperationalStateReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.opennms.features.openconfig.proto.jti.Telemetry.GetOperationalStateRequest,
      org.opennms.features.openconfig.proto.jti.Telemetry.GetOperationalStateReply> getGetTelemetryOperationalStateMethod() {
    io.grpc.MethodDescriptor<org.opennms.features.openconfig.proto.jti.Telemetry.GetOperationalStateRequest, org.opennms.features.openconfig.proto.jti.Telemetry.GetOperationalStateReply> getGetTelemetryOperationalStateMethod;
    if ((getGetTelemetryOperationalStateMethod = OpenConfigTelemetryGrpc.getGetTelemetryOperationalStateMethod) == null) {
      synchronized (OpenConfigTelemetryGrpc.class) {
        if ((getGetTelemetryOperationalStateMethod = OpenConfigTelemetryGrpc.getGetTelemetryOperationalStateMethod) == null) {
          OpenConfigTelemetryGrpc.getGetTelemetryOperationalStateMethod = getGetTelemetryOperationalStateMethod =
              io.grpc.MethodDescriptor.<org.opennms.features.openconfig.proto.jti.Telemetry.GetOperationalStateRequest, org.opennms.features.openconfig.proto.jti.Telemetry.GetOperationalStateReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "getTelemetryOperationalState"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.opennms.features.openconfig.proto.jti.Telemetry.GetOperationalStateRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.opennms.features.openconfig.proto.jti.Telemetry.GetOperationalStateReply.getDefaultInstance()))
              .setSchemaDescriptor(new OpenConfigTelemetryMethodDescriptorSupplier("getTelemetryOperationalState"))
              .build();
        }
      }
    }
    return getGetTelemetryOperationalStateMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.opennms.features.openconfig.proto.jti.Telemetry.DataEncodingRequest,
      org.opennms.features.openconfig.proto.jti.Telemetry.DataEncodingReply> getGetDataEncodingsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "getDataEncodings",
      requestType = org.opennms.features.openconfig.proto.jti.Telemetry.DataEncodingRequest.class,
      responseType = org.opennms.features.openconfig.proto.jti.Telemetry.DataEncodingReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.opennms.features.openconfig.proto.jti.Telemetry.DataEncodingRequest,
      org.opennms.features.openconfig.proto.jti.Telemetry.DataEncodingReply> getGetDataEncodingsMethod() {
    io.grpc.MethodDescriptor<org.opennms.features.openconfig.proto.jti.Telemetry.DataEncodingRequest, org.opennms.features.openconfig.proto.jti.Telemetry.DataEncodingReply> getGetDataEncodingsMethod;
    if ((getGetDataEncodingsMethod = OpenConfigTelemetryGrpc.getGetDataEncodingsMethod) == null) {
      synchronized (OpenConfigTelemetryGrpc.class) {
        if ((getGetDataEncodingsMethod = OpenConfigTelemetryGrpc.getGetDataEncodingsMethod) == null) {
          OpenConfigTelemetryGrpc.getGetDataEncodingsMethod = getGetDataEncodingsMethod =
              io.grpc.MethodDescriptor.<org.opennms.features.openconfig.proto.jti.Telemetry.DataEncodingRequest, org.opennms.features.openconfig.proto.jti.Telemetry.DataEncodingReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "getDataEncodings"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.opennms.features.openconfig.proto.jti.Telemetry.DataEncodingRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.opennms.features.openconfig.proto.jti.Telemetry.DataEncodingReply.getDefaultInstance()))
              .setSchemaDescriptor(new OpenConfigTelemetryMethodDescriptorSupplier("getDataEncodings"))
              .build();
        }
      }
    }
    return getGetDataEncodingsMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static OpenConfigTelemetryStub newStub(io.grpc.Channel channel) {
    return new OpenConfigTelemetryStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static OpenConfigTelemetryBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new OpenConfigTelemetryBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static OpenConfigTelemetryFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new OpenConfigTelemetryFutureStub(channel);
  }

  /**
   * <pre>
   * Interface exported by Agent
   * </pre>
   */
  public static abstract class OpenConfigTelemetryImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * Request an inline subscription for data at the specified path.
     * The device should send telemetry data back on the same
     * connection as the subscription request.
     * </pre>
     */
    public void telemetrySubscribe(org.opennms.features.openconfig.proto.jti.Telemetry.SubscriptionRequest request,
        io.grpc.stub.StreamObserver<org.opennms.features.openconfig.proto.jti.Telemetry.OpenConfigData> responseObserver) {
      asyncUnimplementedUnaryCall(getTelemetrySubscribeMethod(), responseObserver);
    }

    /**
     * <pre>
     * Terminates and removes an exisiting telemetry subscription
     * </pre>
     */
    public void cancelTelemetrySubscription(org.opennms.features.openconfig.proto.jti.Telemetry.CancelSubscriptionRequest request,
        io.grpc.stub.StreamObserver<org.opennms.features.openconfig.proto.jti.Telemetry.CancelSubscriptionReply> responseObserver) {
      asyncUnimplementedUnaryCall(getCancelTelemetrySubscriptionMethod(), responseObserver);
    }

    /**
     * <pre>
     * Get the list of current telemetry subscriptions from the
     * target. This command returns a list of existing subscriptions
     * not including those that are established via configuration.
     * </pre>
     */
    public void getTelemetrySubscriptions(org.opennms.features.openconfig.proto.jti.Telemetry.GetSubscriptionsRequest request,
        io.grpc.stub.StreamObserver<org.opennms.features.openconfig.proto.jti.Telemetry.GetSubscriptionsReply> responseObserver) {
      asyncUnimplementedUnaryCall(getGetTelemetrySubscriptionsMethod(), responseObserver);
    }

    /**
     * <pre>
     * Get Telemetry Agent Operational States
     * </pre>
     */
    public void getTelemetryOperationalState(org.opennms.features.openconfig.proto.jti.Telemetry.GetOperationalStateRequest request,
        io.grpc.stub.StreamObserver<org.opennms.features.openconfig.proto.jti.Telemetry.GetOperationalStateReply> responseObserver) {
      asyncUnimplementedUnaryCall(getGetTelemetryOperationalStateMethod(), responseObserver);
    }

    /**
     * <pre>
     * Return the set of data encodings supported by the device for
     * telemetry data
     * </pre>
     */
    public void getDataEncodings(org.opennms.features.openconfig.proto.jti.Telemetry.DataEncodingRequest request,
        io.grpc.stub.StreamObserver<org.opennms.features.openconfig.proto.jti.Telemetry.DataEncodingReply> responseObserver) {
      asyncUnimplementedUnaryCall(getGetDataEncodingsMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getTelemetrySubscribeMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                org.opennms.features.openconfig.proto.jti.Telemetry.SubscriptionRequest,
                org.opennms.features.openconfig.proto.jti.Telemetry.OpenConfigData>(
                  this, METHODID_TELEMETRY_SUBSCRIBE)))
          .addMethod(
            getCancelTelemetrySubscriptionMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.opennms.features.openconfig.proto.jti.Telemetry.CancelSubscriptionRequest,
                org.opennms.features.openconfig.proto.jti.Telemetry.CancelSubscriptionReply>(
                  this, METHODID_CANCEL_TELEMETRY_SUBSCRIPTION)))
          .addMethod(
            getGetTelemetrySubscriptionsMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.opennms.features.openconfig.proto.jti.Telemetry.GetSubscriptionsRequest,
                org.opennms.features.openconfig.proto.jti.Telemetry.GetSubscriptionsReply>(
                  this, METHODID_GET_TELEMETRY_SUBSCRIPTIONS)))
          .addMethod(
            getGetTelemetryOperationalStateMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.opennms.features.openconfig.proto.jti.Telemetry.GetOperationalStateRequest,
                org.opennms.features.openconfig.proto.jti.Telemetry.GetOperationalStateReply>(
                  this, METHODID_GET_TELEMETRY_OPERATIONAL_STATE)))
          .addMethod(
            getGetDataEncodingsMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.opennms.features.openconfig.proto.jti.Telemetry.DataEncodingRequest,
                org.opennms.features.openconfig.proto.jti.Telemetry.DataEncodingReply>(
                  this, METHODID_GET_DATA_ENCODINGS)))
          .build();
    }
  }

  /**
   * <pre>
   * Interface exported by Agent
   * </pre>
   */
  public static final class OpenConfigTelemetryStub extends io.grpc.stub.AbstractStub<OpenConfigTelemetryStub> {
    private OpenConfigTelemetryStub(io.grpc.Channel channel) {
      super(channel);
    }

    private OpenConfigTelemetryStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected OpenConfigTelemetryStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new OpenConfigTelemetryStub(channel, callOptions);
    }

    /**
     * <pre>
     * Request an inline subscription for data at the specified path.
     * The device should send telemetry data back on the same
     * connection as the subscription request.
     * </pre>
     */
    public void telemetrySubscribe(org.opennms.features.openconfig.proto.jti.Telemetry.SubscriptionRequest request,
        io.grpc.stub.StreamObserver<org.opennms.features.openconfig.proto.jti.Telemetry.OpenConfigData> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getTelemetrySubscribeMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Terminates and removes an exisiting telemetry subscription
     * </pre>
     */
    public void cancelTelemetrySubscription(org.opennms.features.openconfig.proto.jti.Telemetry.CancelSubscriptionRequest request,
        io.grpc.stub.StreamObserver<org.opennms.features.openconfig.proto.jti.Telemetry.CancelSubscriptionReply> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getCancelTelemetrySubscriptionMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Get the list of current telemetry subscriptions from the
     * target. This command returns a list of existing subscriptions
     * not including those that are established via configuration.
     * </pre>
     */
    public void getTelemetrySubscriptions(org.opennms.features.openconfig.proto.jti.Telemetry.GetSubscriptionsRequest request,
        io.grpc.stub.StreamObserver<org.opennms.features.openconfig.proto.jti.Telemetry.GetSubscriptionsReply> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetTelemetrySubscriptionsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Get Telemetry Agent Operational States
     * </pre>
     */
    public void getTelemetryOperationalState(org.opennms.features.openconfig.proto.jti.Telemetry.GetOperationalStateRequest request,
        io.grpc.stub.StreamObserver<org.opennms.features.openconfig.proto.jti.Telemetry.GetOperationalStateReply> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetTelemetryOperationalStateMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Return the set of data encodings supported by the device for
     * telemetry data
     * </pre>
     */
    public void getDataEncodings(org.opennms.features.openconfig.proto.jti.Telemetry.DataEncodingRequest request,
        io.grpc.stub.StreamObserver<org.opennms.features.openconfig.proto.jti.Telemetry.DataEncodingReply> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetDataEncodingsMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * <pre>
   * Interface exported by Agent
   * </pre>
   */
  public static final class OpenConfigTelemetryBlockingStub extends io.grpc.stub.AbstractStub<OpenConfigTelemetryBlockingStub> {
    private OpenConfigTelemetryBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private OpenConfigTelemetryBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected OpenConfigTelemetryBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new OpenConfigTelemetryBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Request an inline subscription for data at the specified path.
     * The device should send telemetry data back on the same
     * connection as the subscription request.
     * </pre>
     */
    public java.util.Iterator<org.opennms.features.openconfig.proto.jti.Telemetry.OpenConfigData> telemetrySubscribe(
        org.opennms.features.openconfig.proto.jti.Telemetry.SubscriptionRequest request) {
      return blockingServerStreamingCall(
          getChannel(), getTelemetrySubscribeMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Terminates and removes an exisiting telemetry subscription
     * </pre>
     */
    public org.opennms.features.openconfig.proto.jti.Telemetry.CancelSubscriptionReply cancelTelemetrySubscription(org.opennms.features.openconfig.proto.jti.Telemetry.CancelSubscriptionRequest request) {
      return blockingUnaryCall(
          getChannel(), getCancelTelemetrySubscriptionMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Get the list of current telemetry subscriptions from the
     * target. This command returns a list of existing subscriptions
     * not including those that are established via configuration.
     * </pre>
     */
    public org.opennms.features.openconfig.proto.jti.Telemetry.GetSubscriptionsReply getTelemetrySubscriptions(org.opennms.features.openconfig.proto.jti.Telemetry.GetSubscriptionsRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetTelemetrySubscriptionsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Get Telemetry Agent Operational States
     * </pre>
     */
    public org.opennms.features.openconfig.proto.jti.Telemetry.GetOperationalStateReply getTelemetryOperationalState(org.opennms.features.openconfig.proto.jti.Telemetry.GetOperationalStateRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetTelemetryOperationalStateMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Return the set of data encodings supported by the device for
     * telemetry data
     * </pre>
     */
    public org.opennms.features.openconfig.proto.jti.Telemetry.DataEncodingReply getDataEncodings(org.opennms.features.openconfig.proto.jti.Telemetry.DataEncodingRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetDataEncodingsMethod(), getCallOptions(), request);
    }
  }

  /**
   * <pre>
   * Interface exported by Agent
   * </pre>
   */
  public static final class OpenConfigTelemetryFutureStub extends io.grpc.stub.AbstractStub<OpenConfigTelemetryFutureStub> {
    private OpenConfigTelemetryFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private OpenConfigTelemetryFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected OpenConfigTelemetryFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new OpenConfigTelemetryFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Terminates and removes an exisiting telemetry subscription
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<org.opennms.features.openconfig.proto.jti.Telemetry.CancelSubscriptionReply> cancelTelemetrySubscription(
        org.opennms.features.openconfig.proto.jti.Telemetry.CancelSubscriptionRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getCancelTelemetrySubscriptionMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Get the list of current telemetry subscriptions from the
     * target. This command returns a list of existing subscriptions
     * not including those that are established via configuration.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<org.opennms.features.openconfig.proto.jti.Telemetry.GetSubscriptionsReply> getTelemetrySubscriptions(
        org.opennms.features.openconfig.proto.jti.Telemetry.GetSubscriptionsRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetTelemetrySubscriptionsMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Get Telemetry Agent Operational States
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<org.opennms.features.openconfig.proto.jti.Telemetry.GetOperationalStateReply> getTelemetryOperationalState(
        org.opennms.features.openconfig.proto.jti.Telemetry.GetOperationalStateRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetTelemetryOperationalStateMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Return the set of data encodings supported by the device for
     * telemetry data
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<org.opennms.features.openconfig.proto.jti.Telemetry.DataEncodingReply> getDataEncodings(
        org.opennms.features.openconfig.proto.jti.Telemetry.DataEncodingRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetDataEncodingsMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_TELEMETRY_SUBSCRIBE = 0;
  private static final int METHODID_CANCEL_TELEMETRY_SUBSCRIPTION = 1;
  private static final int METHODID_GET_TELEMETRY_SUBSCRIPTIONS = 2;
  private static final int METHODID_GET_TELEMETRY_OPERATIONAL_STATE = 3;
  private static final int METHODID_GET_DATA_ENCODINGS = 4;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final OpenConfigTelemetryImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(OpenConfigTelemetryImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_TELEMETRY_SUBSCRIBE:
          serviceImpl.telemetrySubscribe((org.opennms.features.openconfig.proto.jti.Telemetry.SubscriptionRequest) request,
              (io.grpc.stub.StreamObserver<org.opennms.features.openconfig.proto.jti.Telemetry.OpenConfigData>) responseObserver);
          break;
        case METHODID_CANCEL_TELEMETRY_SUBSCRIPTION:
          serviceImpl.cancelTelemetrySubscription((org.opennms.features.openconfig.proto.jti.Telemetry.CancelSubscriptionRequest) request,
              (io.grpc.stub.StreamObserver<org.opennms.features.openconfig.proto.jti.Telemetry.CancelSubscriptionReply>) responseObserver);
          break;
        case METHODID_GET_TELEMETRY_SUBSCRIPTIONS:
          serviceImpl.getTelemetrySubscriptions((org.opennms.features.openconfig.proto.jti.Telemetry.GetSubscriptionsRequest) request,
              (io.grpc.stub.StreamObserver<org.opennms.features.openconfig.proto.jti.Telemetry.GetSubscriptionsReply>) responseObserver);
          break;
        case METHODID_GET_TELEMETRY_OPERATIONAL_STATE:
          serviceImpl.getTelemetryOperationalState((org.opennms.features.openconfig.proto.jti.Telemetry.GetOperationalStateRequest) request,
              (io.grpc.stub.StreamObserver<org.opennms.features.openconfig.proto.jti.Telemetry.GetOperationalStateReply>) responseObserver);
          break;
        case METHODID_GET_DATA_ENCODINGS:
          serviceImpl.getDataEncodings((org.opennms.features.openconfig.proto.jti.Telemetry.DataEncodingRequest) request,
              (io.grpc.stub.StreamObserver<org.opennms.features.openconfig.proto.jti.Telemetry.DataEncodingReply>) responseObserver);
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
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class OpenConfigTelemetryBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    OpenConfigTelemetryBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return org.opennms.features.openconfig.proto.jti.Telemetry.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("OpenConfigTelemetry");
    }
  }

  private static final class OpenConfigTelemetryFileDescriptorSupplier
      extends OpenConfigTelemetryBaseDescriptorSupplier {
    OpenConfigTelemetryFileDescriptorSupplier() {}
  }

  private static final class OpenConfigTelemetryMethodDescriptorSupplier
      extends OpenConfigTelemetryBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    OpenConfigTelemetryMethodDescriptorSupplier(String methodName) {
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
      synchronized (OpenConfigTelemetryGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new OpenConfigTelemetryFileDescriptorSupplier())
              .addMethod(getTelemetrySubscribeMethod())
              .addMethod(getCancelTelemetrySubscriptionMethod())
              .addMethod(getGetTelemetrySubscriptionsMethod())
              .addMethod(getGetTelemetryOperationalStateMethod())
              .addMethod(getGetDataEncodingsMethod())
              .build();
        }
      }
    }
    return result;
  }
}
