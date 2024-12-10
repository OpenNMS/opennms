/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2024 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2024 The OpenNMS Group, Inc.
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

public final class DominionGrpc {
  private DominionGrpc() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  public interface ClientCredentialsOrBuilder extends
      // @@protoc_insertion_point(interface_extends:dominion.v1.ClientCredentials)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <code>string clientId = 1;</code>
     * @return The clientId.
     */
    java.lang.String getClientId();
    /**
     * <code>string clientId = 1;</code>
     * @return The bytes for clientId.
     */
    com.google.protobuf.ByteString
        getClientIdBytes();

    /**
     * <code>string clientSecret = 2;</code>
     * @return The clientSecret.
     */
    java.lang.String getClientSecret();
    /**
     * <code>string clientSecret = 2;</code>
     * @return The bytes for clientSecret.
     */
    com.google.protobuf.ByteString
        getClientSecretBytes();
  }
  /**
   * Protobuf type {@code dominion.v1.ClientCredentials}
   */
  public static final class ClientCredentials extends
      com.google.protobuf.GeneratedMessageV3 implements
      // @@protoc_insertion_point(message_implements:dominion.v1.ClientCredentials)
      ClientCredentialsOrBuilder {
  private static final long serialVersionUID = 0L;
    // Use ClientCredentials.newBuilder() to construct.
    private ClientCredentials(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
      super(builder);
    }
    private ClientCredentials() {
      clientId_ = "";
      clientSecret_ = "";
    }

    @java.lang.Override
    @SuppressWarnings({"unused"})
    protected java.lang.Object newInstance(
        UnusedPrivateParameter unused) {
      return new ClientCredentials();
    }

    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return org.opennms.dominion.local.rpc.grpc.DominionGrpc.internal_static_dominion_v1_ClientCredentials_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return org.opennms.dominion.local.rpc.grpc.DominionGrpc.internal_static_dominion_v1_ClientCredentials_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials.class, org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials.Builder.class);
    }

    public static final int CLIENTID_FIELD_NUMBER = 1;
    @SuppressWarnings("serial")
    private volatile java.lang.Object clientId_ = "";
    /**
     * <code>string clientId = 1;</code>
     * @return The clientId.
     */
    @java.lang.Override
    public java.lang.String getClientId() {
      java.lang.Object ref = clientId_;
      if (ref instanceof java.lang.String) {
        return (java.lang.String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        clientId_ = s;
        return s;
      }
    }
    /**
     * <code>string clientId = 1;</code>
     * @return The bytes for clientId.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getClientIdBytes() {
      java.lang.Object ref = clientId_;
      if (ref instanceof java.lang.String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        clientId_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    public static final int CLIENTSECRET_FIELD_NUMBER = 2;
    @SuppressWarnings("serial")
    private volatile java.lang.Object clientSecret_ = "";
    /**
     * <code>string clientSecret = 2;</code>
     * @return The clientSecret.
     */
    @java.lang.Override
    public java.lang.String getClientSecret() {
      java.lang.Object ref = clientSecret_;
      if (ref instanceof java.lang.String) {
        return (java.lang.String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        clientSecret_ = s;
        return s;
      }
    }
    /**
     * <code>string clientSecret = 2;</code>
     * @return The bytes for clientSecret.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getClientSecretBytes() {
      java.lang.Object ref = clientSecret_;
      if (ref instanceof java.lang.String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        clientSecret_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    private byte memoizedIsInitialized = -1;
    @java.lang.Override
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    @java.lang.Override
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(clientId_)) {
        com.google.protobuf.GeneratedMessageV3.writeString(output, 1, clientId_);
      }
      if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(clientSecret_)) {
        com.google.protobuf.GeneratedMessageV3.writeString(output, 2, clientSecret_);
      }
      getUnknownFields().writeTo(output);
    }

    @java.lang.Override
    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(clientId_)) {
        size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, clientId_);
      }
      if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(clientSecret_)) {
        size += com.google.protobuf.GeneratedMessageV3.computeStringSize(2, clientSecret_);
      }
      size += getUnknownFields().getSerializedSize();
      memoizedSize = size;
      return size;
    }

    @java.lang.Override
    public boolean equals(final java.lang.Object obj) {
      if (obj == this) {
       return true;
      }
      if (!(obj instanceof org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials)) {
        return super.equals(obj);
      }
      org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials other = (org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials) obj;

      if (!getClientId()
          .equals(other.getClientId())) return false;
      if (!getClientSecret()
          .equals(other.getClientSecret())) return false;
      if (!getUnknownFields().equals(other.getUnknownFields())) return false;
      return true;
    }

    @java.lang.Override
    public int hashCode() {
      if (memoizedHashCode != 0) {
        return memoizedHashCode;
      }
      int hash = 41;
      hash = (19 * hash) + getDescriptor().hashCode();
      hash = (37 * hash) + CLIENTID_FIELD_NUMBER;
      hash = (53 * hash) + getClientId().hashCode();
      hash = (37 * hash) + CLIENTSECRET_FIELD_NUMBER;
      hash = (53 * hash) + getClientSecret().hashCode();
      hash = (29 * hash) + getUnknownFields().hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input);
    }

    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    @java.lang.Override
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
    }
    public static Builder newBuilder(org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }
    @java.lang.Override
    public Builder toBuilder() {
      return this == DEFAULT_INSTANCE
          ? new Builder() : new Builder().mergeFrom(this);
    }

    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code dominion.v1.ClientCredentials}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:dominion.v1.ClientCredentials)
        org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentialsOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return org.opennms.dominion.local.rpc.grpc.DominionGrpc.internal_static_dominion_v1_ClientCredentials_descriptor;
      }

      @java.lang.Override
      protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return org.opennms.dominion.local.rpc.grpc.DominionGrpc.internal_static_dominion_v1_ClientCredentials_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials.class, org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials.Builder.class);
      }

      // Construct using org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials.newBuilder()
      private Builder() {

      }

      private Builder(
          com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
        super(parent);

      }
      @java.lang.Override
      public Builder clear() {
        super.clear();
        bitField0_ = 0;
        clientId_ = "";
        clientSecret_ = "";
        return this;
      }

      @java.lang.Override
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return org.opennms.dominion.local.rpc.grpc.DominionGrpc.internal_static_dominion_v1_ClientCredentials_descriptor;
      }

      @java.lang.Override
      public org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials getDefaultInstanceForType() {
        return org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials.getDefaultInstance();
      }

      @java.lang.Override
      public org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials build() {
        org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      @java.lang.Override
      public org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials buildPartial() {
        org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials result = new org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials(this);
        if (bitField0_ != 0) { buildPartial0(result); }
        onBuilt();
        return result;
      }

      private void buildPartial0(org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials result) {
        int from_bitField0_ = bitField0_;
        if (((from_bitField0_ & 0x00000001) != 0)) {
          result.clientId_ = clientId_;
        }
        if (((from_bitField0_ & 0x00000002) != 0)) {
          result.clientSecret_ = clientSecret_;
        }
      }

      @java.lang.Override
      public Builder clone() {
        return super.clone();
      }
      @java.lang.Override
      public Builder setField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return super.setField(field, value);
      }
      @java.lang.Override
      public Builder clearField(
          com.google.protobuf.Descriptors.FieldDescriptor field) {
        return super.clearField(field);
      }
      @java.lang.Override
      public Builder clearOneof(
          com.google.protobuf.Descriptors.OneofDescriptor oneof) {
        return super.clearOneof(oneof);
      }
      @java.lang.Override
      public Builder setRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          int index, java.lang.Object value) {
        return super.setRepeatedField(field, index, value);
      }
      @java.lang.Override
      public Builder addRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return super.addRepeatedField(field, value);
      }
      @java.lang.Override
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials) {
          return mergeFrom((org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials other) {
        if (other == org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials.getDefaultInstance()) return this;
        if (!other.getClientId().isEmpty()) {
          clientId_ = other.clientId_;
          bitField0_ |= 0x00000001;
          onChanged();
        }
        if (!other.getClientSecret().isEmpty()) {
          clientSecret_ = other.clientSecret_;
          bitField0_ |= 0x00000002;
          onChanged();
        }
        this.mergeUnknownFields(other.getUnknownFields());
        onChanged();
        return this;
      }

      @java.lang.Override
      public final boolean isInitialized() {
        return true;
      }

      @java.lang.Override
      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        if (extensionRegistry == null) {
          throw new java.lang.NullPointerException();
        }
        try {
          boolean done = false;
          while (!done) {
            int tag = input.readTag();
            switch (tag) {
              case 0:
                done = true;
                break;
              case 10: {
                clientId_ = input.readStringRequireUtf8();
                bitField0_ |= 0x00000001;
                break;
              } // case 10
              case 18: {
                clientSecret_ = input.readStringRequireUtf8();
                bitField0_ |= 0x00000002;
                break;
              } // case 18
              default: {
                if (!super.parseUnknownField(input, extensionRegistry, tag)) {
                  done = true; // was an endgroup tag
                }
                break;
              } // default:
            } // switch (tag)
          } // while (!done)
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          throw e.unwrapIOException();
        } finally {
          onChanged();
        } // finally
        return this;
      }
      private int bitField0_;

      private java.lang.Object clientId_ = "";
      /**
       * <code>string clientId = 1;</code>
       * @return The clientId.
       */
      public java.lang.String getClientId() {
        java.lang.Object ref = clientId_;
        if (!(ref instanceof java.lang.String)) {
          com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
          java.lang.String s = bs.toStringUtf8();
          clientId_ = s;
          return s;
        } else {
          return (java.lang.String) ref;
        }
      }
      /**
       * <code>string clientId = 1;</code>
       * @return The bytes for clientId.
       */
      public com.google.protobuf.ByteString
          getClientIdBytes() {
        java.lang.Object ref = clientId_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (java.lang.String) ref);
          clientId_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>string clientId = 1;</code>
       * @param value The clientId to set.
       * @return This builder for chaining.
       */
      public Builder setClientId(
          java.lang.String value) {
        if (value == null) { throw new NullPointerException(); }
        clientId_ = value;
        bitField0_ |= 0x00000001;
        onChanged();
        return this;
      }
      /**
       * <code>string clientId = 1;</code>
       * @return This builder for chaining.
       */
      public Builder clearClientId() {
        clientId_ = getDefaultInstance().getClientId();
        bitField0_ = (bitField0_ & ~0x00000001);
        onChanged();
        return this;
      }
      /**
       * <code>string clientId = 1;</code>
       * @param value The bytes for clientId to set.
       * @return This builder for chaining.
       */
      public Builder setClientIdBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) { throw new NullPointerException(); }
        checkByteStringIsUtf8(value);
        clientId_ = value;
        bitField0_ |= 0x00000001;
        onChanged();
        return this;
      }

      private java.lang.Object clientSecret_ = "";
      /**
       * <code>string clientSecret = 2;</code>
       * @return The clientSecret.
       */
      public java.lang.String getClientSecret() {
        java.lang.Object ref = clientSecret_;
        if (!(ref instanceof java.lang.String)) {
          com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
          java.lang.String s = bs.toStringUtf8();
          clientSecret_ = s;
          return s;
        } else {
          return (java.lang.String) ref;
        }
      }
      /**
       * <code>string clientSecret = 2;</code>
       * @return The bytes for clientSecret.
       */
      public com.google.protobuf.ByteString
          getClientSecretBytes() {
        java.lang.Object ref = clientSecret_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (java.lang.String) ref);
          clientSecret_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>string clientSecret = 2;</code>
       * @param value The clientSecret to set.
       * @return This builder for chaining.
       */
      public Builder setClientSecret(
          java.lang.String value) {
        if (value == null) { throw new NullPointerException(); }
        clientSecret_ = value;
        bitField0_ |= 0x00000002;
        onChanged();
        return this;
      }
      /**
       * <code>string clientSecret = 2;</code>
       * @return This builder for chaining.
       */
      public Builder clearClientSecret() {
        clientSecret_ = getDefaultInstance().getClientSecret();
        bitField0_ = (bitField0_ & ~0x00000002);
        onChanged();
        return this;
      }
      /**
       * <code>string clientSecret = 2;</code>
       * @param value The bytes for clientSecret to set.
       * @return This builder for chaining.
       */
      public Builder setClientSecretBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) { throw new NullPointerException(); }
        checkByteStringIsUtf8(value);
        clientSecret_ = value;
        bitField0_ |= 0x00000002;
        onChanged();
        return this;
      }
      @java.lang.Override
      public final Builder setUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.setUnknownFields(unknownFields);
      }

      @java.lang.Override
      public final Builder mergeUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.mergeUnknownFields(unknownFields);
      }


      // @@protoc_insertion_point(builder_scope:dominion.v1.ClientCredentials)
    }

    // @@protoc_insertion_point(class_scope:dominion.v1.ClientCredentials)
    private static final org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials();
    }

    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<ClientCredentials>
        PARSER = new com.google.protobuf.AbstractParser<ClientCredentials>() {
      @java.lang.Override
      public ClientCredentials parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        Builder builder = newBuilder();
        try {
          builder.mergeFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          throw e.setUnfinishedMessage(builder.buildPartial());
        } catch (com.google.protobuf.UninitializedMessageException e) {
          throw e.asInvalidProtocolBufferException().setUnfinishedMessage(builder.buildPartial());
        } catch (java.io.IOException e) {
          throw new com.google.protobuf.InvalidProtocolBufferException(e)
              .setUnfinishedMessage(builder.buildPartial());
        }
        return builder.buildPartial();
      }
    };

    public static com.google.protobuf.Parser<ClientCredentials> parser() {
      return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<ClientCredentials> getParserForType() {
      return PARSER;
    }

    @java.lang.Override
    public org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  public interface ScvSupportedAliasesRequestOrBuilder extends
      // @@protoc_insertion_point(interface_extends:dominion.v1.ScvSupportedAliasesRequest)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <code>.dominion.v1.ClientCredentials clientCredentials = 1;</code>
     * @return Whether the clientCredentials field is set.
     */
    boolean hasClientCredentials();
    /**
     * <code>.dominion.v1.ClientCredentials clientCredentials = 1;</code>
     * @return The clientCredentials.
     */
    org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials getClientCredentials();
    /**
     * <code>.dominion.v1.ClientCredentials clientCredentials = 1;</code>
     */
    org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentialsOrBuilder getClientCredentialsOrBuilder();
  }
  /**
   * Protobuf type {@code dominion.v1.ScvSupportedAliasesRequest}
   */
  public static final class ScvSupportedAliasesRequest extends
      com.google.protobuf.GeneratedMessageV3 implements
      // @@protoc_insertion_point(message_implements:dominion.v1.ScvSupportedAliasesRequest)
      ScvSupportedAliasesRequestOrBuilder {
  private static final long serialVersionUID = 0L;
    // Use ScvSupportedAliasesRequest.newBuilder() to construct.
    private ScvSupportedAliasesRequest(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
      super(builder);
    }
    private ScvSupportedAliasesRequest() {
    }

    @java.lang.Override
    @SuppressWarnings({"unused"})
    protected java.lang.Object newInstance(
        UnusedPrivateParameter unused) {
      return new ScvSupportedAliasesRequest();
    }

    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return org.opennms.dominion.local.rpc.grpc.DominionGrpc.internal_static_dominion_v1_ScvSupportedAliasesRequest_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return org.opennms.dominion.local.rpc.grpc.DominionGrpc.internal_static_dominion_v1_ScvSupportedAliasesRequest_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesRequest.class, org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesRequest.Builder.class);
    }

    private int bitField0_;
    public static final int CLIENTCREDENTIALS_FIELD_NUMBER = 1;
    private org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials clientCredentials_;
    /**
     * <code>.dominion.v1.ClientCredentials clientCredentials = 1;</code>
     * @return Whether the clientCredentials field is set.
     */
    @java.lang.Override
    public boolean hasClientCredentials() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <code>.dominion.v1.ClientCredentials clientCredentials = 1;</code>
     * @return The clientCredentials.
     */
    @java.lang.Override
    public org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials getClientCredentials() {
      return clientCredentials_ == null ? org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials.getDefaultInstance() : clientCredentials_;
    }
    /**
     * <code>.dominion.v1.ClientCredentials clientCredentials = 1;</code>
     */
    @java.lang.Override
    public org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentialsOrBuilder getClientCredentialsOrBuilder() {
      return clientCredentials_ == null ? org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials.getDefaultInstance() : clientCredentials_;
    }

    private byte memoizedIsInitialized = -1;
    @java.lang.Override
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    @java.lang.Override
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      if (((bitField0_ & 0x00000001) != 0)) {
        output.writeMessage(1, getClientCredentials());
      }
      getUnknownFields().writeTo(output);
    }

    @java.lang.Override
    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      if (((bitField0_ & 0x00000001) != 0)) {
        size += com.google.protobuf.CodedOutputStream
          .computeMessageSize(1, getClientCredentials());
      }
      size += getUnknownFields().getSerializedSize();
      memoizedSize = size;
      return size;
    }

    @java.lang.Override
    public boolean equals(final java.lang.Object obj) {
      if (obj == this) {
       return true;
      }
      if (!(obj instanceof org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesRequest)) {
        return super.equals(obj);
      }
      org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesRequest other = (org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesRequest) obj;

      if (hasClientCredentials() != other.hasClientCredentials()) return false;
      if (hasClientCredentials()) {
        if (!getClientCredentials()
            .equals(other.getClientCredentials())) return false;
      }
      if (!getUnknownFields().equals(other.getUnknownFields())) return false;
      return true;
    }

    @java.lang.Override
    public int hashCode() {
      if (memoizedHashCode != 0) {
        return memoizedHashCode;
      }
      int hash = 41;
      hash = (19 * hash) + getDescriptor().hashCode();
      if (hasClientCredentials()) {
        hash = (37 * hash) + CLIENTCREDENTIALS_FIELD_NUMBER;
        hash = (53 * hash) + getClientCredentials().hashCode();
      }
      hash = (29 * hash) + getUnknownFields().hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesRequest parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesRequest parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesRequest parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesRequest parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesRequest parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesRequest parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesRequest parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesRequest parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesRequest parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input);
    }

    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesRequest parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesRequest parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesRequest parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    @java.lang.Override
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
    }
    public static Builder newBuilder(org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesRequest prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }
    @java.lang.Override
    public Builder toBuilder() {
      return this == DEFAULT_INSTANCE
          ? new Builder() : new Builder().mergeFrom(this);
    }

    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code dominion.v1.ScvSupportedAliasesRequest}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:dominion.v1.ScvSupportedAliasesRequest)
        org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesRequestOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return org.opennms.dominion.local.rpc.grpc.DominionGrpc.internal_static_dominion_v1_ScvSupportedAliasesRequest_descriptor;
      }

      @java.lang.Override
      protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return org.opennms.dominion.local.rpc.grpc.DominionGrpc.internal_static_dominion_v1_ScvSupportedAliasesRequest_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesRequest.class, org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesRequest.Builder.class);
      }

      // Construct using org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesRequest.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
          com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessageV3
                .alwaysUseFieldBuilders) {
          getClientCredentialsFieldBuilder();
        }
      }
      @java.lang.Override
      public Builder clear() {
        super.clear();
        bitField0_ = 0;
        clientCredentials_ = null;
        if (clientCredentialsBuilder_ != null) {
          clientCredentialsBuilder_.dispose();
          clientCredentialsBuilder_ = null;
        }
        return this;
      }

      @java.lang.Override
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return org.opennms.dominion.local.rpc.grpc.DominionGrpc.internal_static_dominion_v1_ScvSupportedAliasesRequest_descriptor;
      }

      @java.lang.Override
      public org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesRequest getDefaultInstanceForType() {
        return org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesRequest.getDefaultInstance();
      }

      @java.lang.Override
      public org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesRequest build() {
        org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesRequest result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      @java.lang.Override
      public org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesRequest buildPartial() {
        org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesRequest result = new org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesRequest(this);
        if (bitField0_ != 0) { buildPartial0(result); }
        onBuilt();
        return result;
      }

      private void buildPartial0(org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesRequest result) {
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000001) != 0)) {
          result.clientCredentials_ = clientCredentialsBuilder_ == null
              ? clientCredentials_
              : clientCredentialsBuilder_.build();
          to_bitField0_ |= 0x00000001;
        }
        result.bitField0_ |= to_bitField0_;
      }

      @java.lang.Override
      public Builder clone() {
        return super.clone();
      }
      @java.lang.Override
      public Builder setField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return super.setField(field, value);
      }
      @java.lang.Override
      public Builder clearField(
          com.google.protobuf.Descriptors.FieldDescriptor field) {
        return super.clearField(field);
      }
      @java.lang.Override
      public Builder clearOneof(
          com.google.protobuf.Descriptors.OneofDescriptor oneof) {
        return super.clearOneof(oneof);
      }
      @java.lang.Override
      public Builder setRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          int index, java.lang.Object value) {
        return super.setRepeatedField(field, index, value);
      }
      @java.lang.Override
      public Builder addRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return super.addRepeatedField(field, value);
      }
      @java.lang.Override
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesRequest) {
          return mergeFrom((org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesRequest)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesRequest other) {
        if (other == org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesRequest.getDefaultInstance()) return this;
        if (other.hasClientCredentials()) {
          mergeClientCredentials(other.getClientCredentials());
        }
        this.mergeUnknownFields(other.getUnknownFields());
        onChanged();
        return this;
      }

      @java.lang.Override
      public final boolean isInitialized() {
        return true;
      }

      @java.lang.Override
      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        if (extensionRegistry == null) {
          throw new java.lang.NullPointerException();
        }
        try {
          boolean done = false;
          while (!done) {
            int tag = input.readTag();
            switch (tag) {
              case 0:
                done = true;
                break;
              case 10: {
                input.readMessage(
                    getClientCredentialsFieldBuilder().getBuilder(),
                    extensionRegistry);
                bitField0_ |= 0x00000001;
                break;
              } // case 10
              default: {
                if (!super.parseUnknownField(input, extensionRegistry, tag)) {
                  done = true; // was an endgroup tag
                }
                break;
              } // default:
            } // switch (tag)
          } // while (!done)
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          throw e.unwrapIOException();
        } finally {
          onChanged();
        } // finally
        return this;
      }
      private int bitField0_;

      private org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials clientCredentials_;
      private com.google.protobuf.SingleFieldBuilderV3<
          org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials, org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials.Builder, org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentialsOrBuilder> clientCredentialsBuilder_;
      /**
       * <code>.dominion.v1.ClientCredentials clientCredentials = 1;</code>
       * @return Whether the clientCredentials field is set.
       */
      public boolean hasClientCredentials() {
        return ((bitField0_ & 0x00000001) != 0);
      }
      /**
       * <code>.dominion.v1.ClientCredentials clientCredentials = 1;</code>
       * @return The clientCredentials.
       */
      public org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials getClientCredentials() {
        if (clientCredentialsBuilder_ == null) {
          return clientCredentials_ == null ? org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials.getDefaultInstance() : clientCredentials_;
        } else {
          return clientCredentialsBuilder_.getMessage();
        }
      }
      /**
       * <code>.dominion.v1.ClientCredentials clientCredentials = 1;</code>
       */
      public Builder setClientCredentials(org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials value) {
        if (clientCredentialsBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          clientCredentials_ = value;
        } else {
          clientCredentialsBuilder_.setMessage(value);
        }
        bitField0_ |= 0x00000001;
        onChanged();
        return this;
      }
      /**
       * <code>.dominion.v1.ClientCredentials clientCredentials = 1;</code>
       */
      public Builder setClientCredentials(
          org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials.Builder builderForValue) {
        if (clientCredentialsBuilder_ == null) {
          clientCredentials_ = builderForValue.build();
        } else {
          clientCredentialsBuilder_.setMessage(builderForValue.build());
        }
        bitField0_ |= 0x00000001;
        onChanged();
        return this;
      }
      /**
       * <code>.dominion.v1.ClientCredentials clientCredentials = 1;</code>
       */
      public Builder mergeClientCredentials(org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials value) {
        if (clientCredentialsBuilder_ == null) {
          if (((bitField0_ & 0x00000001) != 0) &&
            clientCredentials_ != null &&
            clientCredentials_ != org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials.getDefaultInstance()) {
            getClientCredentialsBuilder().mergeFrom(value);
          } else {
            clientCredentials_ = value;
          }
        } else {
          clientCredentialsBuilder_.mergeFrom(value);
        }
        if (clientCredentials_ != null) {
          bitField0_ |= 0x00000001;
          onChanged();
        }
        return this;
      }
      /**
       * <code>.dominion.v1.ClientCredentials clientCredentials = 1;</code>
       */
      public Builder clearClientCredentials() {
        bitField0_ = (bitField0_ & ~0x00000001);
        clientCredentials_ = null;
        if (clientCredentialsBuilder_ != null) {
          clientCredentialsBuilder_.dispose();
          clientCredentialsBuilder_ = null;
        }
        onChanged();
        return this;
      }
      /**
       * <code>.dominion.v1.ClientCredentials clientCredentials = 1;</code>
       */
      public org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials.Builder getClientCredentialsBuilder() {
        bitField0_ |= 0x00000001;
        onChanged();
        return getClientCredentialsFieldBuilder().getBuilder();
      }
      /**
       * <code>.dominion.v1.ClientCredentials clientCredentials = 1;</code>
       */
      public org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentialsOrBuilder getClientCredentialsOrBuilder() {
        if (clientCredentialsBuilder_ != null) {
          return clientCredentialsBuilder_.getMessageOrBuilder();
        } else {
          return clientCredentials_ == null ?
              org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials.getDefaultInstance() : clientCredentials_;
        }
      }
      /**
       * <code>.dominion.v1.ClientCredentials clientCredentials = 1;</code>
       */
      private com.google.protobuf.SingleFieldBuilderV3<
          org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials, org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials.Builder, org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentialsOrBuilder> 
          getClientCredentialsFieldBuilder() {
        if (clientCredentialsBuilder_ == null) {
          clientCredentialsBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
              org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials, org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials.Builder, org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentialsOrBuilder>(
                  getClientCredentials(),
                  getParentForChildren(),
                  isClean());
          clientCredentials_ = null;
        }
        return clientCredentialsBuilder_;
      }
      @java.lang.Override
      public final Builder setUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.setUnknownFields(unknownFields);
      }

      @java.lang.Override
      public final Builder mergeUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.mergeUnknownFields(unknownFields);
      }


      // @@protoc_insertion_point(builder_scope:dominion.v1.ScvSupportedAliasesRequest)
    }

    // @@protoc_insertion_point(class_scope:dominion.v1.ScvSupportedAliasesRequest)
    private static final org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesRequest DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesRequest();
    }

    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesRequest getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<ScvSupportedAliasesRequest>
        PARSER = new com.google.protobuf.AbstractParser<ScvSupportedAliasesRequest>() {
      @java.lang.Override
      public ScvSupportedAliasesRequest parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        Builder builder = newBuilder();
        try {
          builder.mergeFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          throw e.setUnfinishedMessage(builder.buildPartial());
        } catch (com.google.protobuf.UninitializedMessageException e) {
          throw e.asInvalidProtocolBufferException().setUnfinishedMessage(builder.buildPartial());
        } catch (java.io.IOException e) {
          throw new com.google.protobuf.InvalidProtocolBufferException(e)
              .setUnfinishedMessage(builder.buildPartial());
        }
        return builder.buildPartial();
      }
    };

    public static com.google.protobuf.Parser<ScvSupportedAliasesRequest> parser() {
      return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<ScvSupportedAliasesRequest> getParserForType() {
      return PARSER;
    }

    @java.lang.Override
    public org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesRequest getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  public interface ScvSupportedAliasesResponseOrBuilder extends
      // @@protoc_insertion_point(interface_extends:dominion.v1.ScvSupportedAliasesResponse)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <code>repeated string aliases = 1;</code>
     * @return A list containing the aliases.
     */
    java.util.List<java.lang.String>
        getAliasesList();
    /**
     * <code>repeated string aliases = 1;</code>
     * @return The count of aliases.
     */
    int getAliasesCount();
    /**
     * <code>repeated string aliases = 1;</code>
     * @param index The index of the element to return.
     * @return The aliases at the given index.
     */
    java.lang.String getAliases(int index);
    /**
     * <code>repeated string aliases = 1;</code>
     * @param index The index of the value to return.
     * @return The bytes of the aliases at the given index.
     */
    com.google.protobuf.ByteString
        getAliasesBytes(int index);
  }
  /**
   * Protobuf type {@code dominion.v1.ScvSupportedAliasesResponse}
   */
  public static final class ScvSupportedAliasesResponse extends
      com.google.protobuf.GeneratedMessageV3 implements
      // @@protoc_insertion_point(message_implements:dominion.v1.ScvSupportedAliasesResponse)
      ScvSupportedAliasesResponseOrBuilder {
  private static final long serialVersionUID = 0L;
    // Use ScvSupportedAliasesResponse.newBuilder() to construct.
    private ScvSupportedAliasesResponse(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
      super(builder);
    }
    private ScvSupportedAliasesResponse() {
      aliases_ =
          com.google.protobuf.LazyStringArrayList.emptyList();
    }

    @java.lang.Override
    @SuppressWarnings({"unused"})
    protected java.lang.Object newInstance(
        UnusedPrivateParameter unused) {
      return new ScvSupportedAliasesResponse();
    }

    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return org.opennms.dominion.local.rpc.grpc.DominionGrpc.internal_static_dominion_v1_ScvSupportedAliasesResponse_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return org.opennms.dominion.local.rpc.grpc.DominionGrpc.internal_static_dominion_v1_ScvSupportedAliasesResponse_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesResponse.class, org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesResponse.Builder.class);
    }

    public static final int ALIASES_FIELD_NUMBER = 1;
    @SuppressWarnings("serial")
    private com.google.protobuf.LazyStringArrayList aliases_ =
        com.google.protobuf.LazyStringArrayList.emptyList();
    /**
     * <code>repeated string aliases = 1;</code>
     * @return A list containing the aliases.
     */
    public com.google.protobuf.ProtocolStringList
        getAliasesList() {
      return aliases_;
    }
    /**
     * <code>repeated string aliases = 1;</code>
     * @return The count of aliases.
     */
    public int getAliasesCount() {
      return aliases_.size();
    }
    /**
     * <code>repeated string aliases = 1;</code>
     * @param index The index of the element to return.
     * @return The aliases at the given index.
     */
    public java.lang.String getAliases(int index) {
      return aliases_.get(index);
    }
    /**
     * <code>repeated string aliases = 1;</code>
     * @param index The index of the value to return.
     * @return The bytes of the aliases at the given index.
     */
    public com.google.protobuf.ByteString
        getAliasesBytes(int index) {
      return aliases_.getByteString(index);
    }

    private byte memoizedIsInitialized = -1;
    @java.lang.Override
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    @java.lang.Override
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      for (int i = 0; i < aliases_.size(); i++) {
        com.google.protobuf.GeneratedMessageV3.writeString(output, 1, aliases_.getRaw(i));
      }
      getUnknownFields().writeTo(output);
    }

    @java.lang.Override
    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      {
        int dataSize = 0;
        for (int i = 0; i < aliases_.size(); i++) {
          dataSize += computeStringSizeNoTag(aliases_.getRaw(i));
        }
        size += dataSize;
        size += 1 * getAliasesList().size();
      }
      size += getUnknownFields().getSerializedSize();
      memoizedSize = size;
      return size;
    }

    @java.lang.Override
    public boolean equals(final java.lang.Object obj) {
      if (obj == this) {
       return true;
      }
      if (!(obj instanceof org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesResponse)) {
        return super.equals(obj);
      }
      org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesResponse other = (org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesResponse) obj;

      if (!getAliasesList()
          .equals(other.getAliasesList())) return false;
      if (!getUnknownFields().equals(other.getUnknownFields())) return false;
      return true;
    }

    @java.lang.Override
    public int hashCode() {
      if (memoizedHashCode != 0) {
        return memoizedHashCode;
      }
      int hash = 41;
      hash = (19 * hash) + getDescriptor().hashCode();
      if (getAliasesCount() > 0) {
        hash = (37 * hash) + ALIASES_FIELD_NUMBER;
        hash = (53 * hash) + getAliasesList().hashCode();
      }
      hash = (29 * hash) + getUnknownFields().hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesResponse parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesResponse parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesResponse parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesResponse parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesResponse parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesResponse parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesResponse parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesResponse parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesResponse parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input);
    }

    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesResponse parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesResponse parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesResponse parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    @java.lang.Override
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
    }
    public static Builder newBuilder(org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesResponse prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }
    @java.lang.Override
    public Builder toBuilder() {
      return this == DEFAULT_INSTANCE
          ? new Builder() : new Builder().mergeFrom(this);
    }

    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code dominion.v1.ScvSupportedAliasesResponse}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:dominion.v1.ScvSupportedAliasesResponse)
        org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesResponseOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return org.opennms.dominion.local.rpc.grpc.DominionGrpc.internal_static_dominion_v1_ScvSupportedAliasesResponse_descriptor;
      }

      @java.lang.Override
      protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return org.opennms.dominion.local.rpc.grpc.DominionGrpc.internal_static_dominion_v1_ScvSupportedAliasesResponse_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesResponse.class, org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesResponse.Builder.class);
      }

      // Construct using org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesResponse.newBuilder()
      private Builder() {

      }

      private Builder(
          com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
        super(parent);

      }
      @java.lang.Override
      public Builder clear() {
        super.clear();
        bitField0_ = 0;
        aliases_ =
            com.google.protobuf.LazyStringArrayList.emptyList();
        return this;
      }

      @java.lang.Override
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return org.opennms.dominion.local.rpc.grpc.DominionGrpc.internal_static_dominion_v1_ScvSupportedAliasesResponse_descriptor;
      }

      @java.lang.Override
      public org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesResponse getDefaultInstanceForType() {
        return org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesResponse.getDefaultInstance();
      }

      @java.lang.Override
      public org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesResponse build() {
        org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesResponse result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      @java.lang.Override
      public org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesResponse buildPartial() {
        org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesResponse result = new org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesResponse(this);
        if (bitField0_ != 0) { buildPartial0(result); }
        onBuilt();
        return result;
      }

      private void buildPartial0(org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesResponse result) {
        int from_bitField0_ = bitField0_;
        if (((from_bitField0_ & 0x00000001) != 0)) {
          aliases_.makeImmutable();
          result.aliases_ = aliases_;
        }
      }

      @java.lang.Override
      public Builder clone() {
        return super.clone();
      }
      @java.lang.Override
      public Builder setField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return super.setField(field, value);
      }
      @java.lang.Override
      public Builder clearField(
          com.google.protobuf.Descriptors.FieldDescriptor field) {
        return super.clearField(field);
      }
      @java.lang.Override
      public Builder clearOneof(
          com.google.protobuf.Descriptors.OneofDescriptor oneof) {
        return super.clearOneof(oneof);
      }
      @java.lang.Override
      public Builder setRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          int index, java.lang.Object value) {
        return super.setRepeatedField(field, index, value);
      }
      @java.lang.Override
      public Builder addRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return super.addRepeatedField(field, value);
      }
      @java.lang.Override
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesResponse) {
          return mergeFrom((org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesResponse)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesResponse other) {
        if (other == org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesResponse.getDefaultInstance()) return this;
        if (!other.aliases_.isEmpty()) {
          if (aliases_.isEmpty()) {
            aliases_ = other.aliases_;
            bitField0_ |= 0x00000001;
          } else {
            ensureAliasesIsMutable();
            aliases_.addAll(other.aliases_);
          }
          onChanged();
        }
        this.mergeUnknownFields(other.getUnknownFields());
        onChanged();
        return this;
      }

      @java.lang.Override
      public final boolean isInitialized() {
        return true;
      }

      @java.lang.Override
      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        if (extensionRegistry == null) {
          throw new java.lang.NullPointerException();
        }
        try {
          boolean done = false;
          while (!done) {
            int tag = input.readTag();
            switch (tag) {
              case 0:
                done = true;
                break;
              case 10: {
                java.lang.String s = input.readStringRequireUtf8();
                ensureAliasesIsMutable();
                aliases_.add(s);
                break;
              } // case 10
              default: {
                if (!super.parseUnknownField(input, extensionRegistry, tag)) {
                  done = true; // was an endgroup tag
                }
                break;
              } // default:
            } // switch (tag)
          } // while (!done)
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          throw e.unwrapIOException();
        } finally {
          onChanged();
        } // finally
        return this;
      }
      private int bitField0_;

      private com.google.protobuf.LazyStringArrayList aliases_ =
          com.google.protobuf.LazyStringArrayList.emptyList();
      private void ensureAliasesIsMutable() {
        if (!aliases_.isModifiable()) {
          aliases_ = new com.google.protobuf.LazyStringArrayList(aliases_);
        }
        bitField0_ |= 0x00000001;
      }
      /**
       * <code>repeated string aliases = 1;</code>
       * @return A list containing the aliases.
       */
      public com.google.protobuf.ProtocolStringList
          getAliasesList() {
        aliases_.makeImmutable();
        return aliases_;
      }
      /**
       * <code>repeated string aliases = 1;</code>
       * @return The count of aliases.
       */
      public int getAliasesCount() {
        return aliases_.size();
      }
      /**
       * <code>repeated string aliases = 1;</code>
       * @param index The index of the element to return.
       * @return The aliases at the given index.
       */
      public java.lang.String getAliases(int index) {
        return aliases_.get(index);
      }
      /**
       * <code>repeated string aliases = 1;</code>
       * @param index The index of the value to return.
       * @return The bytes of the aliases at the given index.
       */
      public com.google.protobuf.ByteString
          getAliasesBytes(int index) {
        return aliases_.getByteString(index);
      }
      /**
       * <code>repeated string aliases = 1;</code>
       * @param index The index to set the value at.
       * @param value The aliases to set.
       * @return This builder for chaining.
       */
      public Builder setAliases(
          int index, java.lang.String value) {
        if (value == null) { throw new NullPointerException(); }
        ensureAliasesIsMutable();
        aliases_.set(index, value);
        bitField0_ |= 0x00000001;
        onChanged();
        return this;
      }
      /**
       * <code>repeated string aliases = 1;</code>
       * @param value The aliases to add.
       * @return This builder for chaining.
       */
      public Builder addAliases(
          java.lang.String value) {
        if (value == null) { throw new NullPointerException(); }
        ensureAliasesIsMutable();
        aliases_.add(value);
        bitField0_ |= 0x00000001;
        onChanged();
        return this;
      }
      /**
       * <code>repeated string aliases = 1;</code>
       * @param values The aliases to add.
       * @return This builder for chaining.
       */
      public Builder addAllAliases(
          java.lang.Iterable<java.lang.String> values) {
        ensureAliasesIsMutable();
        com.google.protobuf.AbstractMessageLite.Builder.addAll(
            values, aliases_);
        bitField0_ |= 0x00000001;
        onChanged();
        return this;
      }
      /**
       * <code>repeated string aliases = 1;</code>
       * @return This builder for chaining.
       */
      public Builder clearAliases() {
        aliases_ =
          com.google.protobuf.LazyStringArrayList.emptyList();
        bitField0_ = (bitField0_ & ~0x00000001);;
        onChanged();
        return this;
      }
      /**
       * <code>repeated string aliases = 1;</code>
       * @param value The bytes of the aliases to add.
       * @return This builder for chaining.
       */
      public Builder addAliasesBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) { throw new NullPointerException(); }
        checkByteStringIsUtf8(value);
        ensureAliasesIsMutable();
        aliases_.add(value);
        bitField0_ |= 0x00000001;
        onChanged();
        return this;
      }
      @java.lang.Override
      public final Builder setUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.setUnknownFields(unknownFields);
      }

      @java.lang.Override
      public final Builder mergeUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.mergeUnknownFields(unknownFields);
      }


      // @@protoc_insertion_point(builder_scope:dominion.v1.ScvSupportedAliasesResponse)
    }

    // @@protoc_insertion_point(class_scope:dominion.v1.ScvSupportedAliasesResponse)
    private static final org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesResponse DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesResponse();
    }

    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesResponse getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<ScvSupportedAliasesResponse>
        PARSER = new com.google.protobuf.AbstractParser<ScvSupportedAliasesResponse>() {
      @java.lang.Override
      public ScvSupportedAliasesResponse parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        Builder builder = newBuilder();
        try {
          builder.mergeFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          throw e.setUnfinishedMessage(builder.buildPartial());
        } catch (com.google.protobuf.UninitializedMessageException e) {
          throw e.asInvalidProtocolBufferException().setUnfinishedMessage(builder.buildPartial());
        } catch (java.io.IOException e) {
          throw new com.google.protobuf.InvalidProtocolBufferException(e)
              .setUnfinishedMessage(builder.buildPartial());
        }
        return builder.buildPartial();
      }
    };

    public static com.google.protobuf.Parser<ScvSupportedAliasesResponse> parser() {
      return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<ScvSupportedAliasesResponse> getParserForType() {
      return PARSER;
    }

    @java.lang.Override
    public org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSupportedAliasesResponse getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  public interface ScvGetCredentialsRequestOrBuilder extends
      // @@protoc_insertion_point(interface_extends:dominion.v1.ScvGetCredentialsRequest)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <code>.dominion.v1.ClientCredentials clientCredentials = 1;</code>
     * @return Whether the clientCredentials field is set.
     */
    boolean hasClientCredentials();
    /**
     * <code>.dominion.v1.ClientCredentials clientCredentials = 1;</code>
     * @return The clientCredentials.
     */
    org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials getClientCredentials();
    /**
     * <code>.dominion.v1.ClientCredentials clientCredentials = 1;</code>
     */
    org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentialsOrBuilder getClientCredentialsOrBuilder();

    /**
     * <code>string alias = 2;</code>
     * @return The alias.
     */
    java.lang.String getAlias();
    /**
     * <code>string alias = 2;</code>
     * @return The bytes for alias.
     */
    com.google.protobuf.ByteString
        getAliasBytes();
  }
  /**
   * Protobuf type {@code dominion.v1.ScvGetCredentialsRequest}
   */
  public static final class ScvGetCredentialsRequest extends
      com.google.protobuf.GeneratedMessageV3 implements
      // @@protoc_insertion_point(message_implements:dominion.v1.ScvGetCredentialsRequest)
      ScvGetCredentialsRequestOrBuilder {
  private static final long serialVersionUID = 0L;
    // Use ScvGetCredentialsRequest.newBuilder() to construct.
    private ScvGetCredentialsRequest(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
      super(builder);
    }
    private ScvGetCredentialsRequest() {
      alias_ = "";
    }

    @java.lang.Override
    @SuppressWarnings({"unused"})
    protected java.lang.Object newInstance(
        UnusedPrivateParameter unused) {
      return new ScvGetCredentialsRequest();
    }

    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return org.opennms.dominion.local.rpc.grpc.DominionGrpc.internal_static_dominion_v1_ScvGetCredentialsRequest_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return org.opennms.dominion.local.rpc.grpc.DominionGrpc.internal_static_dominion_v1_ScvGetCredentialsRequest_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsRequest.class, org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsRequest.Builder.class);
    }

    private int bitField0_;
    public static final int CLIENTCREDENTIALS_FIELD_NUMBER = 1;
    private org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials clientCredentials_;
    /**
     * <code>.dominion.v1.ClientCredentials clientCredentials = 1;</code>
     * @return Whether the clientCredentials field is set.
     */
    @java.lang.Override
    public boolean hasClientCredentials() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <code>.dominion.v1.ClientCredentials clientCredentials = 1;</code>
     * @return The clientCredentials.
     */
    @java.lang.Override
    public org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials getClientCredentials() {
      return clientCredentials_ == null ? org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials.getDefaultInstance() : clientCredentials_;
    }
    /**
     * <code>.dominion.v1.ClientCredentials clientCredentials = 1;</code>
     */
    @java.lang.Override
    public org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentialsOrBuilder getClientCredentialsOrBuilder() {
      return clientCredentials_ == null ? org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials.getDefaultInstance() : clientCredentials_;
    }

    public static final int ALIAS_FIELD_NUMBER = 2;
    @SuppressWarnings("serial")
    private volatile java.lang.Object alias_ = "";
    /**
     * <code>string alias = 2;</code>
     * @return The alias.
     */
    @java.lang.Override
    public java.lang.String getAlias() {
      java.lang.Object ref = alias_;
      if (ref instanceof java.lang.String) {
        return (java.lang.String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        alias_ = s;
        return s;
      }
    }
    /**
     * <code>string alias = 2;</code>
     * @return The bytes for alias.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getAliasBytes() {
      java.lang.Object ref = alias_;
      if (ref instanceof java.lang.String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        alias_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    private byte memoizedIsInitialized = -1;
    @java.lang.Override
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    @java.lang.Override
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      if (((bitField0_ & 0x00000001) != 0)) {
        output.writeMessage(1, getClientCredentials());
      }
      if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(alias_)) {
        com.google.protobuf.GeneratedMessageV3.writeString(output, 2, alias_);
      }
      getUnknownFields().writeTo(output);
    }

    @java.lang.Override
    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      if (((bitField0_ & 0x00000001) != 0)) {
        size += com.google.protobuf.CodedOutputStream
          .computeMessageSize(1, getClientCredentials());
      }
      if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(alias_)) {
        size += com.google.protobuf.GeneratedMessageV3.computeStringSize(2, alias_);
      }
      size += getUnknownFields().getSerializedSize();
      memoizedSize = size;
      return size;
    }

    @java.lang.Override
    public boolean equals(final java.lang.Object obj) {
      if (obj == this) {
       return true;
      }
      if (!(obj instanceof org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsRequest)) {
        return super.equals(obj);
      }
      org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsRequest other = (org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsRequest) obj;

      if (hasClientCredentials() != other.hasClientCredentials()) return false;
      if (hasClientCredentials()) {
        if (!getClientCredentials()
            .equals(other.getClientCredentials())) return false;
      }
      if (!getAlias()
          .equals(other.getAlias())) return false;
      if (!getUnknownFields().equals(other.getUnknownFields())) return false;
      return true;
    }

    @java.lang.Override
    public int hashCode() {
      if (memoizedHashCode != 0) {
        return memoizedHashCode;
      }
      int hash = 41;
      hash = (19 * hash) + getDescriptor().hashCode();
      if (hasClientCredentials()) {
        hash = (37 * hash) + CLIENTCREDENTIALS_FIELD_NUMBER;
        hash = (53 * hash) + getClientCredentials().hashCode();
      }
      hash = (37 * hash) + ALIAS_FIELD_NUMBER;
      hash = (53 * hash) + getAlias().hashCode();
      hash = (29 * hash) + getUnknownFields().hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsRequest parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsRequest parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsRequest parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsRequest parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsRequest parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsRequest parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsRequest parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsRequest parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsRequest parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input);
    }

    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsRequest parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsRequest parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsRequest parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    @java.lang.Override
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
    }
    public static Builder newBuilder(org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsRequest prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }
    @java.lang.Override
    public Builder toBuilder() {
      return this == DEFAULT_INSTANCE
          ? new Builder() : new Builder().mergeFrom(this);
    }

    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code dominion.v1.ScvGetCredentialsRequest}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:dominion.v1.ScvGetCredentialsRequest)
        org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsRequestOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return org.opennms.dominion.local.rpc.grpc.DominionGrpc.internal_static_dominion_v1_ScvGetCredentialsRequest_descriptor;
      }

      @java.lang.Override
      protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return org.opennms.dominion.local.rpc.grpc.DominionGrpc.internal_static_dominion_v1_ScvGetCredentialsRequest_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsRequest.class, org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsRequest.Builder.class);
      }

      // Construct using org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsRequest.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
          com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessageV3
                .alwaysUseFieldBuilders) {
          getClientCredentialsFieldBuilder();
        }
      }
      @java.lang.Override
      public Builder clear() {
        super.clear();
        bitField0_ = 0;
        clientCredentials_ = null;
        if (clientCredentialsBuilder_ != null) {
          clientCredentialsBuilder_.dispose();
          clientCredentialsBuilder_ = null;
        }
        alias_ = "";
        return this;
      }

      @java.lang.Override
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return org.opennms.dominion.local.rpc.grpc.DominionGrpc.internal_static_dominion_v1_ScvGetCredentialsRequest_descriptor;
      }

      @java.lang.Override
      public org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsRequest getDefaultInstanceForType() {
        return org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsRequest.getDefaultInstance();
      }

      @java.lang.Override
      public org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsRequest build() {
        org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsRequest result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      @java.lang.Override
      public org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsRequest buildPartial() {
        org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsRequest result = new org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsRequest(this);
        if (bitField0_ != 0) { buildPartial0(result); }
        onBuilt();
        return result;
      }

      private void buildPartial0(org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsRequest result) {
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000001) != 0)) {
          result.clientCredentials_ = clientCredentialsBuilder_ == null
              ? clientCredentials_
              : clientCredentialsBuilder_.build();
          to_bitField0_ |= 0x00000001;
        }
        if (((from_bitField0_ & 0x00000002) != 0)) {
          result.alias_ = alias_;
        }
        result.bitField0_ |= to_bitField0_;
      }

      @java.lang.Override
      public Builder clone() {
        return super.clone();
      }
      @java.lang.Override
      public Builder setField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return super.setField(field, value);
      }
      @java.lang.Override
      public Builder clearField(
          com.google.protobuf.Descriptors.FieldDescriptor field) {
        return super.clearField(field);
      }
      @java.lang.Override
      public Builder clearOneof(
          com.google.protobuf.Descriptors.OneofDescriptor oneof) {
        return super.clearOneof(oneof);
      }
      @java.lang.Override
      public Builder setRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          int index, java.lang.Object value) {
        return super.setRepeatedField(field, index, value);
      }
      @java.lang.Override
      public Builder addRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return super.addRepeatedField(field, value);
      }
      @java.lang.Override
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsRequest) {
          return mergeFrom((org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsRequest)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsRequest other) {
        if (other == org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsRequest.getDefaultInstance()) return this;
        if (other.hasClientCredentials()) {
          mergeClientCredentials(other.getClientCredentials());
        }
        if (!other.getAlias().isEmpty()) {
          alias_ = other.alias_;
          bitField0_ |= 0x00000002;
          onChanged();
        }
        this.mergeUnknownFields(other.getUnknownFields());
        onChanged();
        return this;
      }

      @java.lang.Override
      public final boolean isInitialized() {
        return true;
      }

      @java.lang.Override
      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        if (extensionRegistry == null) {
          throw new java.lang.NullPointerException();
        }
        try {
          boolean done = false;
          while (!done) {
            int tag = input.readTag();
            switch (tag) {
              case 0:
                done = true;
                break;
              case 10: {
                input.readMessage(
                    getClientCredentialsFieldBuilder().getBuilder(),
                    extensionRegistry);
                bitField0_ |= 0x00000001;
                break;
              } // case 10
              case 18: {
                alias_ = input.readStringRequireUtf8();
                bitField0_ |= 0x00000002;
                break;
              } // case 18
              default: {
                if (!super.parseUnknownField(input, extensionRegistry, tag)) {
                  done = true; // was an endgroup tag
                }
                break;
              } // default:
            } // switch (tag)
          } // while (!done)
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          throw e.unwrapIOException();
        } finally {
          onChanged();
        } // finally
        return this;
      }
      private int bitField0_;

      private org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials clientCredentials_;
      private com.google.protobuf.SingleFieldBuilderV3<
          org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials, org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials.Builder, org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentialsOrBuilder> clientCredentialsBuilder_;
      /**
       * <code>.dominion.v1.ClientCredentials clientCredentials = 1;</code>
       * @return Whether the clientCredentials field is set.
       */
      public boolean hasClientCredentials() {
        return ((bitField0_ & 0x00000001) != 0);
      }
      /**
       * <code>.dominion.v1.ClientCredentials clientCredentials = 1;</code>
       * @return The clientCredentials.
       */
      public org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials getClientCredentials() {
        if (clientCredentialsBuilder_ == null) {
          return clientCredentials_ == null ? org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials.getDefaultInstance() : clientCredentials_;
        } else {
          return clientCredentialsBuilder_.getMessage();
        }
      }
      /**
       * <code>.dominion.v1.ClientCredentials clientCredentials = 1;</code>
       */
      public Builder setClientCredentials(org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials value) {
        if (clientCredentialsBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          clientCredentials_ = value;
        } else {
          clientCredentialsBuilder_.setMessage(value);
        }
        bitField0_ |= 0x00000001;
        onChanged();
        return this;
      }
      /**
       * <code>.dominion.v1.ClientCredentials clientCredentials = 1;</code>
       */
      public Builder setClientCredentials(
          org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials.Builder builderForValue) {
        if (clientCredentialsBuilder_ == null) {
          clientCredentials_ = builderForValue.build();
        } else {
          clientCredentialsBuilder_.setMessage(builderForValue.build());
        }
        bitField0_ |= 0x00000001;
        onChanged();
        return this;
      }
      /**
       * <code>.dominion.v1.ClientCredentials clientCredentials = 1;</code>
       */
      public Builder mergeClientCredentials(org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials value) {
        if (clientCredentialsBuilder_ == null) {
          if (((bitField0_ & 0x00000001) != 0) &&
            clientCredentials_ != null &&
            clientCredentials_ != org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials.getDefaultInstance()) {
            getClientCredentialsBuilder().mergeFrom(value);
          } else {
            clientCredentials_ = value;
          }
        } else {
          clientCredentialsBuilder_.mergeFrom(value);
        }
        if (clientCredentials_ != null) {
          bitField0_ |= 0x00000001;
          onChanged();
        }
        return this;
      }
      /**
       * <code>.dominion.v1.ClientCredentials clientCredentials = 1;</code>
       */
      public Builder clearClientCredentials() {
        bitField0_ = (bitField0_ & ~0x00000001);
        clientCredentials_ = null;
        if (clientCredentialsBuilder_ != null) {
          clientCredentialsBuilder_.dispose();
          clientCredentialsBuilder_ = null;
        }
        onChanged();
        return this;
      }
      /**
       * <code>.dominion.v1.ClientCredentials clientCredentials = 1;</code>
       */
      public org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials.Builder getClientCredentialsBuilder() {
        bitField0_ |= 0x00000001;
        onChanged();
        return getClientCredentialsFieldBuilder().getBuilder();
      }
      /**
       * <code>.dominion.v1.ClientCredentials clientCredentials = 1;</code>
       */
      public org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentialsOrBuilder getClientCredentialsOrBuilder() {
        if (clientCredentialsBuilder_ != null) {
          return clientCredentialsBuilder_.getMessageOrBuilder();
        } else {
          return clientCredentials_ == null ?
              org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials.getDefaultInstance() : clientCredentials_;
        }
      }
      /**
       * <code>.dominion.v1.ClientCredentials clientCredentials = 1;</code>
       */
      private com.google.protobuf.SingleFieldBuilderV3<
          org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials, org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials.Builder, org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentialsOrBuilder> 
          getClientCredentialsFieldBuilder() {
        if (clientCredentialsBuilder_ == null) {
          clientCredentialsBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
              org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials, org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials.Builder, org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentialsOrBuilder>(
                  getClientCredentials(),
                  getParentForChildren(),
                  isClean());
          clientCredentials_ = null;
        }
        return clientCredentialsBuilder_;
      }

      private java.lang.Object alias_ = "";
      /**
       * <code>string alias = 2;</code>
       * @return The alias.
       */
      public java.lang.String getAlias() {
        java.lang.Object ref = alias_;
        if (!(ref instanceof java.lang.String)) {
          com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
          java.lang.String s = bs.toStringUtf8();
          alias_ = s;
          return s;
        } else {
          return (java.lang.String) ref;
        }
      }
      /**
       * <code>string alias = 2;</code>
       * @return The bytes for alias.
       */
      public com.google.protobuf.ByteString
          getAliasBytes() {
        java.lang.Object ref = alias_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (java.lang.String) ref);
          alias_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>string alias = 2;</code>
       * @param value The alias to set.
       * @return This builder for chaining.
       */
      public Builder setAlias(
          java.lang.String value) {
        if (value == null) { throw new NullPointerException(); }
        alias_ = value;
        bitField0_ |= 0x00000002;
        onChanged();
        return this;
      }
      /**
       * <code>string alias = 2;</code>
       * @return This builder for chaining.
       */
      public Builder clearAlias() {
        alias_ = getDefaultInstance().getAlias();
        bitField0_ = (bitField0_ & ~0x00000002);
        onChanged();
        return this;
      }
      /**
       * <code>string alias = 2;</code>
       * @param value The bytes for alias to set.
       * @return This builder for chaining.
       */
      public Builder setAliasBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) { throw new NullPointerException(); }
        checkByteStringIsUtf8(value);
        alias_ = value;
        bitField0_ |= 0x00000002;
        onChanged();
        return this;
      }
      @java.lang.Override
      public final Builder setUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.setUnknownFields(unknownFields);
      }

      @java.lang.Override
      public final Builder mergeUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.mergeUnknownFields(unknownFields);
      }


      // @@protoc_insertion_point(builder_scope:dominion.v1.ScvGetCredentialsRequest)
    }

    // @@protoc_insertion_point(class_scope:dominion.v1.ScvGetCredentialsRequest)
    private static final org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsRequest DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsRequest();
    }

    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsRequest getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<ScvGetCredentialsRequest>
        PARSER = new com.google.protobuf.AbstractParser<ScvGetCredentialsRequest>() {
      @java.lang.Override
      public ScvGetCredentialsRequest parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        Builder builder = newBuilder();
        try {
          builder.mergeFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          throw e.setUnfinishedMessage(builder.buildPartial());
        } catch (com.google.protobuf.UninitializedMessageException e) {
          throw e.asInvalidProtocolBufferException().setUnfinishedMessage(builder.buildPartial());
        } catch (java.io.IOException e) {
          throw new com.google.protobuf.InvalidProtocolBufferException(e)
              .setUnfinishedMessage(builder.buildPartial());
        }
        return builder.buildPartial();
      }
    };

    public static com.google.protobuf.Parser<ScvGetCredentialsRequest> parser() {
      return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<ScvGetCredentialsRequest> getParserForType() {
      return PARSER;
    }

    @java.lang.Override
    public org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsRequest getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  public interface ScvGetCredentialsResponseOrBuilder extends
      // @@protoc_insertion_point(interface_extends:dominion.v1.ScvGetCredentialsResponse)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <code>string user = 1;</code>
     * @return The user.
     */
    java.lang.String getUser();
    /**
     * <code>string user = 1;</code>
     * @return The bytes for user.
     */
    com.google.protobuf.ByteString
        getUserBytes();

    /**
     * <code>string password = 2;</code>
     * @return The password.
     */
    java.lang.String getPassword();
    /**
     * <code>string password = 2;</code>
     * @return The bytes for password.
     */
    com.google.protobuf.ByteString
        getPasswordBytes();

    /**
     * <code>map&lt;string, string&gt; attributes = 3;</code>
     */
    int getAttributesCount();
    /**
     * <code>map&lt;string, string&gt; attributes = 3;</code>
     */
    boolean containsAttributes(
        java.lang.String key);
    /**
     * Use {@link #getAttributesMap()} instead.
     */
    @java.lang.Deprecated
    java.util.Map<java.lang.String, java.lang.String>
    getAttributes();
    /**
     * <code>map&lt;string, string&gt; attributes = 3;</code>
     */
    java.util.Map<java.lang.String, java.lang.String>
    getAttributesMap();
    /**
     * <code>map&lt;string, string&gt; attributes = 3;</code>
     */
    /* nullable */
java.lang.String getAttributesOrDefault(
        java.lang.String key,
        /* nullable */
java.lang.String defaultValue);
    /**
     * <code>map&lt;string, string&gt; attributes = 3;</code>
     */
    java.lang.String getAttributesOrThrow(
        java.lang.String key);
  }
  /**
   * Protobuf type {@code dominion.v1.ScvGetCredentialsResponse}
   */
  public static final class ScvGetCredentialsResponse extends
      com.google.protobuf.GeneratedMessageV3 implements
      // @@protoc_insertion_point(message_implements:dominion.v1.ScvGetCredentialsResponse)
      ScvGetCredentialsResponseOrBuilder {
  private static final long serialVersionUID = 0L;
    // Use ScvGetCredentialsResponse.newBuilder() to construct.
    private ScvGetCredentialsResponse(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
      super(builder);
    }
    private ScvGetCredentialsResponse() {
      user_ = "";
      password_ = "";
    }

    @java.lang.Override
    @SuppressWarnings({"unused"})
    protected java.lang.Object newInstance(
        UnusedPrivateParameter unused) {
      return new ScvGetCredentialsResponse();
    }

    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return org.opennms.dominion.local.rpc.grpc.DominionGrpc.internal_static_dominion_v1_ScvGetCredentialsResponse_descriptor;
    }

    @SuppressWarnings({"rawtypes"})
    @java.lang.Override
    protected com.google.protobuf.MapFieldReflectionAccessor internalGetMapFieldReflection(
        int number) {
      switch (number) {
        case 3:
          return internalGetAttributes();
        default:
          throw new RuntimeException(
              "Invalid map field number: " + number);
      }
    }
    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return org.opennms.dominion.local.rpc.grpc.DominionGrpc.internal_static_dominion_v1_ScvGetCredentialsResponse_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsResponse.class, org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsResponse.Builder.class);
    }

    public static final int USER_FIELD_NUMBER = 1;
    @SuppressWarnings("serial")
    private volatile java.lang.Object user_ = "";
    /**
     * <code>string user = 1;</code>
     * @return The user.
     */
    @java.lang.Override
    public java.lang.String getUser() {
      java.lang.Object ref = user_;
      if (ref instanceof java.lang.String) {
        return (java.lang.String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        user_ = s;
        return s;
      }
    }
    /**
     * <code>string user = 1;</code>
     * @return The bytes for user.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getUserBytes() {
      java.lang.Object ref = user_;
      if (ref instanceof java.lang.String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        user_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    public static final int PASSWORD_FIELD_NUMBER = 2;
    @SuppressWarnings("serial")
    private volatile java.lang.Object password_ = "";
    /**
     * <code>string password = 2;</code>
     * @return The password.
     */
    @java.lang.Override
    public java.lang.String getPassword() {
      java.lang.Object ref = password_;
      if (ref instanceof java.lang.String) {
        return (java.lang.String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        password_ = s;
        return s;
      }
    }
    /**
     * <code>string password = 2;</code>
     * @return The bytes for password.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getPasswordBytes() {
      java.lang.Object ref = password_;
      if (ref instanceof java.lang.String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        password_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    public static final int ATTRIBUTES_FIELD_NUMBER = 3;
    private static final class AttributesDefaultEntryHolder {
      static final com.google.protobuf.MapEntry<
          java.lang.String, java.lang.String> defaultEntry =
              com.google.protobuf.MapEntry
              .<java.lang.String, java.lang.String>newDefaultInstance(
                  org.opennms.dominion.local.rpc.grpc.DominionGrpc.internal_static_dominion_v1_ScvGetCredentialsResponse_AttributesEntry_descriptor, 
                  com.google.protobuf.WireFormat.FieldType.STRING,
                  "",
                  com.google.protobuf.WireFormat.FieldType.STRING,
                  "");
    }
    @SuppressWarnings("serial")
    private com.google.protobuf.MapField<
        java.lang.String, java.lang.String> attributes_;
    private com.google.protobuf.MapField<java.lang.String, java.lang.String>
    internalGetAttributes() {
      if (attributes_ == null) {
        return com.google.protobuf.MapField.emptyMapField(
            AttributesDefaultEntryHolder.defaultEntry);
      }
      return attributes_;
    }
    public int getAttributesCount() {
      return internalGetAttributes().getMap().size();
    }
    /**
     * <code>map&lt;string, string&gt; attributes = 3;</code>
     */
    @java.lang.Override
    public boolean containsAttributes(
        java.lang.String key) {
      if (key == null) { throw new NullPointerException("map key"); }
      return internalGetAttributes().getMap().containsKey(key);
    }
    /**
     * Use {@link #getAttributesMap()} instead.
     */
    @java.lang.Override
    @java.lang.Deprecated
    public java.util.Map<java.lang.String, java.lang.String> getAttributes() {
      return getAttributesMap();
    }
    /**
     * <code>map&lt;string, string&gt; attributes = 3;</code>
     */
    @java.lang.Override
    public java.util.Map<java.lang.String, java.lang.String> getAttributesMap() {
      return internalGetAttributes().getMap();
    }
    /**
     * <code>map&lt;string, string&gt; attributes = 3;</code>
     */
    @java.lang.Override
    public /* nullable */
java.lang.String getAttributesOrDefault(
        java.lang.String key,
        /* nullable */
java.lang.String defaultValue) {
      if (key == null) { throw new NullPointerException("map key"); }
      java.util.Map<java.lang.String, java.lang.String> map =
          internalGetAttributes().getMap();
      return map.containsKey(key) ? map.get(key) : defaultValue;
    }
    /**
     * <code>map&lt;string, string&gt; attributes = 3;</code>
     */
    @java.lang.Override
    public java.lang.String getAttributesOrThrow(
        java.lang.String key) {
      if (key == null) { throw new NullPointerException("map key"); }
      java.util.Map<java.lang.String, java.lang.String> map =
          internalGetAttributes().getMap();
      if (!map.containsKey(key)) {
        throw new java.lang.IllegalArgumentException();
      }
      return map.get(key);
    }

    private byte memoizedIsInitialized = -1;
    @java.lang.Override
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    @java.lang.Override
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(user_)) {
        com.google.protobuf.GeneratedMessageV3.writeString(output, 1, user_);
      }
      if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(password_)) {
        com.google.protobuf.GeneratedMessageV3.writeString(output, 2, password_);
      }
      com.google.protobuf.GeneratedMessageV3
        .serializeStringMapTo(
          output,
          internalGetAttributes(),
          AttributesDefaultEntryHolder.defaultEntry,
          3);
      getUnknownFields().writeTo(output);
    }

    @java.lang.Override
    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(user_)) {
        size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, user_);
      }
      if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(password_)) {
        size += com.google.protobuf.GeneratedMessageV3.computeStringSize(2, password_);
      }
      for (java.util.Map.Entry<java.lang.String, java.lang.String> entry
           : internalGetAttributes().getMap().entrySet()) {
        com.google.protobuf.MapEntry<java.lang.String, java.lang.String>
        attributes__ = AttributesDefaultEntryHolder.defaultEntry.newBuilderForType()
            .setKey(entry.getKey())
            .setValue(entry.getValue())
            .build();
        size += com.google.protobuf.CodedOutputStream
            .computeMessageSize(3, attributes__);
      }
      size += getUnknownFields().getSerializedSize();
      memoizedSize = size;
      return size;
    }

    @java.lang.Override
    public boolean equals(final java.lang.Object obj) {
      if (obj == this) {
       return true;
      }
      if (!(obj instanceof org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsResponse)) {
        return super.equals(obj);
      }
      org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsResponse other = (org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsResponse) obj;

      if (!getUser()
          .equals(other.getUser())) return false;
      if (!getPassword()
          .equals(other.getPassword())) return false;
      if (!internalGetAttributes().equals(
          other.internalGetAttributes())) return false;
      if (!getUnknownFields().equals(other.getUnknownFields())) return false;
      return true;
    }

    @java.lang.Override
    public int hashCode() {
      if (memoizedHashCode != 0) {
        return memoizedHashCode;
      }
      int hash = 41;
      hash = (19 * hash) + getDescriptor().hashCode();
      hash = (37 * hash) + USER_FIELD_NUMBER;
      hash = (53 * hash) + getUser().hashCode();
      hash = (37 * hash) + PASSWORD_FIELD_NUMBER;
      hash = (53 * hash) + getPassword().hashCode();
      if (!internalGetAttributes().getMap().isEmpty()) {
        hash = (37 * hash) + ATTRIBUTES_FIELD_NUMBER;
        hash = (53 * hash) + internalGetAttributes().hashCode();
      }
      hash = (29 * hash) + getUnknownFields().hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsResponse parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsResponse parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsResponse parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsResponse parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsResponse parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsResponse parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsResponse parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsResponse parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsResponse parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input);
    }

    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsResponse parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsResponse parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsResponse parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    @java.lang.Override
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
    }
    public static Builder newBuilder(org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsResponse prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }
    @java.lang.Override
    public Builder toBuilder() {
      return this == DEFAULT_INSTANCE
          ? new Builder() : new Builder().mergeFrom(this);
    }

    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code dominion.v1.ScvGetCredentialsResponse}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:dominion.v1.ScvGetCredentialsResponse)
        org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsResponseOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return org.opennms.dominion.local.rpc.grpc.DominionGrpc.internal_static_dominion_v1_ScvGetCredentialsResponse_descriptor;
      }

      @SuppressWarnings({"rawtypes"})
      protected com.google.protobuf.MapFieldReflectionAccessor internalGetMapFieldReflection(
          int number) {
        switch (number) {
          case 3:
            return internalGetAttributes();
          default:
            throw new RuntimeException(
                "Invalid map field number: " + number);
        }
      }
      @SuppressWarnings({"rawtypes"})
      protected com.google.protobuf.MapFieldReflectionAccessor internalGetMutableMapFieldReflection(
          int number) {
        switch (number) {
          case 3:
            return internalGetMutableAttributes();
          default:
            throw new RuntimeException(
                "Invalid map field number: " + number);
        }
      }
      @java.lang.Override
      protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return org.opennms.dominion.local.rpc.grpc.DominionGrpc.internal_static_dominion_v1_ScvGetCredentialsResponse_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsResponse.class, org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsResponse.Builder.class);
      }

      // Construct using org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsResponse.newBuilder()
      private Builder() {

      }

      private Builder(
          com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
        super(parent);

      }
      @java.lang.Override
      public Builder clear() {
        super.clear();
        bitField0_ = 0;
        user_ = "";
        password_ = "";
        internalGetMutableAttributes().clear();
        return this;
      }

      @java.lang.Override
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return org.opennms.dominion.local.rpc.grpc.DominionGrpc.internal_static_dominion_v1_ScvGetCredentialsResponse_descriptor;
      }

      @java.lang.Override
      public org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsResponse getDefaultInstanceForType() {
        return org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsResponse.getDefaultInstance();
      }

      @java.lang.Override
      public org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsResponse build() {
        org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsResponse result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      @java.lang.Override
      public org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsResponse buildPartial() {
        org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsResponse result = new org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsResponse(this);
        if (bitField0_ != 0) { buildPartial0(result); }
        onBuilt();
        return result;
      }

      private void buildPartial0(org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsResponse result) {
        int from_bitField0_ = bitField0_;
        if (((from_bitField0_ & 0x00000001) != 0)) {
          result.user_ = user_;
        }
        if (((from_bitField0_ & 0x00000002) != 0)) {
          result.password_ = password_;
        }
        if (((from_bitField0_ & 0x00000004) != 0)) {
          result.attributes_ = internalGetAttributes();
          result.attributes_.makeImmutable();
        }
      }

      @java.lang.Override
      public Builder clone() {
        return super.clone();
      }
      @java.lang.Override
      public Builder setField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return super.setField(field, value);
      }
      @java.lang.Override
      public Builder clearField(
          com.google.protobuf.Descriptors.FieldDescriptor field) {
        return super.clearField(field);
      }
      @java.lang.Override
      public Builder clearOneof(
          com.google.protobuf.Descriptors.OneofDescriptor oneof) {
        return super.clearOneof(oneof);
      }
      @java.lang.Override
      public Builder setRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          int index, java.lang.Object value) {
        return super.setRepeatedField(field, index, value);
      }
      @java.lang.Override
      public Builder addRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return super.addRepeatedField(field, value);
      }
      @java.lang.Override
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsResponse) {
          return mergeFrom((org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsResponse)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsResponse other) {
        if (other == org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsResponse.getDefaultInstance()) return this;
        if (!other.getUser().isEmpty()) {
          user_ = other.user_;
          bitField0_ |= 0x00000001;
          onChanged();
        }
        if (!other.getPassword().isEmpty()) {
          password_ = other.password_;
          bitField0_ |= 0x00000002;
          onChanged();
        }
        internalGetMutableAttributes().mergeFrom(
            other.internalGetAttributes());
        bitField0_ |= 0x00000004;
        this.mergeUnknownFields(other.getUnknownFields());
        onChanged();
        return this;
      }

      @java.lang.Override
      public final boolean isInitialized() {
        return true;
      }

      @java.lang.Override
      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        if (extensionRegistry == null) {
          throw new java.lang.NullPointerException();
        }
        try {
          boolean done = false;
          while (!done) {
            int tag = input.readTag();
            switch (tag) {
              case 0:
                done = true;
                break;
              case 10: {
                user_ = input.readStringRequireUtf8();
                bitField0_ |= 0x00000001;
                break;
              } // case 10
              case 18: {
                password_ = input.readStringRequireUtf8();
                bitField0_ |= 0x00000002;
                break;
              } // case 18
              case 26: {
                com.google.protobuf.MapEntry<java.lang.String, java.lang.String>
                attributes__ = input.readMessage(
                    AttributesDefaultEntryHolder.defaultEntry.getParserForType(), extensionRegistry);
                internalGetMutableAttributes().getMutableMap().put(
                    attributes__.getKey(), attributes__.getValue());
                bitField0_ |= 0x00000004;
                break;
              } // case 26
              default: {
                if (!super.parseUnknownField(input, extensionRegistry, tag)) {
                  done = true; // was an endgroup tag
                }
                break;
              } // default:
            } // switch (tag)
          } // while (!done)
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          throw e.unwrapIOException();
        } finally {
          onChanged();
        } // finally
        return this;
      }
      private int bitField0_;

      private java.lang.Object user_ = "";
      /**
       * <code>string user = 1;</code>
       * @return The user.
       */
      public java.lang.String getUser() {
        java.lang.Object ref = user_;
        if (!(ref instanceof java.lang.String)) {
          com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
          java.lang.String s = bs.toStringUtf8();
          user_ = s;
          return s;
        } else {
          return (java.lang.String) ref;
        }
      }
      /**
       * <code>string user = 1;</code>
       * @return The bytes for user.
       */
      public com.google.protobuf.ByteString
          getUserBytes() {
        java.lang.Object ref = user_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (java.lang.String) ref);
          user_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>string user = 1;</code>
       * @param value The user to set.
       * @return This builder for chaining.
       */
      public Builder setUser(
          java.lang.String value) {
        if (value == null) { throw new NullPointerException(); }
        user_ = value;
        bitField0_ |= 0x00000001;
        onChanged();
        return this;
      }
      /**
       * <code>string user = 1;</code>
       * @return This builder for chaining.
       */
      public Builder clearUser() {
        user_ = getDefaultInstance().getUser();
        bitField0_ = (bitField0_ & ~0x00000001);
        onChanged();
        return this;
      }
      /**
       * <code>string user = 1;</code>
       * @param value The bytes for user to set.
       * @return This builder for chaining.
       */
      public Builder setUserBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) { throw new NullPointerException(); }
        checkByteStringIsUtf8(value);
        user_ = value;
        bitField0_ |= 0x00000001;
        onChanged();
        return this;
      }

      private java.lang.Object password_ = "";
      /**
       * <code>string password = 2;</code>
       * @return The password.
       */
      public java.lang.String getPassword() {
        java.lang.Object ref = password_;
        if (!(ref instanceof java.lang.String)) {
          com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
          java.lang.String s = bs.toStringUtf8();
          password_ = s;
          return s;
        } else {
          return (java.lang.String) ref;
        }
      }
      /**
       * <code>string password = 2;</code>
       * @return The bytes for password.
       */
      public com.google.protobuf.ByteString
          getPasswordBytes() {
        java.lang.Object ref = password_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (java.lang.String) ref);
          password_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>string password = 2;</code>
       * @param value The password to set.
       * @return This builder for chaining.
       */
      public Builder setPassword(
          java.lang.String value) {
        if (value == null) { throw new NullPointerException(); }
        password_ = value;
        bitField0_ |= 0x00000002;
        onChanged();
        return this;
      }
      /**
       * <code>string password = 2;</code>
       * @return This builder for chaining.
       */
      public Builder clearPassword() {
        password_ = getDefaultInstance().getPassword();
        bitField0_ = (bitField0_ & ~0x00000002);
        onChanged();
        return this;
      }
      /**
       * <code>string password = 2;</code>
       * @param value The bytes for password to set.
       * @return This builder for chaining.
       */
      public Builder setPasswordBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) { throw new NullPointerException(); }
        checkByteStringIsUtf8(value);
        password_ = value;
        bitField0_ |= 0x00000002;
        onChanged();
        return this;
      }

      private com.google.protobuf.MapField<
          java.lang.String, java.lang.String> attributes_;
      private com.google.protobuf.MapField<java.lang.String, java.lang.String>
          internalGetAttributes() {
        if (attributes_ == null) {
          return com.google.protobuf.MapField.emptyMapField(
              AttributesDefaultEntryHolder.defaultEntry);
        }
        return attributes_;
      }
      private com.google.protobuf.MapField<java.lang.String, java.lang.String>
          internalGetMutableAttributes() {
        if (attributes_ == null) {
          attributes_ = com.google.protobuf.MapField.newMapField(
              AttributesDefaultEntryHolder.defaultEntry);
        }
        if (!attributes_.isMutable()) {
          attributes_ = attributes_.copy();
        }
        bitField0_ |= 0x00000004;
        onChanged();
        return attributes_;
      }
      public int getAttributesCount() {
        return internalGetAttributes().getMap().size();
      }
      /**
       * <code>map&lt;string, string&gt; attributes = 3;</code>
       */
      @java.lang.Override
      public boolean containsAttributes(
          java.lang.String key) {
        if (key == null) { throw new NullPointerException("map key"); }
        return internalGetAttributes().getMap().containsKey(key);
      }
      /**
       * Use {@link #getAttributesMap()} instead.
       */
      @java.lang.Override
      @java.lang.Deprecated
      public java.util.Map<java.lang.String, java.lang.String> getAttributes() {
        return getAttributesMap();
      }
      /**
       * <code>map&lt;string, string&gt; attributes = 3;</code>
       */
      @java.lang.Override
      public java.util.Map<java.lang.String, java.lang.String> getAttributesMap() {
        return internalGetAttributes().getMap();
      }
      /**
       * <code>map&lt;string, string&gt; attributes = 3;</code>
       */
      @java.lang.Override
      public /* nullable */
