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
package org.opennms.netmgt.telemetry.protocols.netflow.transport;

/**
 * Protobuf type {@code SignedValue}
 */
public final class SignedValue extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:SignedValue)
    SignedValueOrBuilder {
private static final long serialVersionUID = 0L;
  // Use SignedValue.newBuilder() to construct.
  private SignedValue(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private SignedValue() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new SignedValue();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowProtos.internal_static_SignedValue_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowProtos.internal_static_SignedValue_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            org.opennms.netmgt.telemetry.protocols.netflow.transport.SignedValue.class, org.opennms.netmgt.telemetry.protocols.netflow.transport.SignedValue.Builder.class);
  }

  public static final int INT64_FIELD_NUMBER = 1;
  private com.google.protobuf.Int64Value int64_;
  /**
   * <code>.google.protobuf.Int64Value int64 = 1;</code>
   * @return Whether the int64 field is set.
   */
  @java.lang.Override
  public boolean hasInt64() {
    return int64_ != null;
  }
  /**
   * <code>.google.protobuf.Int64Value int64 = 1;</code>
   * @return The int64.
   */
  @java.lang.Override
  public com.google.protobuf.Int64Value getInt64() {
    return int64_ == null ? com.google.protobuf.Int64Value.getDefaultInstance() : int64_;
  }
  /**
   * <code>.google.protobuf.Int64Value int64 = 1;</code>
   */
  @java.lang.Override
  public com.google.protobuf.Int64ValueOrBuilder getInt64OrBuilder() {
    return getInt64();
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
    if (int64_ != null) {
      output.writeMessage(1, getInt64());
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (int64_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, getInt64());
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
    if (!(obj instanceof org.opennms.netmgt.telemetry.protocols.netflow.transport.SignedValue)) {
      return super.equals(obj);
    }
    org.opennms.netmgt.telemetry.protocols.netflow.transport.SignedValue other = (org.opennms.netmgt.telemetry.protocols.netflow.transport.SignedValue) obj;

    if (hasInt64() != other.hasInt64()) return false;
    if (hasInt64()) {
      if (!getInt64()
          .equals(other.getInt64())) return false;
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
    if (hasInt64()) {
      hash = (37 * hash) + INT64_FIELD_NUMBER;
      hash = (53 * hash) + getInt64().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.SignedValue parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.SignedValue parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.SignedValue parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.SignedValue parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.SignedValue parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.SignedValue parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.SignedValue parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.SignedValue parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.SignedValue parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.SignedValue parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.SignedValue parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.SignedValue parseFrom(
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
  public static Builder newBuilder(org.opennms.netmgt.telemetry.protocols.netflow.transport.SignedValue prototype) {
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
   * Protobuf type {@code SignedValue}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:SignedValue)
      org.opennms.netmgt.telemetry.protocols.netflow.transport.SignedValueOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowProtos.internal_static_SignedValue_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowProtos.internal_static_SignedValue_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              org.opennms.netmgt.telemetry.protocols.netflow.transport.SignedValue.class, org.opennms.netmgt.telemetry.protocols.netflow.transport.SignedValue.Builder.class);
    }

    // Construct using org.opennms.netmgt.telemetry.protocols.netflow.transport.SignedValue.newBuilder()
    private Builder() {

    }

    private Builder(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);

    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      if (int64Builder_ == null) {
        int64_ = null;
      } else {
        int64_ = null;
        int64Builder_ = null;
      }
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowProtos.internal_static_SignedValue_descriptor;
    }

    @java.lang.Override
    public org.opennms.netmgt.telemetry.protocols.netflow.transport.SignedValue getDefaultInstanceForType() {
      return org.opennms.netmgt.telemetry.protocols.netflow.transport.SignedValue.getDefaultInstance();
    }

    @java.lang.Override
    public org.opennms.netmgt.telemetry.protocols.netflow.transport.SignedValue build() {
      org.opennms.netmgt.telemetry.protocols.netflow.transport.SignedValue result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public org.opennms.netmgt.telemetry.protocols.netflow.transport.SignedValue buildPartial() {
      org.opennms.netmgt.telemetry.protocols.netflow.transport.SignedValue result = new org.opennms.netmgt.telemetry.protocols.netflow.transport.SignedValue(this);
      if (int64Builder_ == null) {
        result.int64_ = int64_;
      } else {
        result.int64_ = int64Builder_.build();
      }
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
      if (other instanceof org.opennms.netmgt.telemetry.protocols.netflow.transport.SignedValue) {
        return mergeFrom((org.opennms.netmgt.telemetry.protocols.netflow.transport.SignedValue)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(org.opennms.netmgt.telemetry.protocols.netflow.transport.SignedValue other) {
      if (other == org.opennms.netmgt.telemetry.protocols.netflow.transport.SignedValue.getDefaultInstance()) return this;
      if (other.hasInt64()) {
        mergeInt64(other.getInt64());
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
                  getInt64FieldBuilder().getBuilder(),
                  extensionRegistry);

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

    private com.google.protobuf.Int64Value int64_;
    private com.google.protobuf.SingleFieldBuilderV3<
        com.google.protobuf.Int64Value, com.google.protobuf.Int64Value.Builder, com.google.protobuf.Int64ValueOrBuilder> int64Builder_;
    /**
     * <code>.google.protobuf.Int64Value int64 = 1;</code>
     * @return Whether the int64 field is set.
     */
    public boolean hasInt64() {
      return int64Builder_ != null || int64_ != null;
    }
    /**
     * <code>.google.protobuf.Int64Value int64 = 1;</code>
     * @return The int64.
     */
    public com.google.protobuf.Int64Value getInt64() {
      if (int64Builder_ == null) {
        return int64_ == null ? com.google.protobuf.Int64Value.getDefaultInstance() : int64_;
      } else {
        return int64Builder_.getMessage();
      }
    }
    /**
     * <code>.google.protobuf.Int64Value int64 = 1;</code>
     */
    public Builder setInt64(com.google.protobuf.Int64Value value) {
      if (int64Builder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        int64_ = value;
        onChanged();
      } else {
        int64Builder_.setMessage(value);
      }

      return this;
    }
    /**
     * <code>.google.protobuf.Int64Value int64 = 1;</code>
     */
    public Builder setInt64(
        com.google.protobuf.Int64Value.Builder builderForValue) {
      if (int64Builder_ == null) {
        int64_ = builderForValue.build();
        onChanged();
      } else {
        int64Builder_.setMessage(builderForValue.build());
      }

      return this;
    }
    /**
     * <code>.google.protobuf.Int64Value int64 = 1;</code>
     */
    public Builder mergeInt64(com.google.protobuf.Int64Value value) {
      if (int64Builder_ == null) {
        if (int64_ != null) {
          int64_ =
            com.google.protobuf.Int64Value.newBuilder(int64_).mergeFrom(value).buildPartial();
        } else {
          int64_ = value;
        }
        onChanged();
      } else {
        int64Builder_.mergeFrom(value);
      }

      return this;
    }
    /**
     * <code>.google.protobuf.Int64Value int64 = 1;</code>
     */
    public Builder clearInt64() {
      if (int64Builder_ == null) {
        int64_ = null;
        onChanged();
      } else {
        int64_ = null;
        int64Builder_ = null;
      }

      return this;
    }
    /**
     * <code>.google.protobuf.Int64Value int64 = 1;</code>
     */
    public com.google.protobuf.Int64Value.Builder getInt64Builder() {
      
      onChanged();
      return getInt64FieldBuilder().getBuilder();
    }
    /**
     * <code>.google.protobuf.Int64Value int64 = 1;</code>
     */
    public com.google.protobuf.Int64ValueOrBuilder getInt64OrBuilder() {
      if (int64Builder_ != null) {
        return int64Builder_.getMessageOrBuilder();
      } else {
        return int64_ == null ?
            com.google.protobuf.Int64Value.getDefaultInstance() : int64_;
      }
    }
    /**
     * <code>.google.protobuf.Int64Value int64 = 1;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        com.google.protobuf.Int64Value, com.google.protobuf.Int64Value.Builder, com.google.protobuf.Int64ValueOrBuilder> 
        getInt64FieldBuilder() {
      if (int64Builder_ == null) {
        int64Builder_ = new com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.Int64Value, com.google.protobuf.Int64Value.Builder, com.google.protobuf.Int64ValueOrBuilder>(
                getInt64(),
                getParentForChildren(),
                isClean());
        int64_ = null;
      }
      return int64Builder_;
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


    // @@protoc_insertion_point(builder_scope:SignedValue)
  }

  // @@protoc_insertion_point(class_scope:SignedValue)
  private static final org.opennms.netmgt.telemetry.protocols.netflow.transport.SignedValue DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new org.opennms.netmgt.telemetry.protocols.netflow.transport.SignedValue();
  }

  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.SignedValue getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<SignedValue>
      PARSER = new com.google.protobuf.AbstractParser<SignedValue>() {
    @java.lang.Override
    public SignedValue parsePartialFrom(
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

  public static com.google.protobuf.Parser<SignedValue> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<SignedValue> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public org.opennms.netmgt.telemetry.protocols.netflow.transport.SignedValue getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

