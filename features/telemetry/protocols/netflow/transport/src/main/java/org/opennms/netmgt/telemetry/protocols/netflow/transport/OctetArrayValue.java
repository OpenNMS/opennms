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
 * Protobuf type {@code OctetArrayValue}
 */
public final class OctetArrayValue extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:OctetArrayValue)
    OctetArrayValueOrBuilder {
private static final long serialVersionUID = 0L;
  // Use OctetArrayValue.newBuilder() to construct.
  private OctetArrayValue(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private OctetArrayValue() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new OctetArrayValue();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowProtos.internal_static_OctetArrayValue_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowProtos.internal_static_OctetArrayValue_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            org.opennms.netmgt.telemetry.protocols.netflow.transport.OctetArrayValue.class, org.opennms.netmgt.telemetry.protocols.netflow.transport.OctetArrayValue.Builder.class);
  }

  public static final int BYTES_FIELD_NUMBER = 1;
  private com.google.protobuf.BytesValue bytes_;
  /**
   * <code>.google.protobuf.BytesValue bytes = 1;</code>
   * @return Whether the bytes field is set.
   */
  @java.lang.Override
  public boolean hasBytes() {
    return bytes_ != null;
  }
  /**
   * <code>.google.protobuf.BytesValue bytes = 1;</code>
   * @return The bytes.
   */
  @java.lang.Override
  public com.google.protobuf.BytesValue getBytes() {
    return bytes_ == null ? com.google.protobuf.BytesValue.getDefaultInstance() : bytes_;
  }
  /**
   * <code>.google.protobuf.BytesValue bytes = 1;</code>
   */
  @java.lang.Override
  public com.google.protobuf.BytesValueOrBuilder getBytesOrBuilder() {
    return getBytes();
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
    if (bytes_ != null) {
      output.writeMessage(1, getBytes());
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (bytes_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, getBytes());
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
    if (!(obj instanceof org.opennms.netmgt.telemetry.protocols.netflow.transport.OctetArrayValue)) {
      return super.equals(obj);
    }
    org.opennms.netmgt.telemetry.protocols.netflow.transport.OctetArrayValue other = (org.opennms.netmgt.telemetry.protocols.netflow.transport.OctetArrayValue) obj;

    if (hasBytes() != other.hasBytes()) return false;
    if (hasBytes()) {
      if (!getBytes()
          .equals(other.getBytes())) return false;
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
    if (hasBytes()) {
      hash = (37 * hash) + BYTES_FIELD_NUMBER;
      hash = (53 * hash) + getBytes().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.OctetArrayValue parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.OctetArrayValue parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.OctetArrayValue parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.OctetArrayValue parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.OctetArrayValue parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.OctetArrayValue parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.OctetArrayValue parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.OctetArrayValue parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.OctetArrayValue parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.OctetArrayValue parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.OctetArrayValue parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.OctetArrayValue parseFrom(
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
  public static Builder newBuilder(org.opennms.netmgt.telemetry.protocols.netflow.transport.OctetArrayValue prototype) {
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
   * Protobuf type {@code OctetArrayValue}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:OctetArrayValue)
      org.opennms.netmgt.telemetry.protocols.netflow.transport.OctetArrayValueOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowProtos.internal_static_OctetArrayValue_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowProtos.internal_static_OctetArrayValue_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              org.opennms.netmgt.telemetry.protocols.netflow.transport.OctetArrayValue.class, org.opennms.netmgt.telemetry.protocols.netflow.transport.OctetArrayValue.Builder.class);
    }

    // Construct using org.opennms.netmgt.telemetry.protocols.netflow.transport.OctetArrayValue.newBuilder()
    private Builder() {

    }

    private Builder(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);

    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      if (bytesBuilder_ == null) {
        bytes_ = null;
      } else {
        bytes_ = null;
        bytesBuilder_ = null;
      }
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowProtos.internal_static_OctetArrayValue_descriptor;
    }

    @java.lang.Override
    public org.opennms.netmgt.telemetry.protocols.netflow.transport.OctetArrayValue getDefaultInstanceForType() {
      return org.opennms.netmgt.telemetry.protocols.netflow.transport.OctetArrayValue.getDefaultInstance();
    }

    @java.lang.Override
    public org.opennms.netmgt.telemetry.protocols.netflow.transport.OctetArrayValue build() {
      org.opennms.netmgt.telemetry.protocols.netflow.transport.OctetArrayValue result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public org.opennms.netmgt.telemetry.protocols.netflow.transport.OctetArrayValue buildPartial() {
      org.opennms.netmgt.telemetry.protocols.netflow.transport.OctetArrayValue result = new org.opennms.netmgt.telemetry.protocols.netflow.transport.OctetArrayValue(this);
      if (bytesBuilder_ == null) {
        result.bytes_ = bytes_;
      } else {
        result.bytes_ = bytesBuilder_.build();
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
      if (other instanceof org.opennms.netmgt.telemetry.protocols.netflow.transport.OctetArrayValue) {
        return mergeFrom((org.opennms.netmgt.telemetry.protocols.netflow.transport.OctetArrayValue)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(org.opennms.netmgt.telemetry.protocols.netflow.transport.OctetArrayValue other) {
      if (other == org.opennms.netmgt.telemetry.protocols.netflow.transport.OctetArrayValue.getDefaultInstance()) return this;
      if (other.hasBytes()) {
        mergeBytes(other.getBytes());
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
                  getBytesFieldBuilder().getBuilder(),
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

    private com.google.protobuf.BytesValue bytes_;
    private com.google.protobuf.SingleFieldBuilderV3<
        com.google.protobuf.BytesValue, com.google.protobuf.BytesValue.Builder, com.google.protobuf.BytesValueOrBuilder> bytesBuilder_;
    /**
     * <code>.google.protobuf.BytesValue bytes = 1;</code>
     * @return Whether the bytes field is set.
     */
    public boolean hasBytes() {
      return bytesBuilder_ != null || bytes_ != null;
    }
    /**
     * <code>.google.protobuf.BytesValue bytes = 1;</code>
     * @return The bytes.
     */
    public com.google.protobuf.BytesValue getBytes() {
      if (bytesBuilder_ == null) {
        return bytes_ == null ? com.google.protobuf.BytesValue.getDefaultInstance() : bytes_;
      } else {
        return bytesBuilder_.getMessage();
      }
    }
    /**
     * <code>.google.protobuf.BytesValue bytes = 1;</code>
     */
    public Builder setBytes(com.google.protobuf.BytesValue value) {
      if (bytesBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        bytes_ = value;
        onChanged();
      } else {
        bytesBuilder_.setMessage(value);
      }

      return this;
    }
    /**
     * <code>.google.protobuf.BytesValue bytes = 1;</code>
     */
    public Builder setBytes(
        com.google.protobuf.BytesValue.Builder builderForValue) {
      if (bytesBuilder_ == null) {
        bytes_ = builderForValue.build();
        onChanged();
      } else {
        bytesBuilder_.setMessage(builderForValue.build());
      }

      return this;
    }
    /**
     * <code>.google.protobuf.BytesValue bytes = 1;</code>
     */
    public Builder mergeBytes(com.google.protobuf.BytesValue value) {
      if (bytesBuilder_ == null) {
        if (bytes_ != null) {
          bytes_ =
            com.google.protobuf.BytesValue.newBuilder(bytes_).mergeFrom(value).buildPartial();
        } else {
          bytes_ = value;
        }
        onChanged();
      } else {
        bytesBuilder_.mergeFrom(value);
      }

      return this;
    }
    /**
     * <code>.google.protobuf.BytesValue bytes = 1;</code>
     */
    public Builder clearBytes() {
      if (bytesBuilder_ == null) {
        bytes_ = null;
        onChanged();
      } else {
        bytes_ = null;
        bytesBuilder_ = null;
      }

      return this;
    }
    /**
     * <code>.google.protobuf.BytesValue bytes = 1;</code>
     */
    public com.google.protobuf.BytesValue.Builder getBytesBuilder() {
      
      onChanged();
      return getBytesFieldBuilder().getBuilder();
    }
    /**
     * <code>.google.protobuf.BytesValue bytes = 1;</code>
     */
    public com.google.protobuf.BytesValueOrBuilder getBytesOrBuilder() {
      if (bytesBuilder_ != null) {
        return bytesBuilder_.getMessageOrBuilder();
      } else {
        return bytes_ == null ?
            com.google.protobuf.BytesValue.getDefaultInstance() : bytes_;
      }
    }
    /**
     * <code>.google.protobuf.BytesValue bytes = 1;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        com.google.protobuf.BytesValue, com.google.protobuf.BytesValue.Builder, com.google.protobuf.BytesValueOrBuilder> 
        getBytesFieldBuilder() {
      if (bytesBuilder_ == null) {
        bytesBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.BytesValue, com.google.protobuf.BytesValue.Builder, com.google.protobuf.BytesValueOrBuilder>(
                getBytes(),
                getParentForChildren(),
                isClean());
        bytes_ = null;
      }
      return bytesBuilder_;
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


    // @@protoc_insertion_point(builder_scope:OctetArrayValue)
  }

  // @@protoc_insertion_point(class_scope:OctetArrayValue)
  private static final org.opennms.netmgt.telemetry.protocols.netflow.transport.OctetArrayValue DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new org.opennms.netmgt.telemetry.protocols.netflow.transport.OctetArrayValue();
  }

  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.OctetArrayValue getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<OctetArrayValue>
      PARSER = new com.google.protobuf.AbstractParser<OctetArrayValue>() {
    @java.lang.Override
    public OctetArrayValue parsePartialFrom(
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

  public static com.google.protobuf.Parser<OctetArrayValue> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<OctetArrayValue> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public org.opennms.netmgt.telemetry.protocols.netflow.transport.OctetArrayValue getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

