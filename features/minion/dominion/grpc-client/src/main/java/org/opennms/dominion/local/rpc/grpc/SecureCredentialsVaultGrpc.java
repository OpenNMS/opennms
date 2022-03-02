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
package org.opennms.dominion.local.rpc.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

import io.grpc.stub.ClientCalls;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.24.0)",
    comments = "Source: dominion.proto")
public final class SecureCredentialsVaultGrpc {

  private SecureCredentialsVaultGrpc() {}

  public static final String SERVICE_NAME = "dominion.v1.SecureCredentialsVault";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesRequest,
      org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesResponse> getScvGetSupportedAliasesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ScvGetSupportedAliases",
      requestType = org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesRequest.class,
      responseType = org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesRequest,
      org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesResponse> getScvGetSupportedAliasesMethod() {
    io.grpc.MethodDescriptor<org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesRequest, org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesResponse> getScvGetSupportedAliasesMethod;
    if ((getScvGetSupportedAliasesMethod = SecureCredentialsVaultGrpc.getScvGetSupportedAliasesMethod) == null) {
      synchronized (SecureCredentialsVaultGrpc.class) {
        if ((getScvGetSupportedAliasesMethod = SecureCredentialsVaultGrpc.getScvGetSupportedAliasesMethod) == null) {
          SecureCredentialsVaultGrpc.getScvGetSupportedAliasesMethod = getScvGetSupportedAliasesMethod =
              io.grpc.MethodDescriptor.<org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesRequest, org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ScvGetSupportedAliases"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesResponse.getDefaultInstance()))
              .setSchemaDescriptor(new SecureCredentialsVaultMethodDescriptorSupplier("ScvGetSupportedAliases"))
              .build();
        }
      }
    }
    return getScvGetSupportedAliasesMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsRequest,
      org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsResponse> getScvGetCredentialsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ScvGetCredentials",
      requestType = org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsRequest.class,
      responseType = org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsRequest,
      org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsResponse> getScvGetCredentialsMethod() {
    io.grpc.MethodDescriptor<org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsRequest, org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsResponse> getScvGetCredentialsMethod;
    if ((getScvGetCredentialsMethod = SecureCredentialsVaultGrpc.getScvGetCredentialsMethod) == null) {
      synchronized (SecureCredentialsVaultGrpc.class) {
        if ((getScvGetCredentialsMethod = SecureCredentialsVaultGrpc.getScvGetCredentialsMethod) == null) {
          SecureCredentialsVaultGrpc.getScvGetCredentialsMethod = getScvGetCredentialsMethod =
              io.grpc.MethodDescriptor.<org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsRequest, org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ScvGetCredentials"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsResponse.getDefaultInstance()))
              .setSchemaDescriptor(new SecureCredentialsVaultMethodDescriptorSupplier("ScvGetCredentials"))
              .build();
        }
      }
    }
    return getScvGetCredentialsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsRequest,
      org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsResponse> getScvSetCredentialsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ScvSetCredentials",
      requestType = org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsRequest.class,
      responseType = org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsRequest,
      org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsResponse> getScvSetCredentialsMethod() {
    io.grpc.MethodDescriptor<org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsRequest, org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsResponse> getScvSetCredentialsMethod;
    if ((getScvSetCredentialsMethod = SecureCredentialsVaultGrpc.getScvSetCredentialsMethod) == null) {
      synchronized (SecureCredentialsVaultGrpc.class) {
        if ((getScvSetCredentialsMethod = SecureCredentialsVaultGrpc.getScvSetCredentialsMethod) == null) {
          SecureCredentialsVaultGrpc.getScvSetCredentialsMethod = getScvSetCredentialsMethod =
              io.grpc.MethodDescriptor.<org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsRequest, org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ScvSetCredentials"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsResponse.getDefaultInstance()))
              .setSchemaDescriptor(new SecureCredentialsVaultMethodDescriptorSupplier("ScvSetCredentials"))
              .build();
        }
      }
    }
    return getScvSetCredentialsMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static SecureCredentialsVaultStub newStub(io.grpc.Channel channel) {
    return new SecureCredentialsVaultStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static SecureCredentialsVaultBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new SecureCredentialsVaultBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static SecureCredentialsVaultFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new SecureCredentialsVaultFutureStub(channel);
  }

  /**
   */
  public static abstract class SecureCredentialsVaultImplBase implements io.grpc.BindableService {

    /**
     */
    public void scvGetSupportedAliases(org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesRequest request,
        io.grpc.stub.StreamObserver<org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getScvGetSupportedAliasesMethod(), responseObserver);
    }

    /**
     */
    public void scvGetCredentials(org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsRequest request,
        io.grpc.stub.StreamObserver<org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getScvGetCredentialsMethod(), responseObserver);
    }

    /**
     */
    public void scvSetCredentials(org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsRequest request,
        io.grpc.stub.StreamObserver<org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getScvSetCredentialsMethod(), responseObserver);
    }

    @Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getScvGetSupportedAliasesMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesRequest,
                org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesResponse>(
                  this, METHODID_SCV_GET_SUPPORTED_ALIASES)))
          .addMethod(
            getScvGetCredentialsMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsRequest,
                org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsResponse>(
                  this, METHODID_SCV_GET_CREDENTIALS)))
          .addMethod(
            getScvSetCredentialsMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsRequest,
                org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsResponse>(
                  this, METHODID_SCV_SET_CREDENTIALS)))
          .build();
    }
  }

  /**
   */
  public static final class SecureCredentialsVaultStub extends io.grpc.stub.AbstractStub<SecureCredentialsVaultStub> {
    private SecureCredentialsVaultStub(io.grpc.Channel channel) {
      super(channel);
    }

    private SecureCredentialsVaultStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected SecureCredentialsVaultStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new SecureCredentialsVaultStub(channel, callOptions);
    }

    /**
     */
    public void scvGetSupportedAliases(org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesRequest request,
        io.grpc.stub.StreamObserver<org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesResponse> responseObserver) {
      ClientCalls.asyncUnaryCall(
          getChannel().newCall(getScvGetSupportedAliasesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void scvGetCredentials(org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsRequest request,
        io.grpc.stub.StreamObserver<org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsResponse> responseObserver) {
      ClientCalls.asyncUnaryCall(
          getChannel().newCall(getScvGetCredentialsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void scvSetCredentials(org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsRequest request,
        io.grpc.stub.StreamObserver<org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsResponse> responseObserver) {
      ClientCalls.asyncUnaryCall(
          getChannel().newCall(getScvSetCredentialsMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class SecureCredentialsVaultBlockingStub extends io.grpc.stub.AbstractStub<SecureCredentialsVaultBlockingStub> {
    private SecureCredentialsVaultBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private SecureCredentialsVaultBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected SecureCredentialsVaultBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new SecureCredentialsVaultBlockingStub(channel, callOptions);
    }

    /**
     */
    public org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesResponse scvGetSupportedAliases(org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesRequest request) {
      return blockingUnaryCall(
          getChannel(), getScvGetSupportedAliasesMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsResponse scvGetCredentials(org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsRequest request) {
      return blockingUnaryCall(
          getChannel(), getScvGetCredentialsMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsResponse scvSetCredentials(org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsRequest request) {
      return blockingUnaryCall(
          getChannel(), getScvSetCredentialsMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class SecureCredentialsVaultFutureStub extends io.grpc.stub.AbstractStub<SecureCredentialsVaultFutureStub> {
    private SecureCredentialsVaultFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private SecureCredentialsVaultFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected SecureCredentialsVaultFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new SecureCredentialsVaultFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesResponse> scvGetSupportedAliases(
        org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getScvGetSupportedAliasesMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsResponse> scvGetCredentials(
        org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getScvGetCredentialsMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsResponse> scvSetCredentials(
        org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getScvSetCredentialsMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_SCV_GET_SUPPORTED_ALIASES = 0;
  private static final int METHODID_SCV_GET_CREDENTIALS = 1;
  private static final int METHODID_SCV_SET_CREDENTIALS = 2;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final SecureCredentialsVaultImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(SecureCredentialsVaultImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_SCV_GET_SUPPORTED_ALIASES:
          serviceImpl.scvGetSupportedAliases((org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesRequest) request,
              (io.grpc.stub.StreamObserver<org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesResponse>) responseObserver);
          break;
        case METHODID_SCV_GET_CREDENTIALS:
          serviceImpl.scvGetCredentials((org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsRequest) request,
              (io.grpc.stub.StreamObserver<org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsResponse>) responseObserver);
          break;
        case METHODID_SCV_SET_CREDENTIALS:
          serviceImpl.scvSetCredentials((org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsRequest) request,
              (io.grpc.stub.StreamObserver<org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsResponse>) responseObserver);
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
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class SecureCredentialsVaultBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    SecureCredentialsVaultBaseDescriptorSupplier() {}

    @Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return org.opennms.dominion.local.rpc.grpc.DominionGrpc.getDescriptor();
    }

    @Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("SecureCredentialsVault");
    }
  }

  private static final class SecureCredentialsVaultFileDescriptorSupplier
      extends SecureCredentialsVaultBaseDescriptorSupplier {
    SecureCredentialsVaultFileDescriptorSupplier() {}
  }

  private static final class SecureCredentialsVaultMethodDescriptorSupplier
      extends SecureCredentialsVaultBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    SecureCredentialsVaultMethodDescriptorSupplier(String methodName) {
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
      synchronized (SecureCredentialsVaultGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new SecureCredentialsVaultFileDescriptorSupplier())
              .addMethod(getScvGetSupportedAliasesMethod())
              .addMethod(getScvGetCredentialsMethod())
              .addMethod(getScvSetCredentialsMethod())
              .build();
        }
      }
    }
    return result;
  }
}
