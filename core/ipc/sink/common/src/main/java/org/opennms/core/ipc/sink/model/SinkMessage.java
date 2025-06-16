/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2025 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2025 The OpenNMS Group, Inc.
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
package org.opennms.core.ipc.sink.model;

/**
 * Protobuf type {@code SinkMessage}
 */
public final class SinkMessage extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:SinkMessage)
    SinkMessageOrBuilder {
private static final long serialVersionUID = 0L;
  // Use SinkMessage.newBuilder() to construct.
  private SinkMessage(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private SinkMessage() {
    messageId_ = "";
    content_ = com.google.protobuf.ByteString.EMPTY;
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new SinkMessage();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return org.opennms.core.ipc.sink.model.SinkMessageProtos.internal_static_SinkMessage_descriptor;
  }

  @SuppressWarnings({"rawtypes"})
  @java.lang.Override
  protected com.google.protobuf.MapFieldReflectionAccessor internalGetMapFieldReflection(
      int number) {
    switch (number) {
      case 5:
        return internalGetTracingInfo();
      default:
        throw new RuntimeException(
            "Invalid map field number: " + number);
    }
  }
  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return org.opennms.core.ipc.sink.model.SinkMessageProtos.internal_static_SinkMessage_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            org.opennms.core.ipc.sink.model.SinkMessage.class, org.opennms.core.ipc.sink.model.SinkMessage.Builder.class);
  }

  public static final int MESSAGE_ID_FIELD_NUMBER = 1;
  @SuppressWarnings("serial")
  private volatile java.lang.Object messageId_ = "";
  /**
   * <code>string message_id = 1;</code>
   * @return The messageId.
   */
  @java.lang.Override
  public java.lang.String getMessageId() {
    java.lang.Object ref = messageId_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      messageId_ = s;
      return s;
    }
  }
  /**
   * <code>string message_id = 1;</code>
   * @return The bytes for messageId.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getMessageIdBytes() {
    java.lang.Object ref = messageId_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      messageId_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int CONTENT_FIELD_NUMBER = 2;
  private com.google.protobuf.ByteString content_ = com.google.protobuf.ByteString.EMPTY;
  /**
   * <code>bytes content = 2;</code>
   * @return The content.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString getContent() {
    return content_;
  }

  public static final int CURRENT_CHUNK_NUMBER_FIELD_NUMBER = 3;
  private int currentChunkNumber_ = 0;
  /**
   * <code>int32 current_chunk_number = 3;</code>
   * @return The currentChunkNumber.
   */
  @java.lang.Override
  public int getCurrentChunkNumber() {
    return currentChunkNumber_;
  }

  public static final int TOTAL_CHUNKS_FIELD_NUMBER = 4;
  private int totalChunks_ = 0;
  /**
   * <code>int32 total_chunks = 4;</code>
   * @return The totalChunks.
   */
  @java.lang.Override
  public int getTotalChunks() {
    return totalChunks_;
  }

  public static final int TRACING_INFO_FIELD_NUMBER = 5;
  private static final class TracingInfoDefaultEntryHolder {
    static final com.google.protobuf.MapEntry<
        java.lang.String, java.lang.String> defaultEntry =
            com.google.protobuf.MapEntry
            .<java.lang.String, java.lang.String>newDefaultInstance(
                org.opennms.core.ipc.sink.model.SinkMessageProtos.internal_static_SinkMessage_TracingInfoEntry_descriptor, 
                com.google.protobuf.WireFormat.FieldType.STRING,
                "",
                com.google.protobuf.WireFormat.FieldType.STRING,
                "");
  }
  @SuppressWarnings("serial")
  private com.google.protobuf.MapField<
      java.lang.String, java.lang.String> tracingInfo_;
  private com.google.protobuf.MapField<java.lang.String, java.lang.String>
  internalGetTracingInfo() {
    if (tracingInfo_ == null) {
      return com.google.protobuf.MapField.emptyMapField(
          TracingInfoDefaultEntryHolder.defaultEntry);
    }
    return tracingInfo_;
  }
  public int getTracingInfoCount() {
    return internalGetTracingInfo().getMap().size();
  }
  /**
   * <code>map&lt;string, string&gt; tracing_info = 5;</code>
   */
  @java.lang.Override
  public boolean containsTracingInfo(
      java.lang.String key) {
    if (key == null) { throw new NullPointerException("map key"); }
    return internalGetTracingInfo().getMap().containsKey(key);
  }
  /**
   * Use {@link #getTracingInfoMap()} instead.
   */
  @java.lang.Override
  @java.lang.Deprecated
  public java.util.Map<java.lang.String, java.lang.String> getTracingInfo() {
    return getTracingInfoMap();
  }
  /**
   * <code>map&lt;string, string&gt; tracing_info = 5;</code>
   */
  @java.lang.Override
  public java.util.Map<java.lang.String, java.lang.String> getTracingInfoMap() {
    return internalGetTracingInfo().getMap();
  }
  /**
   * <code>map&lt;string, string&gt; tracing_info = 5;</code>
   */
  @java.lang.Override
  public /* nullable */