java.lang.String getAttributesOrDefault(
          java.lang.String key,
          /* nullable */
java.lang.String defaultValue) {
        if (key == null) { throw new NullPointerException("map key"); }
        java.util.Map<java.lang.String, java.lang.String> map =
            internalGetAttributes().getMap();
        return map.containsKey(key) ? map.get(key) : defaultValue;
      }
      /**
       * <code>map&lt;string, string&gt; attributes = 3;</code>
       */
      @java.lang.Override
      public java.lang.String getAttributesOrThrow(
          java.lang.String key) {
        if (key == null) { throw new NullPointerException("map key"); }
        java.util.Map<java.lang.String, java.lang.String> map =
            internalGetAttributes().getMap();
        if (!map.containsKey(key)) {
          throw new java.lang.IllegalArgumentException();
        }
        return map.get(key);
      }
      public Builder clearAttributes() {
        bitField0_ = (bitField0_ & ~0x00000004);
        internalGetMutableAttributes().getMutableMap()
            .clear();
        return this;
      }
      /**
       * <code>map&lt;string, string&gt; attributes = 3;</code>
       */
      public Builder removeAttributes(
          java.lang.String key) {
        if (key == null) { throw new NullPointerException("map key"); }
        internalGetMutableAttributes().getMutableMap()
            .remove(key);
        return this;
      }
      /**
       * Use alternate mutation accessors instead.
       */
      @java.lang.Deprecated
      public java.util.Map<java.lang.String, java.lang.String>
          getMutableAttributes() {
        bitField0_ |= 0x00000004;
        return internalGetMutableAttributes().getMutableMap();
      }
      /**
       * <code>map&lt;string, string&gt; attributes = 3;</code>
       */
      public Builder putAttributes(
          java.lang.String key,
          java.lang.String value) {
        if (key == null) { throw new NullPointerException("map key"); }
        if (value == null) { throw new NullPointerException("map value"); }
        internalGetMutableAttributes().getMutableMap()
            .put(key, value);
        bitField0_ |= 0x00000004;
        return this;
      }
      /**
       * <code>map&lt;string, string&gt; attributes = 3;</code>
       */
      public Builder putAllAttributes(
          java.util.Map<java.lang.String, java.lang.String> values) {
        internalGetMutableAttributes().getMutableMap()
            .putAll(values);
        bitField0_ |= 0x00000004;
        return this;
      }
      @java.lang.Override
      public final Builder setUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.setUnknownFields(unknownFields);
      }

      @java.lang.Override
      public final Builder mergeUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.mergeUnknownFields(unknownFields);
      }


      // @@protoc_insertion_point(builder_scope:dominion.v1.ScvGetCredentialsResponse)
    }

    // @@protoc_insertion_point(class_scope:dominion.v1.ScvGetCredentialsResponse)
    private static final org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsResponse DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsResponse();
    }

    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsResponse getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<ScvGetCredentialsResponse>
        PARSER = new com.google.protobuf.AbstractParser<ScvGetCredentialsResponse>() {
      @java.lang.Override
      public ScvGetCredentialsResponse parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        Builder builder = newBuilder();
        try {
          builder.mergeFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          throw e.setUnfinishedMessage(builder.buildPartial());
        } catch (com.google.protobuf.UninitializedMessageException e) {
          throw e.asInvalidProtocolBufferException().setUnfinishedMessage(builder.buildPartial());
        } catch (java.io.IOException e) {
          throw new com.google.protobuf.InvalidProtocolBufferException(e)
              .setUnfinishedMessage(builder.buildPartial());
        }
        return builder.buildPartial();
      }
    };

    public static com.google.protobuf.Parser<ScvGetCredentialsResponse> parser() {
      return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<ScvGetCredentialsResponse> getParserForType() {
      return PARSER;
    }

    @java.lang.Override
    public org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvGetCredentialsResponse getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  public interface ScvSetCredentialsRequestOrBuilder extends
      // @@protoc_insertion_point(interface_extends:dominion.v1.ScvSetCredentialsRequest)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <code>.dominion.v1.ClientCredentials clientCredentials = 1;</code>
     * @return Whether the clientCredentials field is set.
     */
    boolean hasClientCredentials();
    /**
     * <code>.dominion.v1.ClientCredentials clientCredentials = 1;</code>
     * @return The clientCredentials.
     */
    org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials getClientCredentials();
    /**
     * <code>.dominion.v1.ClientCredentials clientCredentials = 1;</code>
     */
    org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentialsOrBuilder getClientCredentialsOrBuilder();

    /**
     * <code>string alias = 2;</code>
     * @return The alias.
     */
    java.lang.String getAlias();
    /**
     * <code>string alias = 2;</code>
     * @return The bytes for alias.
     */
    com.google.protobuf.ByteString
        getAliasBytes();

    /**
     * <code>string user = 3;</code>
     * @return The user.
     */
    java.lang.String getUser();
    /**
     * <code>string user = 3;</code>
     * @return The bytes for user.
     */
    com.google.protobuf.ByteString
        getUserBytes();

    /**
     * <code>string password = 4;</code>
     * @return The password.
     */
    java.lang.String getPassword();
    /**
     * <code>string password = 4;</code>
     * @return The bytes for password.
     */
    com.google.protobuf.ByteString
        getPasswordBytes();

    /**
     * <code>map&lt;string, string&gt; attributes = 5;</code>
     */
    int getAttributesCount();
    /**
     * <code>map&lt;string, string&gt; attributes = 5;</code>
     */
    boolean containsAttributes(
        java.lang.String key);
    /**
     * Use {@link #getAttributesMap()} instead.
     */
    @java.lang.Deprecated
    java.util.Map<java.lang.String, java.lang.String>
    getAttributes();
    /**
     * <code>map&lt;string, string&gt; attributes = 5;</code>
     */
    java.util.Map<java.lang.String, java.lang.String>
    getAttributesMap();
    /**
     * <code>map&lt;string, string&gt; attributes = 5;</code>
     */
    /* nullable */
java.lang.String getAttributesOrDefault(
        java.lang.String key,
        /* nullable */
java.lang.String defaultValue);
    /**
     * <code>map&lt;string, string&gt; attributes = 5;</code>
     */
    java.lang.String getAttributesOrThrow(
        java.lang.String key);
  }
  /**
   * Protobuf type {@code dominion.v1.ScvSetCredentialsRequest}
   */
  public static final class ScvSetCredentialsRequest extends
      com.google.protobuf.GeneratedMessageV3 implements
      // @@protoc_insertion_point(message_implements:dominion.v1.ScvSetCredentialsRequest)
      ScvSetCredentialsRequestOrBuilder {
  private static final long serialVersionUID = 0L;
    // Use ScvSetCredentialsRequest.newBuilder() to construct.
    private ScvSetCredentialsRequest(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
      super(builder);
    }
    private ScvSetCredentialsRequest() {
      alias_ = "";
      user_ = "";
      password_ = "";
    }

    @java.lang.Override
    @SuppressWarnings({"unused"})
    protected java.lang.Object newInstance(
        UnusedPrivateParameter unused) {
      return new ScvSetCredentialsRequest();
    }

    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return org.opennms.dominion.local.rpc.grpc.DominionGrpc.internal_static_dominion_v1_ScvSetCredentialsRequest_descriptor;
    }

    @SuppressWarnings({"rawtypes"})
    @java.lang.Override
    protected com.google.protobuf.MapFieldReflectionAccessor internalGetMapFieldReflection(
        int number) {
      switch (number) {
        case 5:
          return internalGetAttributes();
        default:
          throw new RuntimeException(
              "Invalid map field number: " + number);
      }
    }
    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return org.opennms.dominion.local.rpc.grpc.DominionGrpc.internal_static_dominion_v1_ScvSetCredentialsRequest_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsRequest.class, org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsRequest.Builder.class);
    }

    private int bitField0_;
    public static final int CLIENTCREDENTIALS_FIELD_NUMBER = 1;
    private org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials clientCredentials_;
    /**
     * <code>.dominion.v1.ClientCredentials clientCredentials = 1;</code>
     * @return Whether the clientCredentials field is set.
     */
    @java.lang.Override
    public boolean hasClientCredentials() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <code>.dominion.v1.ClientCredentials clientCredentials = 1;</code>
     * @return The clientCredentials.
     */
    @java.lang.Override
    public org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials getClientCredentials() {
      return clientCredentials_ == null ? org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials.getDefaultInstance() : clientCredentials_;
    }
    /**
     * <code>.dominion.v1.ClientCredentials clientCredentials = 1;</code>
     */
    @java.lang.Override
    public org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentialsOrBuilder getClientCredentialsOrBuilder() {
      return clientCredentials_ == null ? org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials.getDefaultInstance() : clientCredentials_;
    }

    public static final int ALIAS_FIELD_NUMBER = 2;
    @SuppressWarnings("serial")
    private volatile java.lang.Object alias_ = "";
    /**
     * <code>string alias = 2;</code>
     * @return The alias.
     */
    @java.lang.Override
    public java.lang.String getAlias() {
      java.lang.Object ref = alias_;
      if (ref instanceof java.lang.String) {
        return (java.lang.String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        alias_ = s;
        return s;
      }
    }
    /**
     * <code>string alias = 2;</code>
     * @return The bytes for alias.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getAliasBytes() {
      java.lang.Object ref = alias_;
      if (ref instanceof java.lang.String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        alias_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    public static final int USER_FIELD_NUMBER = 3;
    @SuppressWarnings("serial")
    private volatile java.lang.Object user_ = "";
    /**
     * <code>string user = 3;</code>
     * @return The user.
     */
    @java.lang.Override
    public java.lang.String getUser() {
      java.lang.Object ref = user_;
      if (ref instanceof java.lang.String) {
        return (java.lang.String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        user_ = s;
        return s;
      }
    }
    /**
     * <code>string user = 3;</code>
     * @return The bytes for user.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getUserBytes() {
      java.lang.Object ref = user_;
      if (ref instanceof java.lang.String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        user_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    public static final int PASSWORD_FIELD_NUMBER = 4;
    @SuppressWarnings("serial")
    private volatile java.lang.Object password_ = "";
    /**
     * <code>string password = 4;</code>
     * @return The password.
     */
    @java.lang.Override
    public java.lang.String getPassword() {
      java.lang.Object ref = password_;
      if (ref instanceof java.lang.String) {
        return (java.lang.String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        password_ = s;
        return s;
      }
    }
    /**
     * <code>string password = 4;</code>
     * @return The bytes for password.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getPasswordBytes() {
      java.lang.Object ref = password_;
      if (ref instanceof java.lang.String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        password_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    public static final int ATTRIBUTES_FIELD_NUMBER = 5;
    private static final class AttributesDefaultEntryHolder {
      static final com.google.protobuf.MapEntry<
          java.lang.String, java.lang.String> defaultEntry =
              com.google.protobuf.MapEntry
              .<java.lang.String, java.lang.String>newDefaultInstance(
                  org.opennms.dominion.local.rpc.grpc.DominionGrpc.internal_static_dominion_v1_ScvSetCredentialsRequest_AttributesEntry_descriptor, 
                  com.google.protobuf.WireFormat.FieldType.STRING,
                  "",
                  com.google.protobuf.WireFormat.FieldType.STRING,
                  "");
    }
    @SuppressWarnings("serial")
    private com.google.protobuf.MapField<
        java.lang.String, java.lang.String> attributes_;
    private com.google.protobuf.MapField<java.lang.String, java.lang.String>
    internalGetAttributes() {
      if (attributes_ == null) {
        return com.google.protobuf.MapField.emptyMapField(
            AttributesDefaultEntryHolder.defaultEntry);
      }
      return attributes_;
    }
    public int getAttributesCount() {
      return internalGetAttributes().getMap().size();
    }
    /**
     * <code>map&lt;string, string&gt; attributes = 5;</code>
     */
    @java.lang.Override
    public boolean containsAttributes(
        java.lang.String key) {
      if (key == null) { throw new NullPointerException("map key"); }
      return internalGetAttributes().getMap().containsKey(key);
    }
    /**
     * Use {@link #getAttributesMap()} instead.
     */
    @java.lang.Override
    @java.lang.Deprecated
    public java.util.Map<java.lang.String, java.lang.String> getAttributes() {
      return getAttributesMap();
    }
    /**
     * <code>map&lt;string, string&gt; attributes = 5;</code>
     */
    @java.lang.Override
    public java.util.Map<java.lang.String, java.lang.String> getAttributesMap() {
      return internalGetAttributes().getMap();
    }
    /**
     * <code>map&lt;string, string&gt; attributes = 5;</code>
     */
    @java.lang.Override
    public /* nullable */
java.lang.String getAttributesOrDefault(
        java.lang.String key,
        /* nullable */
java.lang.String defaultValue) {
      if (key == null) { throw new NullPointerException("map key"); }
      java.util.Map<java.lang.String, java.lang.String> map =
          internalGetAttributes().getMap();
      return map.containsKey(key) ? map.get(key) : defaultValue;
    }
    /**
     * <code>map&lt;string, string&gt; attributes = 5;</code>
     */
    @java.lang.Override
    public java.lang.String getAttributesOrThrow(
        java.lang.String key) {
      if (key == null) { throw new NullPointerException("map key"); }
      java.util.Map<java.lang.String, java.lang.String> map =
          internalGetAttributes().getMap();
      if (!map.containsKey(key)) {
        throw new java.lang.IllegalArgumentException();
      }
      return map.get(key);
    }

    private byte memoizedIsInitialized = -1;
    @java.lang.Override
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    @java.lang.Override
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      if (((bitField0_ & 0x00000001) != 0)) {
        output.writeMessage(1, getClientCredentials());
      }
      if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(alias_)) {
        com.google.protobuf.GeneratedMessageV3.writeString(output, 2, alias_);
      }
      if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(user_)) {
        com.google.protobuf.GeneratedMessageV3.writeString(output, 3, user_);
      }
      if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(password_)) {
        com.google.protobuf.GeneratedMessageV3.writeString(output, 4, password_);
      }
      com.google.protobuf.GeneratedMessageV3
        .serializeStringMapTo(
          output,
          internalGetAttributes(),
          AttributesDefaultEntryHolder.defaultEntry,
          5);
      getUnknownFields().writeTo(output);
    }

    @java.lang.Override
    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      if (((bitField0_ & 0x00000001) != 0)) {
        size += com.google.protobuf.CodedOutputStream
          .computeMessageSize(1, getClientCredentials());
      }
      if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(alias_)) {
        size += com.google.protobuf.GeneratedMessageV3.computeStringSize(2, alias_);
      }
      if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(user_)) {
        size += com.google.protobuf.GeneratedMessageV3.computeStringSize(3, user_);
      }
      if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(password_)) {
        size += com.google.protobuf.GeneratedMessageV3.computeStringSize(4, password_);
      }
      for (java.util.Map.Entry<java.lang.String, java.lang.String> entry
           : internalGetAttributes().getMap().entrySet()) {
        com.google.protobuf.MapEntry<java.lang.String, java.lang.String>
        attributes__ = AttributesDefaultEntryHolder.defaultEntry.newBuilderForType()
            .setKey(entry.getKey())
            .setValue(entry.getValue())
            .build();
        size += com.google.protobuf.CodedOutputStream
            .computeMessageSize(5, attributes__);
      }
      size += getUnknownFields().getSerializedSize();
      memoizedSize = size;
      return size;
    }

    @java.lang.Override
    public boolean equals(final java.lang.Object obj) {
      if (obj == this) {
       return true;
      }
      if (!(obj instanceof org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsRequest)) {
        return super.equals(obj);
      }
      org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsRequest other = (org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsRequest) obj;

      if (hasClientCredentials() != other.hasClientCredentials()) return false;
      if (hasClientCredentials()) {
        if (!getClientCredentials()
            .equals(other.getClientCredentials())) return false;
      }
      if (!getAlias()
          .equals(other.getAlias())) return false;
      if (!getUser()
          .equals(other.getUser())) return false;
      if (!getPassword()
          .equals(other.getPassword())) return false;
      if (!internalGetAttributes().equals(
          other.internalGetAttributes())) return false;
      if (!getUnknownFields().equals(other.getUnknownFields())) return false;
      return true;
    }

    @java.lang.Override
    public int hashCode() {
      if (memoizedHashCode != 0) {
        return memoizedHashCode;
      }
      int hash = 41;
      hash = (19 * hash) + getDescriptor().hashCode();
      if (hasClientCredentials()) {
        hash = (37 * hash) + CLIENTCREDENTIALS_FIELD_NUMBER;
        hash = (53 * hash) + getClientCredentials().hashCode();
      }
      hash = (37 * hash) + ALIAS_FIELD_NUMBER;
      hash = (53 * hash) + getAlias().hashCode();
      hash = (37 * hash) + USER_FIELD_NUMBER;
      hash = (53 * hash) + getUser().hashCode();
      hash = (37 * hash) + PASSWORD_FIELD_NUMBER;
      hash = (53 * hash) + getPassword().hashCode();
      if (!internalGetAttributes().getMap().isEmpty()) {
        hash = (37 * hash) + ATTRIBUTES_FIELD_NUMBER;
        hash = (53 * hash) + internalGetAttributes().hashCode();
      }
      hash = (29 * hash) + getUnknownFields().hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsRequest parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsRequest parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsRequest parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsRequest parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsRequest parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsRequest parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsRequest parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsRequest parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsRequest parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input);
    }

    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsRequest parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsRequest parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsRequest parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    @java.lang.Override
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
    }
    public static Builder newBuilder(org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsRequest prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }
    @java.lang.Override
    public Builder toBuilder() {
      return this == DEFAULT_INSTANCE
          ? new Builder() : new Builder().mergeFrom(this);
    }

    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code dominion.v1.ScvSetCredentialsRequest}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:dominion.v1.ScvSetCredentialsRequest)
        org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsRequestOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return org.opennms.dominion.local.rpc.grpc.DominionGrpc.internal_static_dominion_v1_ScvSetCredentialsRequest_descriptor;
      }

      @SuppressWarnings({"rawtypes"})
      protected com.google.protobuf.MapFieldReflectionAccessor internalGetMapFieldReflection(
          int number) {
        switch (number) {
          case 5:
            return internalGetAttributes();
          default:
            throw new RuntimeException(
                "Invalid map field number: " + number);
        }
      }
      @SuppressWarnings({"rawtypes"})
      protected com.google.protobuf.MapFieldReflectionAccessor internalGetMutableMapFieldReflection(
          int number) {
        switch (number) {
          case 5:
            return internalGetMutableAttributes();
          default:
            throw new RuntimeException(
                "Invalid map field number: " + number);
        }
      }
      @java.lang.Override
      protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return org.opennms.dominion.local.rpc.grpc.DominionGrpc.internal_static_dominion_v1_ScvSetCredentialsRequest_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsRequest.class, org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsRequest.Builder.class);
      }

      // Construct using org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsRequest.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
          com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessageV3
                .alwaysUseFieldBuilders) {
          getClientCredentialsFieldBuilder();
        }
      }
      @java.lang.Override
      public Builder clear() {
        super.clear();
        bitField0_ = 0;
        clientCredentials_ = null;
        if (clientCredentialsBuilder_ != null) {
          clientCredentialsBuilder_.dispose();
          clientCredentialsBuilder_ = null;
        }
        alias_ = "";
        user_ = "";
        password_ = "";
        internalGetMutableAttributes().clear();
        return this;
      }

      @java.lang.Override
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return org.opennms.dominion.local.rpc.grpc.DominionGrpc.internal_static_dominion_v1_ScvSetCredentialsRequest_descriptor;
      }

      @java.lang.Override
      public org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsRequest getDefaultInstanceForType() {
        return org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsRequest.getDefaultInstance();
      }

      @java.lang.Override
      public org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsRequest build() {
        org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsRequest result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      @java.lang.Override
      public org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsRequest buildPartial() {
        org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsRequest result = new org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsRequest(this);
        if (bitField0_ != 0) { buildPartial0(result); }
        onBuilt();
        return result;
      }

      private void buildPartial0(org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsRequest result) {
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000001) != 0)) {
          result.clientCredentials_ = clientCredentialsBuilder_ == null
              ? clientCredentials_
              : clientCredentialsBuilder_.build();
          to_bitField0_ |= 0x00000001;
        }
        if (((from_bitField0_ & 0x00000002) != 0)) {
          result.alias_ = alias_;
        }
        if (((from_bitField0_ & 0x00000004) != 0)) {
          result.user_ = user_;
        }
        if (((from_bitField0_ & 0x00000008) != 0)) {
          result.password_ = password_;
        }
        if (((from_bitField0_ & 0x00000010) != 0)) {
          result.attributes_ = internalGetAttributes();
          result.attributes_.makeImmutable();
        }
        result.bitField0_ |= to_bitField0_;
      }

      @java.lang.Override
      public Builder clone() {
        return super.clone();
      }
      @java.lang.Override
      public Builder setField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return super.setField(field, value);
      }
      @java.lang.Override
      public Builder clearField(
          com.google.protobuf.Descriptors.FieldDescriptor field) {
        return super.clearField(field);
      }
      @java.lang.Override
      public Builder clearOneof(
          com.google.protobuf.Descriptors.OneofDescriptor oneof) {
        return super.clearOneof(oneof);
      }
      @java.lang.Override
      public Builder setRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          int index, java.lang.Object value) {
        return super.setRepeatedField(field, index, value);
      }
      @java.lang.Override
      public Builder addRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return super.addRepeatedField(field, value);
      }
      @java.lang.Override
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsRequest) {
          return mergeFrom((org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsRequest)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsRequest other) {
        if (other == org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsRequest.getDefaultInstance()) return this;
        if (other.hasClientCredentials()) {
          mergeClientCredentials(other.getClientCredentials());
        }
        if (!other.getAlias().isEmpty()) {
          alias_ = other.alias_;
          bitField0_ |= 0x00000002;
          onChanged();
        }
        if (!other.getUser().isEmpty()) {
          user_ = other.user_;
          bitField0_ |= 0x00000004;
          onChanged();
        }
        if (!other.getPassword().isEmpty()) {
          password_ = other.password_;
          bitField0_ |= 0x00000008;
          onChanged();
        }
        internalGetMutableAttributes().mergeFrom(
            other.internalGetAttributes());
        bitField0_ |= 0x00000010;
        this.mergeUnknownFields(other.getUnknownFields());
        onChanged();
        return this;
      }

      @java.lang.Override
      public final boolean isInitialized() {
        return true;
      }

      @java.lang.Override
      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        if (extensionRegistry == null) {
          throw new java.lang.NullPointerException();
        }
        try {
          boolean done = false;
          while (!done) {
            int tag = input.readTag();
            switch (tag) {
              case 0:
                done = true;
                break;
              case 10: {
                input.readMessage(
                    getClientCredentialsFieldBuilder().getBuilder(),
                    extensionRegistry);
                bitField0_ |= 0x00000001;
                break;
              } // case 10
              case 18: {
                alias_ = input.readStringRequireUtf8();
                bitField0_ |= 0x00000002;
                break;
              } // case 18
              case 26: {
                user_ = input.readStringRequireUtf8();
                bitField0_ |= 0x00000004;
                break;
              } // case 26
              case 34: {
                password_ = input.readStringRequireUtf8();
                bitField0_ |= 0x00000008;
                break;
              } // case 34
              case 42: {
                com.google.protobuf.MapEntry<java.lang.String, java.lang.String>
                attributes__ = input.readMessage(
                    AttributesDefaultEntryHolder.defaultEntry.getParserForType(), extensionRegistry);
                internalGetMutableAttributes().getMutableMap().put(
                    attributes__.getKey(), attributes__.getValue());
                bitField0_ |= 0x00000010;
                break;
              } // case 42
              default: {
                if (!super.parseUnknownField(input, extensionRegistry, tag)) {
                  done = true; // was an endgroup tag
                }
                break;
              } // default:
            } // switch (tag)
          } // while (!done)
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          throw e.unwrapIOException();
        } finally {
          onChanged();
        } // finally
        return this;
      }
      private int bitField0_;

      private org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials clientCredentials_;
      private com.google.protobuf.SingleFieldBuilderV3<
          org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials, org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials.Builder, org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentialsOrBuilder> clientCredentialsBuilder_;
      /**
       * <code>.dominion.v1.ClientCredentials clientCredentials = 1;</code>
       * @return Whether the clientCredentials field is set.
       */
      public boolean hasClientCredentials() {
        return ((bitField0_ & 0x00000001) != 0);
      }
      /**
       * <code>.dominion.v1.ClientCredentials clientCredentials = 1;</code>
       * @return The clientCredentials.
       */
      public org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials getClientCredentials() {
        if (clientCredentialsBuilder_ == null) {
          return clientCredentials_ == null ? org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials.getDefaultInstance() : clientCredentials_;
        } else {
          return clientCredentialsBuilder_.getMessage();
        }
      }
      /**
       * <code>.dominion.v1.ClientCredentials clientCredentials = 1;</code>
       */
      public Builder setClientCredentials(org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials value) {
        if (clientCredentialsBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          clientCredentials_ = value;
        } else {
          clientCredentialsBuilder_.setMessage(value);
        }
        bitField0_ |= 0x00000001;
        onChanged();
        return this;
      }
      /**
       * <code>.dominion.v1.ClientCredentials clientCredentials = 1;</code>
       */
      public Builder setClientCredentials(
          org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials.Builder builderForValue) {
        if (clientCredentialsBuilder_ == null) {
          clientCredentials_ = builderForValue.build();
        } else {
          clientCredentialsBuilder_.setMessage(builderForValue.build());
        }
        bitField0_ |= 0x00000001;
        onChanged();
        return this;
      }
      /**
       * <code>.dominion.v1.ClientCredentials clientCredentials = 1;</code>
       */
      public Builder mergeClientCredentials(org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials value) {
        if (clientCredentialsBuilder_ == null) {
          if (((bitField0_ & 0x00000001) != 0) &&
            clientCredentials_ != null &&
            clientCredentials_ != org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials.getDefaultInstance()) {
            getClientCredentialsBuilder().mergeFrom(value);
          } else {
            clientCredentials_ = value;
          }
        } else {
          clientCredentialsBuilder_.mergeFrom(value);
        }
        if (clientCredentials_ != null) {
          bitField0_ |= 0x00000001;
          onChanged();
        }
        return this;
      }
      /**
       * <code>.dominion.v1.ClientCredentials clientCredentials = 1;</code>
       */
      public Builder clearClientCredentials() {
        bitField0_ = (bitField0_ & ~0x00000001);
        clientCredentials_ = null;
        if (clientCredentialsBuilder_ != null) {
          clientCredentialsBuilder_.dispose();
          clientCredentialsBuilder_ = null;
        }
        onChanged();
        return this;
      }
      /**
       * <code>.dominion.v1.ClientCredentials clientCredentials = 1;</code>
       */
      public org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials.Builder getClientCredentialsBuilder() {
        bitField0_ |= 0x00000001;
        onChanged();
        return getClientCredentialsFieldBuilder().getBuilder();
      }
      /**
       * <code>.dominion.v1.ClientCredentials clientCredentials = 1;</code>
       */
      public org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentialsOrBuilder getClientCredentialsOrBuilder() {
        if (clientCredentialsBuilder_ != null) {
          return clientCredentialsBuilder_.getMessageOrBuilder();
        } else {
          return clientCredentials_ == null ?
              org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials.getDefaultInstance() : clientCredentials_;
        }
      }
      /**
       * <code>.dominion.v1.ClientCredentials clientCredentials = 1;</code>
       */
      private com.google.protobuf.SingleFieldBuilderV3<
          org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials, org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials.Builder, org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentialsOrBuilder> 
          getClientCredentialsFieldBuilder() {
        if (clientCredentialsBuilder_ == null) {
          clientCredentialsBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
              org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials, org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentials.Builder, org.opennms.dominion.local.rpc.grpc.DominionGrpc.ClientCredentialsOrBuilder>(
                  getClientCredentials(),
                  getParentForChildren(),
                  isClean());
          clientCredentials_ = null;
        }
        return clientCredentialsBuilder_;
      }

      private java.lang.Object alias_ = "";
      /**
       * <code>string alias = 2;</code>
       * @return The alias.
       */
      public java.lang.String getAlias() {
        java.lang.Object ref = alias_;
        if (!(ref instanceof java.lang.String)) {
          com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
          java.lang.String s = bs.toStringUtf8();
          alias_ = s;
          return s;
        } else {
          return (java.lang.String) ref;
        }
      }
      /**
       * <code>string alias = 2;</code>
       * @return The bytes for alias.
       */
      public com.google.protobuf.ByteString
          getAliasBytes() {
        java.lang.Object ref = alias_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (java.lang.String) ref);
          alias_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>string alias = 2;</code>
       * @param value The alias to set.
       * @return This builder for chaining.
       */
      public Builder setAlias(
          java.lang.String value) {
        if (value == null) { throw new NullPointerException(); }
        alias_ = value;
        bitField0_ |= 0x00000002;
        onChanged();
        return this;
      }
      /**
       * <code>string alias = 2;</code>
       * @return This builder for chaining.
       */
      public Builder clearAlias() {
        alias_ = getDefaultInstance().getAlias();
        bitField0_ = (bitField0_ & ~0x00000002);
        onChanged();
        return this;
      }
      /**
       * <code>string alias = 2;</code>
       * @param value The bytes for alias to set.
       * @return This builder for chaining.
       */
      public Builder setAliasBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) { throw new NullPointerException(); }
        checkByteStringIsUtf8(value);
        alias_ = value;
        bitField0_ |= 0x00000002;
        onChanged();
        return this;
      }

      private java.lang.Object user_ = "";
      /**
       * <code>string user = 3;</code>
       * @return The user.
       */
      public java.lang.String getUser() {
        java.lang.Object ref = user_;
        if (!(ref instanceof java.lang.String)) {
          com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
          java.lang.String s = bs.toStringUtf8();
          user_ = s;
          return s;
        } else {
          return (java.lang.String) ref;
        }
      }
      /**
       * <code>string user = 3;</code>
       * @return The bytes for user.
       */
      public com.google.protobuf.ByteString
          getUserBytes() {
        java.lang.Object ref = user_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (java.lang.String) ref);
          user_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>string user = 3;</code>
       * @param value The user to set.
       * @return This builder for chaining.
       */
      public Builder setUser(
          java.lang.String value) {
        if (value == null) { throw new NullPointerException(); }
        user_ = value;
        bitField0_ |= 0x00000004;
        onChanged();
        return this;
      }
      /**
       * <code>string user = 3;</code>
       * @return This builder for chaining.
       */
      public Builder clearUser() {
        user_ = getDefaultInstance().getUser();
        bitField0_ = (bitField0_ & ~0x00000004);
        onChanged();
        return this;
      }
      /**
       * <code>string user = 3;</code>
       * @param value The bytes for user to set.
       * @return This builder for chaining.
       */
      public Builder setUserBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) { throw new NullPointerException(); }
        checkByteStringIsUtf8(value);
        user_ = value;
        bitField0_ |= 0x00000004;
        onChanged();
        return this;
      }

      private java.lang.Object password_ = "";
      /**
       * <code>string password = 4;</code>
       * @return The password.
       */
      public java.lang.String getPassword() {
        java.lang.Object ref = password_;
        if (!(ref instanceof java.lang.String)) {
          com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
          java.lang.String s = bs.toStringUtf8();
          password_ = s;
          return s;
        } else {
          return (java.lang.String) ref;
        }
      }
      /**
       * <code>string password = 4;</code>
       * @return The bytes for password.
       */
      public com.google.protobuf.ByteString
          getPasswordBytes() {
        java.lang.Object ref = password_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (java.lang.String) ref);
          password_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>string password = 4;</code>
       * @param value The password to set.
       * @return This builder for chaining.
       */
      public Builder setPassword(
          java.lang.String value) {
        if (value == null) { throw new NullPointerException(); }
        password_ = value;
        bitField0_ |= 0x00000008;
        onChanged();
        return this;
      }
      /**
       * <code>string password = 4;</code>
       * @return This builder for chaining.
       */
      public Builder clearPassword() {
        password_ = getDefaultInstance().getPassword();
        bitField0_ = (bitField0_ & ~0x00000008);
        onChanged();
        return this;
      }
      /**
       * <code>string password = 4;</code>
       * @param value The bytes for password to set.
       * @return This builder for chaining.
       */
      public Builder setPasswordBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) { throw new NullPointerException(); }
        checkByteStringIsUtf8(value);
        password_ = value;
        bitField0_ |= 0x00000008;
        onChanged();
        return this;
      }

      private com.google.protobuf.MapField<
          java.lang.String, java.lang.String> attributes_;
      private com.google.protobuf.MapField<java.lang.String, java.lang.String>
          internalGetAttributes() {
        if (attributes_ == null) {
          return com.google.protobuf.MapField.emptyMapField(
              AttributesDefaultEntryHolder.defaultEntry);
        }
        return attributes_;
      }
      private com.google.protobuf.MapField<java.lang.String, java.lang.String>
          internalGetMutableAttributes() {
        if (attributes_ == null) {
          attributes_ = com.google.protobuf.MapField.newMapField(
              AttributesDefaultEntryHolder.defaultEntry);
        }
        if (!attributes_.isMutable()) {
          attributes_ = attributes_.copy();
        }
        bitField0_ |= 0x00000010;
        onChanged();
        return attributes_;
      }
      public int getAttributesCount() {
        return internalGetAttributes().getMap().size();
      }
      /**
       * <code>map&lt;string, string&gt; attributes = 5;</code>
       */
      @java.lang.Override
      public boolean containsAttributes(
          java.lang.String key) {
        if (key == null) { throw new NullPointerException("map key"); }
        return internalGetAttributes().getMap().containsKey(key);
      }
      /**
       * Use {@link #getAttributesMap()} instead.
       */
      @java.lang.Override
      @java.lang.Deprecated
      public java.util.Map<java.lang.String, java.lang.String> getAttributes() {
        return getAttributesMap();
      }
      /**
       * <code>map&lt;string, string&gt; attributes = 5;</code>
       */
      @java.lang.Override
      public java.util.Map<java.lang.String, java.lang.String> getAttributesMap() {
        return internalGetAttributes().getMap();
      }
      /**
       * <code>map&lt;string, string&gt; attributes = 5;</code>
       */
      @java.lang.Override
      public /* nullable */
