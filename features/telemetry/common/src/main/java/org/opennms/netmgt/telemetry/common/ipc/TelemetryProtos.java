/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.common.ipc;

import org.opennms.core.ipc.sink.api.Message;
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLogEntry;

public final class TelemetryProtos {
  private TelemetryProtos() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  public interface TelemetryMessageOrBuilder extends
      // @@protoc_insertion_point(interface_extends:TelemetryMessage)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <code>required uint64 timestamp = 1;</code>
     * @return Whether the timestamp field is set.
     */
    boolean hasTimestamp();
    /**
     * <code>required uint64 timestamp = 1;</code>
     * @return The timestamp.
     */
    long getTimestamp();

    /**
     * <code>required bytes bytes = 2;</code>
     * @return Whether the bytes field is set.
     */
    boolean hasBytes();
    /**
     * <code>required bytes bytes = 2;</code>
     * @return The bytes.
     */
    com.google.protobuf.ByteString getBytes();
  }
  /**
   * Protobuf type {@code TelemetryMessage}
   */
  public static final class TelemetryMessage extends
      com.google.protobuf.GeneratedMessageV3 implements
      // @@protoc_insertion_point(message_implements:TelemetryMessage)
      TelemetryMessageOrBuilder, TelemetryMessageLogEntry {
  private static final long serialVersionUID = 0L;
    // Use TelemetryMessage.newBuilder() to construct.
    private TelemetryMessage(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
      super(builder);
    }
    private TelemetryMessage() {
      bytes_ = com.google.protobuf.ByteString.EMPTY;
    }

    @java.lang.Override
    @SuppressWarnings({"unused"})
    protected java.lang.Object newInstance(
        UnusedPrivateParameter unused) {
      return new TelemetryMessage();
    }

    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return this.unknownFields;
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.internal_static_TelemetryMessage_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.internal_static_TelemetryMessage_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage.class, org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage.Builder.class);
    }

    private int bitField0_;
    public static final int TIMESTAMP_FIELD_NUMBER = 1;
    private long timestamp_;
    /**
     * <code>required uint64 timestamp = 1;</code>
     * @return Whether the timestamp field is set.
     */
    @java.lang.Override
    public boolean hasTimestamp() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <code>required uint64 timestamp = 1;</code>
     * @return The timestamp.
     */
    @java.lang.Override
    public long getTimestamp() {
      return timestamp_;
    }

    public static final int BYTES_FIELD_NUMBER = 2;
    private com.google.protobuf.ByteString bytes_;
    /**
     * <code>required bytes bytes = 2;</code>
     * @return Whether the bytes field is set.
     */
    @java.lang.Override
    public boolean hasBytes() {
      return ((bitField0_ & 0x00000002) != 0);
    }
    /**
     * <code>required bytes bytes = 2;</code>
     * @return The bytes.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString getBytes() {
      return bytes_;
    }

    public byte[] getByteArray() {
      return bytes_.toByteArray();
    }

    private byte memoizedIsInitialized = -1;
    @java.lang.Override
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      if (!hasTimestamp()) {
        memoizedIsInitialized = 0;
        return false;
      }
      if (!hasBytes()) {
        memoizedIsInitialized = 0;
        return false;
      }
      memoizedIsInitialized = 1;
      return true;
    }

    @java.lang.Override
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      if (((bitField0_ & 0x00000001) != 0)) {
        output.writeUInt64(1, timestamp_);
      }
      if (((bitField0_ & 0x00000002) != 0)) {
        output.writeBytes(2, bytes_);
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
          .computeUInt64Size(1, timestamp_);
      }
      if (((bitField0_ & 0x00000002) != 0)) {
        size += com.google.protobuf.CodedOutputStream
          .computeBytesSize(2, bytes_);
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
      if (!(obj instanceof org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage)) {
        return super.equals(obj);
      }
      org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage other = (org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage) obj;

      if (hasTimestamp() != other.hasTimestamp()) return false;
      if (hasTimestamp()) {
        if (getTimestamp()
            != other.getTimestamp()) return false;
      }
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
      if (hasTimestamp()) {
        hash = (37 * hash) + TIMESTAMP_FIELD_NUMBER;
        hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
            getTimestamp());
      }
      if (hasBytes()) {
        hash = (37 * hash) + BYTES_FIELD_NUMBER;
        hash = (53 * hash) + getBytes().hashCode();
      }
      hash = (29 * hash) + getUnknownFields().hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }
    public static org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input);
    }
    public static org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage parseFrom(
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
    public static Builder newBuilder(org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage prototype) {
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
     * Protobuf type {@code TelemetryMessage}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:TelemetryMessage)
        org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessageOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.internal_static_TelemetryMessage_descriptor;
      }

      @java.lang.Override
      protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.internal_static_TelemetryMessage_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage.class, org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage.Builder.class);
      }

      // Construct using org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage.newBuilder()
      private Builder() {

      }

      private Builder(
          com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
        super(parent);

      }
      @java.lang.Override
      public Builder clear() {
        super.clear();
        timestamp_ = 0L;
        bitField0_ = (bitField0_ & ~0x00000001);
        bytes_ = com.google.protobuf.ByteString.EMPTY;
        bitField0_ = (bitField0_ & ~0x00000002);
        return this;
      }

      @java.lang.Override
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.internal_static_TelemetryMessage_descriptor;
      }

      @java.lang.Override
      public org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage getDefaultInstanceForType() {
        return org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage.getDefaultInstance();
      }

      @java.lang.Override
      public org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage build() {
        org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      @java.lang.Override
      public org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage buildPartial() {
        org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage result = new org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000001) != 0)) {
          result.timestamp_ = timestamp_;
          to_bitField0_ |= 0x00000001;
        }
        if (((from_bitField0_ & 0x00000002) != 0)) {
          to_bitField0_ |= 0x00000002;
        }
        result.bytes_ = bytes_;
        result.bitField0_ = to_bitField0_;
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
        if (other instanceof org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage) {
          return mergeFrom((org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage other) {
        if (other == org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage.getDefaultInstance()) return this;
        if (other.hasTimestamp()) {
          setTimestamp(other.getTimestamp());
        }
        if (other.hasBytes()) {
          setBytes(other.getBytes());
        }
        this.mergeUnknownFields(other.getUnknownFields());
        onChanged();
        return this;
      }

      @java.lang.Override
      public final boolean isInitialized() {
        if (!hasTimestamp()) {
          return false;
        }
        if (!hasBytes()) {
          return false;
        }
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
              case 8: {
                timestamp_ = input.readUInt64();
                bitField0_ |= 0x00000001;
                break;
              } // case 8
              case 18: {
                bytes_ = input.readBytes();
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

      private long timestamp_ ;
      /**
       * <code>required uint64 timestamp = 1;</code>
       * @return Whether the timestamp field is set.
       */
      @java.lang.Override
      public boolean hasTimestamp() {
        return ((bitField0_ & 0x00000001) != 0);
      }
      /**
       * <code>required uint64 timestamp = 1;</code>
       * @return The timestamp.
       */
      @java.lang.Override
      public long getTimestamp() {
        return timestamp_;
      }
      /**
       * <code>required uint64 timestamp = 1;</code>
       * @param value The timestamp to set.
       * @return This builder for chaining.
       */
      public Builder setTimestamp(long value) {
        bitField0_ |= 0x00000001;
        timestamp_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>required uint64 timestamp = 1;</code>
       * @return This builder for chaining.
       */
      public Builder clearTimestamp() {
        bitField0_ = (bitField0_ & ~0x00000001);
        timestamp_ = 0L;
        onChanged();
        return this;
      }

      private com.google.protobuf.ByteString bytes_ = com.google.protobuf.ByteString.EMPTY;
      /**
       * <code>required bytes bytes = 2;</code>
       * @return Whether the bytes field is set.
       */
      @java.lang.Override
      public boolean hasBytes() {
        return ((bitField0_ & 0x00000002) != 0);
      }
      /**
       * <code>required bytes bytes = 2;</code>
       * @return The bytes.
       */
      @java.lang.Override
      public com.google.protobuf.ByteString getBytes() {
        return bytes_;
      }
      /**
       * <code>required bytes bytes = 2;</code>
       * @param value The bytes to set.
       * @return This builder for chaining.
       */
      public Builder setBytes(com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000002;
        bytes_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>required bytes bytes = 2;</code>
       * @return This builder for chaining.
       */
      public Builder clearBytes() {
        bitField0_ = (bitField0_ & ~0x00000002);
        bytes_ = getDefaultInstance().getBytes();
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


      // @@protoc_insertion_point(builder_scope:TelemetryMessage)
    }

    // @@protoc_insertion_point(class_scope:TelemetryMessage)
    private static final org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage();
    }

    public static org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    @java.lang.Deprecated public static final com.google.protobuf.Parser<TelemetryMessage>
        PARSER = new com.google.protobuf.AbstractParser<TelemetryMessage>() {
      @java.lang.Override
      public TelemetryMessage parsePartialFrom(
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

    public static com.google.protobuf.Parser<TelemetryMessage> parser() {
      return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<TelemetryMessage> getParserForType() {
      return PARSER;
    }

    @java.lang.Override
    public org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  public interface TelemetryMessageLogOrBuilder extends
      // @@protoc_insertion_point(interface_extends:TelemetryMessageLog)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <code>required string location = 1;</code>
     * @return Whether the location field is set.
     */
    boolean hasLocation();
    /**
     * <code>required string location = 1;</code>
     * @return The location.
     */
    java.lang.String getLocation();
    /**
     * <code>required string location = 1;</code>
     * @return The bytes for location.
     */
    com.google.protobuf.ByteString
        getLocationBytes();

    /**
     * <code>required string system_id = 2;</code>
     * @return Whether the systemId field is set.
     */
    boolean hasSystemId();
    /**
     * <code>required string system_id = 2;</code>
     * @return The systemId.
     */
    java.lang.String getSystemId();
    /**
     * <code>required string system_id = 2;</code>
     * @return The bytes for systemId.
     */
    com.google.protobuf.ByteString
        getSystemIdBytes();

    /**
     * <code>optional string source_address = 3;</code>
     * @return Whether the sourceAddress field is set.
     */
    boolean hasSourceAddress();
    /**
     * <code>optional string source_address = 3;</code>
     * @return The sourceAddress.
     */
    java.lang.String getSourceAddress();
    /**
     * <code>optional string source_address = 3;</code>
     * @return The bytes for sourceAddress.
     */
    com.google.protobuf.ByteString
        getSourceAddressBytes();

    /**
     * <code>optional uint32 source_port = 4;</code>
     * @return Whether the sourcePort field is set.
     */
    boolean hasSourcePort();
    /**
     * <code>optional uint32 source_port = 4;</code>
     * @return The sourcePort.
     */
    int getSourcePort();

    /**
     * <code>repeated .TelemetryMessage message = 5;</code>
     */
    java.util.List<org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage> 
        getMessageList();
    /**
     * <code>repeated .TelemetryMessage message = 5;</code>
     */
    org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage getMessage(int index);
    /**
     * <code>repeated .TelemetryMessage message = 5;</code>
     */
    int getMessageCount();
    /**
     * <code>repeated .TelemetryMessage message = 5;</code>
     */
    java.util.List<? extends org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessageOrBuilder> 
        getMessageOrBuilderList();
    /**
     * <code>repeated .TelemetryMessage message = 5;</code>
     */
    org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessageOrBuilder getMessageOrBuilder(
        int index);
  }
  /**
   * Protobuf type {@code TelemetryMessageLog}
   */
  public static final class TelemetryMessageLog extends
      com.google.protobuf.GeneratedMessageV3 implements
      // @@protoc_insertion_point(message_implements:TelemetryMessageLog)
      TelemetryMessageLogOrBuilder, Message, org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLog {
  private static final long serialVersionUID = 0L;
    // Use TelemetryMessageLog.newBuilder() to construct.
    private TelemetryMessageLog(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
      super(builder);
    }
    private TelemetryMessageLog() {
      location_ = "";
      systemId_ = "";
      sourceAddress_ = "";
      message_ = java.util.Collections.emptyList();
    }

    @java.lang.Override
    @SuppressWarnings({"unused"})
    protected java.lang.Object newInstance(
        UnusedPrivateParameter unused) {
      return new TelemetryMessageLog();
    }

    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return this.unknownFields;
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.internal_static_TelemetryMessageLog_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.internal_static_TelemetryMessageLog_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessageLog.class, org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessageLog.Builder.class);
    }

    private int bitField0_;
    public static final int LOCATION_FIELD_NUMBER = 1;
    private volatile java.lang.Object location_;
    /**
     * <code>required string location = 1;</code>
     * @return Whether the location field is set.
     */
    @java.lang.Override
    public boolean hasLocation() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <code>required string location = 1;</code>
     * @return The location.
     */
    @java.lang.Override
    public java.lang.String getLocation() {
      java.lang.Object ref = location_;
      if (ref instanceof java.lang.String) {
        return (java.lang.String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        if (bs.isValidUtf8()) {
          location_ = s;
        }
        return s;
      }
    }
    /**
     * <code>required string location = 1;</code>
     * @return The bytes for location.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getLocationBytes() {
      java.lang.Object ref = location_;
      if (ref instanceof java.lang.String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        location_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    public static final int SYSTEM_ID_FIELD_NUMBER = 2;
    private volatile java.lang.Object systemId_;
    /**
     * <code>required string system_id = 2;</code>
     * @return Whether the systemId field is set.
     */
    @java.lang.Override
    public boolean hasSystemId() {
      return ((bitField0_ & 0x00000002) != 0);
    }
    /**
     * <code>required string system_id = 2;</code>
     * @return The systemId.
     */
    @java.lang.Override
    public java.lang.String getSystemId() {
      java.lang.Object ref = systemId_;
      if (ref instanceof java.lang.String) {
        return (java.lang.String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        if (bs.isValidUtf8()) {
          systemId_ = s;
        }
        return s;
      }
    }
    /**
     * <code>required string system_id = 2;</code>
     * @return The bytes for systemId.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getSystemIdBytes() {
      java.lang.Object ref = systemId_;
      if (ref instanceof java.lang.String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        systemId_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    public static final int SOURCE_ADDRESS_FIELD_NUMBER = 3;
    private volatile java.lang.Object sourceAddress_;
    /**
     * <code>optional string source_address = 3;</code>
     * @return Whether the sourceAddress field is set.
     */
    @java.lang.Override
    public boolean hasSourceAddress() {
      return ((bitField0_ & 0x00000004) != 0);
    }
    /**
     * <code>optional string source_address = 3;</code>
     * @return The sourceAddress.
     */
    @java.lang.Override
    public java.lang.String getSourceAddress() {
      java.lang.Object ref = sourceAddress_;
      if (ref instanceof java.lang.String) {
        return (java.lang.String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        if (bs.isValidUtf8()) {
          sourceAddress_ = s;
        }
        return s;
      }
    }
    /**
     * <code>optional string source_address = 3;</code>
     * @return The bytes for sourceAddress.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getSourceAddressBytes() {
      java.lang.Object ref = sourceAddress_;
      if (ref instanceof java.lang.String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        sourceAddress_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    public static final int SOURCE_PORT_FIELD_NUMBER = 4;
    private int sourcePort_;
    /**
     * <code>optional uint32 source_port = 4;</code>
     * @return Whether the sourcePort field is set.
     */
    @java.lang.Override
    public boolean hasSourcePort() {
      return ((bitField0_ & 0x00000008) != 0);
    }
    /**
     * <code>optional uint32 source_port = 4;</code>
     * @return The sourcePort.
     */
    @java.lang.Override
    public int getSourcePort() {
      return sourcePort_;
    }

    public static final int MESSAGE_FIELD_NUMBER = 5;
    private java.util.List<org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage> message_;
    /**
     * <code>repeated .TelemetryMessage message = 5;</code>
     */
    @java.lang.Override
    public java.util.List<org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage> getMessageList() {
      return message_;
    }


    /**
     * <code>repeated .TelemetryMessage message = 5;</code>
     */
    @java.lang.Override
    public java.util.List<? extends org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessageOrBuilder> 
        getMessageOrBuilderList() {
      return message_;
    }
    /**
     * <code>repeated .TelemetryMessage message = 5;</code>
     */
    @java.lang.Override
    public int getMessageCount() {
      return message_.size();
    }
    /**
     * <code>repeated .TelemetryMessage message = 5;</code>
     */
    @java.lang.Override
    public org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage getMessage(int index) {
      return message_.get(index);
    }
    /**
     * <code>repeated .TelemetryMessage message = 5;</code>
     */
    @java.lang.Override
    public org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessageOrBuilder getMessageOrBuilder(
        int index) {
      return message_.get(index);
    }

    private byte memoizedIsInitialized = -1;
    @java.lang.Override
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      if (!hasLocation()) {
        memoizedIsInitialized = 0;
        return false;
      }
      if (!hasSystemId()) {
        memoizedIsInitialized = 0;
        return false;
      }
      for (int i = 0; i < getMessageCount(); i++) {
        if (!getMessage(i).isInitialized()) {
          memoizedIsInitialized = 0;
          return false;
        }
      }
      memoizedIsInitialized = 1;
      return true;
    }

    @java.lang.Override
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      if (((bitField0_ & 0x00000001) != 0)) {
        com.google.protobuf.GeneratedMessageV3.writeString(output, 1, location_);
      }
      if (((bitField0_ & 0x00000002) != 0)) {
        com.google.protobuf.GeneratedMessageV3.writeString(output, 2, systemId_);
      }
      if (((bitField0_ & 0x00000004) != 0)) {
        com.google.protobuf.GeneratedMessageV3.writeString(output, 3, sourceAddress_);
      }
      if (((bitField0_ & 0x00000008) != 0)) {
        output.writeUInt32(4, sourcePort_);
      }
      for (int i = 0; i < message_.size(); i++) {
        output.writeMessage(5, message_.get(i));
      }
      getUnknownFields().writeTo(output);
    }

    @java.lang.Override
    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      if (((bitField0_ & 0x00000001) != 0)) {
        size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, location_);
      }
      if (((bitField0_ & 0x00000002) != 0)) {
        size += com.google.protobuf.GeneratedMessageV3.computeStringSize(2, systemId_);
      }
      if (((bitField0_ & 0x00000004) != 0)) {
        size += com.google.protobuf.GeneratedMessageV3.computeStringSize(3, sourceAddress_);
      }
      if (((bitField0_ & 0x00000008) != 0)) {
        size += com.google.protobuf.CodedOutputStream
          .computeUInt32Size(4, sourcePort_);
      }
      for (int i = 0; i < message_.size(); i++) {
        size += com.google.protobuf.CodedOutputStream
          .computeMessageSize(5, message_.get(i));
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
      if (!(obj instanceof org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessageLog)) {
        return super.equals(obj);
      }
      org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessageLog other = (org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessageLog) obj;

      if (hasLocation() != other.hasLocation()) return false;
      if (hasLocation()) {
        if (!getLocation()
            .equals(other.getLocation())) return false;
      }
      if (hasSystemId() != other.hasSystemId()) return false;
      if (hasSystemId()) {
        if (!getSystemId()
            .equals(other.getSystemId())) return false;
      }
      if (hasSourceAddress() != other.hasSourceAddress()) return false;
      if (hasSourceAddress()) {
        if (!getSourceAddress()
            .equals(other.getSourceAddress())) return false;
      }
      if (hasSourcePort() != other.hasSourcePort()) return false;
      if (hasSourcePort()) {
        if (getSourcePort()
            != other.getSourcePort()) return false;
      }
      if (!getMessageList()
          .equals(other.getMessageList())) return false;
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
      if (hasLocation()) {
        hash = (37 * hash) + LOCATION_FIELD_NUMBER;
        hash = (53 * hash) + getLocation().hashCode();
      }
      if (hasSystemId()) {
        hash = (37 * hash) + SYSTEM_ID_FIELD_NUMBER;
        hash = (53 * hash) + getSystemId().hashCode();
      }
      if (hasSourceAddress()) {
        hash = (37 * hash) + SOURCE_ADDRESS_FIELD_NUMBER;
        hash = (53 * hash) + getSourceAddress().hashCode();
      }
      if (hasSourcePort()) {
        hash = (37 * hash) + SOURCE_PORT_FIELD_NUMBER;
        hash = (53 * hash) + getSourcePort();
      }
      if (getMessageCount() > 0) {
        hash = (37 * hash) + MESSAGE_FIELD_NUMBER;
        hash = (53 * hash) + getMessageList().hashCode();
      }
      hash = (29 * hash) + getUnknownFields().hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessageLog parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessageLog parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessageLog parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessageLog parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessageLog parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessageLog parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessageLog parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessageLog parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }
    public static org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessageLog parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input);
    }
    public static org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessageLog parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessageLog parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessageLog parseFrom(
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
    public static Builder newBuilder(org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessageLog prototype) {
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
     * Protobuf type {@code TelemetryMessageLog}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:TelemetryMessageLog)
        org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessageLogOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.internal_static_TelemetryMessageLog_descriptor;
      }

      @java.lang.Override
      protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.internal_static_TelemetryMessageLog_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessageLog.class, org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessageLog.Builder.class);
      }

      // Construct using org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessageLog.newBuilder()
      private Builder() {

      }

      private Builder(
          com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
        super(parent);

      }
      @java.lang.Override
      public Builder clear() {
        super.clear();
        location_ = "";
        bitField0_ = (bitField0_ & ~0x00000001);
        systemId_ = "";
        bitField0_ = (bitField0_ & ~0x00000002);
        sourceAddress_ = "";
        bitField0_ = (bitField0_ & ~0x00000004);
        sourcePort_ = 0;
        bitField0_ = (bitField0_ & ~0x00000008);
        if (messageBuilder_ == null) {
          message_ = java.util.Collections.emptyList();
        } else {
          message_ = null;
          messageBuilder_.clear();
        }
        bitField0_ = (bitField0_ & ~0x00000010);
        return this;
      }

      @java.lang.Override
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.internal_static_TelemetryMessageLog_descriptor;
      }

      @java.lang.Override
      public org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessageLog getDefaultInstanceForType() {
        return org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessageLog.getDefaultInstance();
      }

      @java.lang.Override
      public org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessageLog build() {
        org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessageLog result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      @java.lang.Override
      public org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessageLog buildPartial() {
        org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessageLog result = new org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessageLog(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000001) != 0)) {
          to_bitField0_ |= 0x00000001;
        }
        result.location_ = location_;
        if (((from_bitField0_ & 0x00000002) != 0)) {
          to_bitField0_ |= 0x00000002;
        }
        result.systemId_ = systemId_;
        if (((from_bitField0_ & 0x00000004) != 0)) {
          to_bitField0_ |= 0x00000004;
        }
        result.sourceAddress_ = sourceAddress_;
        if (((from_bitField0_ & 0x00000008) != 0)) {
          result.sourcePort_ = sourcePort_;
          to_bitField0_ |= 0x00000008;
        }
        if (messageBuilder_ == null) {
          if (((bitField0_ & 0x00000010) != 0)) {
            message_ = java.util.Collections.unmodifiableList(message_);
            bitField0_ = (bitField0_ & ~0x00000010);
          }
          result.message_ = message_;
        } else {
          result.message_ = messageBuilder_.build();
        }
        result.bitField0_ = to_bitField0_;
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
        if (other instanceof org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessageLog) {
          return mergeFrom((org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessageLog)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessageLog other) {
        if (other == org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessageLog.getDefaultInstance()) return this;
        if (other.hasLocation()) {
          bitField0_ |= 0x00000001;
          location_ = other.location_;
          onChanged();
        }
        if (other.hasSystemId()) {
          bitField0_ |= 0x00000002;
          systemId_ = other.systemId_;
          onChanged();
        }
        if (other.hasSourceAddress()) {
          bitField0_ |= 0x00000004;
          sourceAddress_ = other.sourceAddress_;
          onChanged();
        }
        if (other.hasSourcePort()) {
          setSourcePort(other.getSourcePort());
        }
        if (messageBuilder_ == null) {
          if (!other.message_.isEmpty()) {
            if (message_.isEmpty()) {
              message_ = other.message_;
              bitField0_ = (bitField0_ & ~0x00000010);
            } else {
              ensureMessageIsMutable();
              message_.addAll(other.message_);
            }
            onChanged();
          }
        } else {
          if (!other.message_.isEmpty()) {
            if (messageBuilder_.isEmpty()) {
              messageBuilder_.dispose();
              messageBuilder_ = null;
              message_ = other.message_;
              bitField0_ = (bitField0_ & ~0x00000010);
              messageBuilder_ = 
                com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders ?
                   getMessageFieldBuilder() : null;
            } else {
              messageBuilder_.addAllMessages(other.message_);
            }
          }
        }
        this.mergeUnknownFields(other.getUnknownFields());
        onChanged();
        return this;
      }

      @java.lang.Override
      public final boolean isInitialized() {
        if (!hasLocation()) {
          return false;
        }
        if (!hasSystemId()) {
          return false;
        }
        for (int i = 0; i < getMessageCount(); i++) {
          if (!getMessage(i).isInitialized()) {
            return false;
          }
        }
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
                location_ = input.readBytes();
                bitField0_ |= 0x00000001;
                break;
              } // case 10
              case 18: {
                systemId_ = input.readBytes();
                bitField0_ |= 0x00000002;
                break;
              } // case 18
              case 26: {
                sourceAddress_ = input.readBytes();
                bitField0_ |= 0x00000004;
                break;
              } // case 26
              case 32: {
                sourcePort_ = input.readUInt32();
                bitField0_ |= 0x00000008;
                break;
              } // case 32
              case 42: {
                org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage m =
                    input.readMessage(
                        org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage.PARSER,
                        extensionRegistry);
                if (messageBuilder_ == null) {
                  ensureMessageIsMutable();
                  message_.add(m);
                } else {
                  messageBuilder_.addMessage(m);
                }
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

      private java.lang.Object location_ = "";
      /**
       * <code>required string location = 1;</code>
       * @return Whether the location field is set.
       */
      public boolean hasLocation() {
        return ((bitField0_ & 0x00000001) != 0);
      }
      /**
       * <code>required string location = 1;</code>
       * @return The location.
       */
      public java.lang.String getLocation() {
        java.lang.Object ref = location_;
        if (!(ref instanceof java.lang.String)) {
          com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
          java.lang.String s = bs.toStringUtf8();
          if (bs.isValidUtf8()) {
            location_ = s;
          }
          return s;
        } else {
          return (java.lang.String) ref;
        }
      }
      /**
       * <code>required string location = 1;</code>
       * @return The bytes for location.
       */
      public com.google.protobuf.ByteString
          getLocationBytes() {
        java.lang.Object ref = location_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (java.lang.String) ref);
          location_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>required string location = 1;</code>
       * @param value The location to set.
       * @return This builder for chaining.
       */
      public Builder setLocation(
          java.lang.String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000001;
        location_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>required string location = 1;</code>
       * @return This builder for chaining.
       */
      public Builder clearLocation() {
        bitField0_ = (bitField0_ & ~0x00000001);
        location_ = getDefaultInstance().getLocation();
        onChanged();
        return this;
      }
      /**
       * <code>required string location = 1;</code>
       * @param value The bytes for location to set.
       * @return This builder for chaining.
       */
      public Builder setLocationBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000001;
        location_ = value;
        onChanged();
        return this;
      }

      private java.lang.Object systemId_ = "";
      /**
       * <code>required string system_id = 2;</code>
       * @return Whether the systemId field is set.
       */
      public boolean hasSystemId() {
        return ((bitField0_ & 0x00000002) != 0);
      }
      /**
       * <code>required string system_id = 2;</code>
       * @return The systemId.
       */
      public java.lang.String getSystemId() {
        java.lang.Object ref = systemId_;
        if (!(ref instanceof java.lang.String)) {
          com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
          java.lang.String s = bs.toStringUtf8();
          if (bs.isValidUtf8()) {
            systemId_ = s;
          }
          return s;
        } else {
          return (java.lang.String) ref;
        }
      }
      /**
       * <code>required string system_id = 2;</code>
       * @return The bytes for systemId.
       */
      public com.google.protobuf.ByteString
          getSystemIdBytes() {
        java.lang.Object ref = systemId_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (java.lang.String) ref);
          systemId_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>required string system_id = 2;</code>
       * @param value The systemId to set.
       * @return This builder for chaining.
       */
      public Builder setSystemId(
          java.lang.String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000002;
        systemId_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>required string system_id = 2;</code>
       * @return This builder for chaining.
       */
      public Builder clearSystemId() {
        bitField0_ = (bitField0_ & ~0x00000002);
        systemId_ = getDefaultInstance().getSystemId();
        onChanged();
        return this;
      }
      /**
       * <code>required string system_id = 2;</code>
       * @param value The bytes for systemId to set.
       * @return This builder for chaining.
       */
      public Builder setSystemIdBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000002;
        systemId_ = value;
        onChanged();
        return this;
      }

      private java.lang.Object sourceAddress_ = "";
      /**
       * <code>optional string source_address = 3;</code>
       * @return Whether the sourceAddress field is set.
       */
      public boolean hasSourceAddress() {
        return ((bitField0_ & 0x00000004) != 0);
      }
      /**
       * <code>optional string source_address = 3;</code>
       * @return The sourceAddress.
       */
      public java.lang.String getSourceAddress() {
        java.lang.Object ref = sourceAddress_;
        if (!(ref instanceof java.lang.String)) {
          com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
          java.lang.String s = bs.toStringUtf8();
          if (bs.isValidUtf8()) {
            sourceAddress_ = s;
          }
          return s;
        } else {
          return (java.lang.String) ref;
        }
      }
      /**
       * <code>optional string source_address = 3;</code>
       * @return The bytes for sourceAddress.
       */
      public com.google.protobuf.ByteString
          getSourceAddressBytes() {
        java.lang.Object ref = sourceAddress_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (java.lang.String) ref);
          sourceAddress_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>optional string source_address = 3;</code>
       * @param value The sourceAddress to set.
       * @return This builder for chaining.
       */
      public Builder setSourceAddress(
          java.lang.String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000004;
        sourceAddress_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>optional string source_address = 3;</code>
       * @return This builder for chaining.
       */
      public Builder clearSourceAddress() {
        bitField0_ = (bitField0_ & ~0x00000004);
        sourceAddress_ = getDefaultInstance().getSourceAddress();
        onChanged();
        return this;
      }
      /**
       * <code>optional string source_address = 3;</code>
       * @param value The bytes for sourceAddress to set.
       * @return This builder for chaining.
       */
      public Builder setSourceAddressBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000004;
        sourceAddress_ = value;
        onChanged();
        return this;
      }

      private int sourcePort_ ;
      /**
       * <code>optional uint32 source_port = 4;</code>
       * @return Whether the sourcePort field is set.
       */
      @java.lang.Override
      public boolean hasSourcePort() {
        return ((bitField0_ & 0x00000008) != 0);
      }
      /**
       * <code>optional uint32 source_port = 4;</code>
       * @return The sourcePort.
       */
      @java.lang.Override
      public int getSourcePort() {
        return sourcePort_;
      }
      /**
       * <code>optional uint32 source_port = 4;</code>
       * @param value The sourcePort to set.
       * @return This builder for chaining.
       */
      public Builder setSourcePort(int value) {
        bitField0_ |= 0x00000008;
        sourcePort_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>optional uint32 source_port = 4;</code>
       * @return This builder for chaining.
       */
      public Builder clearSourcePort() {
        bitField0_ = (bitField0_ & ~0x00000008);
        sourcePort_ = 0;
        onChanged();
        return this;
      }

      private java.util.List<org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage> message_ =
        java.util.Collections.emptyList();
      private void ensureMessageIsMutable() {
        if (!((bitField0_ & 0x00000010) != 0)) {
          message_ = new java.util.ArrayList<org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage>(message_);
          bitField0_ |= 0x00000010;
         }
      }

      private com.google.protobuf.RepeatedFieldBuilderV3<
          org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage, org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage.Builder, org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessageOrBuilder> messageBuilder_;

      /**
       * <code>repeated .TelemetryMessage message = 5;</code>
       */
      public java.util.List<org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage> getMessageList() {
        if (messageBuilder_ == null) {
          return java.util.Collections.unmodifiableList(message_);
        } else {
          return messageBuilder_.getMessageList();
        }
      }
      /**
       * <code>repeated .TelemetryMessage message = 5;</code>
       */
      public int getMessageCount() {
        if (messageBuilder_ == null) {
          return message_.size();
        } else {
          return messageBuilder_.getCount();
        }
      }
      /**
       * <code>repeated .TelemetryMessage message = 5;</code>
       */
      public org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage getMessage(int index) {
        if (messageBuilder_ == null) {
          return message_.get(index);
        } else {
          return messageBuilder_.getMessage(index);
        }
      }
      /**
       * <code>repeated .TelemetryMessage message = 5;</code>
       */
      public Builder setMessage(
          int index, org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage value) {
        if (messageBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          ensureMessageIsMutable();
          message_.set(index, value);
          onChanged();
        } else {
          messageBuilder_.setMessage(index, value);
        }
        return this;
      }
      /**
       * <code>repeated .TelemetryMessage message = 5;</code>
       */
      public Builder setMessage(
          int index, org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage.Builder builderForValue) {
        if (messageBuilder_ == null) {
          ensureMessageIsMutable();
          message_.set(index, builderForValue.build());
          onChanged();
        } else {
          messageBuilder_.setMessage(index, builderForValue.build());
        }
        return this;
      }
      /**
       * <code>repeated .TelemetryMessage message = 5;</code>
       */
      public Builder addMessage(org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage value) {
        if (messageBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          ensureMessageIsMutable();
          message_.add(value);
          onChanged();
        } else {
          messageBuilder_.addMessage(value);
        }
        return this;
      }
      /**
       * <code>repeated .TelemetryMessage message = 5;</code>
       */
      public Builder addMessage(
          int index, org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage value) {
        if (messageBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          ensureMessageIsMutable();
          message_.add(index, value);
          onChanged();
        } else {
          messageBuilder_.addMessage(index, value);
        }
        return this;
      }
      /**
       * <code>repeated .TelemetryMessage message = 5;</code>
       */
      public Builder addMessage(
          org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage.Builder builderForValue) {
        if (messageBuilder_ == null) {
          ensureMessageIsMutable();
          message_.add(builderForValue.build());
          onChanged();
        } else {
          messageBuilder_.addMessage(builderForValue.build());
        }
        return this;
      }
      /**
       * <code>repeated .TelemetryMessage message = 5;</code>
       */
      public Builder addMessage(
          int index, org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage.Builder builderForValue) {
        if (messageBuilder_ == null) {
          ensureMessageIsMutable();
          message_.add(index, builderForValue.build());
          onChanged();
        } else {
          messageBuilder_.addMessage(index, builderForValue.build());
        }
        return this;
      }
      /**
       * <code>repeated .TelemetryMessage message = 5;</code>
       */
      public Builder addAllMessage(
          java.lang.Iterable<? extends org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage> values) {
        if (messageBuilder_ == null) {
          ensureMessageIsMutable();
          com.google.protobuf.AbstractMessageLite.Builder.addAll(
              values, message_);
          onChanged();
        } else {
          messageBuilder_.addAllMessages(values);
        }
        return this;
      }
      /**
       * <code>repeated .TelemetryMessage message = 5;</code>
       */
      public Builder clearMessage() {
        if (messageBuilder_ == null) {
          message_ = java.util.Collections.emptyList();
          bitField0_ = (bitField0_ & ~0x00000010);
          onChanged();
        } else {
          messageBuilder_.clear();
        }
        return this;
      }
      /**
       * <code>repeated .TelemetryMessage message = 5;</code>
       */
      public Builder removeMessage(int index) {
        if (messageBuilder_ == null) {
          ensureMessageIsMutable();
          message_.remove(index);
          onChanged();
        } else {
          messageBuilder_.remove(index);
        }
        return this;
      }
      /**
       * <code>repeated .TelemetryMessage message = 5;</code>
       */
      public org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage.Builder getMessageBuilder(
          int index) {
        return getMessageFieldBuilder().getBuilder(index);
      }
      /**
       * <code>repeated .TelemetryMessage message = 5;</code>
       */
      public org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessageOrBuilder getMessageOrBuilder(
          int index) {
        if (messageBuilder_ == null) {
          return message_.get(index);  } else {
          return messageBuilder_.getMessageOrBuilder(index);
        }
      }
      /**
       * <code>repeated .TelemetryMessage message = 5;</code>
       */
      public java.util.List<? extends org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessageOrBuilder> 
           getMessageOrBuilderList() {
        if (messageBuilder_ != null) {
          return messageBuilder_.getMessageOrBuilderList();
        } else {
          return java.util.Collections.unmodifiableList(message_);
        }
      }
      /**
       * <code>repeated .TelemetryMessage message = 5;</code>
       */
      public org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage.Builder addMessageBuilder() {
        return getMessageFieldBuilder().addBuilder(
            org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage.getDefaultInstance());
      }
      /**
       * <code>repeated .TelemetryMessage message = 5;</code>
       */
      public org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage.Builder addMessageBuilder(
          int index) {
        return getMessageFieldBuilder().addBuilder(
            index, org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage.getDefaultInstance());
      }
      /**
       * <code>repeated .TelemetryMessage message = 5;</code>
       */
      public java.util.List<org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage.Builder> 
           getMessageBuilderList() {
        return getMessageFieldBuilder().getBuilderList();
      }
      private com.google.protobuf.RepeatedFieldBuilderV3<
          org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage, org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage.Builder, org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessageOrBuilder> 
          getMessageFieldBuilder() {
        if (messageBuilder_ == null) {
          messageBuilder_ = new com.google.protobuf.RepeatedFieldBuilderV3<
              org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage, org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessage.Builder, org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessageOrBuilder>(
                  message_,
                  ((bitField0_ & 0x00000010) != 0),
                  getParentForChildren(),
                  isClean());
          message_ = null;
        }
        return messageBuilder_;
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


      // @@protoc_insertion_point(builder_scope:TelemetryMessageLog)
    }

    // @@protoc_insertion_point(class_scope:TelemetryMessageLog)
    private static final org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessageLog DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessageLog();
    }

    public static org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessageLog getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    @java.lang.Deprecated public static final com.google.protobuf.Parser<TelemetryMessageLog>
        PARSER = new com.google.protobuf.AbstractParser<TelemetryMessageLog>() {
      @java.lang.Override
      public TelemetryMessageLog parsePartialFrom(
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

    public static com.google.protobuf.Parser<TelemetryMessageLog> parser() {
      return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<TelemetryMessageLog> getParserForType() {
      return PARSER;
    }

    @java.lang.Override
    public org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos.TelemetryMessageLog getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_TelemetryMessage_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_TelemetryMessage_fieldAccessorTable;
  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_TelemetryMessageLog_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_TelemetryMessageLog_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\017telemetry.proto\"4\n\020TelemetryMessage\022\021\n" +
      "\ttimestamp\030\001 \002(\004\022\r\n\005bytes\030\002 \002(\014\"\213\001\n\023Tele" +
      "metryMessageLog\022\020\n\010location\030\001 \002(\t\022\021\n\tsys" +
      "tem_id\030\002 \002(\t\022\026\n\016source_address\030\003 \001(\t\022\023\n\013" +
      "source_port\030\004 \001(\r\022\"\n\007message\030\005 \003(\0132\021.Tel" +
      "emetryMessageB:\n\'org.opennms.netmgt.tele" +
      "metry.common.ipcB\017TelemetryProtos"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        });
    internal_static_TelemetryMessage_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_TelemetryMessage_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_TelemetryMessage_descriptor,
        new java.lang.String[] { "Timestamp", "Bytes", });
    internal_static_TelemetryMessageLog_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_TelemetryMessageLog_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_TelemetryMessageLog_descriptor,
        new java.lang.String[] { "Location", "SystemId", "SourceAddress", "SourcePort", "Message", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}
