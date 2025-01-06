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
package org.opennms.core.ipc.twin.model;

/**
 * <pre>
 *
 *Twin Response object sent by OpenNMS for RPC response as well as for Sink update.
 * </pre>
 *
 * Protobuf type {@code TwinResponseProto}
 */
public final class TwinResponseProto extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:TwinResponseProto)
    TwinResponseProtoOrBuilder {
private static final long serialVersionUID = 0L;
  // Use TwinResponseProto.newBuilder() to construct.
  private TwinResponseProto(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private TwinResponseProto() {
    consumerKey_ = "";
    twinObject_ = com.google.protobuf.ByteString.EMPTY;
    systemId_ = "";
    location_ = "";
    sessionId_ = "";
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new TwinResponseProto();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return org.opennms.core.ipc.twin.model.TwinMessageProto.internal_static_TwinResponseProto_descriptor;
  }

  @SuppressWarnings({"rawtypes"})
  @java.lang.Override
  protected com.google.protobuf.MapFieldReflectionAccessor internalGetMapFieldReflection(
      int number) {
    switch (number) {
      case 8:
        return internalGetTracingInfo();
      default:
        throw new RuntimeException(
            "Invalid map field number: " + number);
    }
  }
  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return org.opennms.core.ipc.twin.model.TwinMessageProto.internal_static_TwinResponseProto_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            org.opennms.core.ipc.twin.model.TwinResponseProto.class, org.opennms.core.ipc.twin.model.TwinResponseProto.Builder.class);
  }

  public static final int CONSUMER_KEY_FIELD_NUMBER = 1;
  @SuppressWarnings("serial")
  private volatile java.lang.Object consumerKey_ = "";
  /**
   * <code>string consumer_key = 1;</code>
   * @return The consumerKey.
   */
  @java.lang.Override
  public java.lang.String getConsumerKey() {
    java.lang.Object ref = consumerKey_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      consumerKey_ = s;
      return s;
    }
  }
  /**
   * <code>string consumer_key = 1;</code>
   * @return The bytes for consumerKey.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getConsumerKeyBytes() {
    java.lang.Object ref = consumerKey_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      consumerKey_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int TWIN_OBJECT_FIELD_NUMBER = 2;
  private com.google.protobuf.ByteString twinObject_ = com.google.protobuf.ByteString.EMPTY;
  /**
   * <code>bytes twin_object = 2;</code>
   * @return The twinObject.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString getTwinObject() {
    return twinObject_;
  }

  public static final int SYSTEM_ID_FIELD_NUMBER = 3;
  @SuppressWarnings("serial")
  private volatile java.lang.Object systemId_ = "";
  /**
   * <code>string system_id = 3;</code>
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
      systemId_ = s;
      return s;
    }
  }
  /**
   * <code>string system_id = 3;</code>
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

  public static final int LOCATION_FIELD_NUMBER = 4;
  @SuppressWarnings("serial")
  private volatile java.lang.Object location_ = "";
  /**
   * <code>string location = 4;</code>
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
      location_ = s;
      return s;
    }
  }
  /**
   * <code>string location = 4;</code>
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

  public static final int IS_PATCH_OBJECT_FIELD_NUMBER = 5;
  private boolean isPatchObject_ = false;
  /**
   * <code>bool is_patch_object = 5;</code>
   * @return The isPatchObject.
   */
  @java.lang.Override
  public boolean getIsPatchObject() {
    return isPatchObject_;
  }

  public static final int SESSION_ID_FIELD_NUMBER = 6;
  @SuppressWarnings("serial")
  private volatile java.lang.Object sessionId_ = "";
  /**
   * <code>string session_id = 6;</code>
   * @return The sessionId.
   */
  @java.lang.Override
  public java.lang.String getSessionId() {
    java.lang.Object ref = sessionId_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      sessionId_ = s;
      return s;
    }
  }
  /**
   * <code>string session_id = 6;</code>
   * @return The bytes for sessionId.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getSessionIdBytes() {
    java.lang.Object ref = sessionId_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      sessionId_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int VERSION_FIELD_NUMBER = 7;
  private int version_ = 0;
  /**
   * <code>int32 version = 7;</code>
   * @return The version.
   */
  @java.lang.Override
  public int getVersion() {
    return version_;
  }

  public static final int TRACING_INFO_FIELD_NUMBER = 8;
  private static final class TracingInfoDefaultEntryHolder {
    static final com.google.protobuf.MapEntry<
        java.lang.String, java.lang.String> defaultEntry =
            com.google.protobuf.MapEntry
            .<java.lang.String, java.lang.String>newDefaultInstance(
                org.opennms.core.ipc.twin.model.TwinMessageProto.internal_static_TwinResponseProto_TracingInfoEntry_descriptor, 
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
   * <code>map&lt;string, string&gt; tracing_info = 8;</code>
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
   * <code>map&lt;string, string&gt; tracing_info = 8;</code>
   */
  @java.lang.Override
  public java.util.Map<java.lang.String, java.lang.String> getTracingInfoMap() {
    return internalGetTracingInfo().getMap();
  }
  /**
   * <code>map&lt;string, string&gt; tracing_info = 8;</code>
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
   * <code>map&lt;string, string&gt; tracing_info = 8;</code>
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
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(consumerKey_)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, consumerKey_);
    }
    if (!twinObject_.isEmpty()) {
      output.writeBytes(2, twinObject_);
    }
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(systemId_)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 3, systemId_);
    }
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(location_)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 4, location_);
    }
    if (isPatchObject_ != false) {
      output.writeBool(5, isPatchObject_);
    }
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(sessionId_)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 6, sessionId_);
    }
    if (version_ != 0) {
      output.writeInt32(7, version_);
    }
    com.google.protobuf.GeneratedMessageV3
      .serializeStringMapTo(
        output,
        internalGetTracingInfo(),
        TracingInfoDefaultEntryHolder.defaultEntry,
        8);
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(consumerKey_)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, consumerKey_);
    }
    if (!twinObject_.isEmpty()) {
      size += com.google.protobuf.CodedOutputStream
        .computeBytesSize(2, twinObject_);
    }
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(systemId_)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(3, systemId_);
    }
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(location_)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(4, location_);
    }
    if (isPatchObject_ != false) {
      size += com.google.protobuf.CodedOutputStream
        .computeBoolSize(5, isPatchObject_);
    }
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(sessionId_)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(6, sessionId_);
    }
    if (version_ != 0) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt32Size(7, version_);
    }
    for (java.util.Map.Entry<java.lang.String, java.lang.String> entry
         : internalGetTracingInfo().getMap().entrySet()) {
      com.google.protobuf.MapEntry<java.lang.String, java.lang.String>
      tracingInfo__ = TracingInfoDefaultEntryHolder.defaultEntry.newBuilderForType()
          .setKey(entry.getKey())
          .setValue(entry.getValue())
          .build();
      size += com.google.protobuf.CodedOutputStream
          .computeMessageSize(8, tracingInfo__);
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
    if (!(obj instanceof org.opennms.core.ipc.twin.model.TwinResponseProto)) {
      return super.equals(obj);
    }
    org.opennms.core.ipc.twin.model.TwinResponseProto other = (org.opennms.core.ipc.twin.model.TwinResponseProto) obj;

    if (!getConsumerKey()
        .equals(other.getConsumerKey())) return false;
    if (!getTwinObject()
        .equals(other.getTwinObject())) return false;
    if (!getSystemId()
        .equals(other.getSystemId())) return false;
    if (!getLocation()
        .equals(other.getLocation())) return false;
    if (getIsPatchObject()
        != other.getIsPatchObject()) return false;
    if (!getSessionId()
        .equals(other.getSessionId())) return false;
    if (getVersion()
        != other.getVersion()) return false;
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
    hash = (37 * hash) + CONSUMER_KEY_FIELD_NUMBER;
    hash = (53 * hash) + getConsumerKey().hashCode();
    hash = (37 * hash) + TWIN_OBJECT_FIELD_NUMBER;
    hash = (53 * hash) + getTwinObject().hashCode();
    hash = (37 * hash) + SYSTEM_ID_FIELD_NUMBER;
    hash = (53 * hash) + getSystemId().hashCode();
    hash = (37 * hash) + LOCATION_FIELD_NUMBER;
    hash = (53 * hash) + getLocation().hashCode();
    hash = (37 * hash) + IS_PATCH_OBJECT_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashBoolean(
        getIsPatchObject());
    hash = (37 * hash) + SESSION_ID_FIELD_NUMBER;
    hash = (53 * hash) + getSessionId().hashCode();
    hash = (37 * hash) + VERSION_FIELD_NUMBER;
    hash = (53 * hash) + getVersion();
    if (!internalGetTracingInfo().getMap().isEmpty()) {
      hash = (37 * hash) + TRACING_INFO_FIELD_NUMBER;
      hash = (53 * hash) + internalGetTracingInfo().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static org.opennms.core.ipc.twin.model.TwinResponseProto parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.opennms.core.ipc.twin.model.TwinResponseProto parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.opennms.core.ipc.twin.model.TwinResponseProto parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.opennms.core.ipc.twin.model.TwinResponseProto parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.opennms.core.ipc.twin.model.TwinResponseProto parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.opennms.core.ipc.twin.model.TwinResponseProto parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.opennms.core.ipc.twin.model.TwinResponseProto parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static org.opennms.core.ipc.twin.model.TwinResponseProto parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static org.opennms.core.ipc.twin.model.TwinResponseProto parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static org.opennms.core.ipc.twin.model.TwinResponseProto parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static org.opennms.core.ipc.twin.model.TwinResponseProto parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static org.opennms.core.ipc.twin.model.TwinResponseProto parseFrom(
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
  public static Builder newBuilder(org.opennms.core.ipc.twin.model.TwinResponseProto prototype) {
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
   * <pre>
   *
   *Twin Response object sent by OpenNMS for RPC response as well as for Sink update.
   * </pre>
   *
   * Protobuf type {@code TwinResponseProto}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:TwinResponseProto)
      org.opennms.core.ipc.twin.model.TwinResponseProtoOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return org.opennms.core.ipc.twin.model.TwinMessageProto.internal_static_TwinResponseProto_descriptor;
    }

    @SuppressWarnings({"rawtypes"})
    protected com.google.protobuf.MapFieldReflectionAccessor internalGetMapFieldReflection(
        int number) {
      switch (number) {
        case 8:
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
        case 8:
          return internalGetMutableTracingInfo();
        default:
          throw new RuntimeException(
              "Invalid map field number: " + number);
      }
    }
    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return org.opennms.core.ipc.twin.model.TwinMessageProto.internal_static_TwinResponseProto_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              org.opennms.core.ipc.twin.model.TwinResponseProto.class, org.opennms.core.ipc.twin.model.TwinResponseProto.Builder.class);
    }

    // Construct using org.opennms.core.ipc.twin.model.TwinResponseProto.newBuilder()
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
      consumerKey_ = "";
      twinObject_ = com.google.protobuf.ByteString.EMPTY;
      systemId_ = "";
      location_ = "";
      isPatchObject_ = false;
      sessionId_ = "";
      version_ = 0;
      internalGetMutableTracingInfo().clear();
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return org.opennms.core.ipc.twin.model.TwinMessageProto.internal_static_TwinResponseProto_descriptor;
    }

    @java.lang.Override
    public org.opennms.core.ipc.twin.model.TwinResponseProto getDefaultInstanceForType() {
      return org.opennms.core.ipc.twin.model.TwinResponseProto.getDefaultInstance();
    }

    @java.lang.Override
    public org.opennms.core.ipc.twin.model.TwinResponseProto build() {
      org.opennms.core.ipc.twin.model.TwinResponseProto result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public org.opennms.core.ipc.twin.model.TwinResponseProto buildPartial() {
      org.opennms.core.ipc.twin.model.TwinResponseProto result = new org.opennms.core.ipc.twin.model.TwinResponseProto(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(org.opennms.core.ipc.twin.model.TwinResponseProto result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.consumerKey_ = consumerKey_;
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.twinObject_ = twinObject_;
      }
      if (((from_bitField0_ & 0x00000004) != 0)) {
        result.systemId_ = systemId_;
      }
      if (((from_bitField0_ & 0x00000008) != 0)) {
        result.location_ = location_;
      }
      if (((from_bitField0_ & 0x00000010) != 0)) {
        result.isPatchObject_ = isPatchObject_;
      }
      if (((from_bitField0_ & 0x00000020) != 0)) {
        result.sessionId_ = sessionId_;
      }
      if (((from_bitField0_ & 0x00000040) != 0)) {
        result.version_ = version_;
      }
      if (((from_bitField0_ & 0x00000080) != 0)) {
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
      if (other instanceof org.opennms.core.ipc.twin.model.TwinResponseProto) {
        return mergeFrom((org.opennms.core.ipc.twin.model.TwinResponseProto)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(org.opennms.core.ipc.twin.model.TwinResponseProto other) {
      if (other == org.opennms.core.ipc.twin.model.TwinResponseProto.getDefaultInstance()) return this;
      if (!other.getConsumerKey().isEmpty()) {
        consumerKey_ = other.consumerKey_;
        bitField0_ |= 0x00000001;
        onChanged();
      }
      if (other.getTwinObject() != com.google.protobuf.ByteString.EMPTY) {
        setTwinObject(other.getTwinObject());
      }
      if (!other.getSystemId().isEmpty()) {
        systemId_ = other.systemId_;
        bitField0_ |= 0x00000004;
        onChanged();
      }
      if (!other.getLocation().isEmpty()) {
        location_ = other.location_;
        bitField0_ |= 0x00000008;
        onChanged();
      }
      if (other.getIsPatchObject() != false) {
        setIsPatchObject(other.getIsPatchObject());
      }
      if (!other.getSessionId().isEmpty()) {
        sessionId_ = other.sessionId_;
        bitField0_ |= 0x00000020;
        onChanged();
      }
      if (other.getVersion() != 0) {
        setVersion(other.getVersion());
      }
      internalGetMutableTracingInfo().mergeFrom(
          other.internalGetTracingInfo());
      bitField0_ |= 0x00000080;
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
              consumerKey_ = input.readStringRequireUtf8();
              bitField0_ |= 0x00000001;
              break;
            } // case 10
            case 18: {
              twinObject_ = input.readBytes();
              bitField0_ |= 0x00000002;
              break;
            } // case 18
            case 26: {
              systemId_ = input.readStringRequireUtf8();
              bitField0_ |= 0x00000004;
              break;
            } // case 26
            case 34: {
              location_ = input.readStringRequireUtf8();
              bitField0_ |= 0x00000008;
              break;
            } // case 34
            case 40: {
              isPatchObject_ = input.readBool();
              bitField0_ |= 0x00000010;
              break;
            } // case 40
            case 50: {
              sessionId_ = input.readStringRequireUtf8();
              bitField0_ |= 0x00000020;
              break;
            } // case 50
            case 56: {
              version_ = input.readInt32();
              bitField0_ |= 0x00000040;
              break;
            } // case 56
            case 66: {
              com.google.protobuf.MapEntry<java.lang.String, java.lang.String>
              tracingInfo__ = input.readMessage(
                  TracingInfoDefaultEntryHolder.defaultEntry.getParserForType(), extensionRegistry);
              internalGetMutableTracingInfo().getMutableMap().put(
                  tracingInfo__.getKey(), tracingInfo__.getValue());
              bitField0_ |= 0x00000080;
              break;
            } // case 66
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

    private java.lang.Object consumerKey_ = "";
    /**
     * <code>string consumer_key = 1;</code>
     * @return The consumerKey.
     */
    public java.lang.String getConsumerKey() {
      java.lang.Object ref = consumerKey_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        consumerKey_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string consumer_key = 1;</code>
     * @return The bytes for consumerKey.
     */
    public com.google.protobuf.ByteString
        getConsumerKeyBytes() {
      java.lang.Object ref = consumerKey_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        consumerKey_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string consumer_key = 1;</code>
     * @param value The consumerKey to set.
     * @return This builder for chaining.
     */
    public Builder setConsumerKey(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      consumerKey_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>string consumer_key = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearConsumerKey() {
      consumerKey_ = getDefaultInstance().getConsumerKey();
      bitField0_ = (bitField0_ & ~0x00000001);
      onChanged();
      return this;
    }
    /**
     * <code>string consumer_key = 1;</code>
     * @param value The bytes for consumerKey to set.
     * @return This builder for chaining.
     */
    public Builder setConsumerKeyBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      consumerKey_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }

    private com.google.protobuf.ByteString twinObject_ = com.google.protobuf.ByteString.EMPTY;
    /**
     * <code>bytes twin_object = 2;</code>
     * @return The twinObject.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString getTwinObject() {
      return twinObject_;
    }
    /**
     * <code>bytes twin_object = 2;</code>
     * @param value The twinObject to set.
     * @return This builder for chaining.
     */
    public Builder setTwinObject(com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      twinObject_ = value;
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>bytes twin_object = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearTwinObject() {
      bitField0_ = (bitField0_ & ~0x00000002);
      twinObject_ = getDefaultInstance().getTwinObject();
      onChanged();
      return this;
    }

    private java.lang.Object systemId_ = "";
    /**
     * <code>string system_id = 3;</code>
     * @return The systemId.
     */
    public java.lang.String getSystemId() {
      java.lang.Object ref = systemId_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        systemId_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string system_id = 3;</code>
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
     * <code>string system_id = 3;</code>
     * @param value The systemId to set.
     * @return This builder for chaining.
     */
    public Builder setSystemId(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      systemId_ = value;
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }
    /**
     * <code>string system_id = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearSystemId() {
      systemId_ = getDefaultInstance().getSystemId();
      bitField0_ = (bitField0_ & ~0x00000004);
      onChanged();
      return this;
    }
    /**
     * <code>string system_id = 3;</code>
     * @param value The bytes for systemId to set.
     * @return This builder for chaining.
     */
    public Builder setSystemIdBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      systemId_ = value;
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }

    private java.lang.Object location_ = "";
    /**
     * <code>string location = 4;</code>
     * @return The location.
     */
    public java.lang.String getLocation() {
      java.lang.Object ref = location_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        location_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string location = 4;</code>
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
     * <code>string location = 4;</code>
     * @param value The location to set.
     * @return This builder for chaining.
     */
    public Builder setLocation(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      location_ = value;
      bitField0_ |= 0x00000008;
      onChanged();
      return this;
    }
    /**
     * <code>string location = 4;</code>
     * @return This builder for chaining.
     */
    public Builder clearLocation() {
      location_ = getDefaultInstance().getLocation();
      bitField0_ = (bitField0_ & ~0x00000008);
      onChanged();
      return this;
    }
    /**
     * <code>string location = 4;</code>
     * @param value The bytes for location to set.
     * @return This builder for chaining.
     */
    public Builder setLocationBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      location_ = value;
      bitField0_ |= 0x00000008;
      onChanged();
      return this;
    }

    private boolean isPatchObject_ ;
    /**
     * <code>bool is_patch_object = 5;</code>
     * @return The isPatchObject.
     */
    @java.lang.Override
    public boolean getIsPatchObject() {
      return isPatchObject_;
    }
    /**
     * <code>bool is_patch_object = 5;</code>
     * @param value The isPatchObject to set.
     * @return This builder for chaining.
     */
    public Builder setIsPatchObject(boolean value) {

      isPatchObject_ = value;
      bitField0_ |= 0x00000010;
      onChanged();
      return this;
    }
    /**
     * <code>bool is_patch_object = 5;</code>
     * @return This builder for chaining.
     */
    public Builder clearIsPatchObject() {
      bitField0_ = (bitField0_ & ~0x00000010);
      isPatchObject_ = false;
      onChanged();
      return this;
    }

    private java.lang.Object sessionId_ = "";
    /**
     * <code>string session_id = 6;</code>
     * @return The sessionId.
     */
    public java.lang.String getSessionId() {
      java.lang.Object ref = sessionId_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        sessionId_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string session_id = 6;</code>
     * @return The bytes for sessionId.
     */
    public com.google.protobuf.ByteString
        getSessionIdBytes() {
      java.lang.Object ref = sessionId_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        sessionId_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string session_id = 6;</code>
     * @param value The sessionId to set.
     * @return This builder for chaining.
     */
    public Builder setSessionId(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      sessionId_ = value;
      bitField0_ |= 0x00000020;
      onChanged();
      return this;
    }
    /**
     * <code>string session_id = 6;</code>
     * @return This builder for chaining.
     */
    public Builder clearSessionId() {
      sessionId_ = getDefaultInstance().getSessionId();
      bitField0_ = (bitField0_ & ~0x00000020);
      onChanged();
      return this;
    }
    /**
     * <code>string session_id = 6;</code>
     * @param value The bytes for sessionId to set.
     * @return This builder for chaining.
     */
    public Builder setSessionIdBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      sessionId_ = value;
      bitField0_ |= 0x00000020;
      onChanged();
      return this;
    }

    private int version_ ;
    /**
     * <code>int32 version = 7;</code>
     * @return The version.
     */
    @java.lang.Override
    public int getVersion() {
      return version_;
    }
    /**
     * <code>int32 version = 7;</code>
     * @param value The version to set.
     * @return This builder for chaining.
     */
    public Builder setVersion(int value) {

      version_ = value;
      bitField0_ |= 0x00000040;
      onChanged();
      return this;
    }
    /**
     * <code>int32 version = 7;</code>
     * @return This builder for chaining.
     */
    public Builder clearVersion() {
      bitField0_ = (bitField0_ & ~0x00000040);
      version_ = 0;
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
      bitField0_ |= 0x00000080;
      onChanged();
      return tracingInfo_;
    }
    public int getTracingInfoCount() {
      return internalGetTracingInfo().getMap().size();
    }
    /**
     * <code>map&lt;string, string&gt; tracing_info = 8;</code>
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
     * <code>map&lt;string, string&gt; tracing_info = 8;</code>
     */
    @java.lang.Override
    public java.util.Map<java.lang.String, java.lang.String> getTracingInfoMap() {
      return internalGetTracingInfo().getMap();
    }
    /**
     * <code>map&lt;string, string&gt; tracing_info = 8;</code>
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
     * <code>map&lt;string, string&gt; tracing_info = 8;</code>
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
      bitField0_ = (bitField0_ & ~0x00000080);
      internalGetMutableTracingInfo().getMutableMap()
          .clear();
      return this;
    }
    /**
     * <code>map&lt;string, string&gt; tracing_info = 8;</code>
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
      bitField0_ |= 0x00000080;
      return internalGetMutableTracingInfo().getMutableMap();
    }
    /**
     * <code>map&lt;string, string&gt; tracing_info = 8;</code>
     */
    public Builder putTracingInfo(
        java.lang.String key,
        java.lang.String value) {
      if (key == null) { throw new NullPointerException("map key"); }
      if (value == null) { throw new NullPointerException("map value"); }
      internalGetMutableTracingInfo().getMutableMap()
          .put(key, value);
      bitField0_ |= 0x00000080;
      return this;
    }
    /**
     * <code>map&lt;string, string&gt; tracing_info = 8;</code>
     */
    public Builder putAllTracingInfo(
        java.util.Map<java.lang.String, java.lang.String> values) {
      internalGetMutableTracingInfo().getMutableMap()
          .putAll(values);
      bitField0_ |= 0x00000080;
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


    // @@protoc_insertion_point(builder_scope:TwinResponseProto)
  }

  // @@protoc_insertion_point(class_scope:TwinResponseProto)
  private static final org.opennms.core.ipc.twin.model.TwinResponseProto DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new org.opennms.core.ipc.twin.model.TwinResponseProto();
  }

  public static org.opennms.core.ipc.twin.model.TwinResponseProto getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<TwinResponseProto>
      PARSER = new com.google.protobuf.AbstractParser<TwinResponseProto>() {
    @java.lang.Override
    public TwinResponseProto parsePartialFrom(
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

  public static com.google.protobuf.Parser<TwinResponseProto> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<TwinResponseProto> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public org.opennms.core.ipc.twin.model.TwinResponseProto getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

