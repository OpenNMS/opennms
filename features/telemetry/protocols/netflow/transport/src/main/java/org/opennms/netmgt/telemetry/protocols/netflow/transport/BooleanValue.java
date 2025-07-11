/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: netflow.proto

// Protobuf Java Version: 3.25.5
package org.opennms.netmgt.telemetry.protocols.netflow.transport;

/**
 * Protobuf type {@code BooleanValue}
 */
public final class BooleanValue extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:BooleanValue)
    BooleanValueOrBuilder {
private static final long serialVersionUID = 0L;
  // Use BooleanValue.newBuilder() to construct.
  private BooleanValue(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private BooleanValue() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new BooleanValue();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowProtos.internal_static_BooleanValue_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowProtos.internal_static_BooleanValue_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            org.opennms.netmgt.telemetry.protocols.netflow.transport.BooleanValue.class, org.opennms.netmgt.telemetry.protocols.netflow.transport.BooleanValue.Builder.class);
  }

  private int bitField0_;
  public static final int BOOL_FIELD_NUMBER = 1;
  private com.google.protobuf.BoolValue bool_;
  /**
   * <code>.google.protobuf.BoolValue bool = 1;</code>
   * @return Whether the bool field is set.
   */
  @java.lang.Override
  public boolean hasBool() {
    return ((bitField0_ & 0x00000001) != 0);
  }
  /**
   * <code>.google.protobuf.BoolValue bool = 1;</code>
   * @return The bool.
   */
  @java.lang.Override
  public com.google.protobuf.BoolValue getBool() {
    return bool_ == null ? com.google.protobuf.BoolValue.getDefaultInstance() : bool_;
  }
  /**
   * <code>.google.protobuf.BoolValue bool = 1;</code>
   */
  @java.lang.Override
  public com.google.protobuf.BoolValueOrBuilder getBoolOrBuilder() {
    return bool_ == null ? com.google.protobuf.BoolValue.getDefaultInstance() : bool_;
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
      output.writeMessage(1, getBool());
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
        .computeMessageSize(1, getBool());
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
    if (!(obj instanceof org.opennms.netmgt.telemetry.protocols.netflow.transport.BooleanValue)) {
      return super.equals(obj);
    }
    org.opennms.netmgt.telemetry.protocols.netflow.transport.BooleanValue other = (org.opennms.netmgt.telemetry.protocols.netflow.transport.BooleanValue) obj;

    if (hasBool() != other.hasBool()) return false;
    if (hasBool()) {
      if (!getBool()
          .equals(other.getBool())) return false;
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
    if (hasBool()) {
      hash = (37 * hash) + BOOL_FIELD_NUMBER;
      hash = (53 * hash) + getBool().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.BooleanValue parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.BooleanValue parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.BooleanValue parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.BooleanValue parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.BooleanValue parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.BooleanValue parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.BooleanValue parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.BooleanValue parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.BooleanValue parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.BooleanValue parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.BooleanValue parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.BooleanValue parseFrom(
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
  public static Builder newBuilder(org.opennms.netmgt.telemetry.protocols.netflow.transport.BooleanValue prototype) {
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
   * Protobuf type {@code BooleanValue}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:BooleanValue)
      org.opennms.netmgt.telemetry.protocols.netflow.transport.BooleanValueOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowProtos.internal_static_BooleanValue_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowProtos.internal_static_BooleanValue_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              org.opennms.netmgt.telemetry.protocols.netflow.transport.BooleanValue.class, org.opennms.netmgt.telemetry.protocols.netflow.transport.BooleanValue.Builder.class);
    }

    // Construct using org.opennms.netmgt.telemetry.protocols.netflow.transport.BooleanValue.newBuilder()
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
        getBoolFieldBuilder();
      }
    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      bitField0_ = 0;
      bool_ = null;
      if (boolBuilder_ != null) {
        boolBuilder_.dispose();
        boolBuilder_ = null;
      }
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowProtos.internal_static_BooleanValue_descriptor;
    }

    @java.lang.Override
    public org.opennms.netmgt.telemetry.protocols.netflow.transport.BooleanValue getDefaultInstanceForType() {
      return org.opennms.netmgt.telemetry.protocols.netflow.transport.BooleanValue.getDefaultInstance();
    }

    @java.lang.Override
    public org.opennms.netmgt.telemetry.protocols.netflow.transport.BooleanValue build() {
      org.opennms.netmgt.telemetry.protocols.netflow.transport.BooleanValue result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public org.opennms.netmgt.telemetry.protocols.netflow.transport.BooleanValue buildPartial() {
      org.opennms.netmgt.telemetry.protocols.netflow.transport.BooleanValue result = new org.opennms.netmgt.telemetry.protocols.netflow.transport.BooleanValue(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(org.opennms.netmgt.telemetry.protocols.netflow.transport.BooleanValue result) {
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.bool_ = boolBuilder_ == null
            ? bool_
            : boolBuilder_.build();
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
      if (other instanceof org.opennms.netmgt.telemetry.protocols.netflow.transport.BooleanValue) {
        return mergeFrom((org.opennms.netmgt.telemetry.protocols.netflow.transport.BooleanValue)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(org.opennms.netmgt.telemetry.protocols.netflow.transport.BooleanValue other) {
      if (other == org.opennms.netmgt.telemetry.protocols.netflow.transport.BooleanValue.getDefaultInstance()) return this;
      if (other.hasBool()) {
        mergeBool(other.getBool());
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
                  getBoolFieldBuilder().getBuilder(),
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

    private com.google.protobuf.BoolValue bool_;
    private com.google.protobuf.SingleFieldBuilderV3<
        com.google.protobuf.BoolValue, com.google.protobuf.BoolValue.Builder, com.google.protobuf.BoolValueOrBuilder> boolBuilder_;
    /**
     * <code>.google.protobuf.BoolValue bool = 1;</code>
     * @return Whether the bool field is set.
     */
    public boolean hasBool() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <code>.google.protobuf.BoolValue bool = 1;</code>
     * @return The bool.
     */
    public com.google.protobuf.BoolValue getBool() {
      if (boolBuilder_ == null) {
        return bool_ == null ? com.google.protobuf.BoolValue.getDefaultInstance() : bool_;
      } else {
        return boolBuilder_.getMessage();
      }
    }
    /**
     * <code>.google.protobuf.BoolValue bool = 1;</code>
     */
    public Builder setBool(com.google.protobuf.BoolValue value) {
      if (boolBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        bool_ = value;
      } else {
        boolBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.google.protobuf.BoolValue bool = 1;</code>
     */
    public Builder setBool(
        com.google.protobuf.BoolValue.Builder builderForValue) {
      if (boolBuilder_ == null) {
        bool_ = builderForValue.build();
      } else {
        boolBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.google.protobuf.BoolValue bool = 1;</code>
     */
    public Builder mergeBool(com.google.protobuf.BoolValue value) {
      if (boolBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0) &&
          bool_ != null &&
          bool_ != com.google.protobuf.BoolValue.getDefaultInstance()) {
          getBoolBuilder().mergeFrom(value);
        } else {
          bool_ = value;
        }
      } else {
        boolBuilder_.mergeFrom(value);
      }
      if (bool_ != null) {
        bitField0_ |= 0x00000001;
        onChanged();
      }
      return this;
    }
    /**
     * <code>.google.protobuf.BoolValue bool = 1;</code>
     */
    public Builder clearBool() {
      bitField0_ = (bitField0_ & ~0x00000001);
      bool_ = null;
      if (boolBuilder_ != null) {
        boolBuilder_.dispose();
        boolBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <code>.google.protobuf.BoolValue bool = 1;</code>
     */
    public com.google.protobuf.BoolValue.Builder getBoolBuilder() {
      bitField0_ |= 0x00000001;
      onChanged();
      return getBoolFieldBuilder().getBuilder();
    }
    /**
     * <code>.google.protobuf.BoolValue bool = 1;</code>
     */
    public com.google.protobuf.BoolValueOrBuilder getBoolOrBuilder() {
      if (boolBuilder_ != null) {
        return boolBuilder_.getMessageOrBuilder();
      } else {
        return bool_ == null ?
            com.google.protobuf.BoolValue.getDefaultInstance() : bool_;
      }
    }
    /**
     * <code>.google.protobuf.BoolValue bool = 1;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        com.google.protobuf.BoolValue, com.google.protobuf.BoolValue.Builder, com.google.protobuf.BoolValueOrBuilder> 
        getBoolFieldBuilder() {
      if (boolBuilder_ == null) {
        boolBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.BoolValue, com.google.protobuf.BoolValue.Builder, com.google.protobuf.BoolValueOrBuilder>(
                getBool(),
                getParentForChildren(),
                isClean());
        bool_ = null;
      }
      return boolBuilder_;
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


    // @@protoc_insertion_point(builder_scope:BooleanValue)
  }

  // @@protoc_insertion_point(class_scope:BooleanValue)
  private static final org.opennms.netmgt.telemetry.protocols.netflow.transport.BooleanValue DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new org.opennms.netmgt.telemetry.protocols.netflow.transport.BooleanValue();
  }

  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.BooleanValue getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<BooleanValue>
      PARSER = new com.google.protobuf.AbstractParser<BooleanValue>() {
    @java.lang.Override
    public BooleanValue parsePartialFrom(
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

  public static com.google.protobuf.Parser<BooleanValue> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<BooleanValue> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public org.opennms.netmgt.telemetry.protocols.netflow.transport.BooleanValue getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