java.lang.String getAttributesOrDefault(
          java.lang.String key,
          /* nullable */
java.lang.String defaultValue) {
        if (key == null) { throw new NullPointerException("map key"); }
        java.util.Map<java.lang.String, java.lang.String> map =
            internalGetAttributes().getMap();
        return map.containsKey(key) ? map.get(key) : defaultValue;
      }
      /**
       * <code>map&lt;string, string&gt; attributes = 5;</code>
       */
      @java.lang.Override
      public java.lang.String getAttributesOrThrow(
          java.lang.String key) {
        if (key == null) { throw new NullPointerException("map key"); }
        java.util.Map<java.lang.String, java.lang.String> map =
            internalGetAttributes().getMap();
        if (!map.containsKey(key)) {
          throw new java.lang.IllegalArgumentException();
        }
        return map.get(key);
      }
      public Builder clearAttributes() {
        bitField0_ = (bitField0_ & ~0x00000010);
        internalGetMutableAttributes().getMutableMap()
            .clear();
        return this;
      }
      /**
       * <code>map&lt;string, string&gt; attributes = 5;</code>
       */
      public Builder removeAttributes(
          java.lang.String key) {
        if (key == null) { throw new NullPointerException("map key"); }
        internalGetMutableAttributes().getMutableMap()
            .remove(key);
        return this;
      }
      /**
       * Use alternate mutation accessors instead.
       */
      @java.lang.Deprecated
      public java.util.Map<java.lang.String, java.lang.String>
          getMutableAttributes() {
        bitField0_ |= 0x00000010;
        return internalGetMutableAttributes().getMutableMap();
      }
      /**
       * <code>map&lt;string, string&gt; attributes = 5;</code>
       */
      public Builder putAttributes(
          java.lang.String key,
          java.lang.String value) {
        if (key == null) { throw new NullPointerException("map key"); }
        if (value == null) { throw new NullPointerException("map value"); }
        internalGetMutableAttributes().getMutableMap()
            .put(key, value);
        bitField0_ |= 0x00000010;
        return this;
      }
      /**
       * <code>map&lt;string, string&gt; attributes = 5;</code>
       */
      public Builder putAllAttributes(
          java.util.Map<java.lang.String, java.lang.String> values) {
        internalGetMutableAttributes().getMutableMap()
            .putAll(values);
        bitField0_ |= 0x00000010;
        return this;
      }
      @java.lang.Override
      public final Builder setUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.setUnknownFields(unknownFields);
      }

      @java.lang.Override
      public final Builder mergeUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.mergeUnknownFields(unknownFields);
      }


      // @@protoc_insertion_point(builder_scope:dominion.v1.ScvSetCredentialsRequest)
    }

    // @@protoc_insertion_point(class_scope:dominion.v1.ScvSetCredentialsRequest)
    private static final org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsRequest DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsRequest();
    }

    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsRequest getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<ScvSetCredentialsRequest>
        PARSER = new com.google.protobuf.AbstractParser<ScvSetCredentialsRequest>() {
      @java.lang.Override
      public ScvSetCredentialsRequest parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        Builder builder = newBuilder();
        try {
          builder.mergeFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          throw e.setUnfinishedMessage(builder.buildPartial());
        } catch (com.google.protobuf.UninitializedMessageException e) {
          throw e.asInvalidProtocolBufferException().setUnfinishedMessage(builder.buildPartial());
        } catch (java.io.IOException e) {
          throw new com.google.protobuf.InvalidProtocolBufferException(e)
              .setUnfinishedMessage(builder.buildPartial());
        }
        return builder.buildPartial();
      }
    };

    public static com.google.protobuf.Parser<ScvSetCredentialsRequest> parser() {
      return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<ScvSetCredentialsRequest> getParserForType() {
      return PARSER;
    }

    @java.lang.Override
    public org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsRequest getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  public interface ScvSetCredentialsResponseOrBuilder extends
      // @@protoc_insertion_point(interface_extends:dominion.v1.ScvSetCredentialsResponse)
      com.google.protobuf.MessageOrBuilder {
  }
  /**
   * Protobuf type {@code dominion.v1.ScvSetCredentialsResponse}
   */
  public static final class ScvSetCredentialsResponse extends
      com.google.protobuf.GeneratedMessageV3 implements
      // @@protoc_insertion_point(message_implements:dominion.v1.ScvSetCredentialsResponse)
      ScvSetCredentialsResponseOrBuilder {
  private static final long serialVersionUID = 0L;
    // Use ScvSetCredentialsResponse.newBuilder() to construct.
    private ScvSetCredentialsResponse(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
      super(builder);
    }
    private ScvSetCredentialsResponse() {
    }

    @java.lang.Override
    @SuppressWarnings({"unused"})
    protected java.lang.Object newInstance(
        UnusedPrivateParameter unused) {
      return new ScvSetCredentialsResponse();
    }

    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return org.opennms.dominion.local.rpc.grpc.DominionGrpc.internal_static_dominion_v1_ScvSetCredentialsResponse_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return org.opennms.dominion.local.rpc.grpc.DominionGrpc.internal_static_dominion_v1_ScvSetCredentialsResponse_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsResponse.class, org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsResponse.Builder.class);
    }

    private byte memoizedIsInitialized = -1;
    @java.lang.Override
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    @java.lang.Override
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      getUnknownFields().writeTo(output);
    }

    @java.lang.Override
    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      size += getUnknownFields().getSerializedSize();
      memoizedSize = size;
      return size;
    }

    @java.lang.Override
    public boolean equals(final java.lang.Object obj) {
      if (obj == this) {
       return true;
      }
      if (!(obj instanceof org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsResponse)) {
        return super.equals(obj);
      }
      org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsResponse other = (org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsResponse) obj;

      if (!getUnknownFields().equals(other.getUnknownFields())) return false;
      return true;
    }

    @java.lang.Override
    public int hashCode() {
      if (memoizedHashCode != 0) {
        return memoizedHashCode;
      }
      int hash = 41;
      hash = (19 * hash) + getDescriptor().hashCode();
      hash = (29 * hash) + getUnknownFields().hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsResponse parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsResponse parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsResponse parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsResponse parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsResponse parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsResponse parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsResponse parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsResponse parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsResponse parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input);
    }

    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsResponse parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsResponse parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsResponse parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    @java.lang.Override
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
    }
    public static Builder newBuilder(org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsResponse prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }
    @java.lang.Override
    public Builder toBuilder() {
      return this == DEFAULT_INSTANCE
          ? new Builder() : new Builder().mergeFrom(this);
    }

    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code dominion.v1.ScvSetCredentialsResponse}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:dominion.v1.ScvSetCredentialsResponse)
        org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsResponseOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return org.opennms.dominion.local.rpc.grpc.DominionGrpc.internal_static_dominion_v1_ScvSetCredentialsResponse_descriptor;
      }

      @java.lang.Override
      protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return org.opennms.dominion.local.rpc.grpc.DominionGrpc.internal_static_dominion_v1_ScvSetCredentialsResponse_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsResponse.class, org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsResponse.Builder.class);
      }

      // Construct using org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsResponse.newBuilder()
      private Builder() {

      }

      private Builder(
          com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
        super(parent);

      }
      @java.lang.Override
      public Builder clear() {
        super.clear();
        return this;
      }

      @java.lang.Override
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return org.opennms.dominion.local.rpc.grpc.DominionGrpc.internal_static_dominion_v1_ScvSetCredentialsResponse_descriptor;
      }

      @java.lang.Override
      public org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsResponse getDefaultInstanceForType() {
        return org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsResponse.getDefaultInstance();
      }

      @java.lang.Override
      public org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsResponse build() {
        org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsResponse result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      @java.lang.Override
      public org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsResponse buildPartial() {
        org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsResponse result = new org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsResponse(this);
        onBuilt();
        return result;
      }

      @java.lang.Override
      public Builder clone() {
        return super.clone();
      }
      @java.lang.Override
      public Builder setField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return super.setField(field, value);
      }
      @java.lang.Override
      public Builder clearField(
          com.google.protobuf.Descriptors.FieldDescriptor field) {
        return super.clearField(field);
      }
      @java.lang.Override
      public Builder clearOneof(
          com.google.protobuf.Descriptors.OneofDescriptor oneof) {
        return super.clearOneof(oneof);
      }
      @java.lang.Override
      public Builder setRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          int index, java.lang.Object value) {
        return super.setRepeatedField(field, index, value);
      }
      @java.lang.Override
      public Builder addRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return super.addRepeatedField(field, value);
      }
      @java.lang.Override
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsResponse) {
          return mergeFrom((org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsResponse)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsResponse other) {
        if (other == org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsResponse.getDefaultInstance()) return this;
        this.mergeUnknownFields(other.getUnknownFields());
        onChanged();
        return this;
      }

      @java.lang.Override
      public final boolean isInitialized() {
        return true;
      }

      @java.lang.Override
      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        if (extensionRegistry == null) {
          throw new java.lang.NullPointerException();
        }
        try {
          boolean done = false;
          while (!done) {
            int tag = input.readTag();
            switch (tag) {
              case 0:
                done = true;
                break;
              default: {
                if (!super.parseUnknownField(input, extensionRegistry, tag)) {
                  done = true; // was an endgroup tag
                }
                break;
              } // default:
            } // switch (tag)
          } // while (!done)
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          throw e.unwrapIOException();
        } finally {
          onChanged();
        } // finally
        return this;
      }
      @java.lang.Override
      public final Builder setUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.setUnknownFields(unknownFields);
      }

      @java.lang.Override
      public final Builder mergeUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.mergeUnknownFields(unknownFields);
      }


      // @@protoc_insertion_point(builder_scope:dominion.v1.ScvSetCredentialsResponse)
    }

    // @@protoc_insertion_point(class_scope:dominion.v1.ScvSetCredentialsResponse)
    private static final org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsResponse DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsResponse();
    }

    public static org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsResponse getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<ScvSetCredentialsResponse>
        PARSER = new com.google.protobuf.AbstractParser<ScvSetCredentialsResponse>() {
      @java.lang.Override
      public ScvSetCredentialsResponse parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        Builder builder = newBuilder();
        try {
          builder.mergeFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          throw e.setUnfinishedMessage(builder.buildPartial());
        } catch (com.google.protobuf.UninitializedMessageException e) {
          throw e.asInvalidProtocolBufferException().setUnfinishedMessage(builder.buildPartial());
        } catch (java.io.IOException e) {
          throw new com.google.protobuf.InvalidProtocolBufferException(e)
              .setUnfinishedMessage(builder.buildPartial());
        }
        return builder.buildPartial();
      }
    };

    public static com.google.protobuf.Parser<ScvSetCredentialsResponse> parser() {
      return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<ScvSetCredentialsResponse> getParserForType() {
      return PARSER;
    }

    @java.lang.Override
    public org.opennms.dominion.local.rpc.grpc.DominionGrpc.ScvSetCredentialsResponse getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_dominion_v1_ClientCredentials_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_dominion_v1_ClientCredentials_fieldAccessorTable;
  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_dominion_v1_ScvSupportedAliasesRequest_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_dominion_v1_ScvSupportedAliasesRequest_fieldAccessorTable;
  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_dominion_v1_ScvSupportedAliasesResponse_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_dominion_v1_ScvSupportedAliasesResponse_fieldAccessorTable;
  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_dominion_v1_ScvGetCredentialsRequest_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_dominion_v1_ScvGetCredentialsRequest_fieldAccessorTable;
  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_dominion_v1_ScvGetCredentialsResponse_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_dominion_v1_ScvGetCredentialsResponse_fieldAccessorTable;
  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_dominion_v1_ScvGetCredentialsResponse_AttributesEntry_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_dominion_v1_ScvGetCredentialsResponse_AttributesEntry_fieldAccessorTable;
  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_dominion_v1_ScvSetCredentialsRequest_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_dominion_v1_ScvSetCredentialsRequest_fieldAccessorTable;
  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_dominion_v1_ScvSetCredentialsRequest_AttributesEntry_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_dominion_v1_ScvSetCredentialsRequest_AttributesEntry_fieldAccessorTable;
  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_dominion_v1_ScvSetCredentialsResponse_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_dominion_v1_ScvSetCredentialsResponse_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\016dominion.proto\022\013dominion.v1\";\n\021ClientC" +
      "redentials\022\020\n\010clientId\030\001 \001(\t\022\024\n\014clientSe" +
      "cret\030\002 \001(\t\"W\n\032ScvSupportedAliasesRequest" +
      "\0229\n\021clientCredentials\030\001 \001(\0132\036.dominion.v" +
      "1.ClientCredentials\".\n\033ScvSupportedAlias" +
      "esResponse\022\017\n\007aliases\030\001 \003(\t\"d\n\030ScvGetCre" +
      "dentialsRequest\0229\n\021clientCredentials\030\001 \001" +
      "(\0132\036.dominion.v1.ClientCredentials\022\r\n\005al" +
      "ias\030\002 \001(\t\"\272\001\n\031ScvGetCredentialsResponse\022" +
      "\014\n\004user\030\001 \001(\t\022\020\n\010password\030\002 \001(\t\022J\n\nattri" +
      "butes\030\003 \003(\01326.dominion.v1.ScvGetCredenti" +
      "alsResponse.AttributesEntry\0321\n\017Attribute" +
      "sEntry\022\013\n\003key\030\001 \001(\t\022\r\n\005value\030\002 \001(\t:\0028\001\"\202" +
      "\002\n\030ScvSetCredentialsRequest\0229\n\021clientCre" +
      "dentials\030\001 \001(\0132\036.dominion.v1.ClientCrede" +
      "ntials\022\r\n\005alias\030\002 \001(\t\022\014\n\004user\030\003 \001(\t\022\020\n\010p" +
      "assword\030\004 \001(\t\022I\n\nattributes\030\005 \003(\01325.domi" +
      "nion.v1.ScvSetCredentialsRequest.Attribu" +
      "tesEntry\0321\n\017AttributesEntry\022\013\n\003key\030\001 \001(\t" +
      "\022\r\n\005value\030\002 \001(\t:\0028\001\"\033\n\031ScvSetCredentials" +
      "Response2\315\002\n\026SecureCredentialsVault\022k\n\026S" +
      "cvGetSupportedAliases\022\'.dominion.v1.ScvS" +
      "upportedAliasesRequest\032(.dominion.v1.Scv" +
      "SupportedAliasesResponse\022b\n\021ScvGetCreden" +
      "tials\022%.dominion.v1.ScvGetCredentialsReq" +
      "uest\032&.dominion.v1.ScvGetCredentialsResp" +
      "onse\022b\n\021ScvSetCredentials\022%.dominion.v1." +
      "ScvSetCredentialsRequest\032&.dominion.v1.S" +
      "cvSetCredentialsResponseB3\n#org.opennms." +
      "dominion.local.rpc.grpcB\014DominionGrpcb\006p" +
      "roto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        });
    internal_static_dominion_v1_ClientCredentials_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_dominion_v1_ClientCredentials_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_dominion_v1_ClientCredentials_descriptor,
        new java.lang.String[] { "ClientId", "ClientSecret", });
    internal_static_dominion_v1_ScvSupportedAliasesRequest_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_dominion_v1_ScvSupportedAliasesRequest_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_dominion_v1_ScvSupportedAliasesRequest_descriptor,
        new java.lang.String[] { "ClientCredentials", });
    internal_static_dominion_v1_ScvSupportedAliasesResponse_descriptor =
      getDescriptor().getMessageTypes().get(2);
    internal_static_dominion_v1_ScvSupportedAliasesResponse_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_dominion_v1_ScvSupportedAliasesResponse_descriptor,
        new java.lang.String[] { "Aliases", });
    internal_static_dominion_v1_ScvGetCredentialsRequest_descriptor =
      getDescriptor().getMessageTypes().get(3);
    internal_static_dominion_v1_ScvGetCredentialsRequest_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_dominion_v1_ScvGetCredentialsRequest_descriptor,
        new java.lang.String[] { "ClientCredentials", "Alias", });
    internal_static_dominion_v1_ScvGetCredentialsResponse_descriptor =
      getDescriptor().getMessageTypes().get(4);
    internal_static_dominion_v1_ScvGetCredentialsResponse_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_dominion_v1_ScvGetCredentialsResponse_descriptor,
        new java.lang.String[] { "User", "Password", "Attributes", });
    internal_static_dominion_v1_ScvGetCredentialsResponse_AttributesEntry_descriptor =
      internal_static_dominion_v1_ScvGetCredentialsResponse_descriptor.getNestedTypes().get(0);
    internal_static_dominion_v1_ScvGetCredentialsResponse_AttributesEntry_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_dominion_v1_ScvGetCredentialsResponse_AttributesEntry_descriptor,
        new java.lang.String[] { "Key", "Value", });
    internal_static_dominion_v1_ScvSetCredentialsRequest_descriptor =
      getDescriptor().getMessageTypes().get(5);
    internal_static_dominion_v1_ScvSetCredentialsRequest_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_dominion_v1_ScvSetCredentialsRequest_descriptor,
        new java.lang.String[] { "ClientCredentials", "Alias", "User", "Password", "Attributes", });
    internal_static_dominion_v1_ScvSetCredentialsRequest_AttributesEntry_descriptor =
      internal_static_dominion_v1_ScvSetCredentialsRequest_descriptor.getNestedTypes().get(0);
    internal_static_dominion_v1_ScvSetCredentialsRequest_AttributesEntry_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_dominion_v1_ScvSetCredentialsRequest_AttributesEntry_descriptor,
        new java.lang.String[] { "Key", "Value", });
    internal_static_dominion_v1_ScvSetCredentialsResponse_descriptor =
      getDescriptor().getMessageTypes().get(6);
    internal_static_dominion_v1_ScvSetCredentialsResponse_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_dominion_v1_ScvSetCredentialsResponse_descriptor,
        new java.lang.String[] { });
  }

  // @@protoc_insertion_point(outer_class_scope)
}