java.lang.String getTracingInfoOrDefault(
      java.lang.String key,
      /* nullable */
java.lang.String defaultValue) {
    if (key == null) { throw new NullPointerException("map key"); }
    java.util.Map<java.lang.String, java.lang.String> map =
        internalGetTracingInfo().getMap();
    return map.containsKey(key) ? map.get(key) : defaultValue;
  }
  /**
   * <code>map&lt;string, string&gt; tracing_info = 5;</code>
   */
  @java.lang.Override
  public java.lang.String getTracingInfoOrThrow(
      java.lang.String key) {
    if (key == null) { throw new NullPointerException("map key"); }
    java.util.Map<java.lang.String, java.lang.String> map =
        internalGetTracingInfo().getMap();
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
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(messageId_)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, messageId_);
    }
    if (!content_.isEmpty()) {
      output.writeBytes(2, content_);
    }
    if (currentChunkNumber_ != 0) {
      output.writeInt32(3, currentChunkNumber_);
    }
    if (totalChunks_ != 0) {
      output.writeInt32(4, totalChunks_);
    }
    com.google.protobuf.GeneratedMessageV3
      .serializeStringMapTo(
        output,
        internalGetTracingInfo(),
        TracingInfoDefaultEntryHolder.defaultEntry,
        5);
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(messageId_)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, messageId_);
    }
    if (!content_.isEmpty()) {
      size += com.google.protobuf.CodedOutputStream
        .computeBytesSize(2, content_);
    }
    if (currentChunkNumber_ != 0) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt32Size(3, currentChunkNumber_);
    }
    if (totalChunks_ != 0) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt32Size(4, totalChunks_);
    }
    for (java.util.Map.Entry<java.lang.String, java.lang.String> entry
         : internalGetTracingInfo().getMap().entrySet()) {
      com.google.protobuf.MapEntry<java.lang.String, java.lang.String>
      tracingInfo__ = TracingInfoDefaultEntryHolder.defaultEntry.newBuilderForType()
          .setKey(entry.getKey())
          .setValue(entry.getValue())
          .build();
      size += com.google.protobuf.CodedOutputStream
          .computeMessageSize(5, tracingInfo__);
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
    if (!(obj instanceof org.opennms.core.ipc.sink.model.SinkMessage)) {
      return super.equals(obj);
    }
    org.opennms.core.ipc.sink.model.SinkMessage other = (org.opennms.core.ipc.sink.model.SinkMessage) obj;

    if (!getMessageId()
        .equals(other.getMessageId())) return false;
    if (!getContent()
        .equals(other.getContent())) return false;
    if (getCurrentChunkNumber()
        != other.getCurrentChunkNumber()) return false;
    if (getTotalChunks()
        != other.getTotalChunks()) return false;
    if (!internalGetTracingInfo().equals(
        other.internalGetTracingInfo())) return false;
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
    hash = (37 * hash) + MESSAGE_ID_FIELD_NUMBER;
    hash = (53 * hash) + getMessageId().hashCode();
    hash = (37 * hash) + CONTENT_FIELD_NUMBER;
    hash = (53 * hash) + getContent().hashCode();
    hash = (37 * hash) + CURRENT_CHUNK_NUMBER_FIELD_NUMBER;
    hash = (53 * hash) + getCurrentChunkNumber();
    hash = (37 * hash) + TOTAL_CHUNKS_FIELD_NUMBER;
    hash = (53 * hash) + getTotalChunks();
    if (!internalGetTracingInfo().getMap().isEmpty()) {
      hash = (37 * hash) + TRACING_INFO_FIELD_NUMBER;
      hash = (53 * hash) + internalGetTracingInfo().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static org.opennms.core.ipc.sink.model.SinkMessage parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.opennms.core.ipc.sink.model.SinkMessage parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.opennms.core.ipc.sink.model.SinkMessage parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.opennms.core.ipc.sink.model.SinkMessage parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.opennms.core.ipc.sink.model.SinkMessage parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.opennms.core.ipc.sink.model.SinkMessage parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.opennms.core.ipc.sink.model.SinkMessage parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static org.opennms.core.ipc.sink.model.SinkMessage parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static org.opennms.core.ipc.sink.model.SinkMessage parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static org.opennms.core.ipc.sink.model.SinkMessage parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static org.opennms.core.ipc.sink.model.SinkMessage parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static org.opennms.core.ipc.sink.model.SinkMessage parseFrom(
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
  public static Builder newBuilder(org.opennms.core.ipc.sink.model.SinkMessage prototype) {
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
   * Protobuf type {@code SinkMessage}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:SinkMessage)
      org.opennms.core.ipc.sink.model.SinkMessageOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return org.opennms.core.ipc.sink.model.SinkMessageProtos.internal_static_SinkMessage_descriptor;
    }

    @SuppressWarnings({"rawtypes"})
    protected com.google.protobuf.MapFieldReflectionAccessor internalGetMapFieldReflection(
        int number) {
      switch (number) {
        case 5:
          return internalGetTracingInfo();
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
          return internalGetMutableTracingInfo();
        default:
          throw new RuntimeException(
              "Invalid map field number: " + number);
      }
    }
    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return org.opennms.core.ipc.sink.model.SinkMessageProtos.internal_static_SinkMessage_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              org.opennms.core.ipc.sink.model.SinkMessage.class, org.opennms.core.ipc.sink.model.SinkMessage.Builder.class);
    }

    // Construct using org.opennms.core.ipc.sink.model.SinkMessage.newBuilder()
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
      messageId_ = "";
      content_ = com.google.protobuf.ByteString.EMPTY;
      currentChunkNumber_ = 0;
      totalChunks_ = 0;
      internalGetMutableTracingInfo().clear();
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return org.opennms.core.ipc.sink.model.SinkMessageProtos.internal_static_SinkMessage_descriptor;
    }

    @java.lang.Override
    public org.opennms.core.ipc.sink.model.SinkMessage getDefaultInstanceForType() {
      return org.opennms.core.ipc.sink.model.SinkMessage.getDefaultInstance();
    }

    @java.lang.Override
    public org.opennms.core.ipc.sink.model.SinkMessage build() {
      org.opennms.core.ipc.sink.model.SinkMessage result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public org.opennms.core.ipc.sink.model.SinkMessage buildPartial() {
      org.opennms.core.ipc.sink.model.SinkMessage result = new org.opennms.core.ipc.sink.model.SinkMessage(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(org.opennms.core.ipc.sink.model.SinkMessage result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.messageId_ = messageId_;
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.content_ = content_;
      }
      if (((from_bitField0_ & 0x00000004) != 0)) {
        result.currentChunkNumber_ = currentChunkNumber_;
      }
      if (((from_bitField0_ & 0x00000008) != 0)) {
        result.totalChunks_ = totalChunks_;
      }
      if (((from_bitField0_ & 0x00000010) != 0)) {
        result.tracingInfo_ = internalGetTracingInfo();
        result.tracingInfo_.makeImmutable();
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
      if (other instanceof org.opennms.core.ipc.sink.model.SinkMessage) {
        return mergeFrom((org.opennms.core.ipc.sink.model.SinkMessage)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(org.opennms.core.ipc.sink.model.SinkMessage other) {
      if (other == org.opennms.core.ipc.sink.model.SinkMessage.getDefaultInstance()) return this;
      if (!other.getMessageId().isEmpty()) {
        messageId_ = other.messageId_;
        bitField0_ |= 0x00000001;
        onChanged();
      }
      if (other.getContent() != com.google.protobuf.ByteString.EMPTY) {
        setContent(other.getContent());
      }
      if (other.getCurrentChunkNumber() != 0) {
        setCurrentChunkNumber(other.getCurrentChunkNumber());
      }
      if (other.getTotalChunks() != 0) {
        setTotalChunks(other.getTotalChunks());
      }
      internalGetMutableTracingInfo().mergeFrom(
          other.internalGetTracingInfo());
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
              messageId_ = input.readStringRequireUtf8();
              bitField0_ |= 0x00000001;
              break;
            } // case 10
            case 18: {
              content_ = input.readBytes();
              bitField0_ |= 0x00000002;
              break;
            } // case 18
            case 24: {
              currentChunkNumber_ = input.readInt32();
              bitField0_ |= 0x00000004;
              break;
            } // case 24
            case 32: {
              totalChunks_ = input.readInt32();
              bitField0_ |= 0x00000008;
              break;
            } // case 32
            case 42: {
              com.google.protobuf.MapEntry<java.lang.String, java.lang.String>
              tracingInfo__ = input.readMessage(
                  TracingInfoDefaultEntryHolder.defaultEntry.getParserForType(), extensionRegistry);
              internalGetMutableTracingInfo().getMutableMap().put(
                  tracingInfo__.getKey(), tracingInfo__.getValue());
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

    private java.lang.Object messageId_ = "";
    /**
     * <code>string message_id = 1;</code>
     * @return The messageId.
     */
    public java.lang.String getMessageId() {
      java.lang.Object ref = messageId_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        messageId_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string message_id = 1;</code>
     * @return The bytes for messageId.
     */
    public com.google.protobuf.ByteString
        getMessageIdBytes() {
      java.lang.Object ref = messageId_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        messageId_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string message_id = 1;</code>
     * @param value The messageId to set.
     * @return This builder for chaining.
     */
    public Builder setMessageId(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      messageId_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>string message_id = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearMessageId() {
      messageId_ = getDefaultInstance().getMessageId();
      bitField0_ = (bitField0_ & ~0x00000001);
      onChanged();
      return this;
    }
    /**
     * <code>string message_id = 1;</code>
     * @param value The bytes for messageId to set.
     * @return This builder for chaining.
     */
    public Builder setMessageIdBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      messageId_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }

    private com.google.protobuf.ByteString content_ = com.google.protobuf.ByteString.EMPTY;
    /**
     * <code>bytes content = 2;</code>
     * @return The content.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString getContent() {
      return content_;
    }
    /**
     * <code>bytes content = 2;</code>
     * @param value The content to set.
     * @return This builder for chaining.
     */
    public Builder setContent(com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      content_ = value;
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>bytes content = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearContent() {
      bitField0_ = (bitField0_ & ~0x00000002);
      content_ = getDefaultInstance().getContent();
      onChanged();
      return this;
    }

    private int currentChunkNumber_ ;
    /**
     * <code>int32 current_chunk_number = 3;</code>
     * @return The currentChunkNumber.
     */
    @java.lang.Override
    public int getCurrentChunkNumber() {
      return currentChunkNumber_;
    }
    /**
     * <code>int32 current_chunk_number = 3;</code>
     * @param value The currentChunkNumber to set.
     * @return This builder for chaining.
     */
    public Builder setCurrentChunkNumber(int value) {

      currentChunkNumber_ = value;
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }
    /**
     * <code>int32 current_chunk_number = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearCurrentChunkNumber() {
      bitField0_ = (bitField0_ & ~0x00000004);
      currentChunkNumber_ = 0;
      onChanged();
      return this;
    }

    private int totalChunks_ ;
    /**
     * <code>int32 total_chunks = 4;</code>
     * @return The totalChunks.
     */
    @java.lang.Override
    public int getTotalChunks() {
      return totalChunks_;
    }
    /**
     * <code>int32 total_chunks = 4;</code>
     * @param value The totalChunks to set.
     * @return This builder for chaining.
     */
    public Builder setTotalChunks(int value) {

      totalChunks_ = value;
      bitField0_ |= 0x00000008;
      onChanged();
      return this;
    }
    /**
     * <code>int32 total_chunks = 4;</code>
     * @return This builder for chaining.
     */
    public Builder clearTotalChunks() {
      bitField0_ = (bitField0_ & ~0x00000008);
      totalChunks_ = 0;
      onChanged();
      return this;
    }

    private com.google.protobuf.MapField<
        java.lang.String, java.lang.String> tracingInfo_;
    private com.google.protobuf.MapField<java.lang.String, java.lang.String>
        internalGetTracingInfo() {
      if (tracingInfo_ == null) {
        return com.google.protobuf.MapField.emptyMapField(
            TracingInfoDefaultEntryHolder.defaultEntry);
      }
      return tracingInfo_;
    }
    private com.google.protobuf.MapField<java.lang.String, java.lang.String>
        internalGetMutableTracingInfo() {
      if (tracingInfo_ == null) {
        tracingInfo_ = com.google.protobuf.MapField.newMapField(
            TracingInfoDefaultEntryHolder.defaultEntry);
      }
      if (!tracingInfo_.isMutable()) {
        tracingInfo_ = tracingInfo_.copy();
      }
      bitField0_ |= 0x00000010;
      onChanged();
      return tracingInfo_;
    }
    public int getTracingInfoCount() {
      return internalGetTracingInfo().getMap().size();
    }
    /**
     * <code>map&lt;string, string&gt; tracing_info = 5;</code>
     */
    @java.lang.Override
    public boolean containsTracingInfo(
        java.lang.String key) {
      if (key == null) { throw new NullPointerException("map key"); }
      return internalGetTracingInfo().getMap().containsKey(key);
    }
    /**
     * Use {@link #getTracingInfoMap()} instead.
     */
    @java.lang.Override
    @java.lang.Deprecated
    public java.util.Map<java.lang.String, java.lang.String> getTracingInfo() {
      return getTracingInfoMap();
    }
    /**
     * <code>map&lt;string, string&gt; tracing_info = 5;</code>
     */
    @java.lang.Override
    public java.util.Map<java.lang.String, java.lang.String> getTracingInfoMap() {
      return internalGetTracingInfo().getMap();
    }
    /**
     * <code>map&lt;string, string&gt; tracing_info = 5;</code>
     */
    @java.lang.Override
    public /* nullable */
java.lang.String getTracingInfoOrDefault(
        java.lang.String key,
        /* nullable */
java.lang.String defaultValue) {
      if (key == null) { throw new NullPointerException("map key"); }
      java.util.Map<java.lang.String, java.lang.String> map =
          internalGetTracingInfo().getMap();
      return map.containsKey(key) ? map.get(key) : defaultValue;
    }
    /**
     * <code>map&lt;string, string&gt; tracing_info = 5;</code>
     */
    @java.lang.Override
    public java.lang.String getTracingInfoOrThrow(
        java.lang.String key) {
      if (key == null) { throw new NullPointerException("map key"); }
      java.util.Map<java.lang.String, java.lang.String> map =
          internalGetTracingInfo().getMap();
      if (!map.containsKey(key)) {
        throw new java.lang.IllegalArgumentException();
      }
      return map.get(key);
    }
    public Builder clearTracingInfo() {
      bitField0_ = (bitField0_ & ~0x00000010);
      internalGetMutableTracingInfo().getMutableMap()
          .clear();
      return this;
    }
    /**
     * <code>map&lt;string, string&gt; tracing_info = 5;</code>
     */
    public Builder removeTracingInfo(
        java.lang.String key) {
      if (key == null) { throw new NullPointerException("map key"); }
      internalGetMutableTracingInfo().getMutableMap()
          .remove(key);
      return this;
    }
    /**
     * Use alternate mutation accessors instead.
     */
    @java.lang.Deprecated
    public java.util.Map<java.lang.String, java.lang.String>
        getMutableTracingInfo() {
      bitField0_ |= 0x00000010;
      return internalGetMutableTracingInfo().getMutableMap();
    }
    /**
     * <code>map&lt;string, string&gt; tracing_info = 5;</code>
     */
    public Builder putTracingInfo(
        java.lang.String key,
        java.lang.String value) {
      if (key == null) { throw new NullPointerException("map key"); }
      if (value == null) { throw new NullPointerException("map value"); }
      internalGetMutableTracingInfo().getMutableMap()
          .put(key, value);
      bitField0_ |= 0x00000010;
      return this;
    }
    /**
     * <code>map&lt;string, string&gt; tracing_info = 5;</code>
     */
    public Builder putAllTracingInfo(
        java.util.Map<java.lang.String, java.lang.String> values) {
      internalGetMutableTracingInfo().getMutableMap()
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


    // @@protoc_insertion_point(builder_scope:SinkMessage)
  }

  // @@protoc_insertion_point(class_scope:SinkMessage)
  private static final org.opennms.core.ipc.sink.model.SinkMessage DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new org.opennms.core.ipc.sink.model.SinkMessage();
  }

  public static org.opennms.core.ipc.sink.model.SinkMessage getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<SinkMessage>
      PARSER = new com.google.protobuf.AbstractParser<SinkMessage>() {
    @java.lang.Override
    public SinkMessage parsePartialFrom(
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

  public static com.google.protobuf.Parser<SinkMessage> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<SinkMessage> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public org.opennms.core.ipc.sink.model.SinkMessage getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

