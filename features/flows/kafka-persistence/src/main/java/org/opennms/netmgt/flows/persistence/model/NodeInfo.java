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
package org.opennms.netmgt.flows.persistence.model;

/**
 * Protobuf type {@code NodeInfo}
 */
public final class NodeInfo extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:NodeInfo)
    NodeInfoOrBuilder {
private static final long serialVersionUID = 0L;
  // Use NodeInfo.newBuilder() to construct.
  private NodeInfo(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private NodeInfo() {
    foreignSource_ = "";
    foreginId_ = "";
    categories_ =
        com.google.protobuf.LazyStringArrayList.emptyList();
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new NodeInfo();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return org.opennms.netmgt.flows.persistence.model.EnrichedFlowProtos.internal_static_NodeInfo_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return org.opennms.netmgt.flows.persistence.model.EnrichedFlowProtos.internal_static_NodeInfo_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            org.opennms.netmgt.flows.persistence.model.NodeInfo.class, org.opennms.netmgt.flows.persistence.model.NodeInfo.Builder.class);
  }

  public static final int FOREIGN_SOURCE_FIELD_NUMBER = 1;
  @SuppressWarnings("serial")
  private volatile java.lang.Object foreignSource_ = "";
  /**
   * <code>string foreign_source = 1;</code>
   * @return The foreignSource.
   */
  @java.lang.Override
  public java.lang.String getForeignSource() {
    java.lang.Object ref = foreignSource_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      foreignSource_ = s;
      return s;
    }
  }
  /**
   * <code>string foreign_source = 1;</code>
   * @return The bytes for foreignSource.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getForeignSourceBytes() {
    java.lang.Object ref = foreignSource_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      foreignSource_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int FOREGIN_ID_FIELD_NUMBER = 2;
  @SuppressWarnings("serial")
  private volatile java.lang.Object foreginId_ = "";
  /**
   * <code>string foregin_id = 2;</code>
   * @return The foreginId.
   */
  @java.lang.Override
  public java.lang.String getForeginId() {
    java.lang.Object ref = foreginId_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      foreginId_ = s;
      return s;
    }
  }
  /**
   * <code>string foregin_id = 2;</code>
   * @return The bytes for foreginId.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getForeginIdBytes() {
    java.lang.Object ref = foreginId_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      foreginId_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int NODE_ID_FIELD_NUMBER = 3;
  private int nodeId_ = 0;
  /**
   * <code>uint32 node_id = 3;</code>
   * @return The nodeId.
   */
  @java.lang.Override
  public int getNodeId() {
    return nodeId_;
  }

  public static final int CATEGORIES_FIELD_NUMBER = 4;
  @SuppressWarnings("serial")
  private com.google.protobuf.LazyStringArrayList categories_ =
      com.google.protobuf.LazyStringArrayList.emptyList();
  /**
   * <code>repeated string categories = 4;</code>
   * @return A list containing the categories.
   */
  public com.google.protobuf.ProtocolStringList
      getCategoriesList() {
    return categories_;
  }
  /**
   * <code>repeated string categories = 4;</code>
   * @return The count of categories.
   */
  public int getCategoriesCount() {
    return categories_.size();
  }
  /**
   * <code>repeated string categories = 4;</code>
   * @param index The index of the element to return.
   * @return The categories at the given index.
   */
  public java.lang.String getCategories(int index) {
    return categories_.get(index);
  }
  /**
   * <code>repeated string categories = 4;</code>
   * @param index The index of the value to return.
   * @return The bytes of the categories at the given index.
   */
  public com.google.protobuf.ByteString
      getCategoriesBytes(int index) {
    return categories_.getByteString(index);
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
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(foreignSource_)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, foreignSource_);
    }
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(foreginId_)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 2, foreginId_);
    }
    if (nodeId_ != 0) {
      output.writeUInt32(3, nodeId_);
    }
    for (int i = 0; i < categories_.size(); i++) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 4, categories_.getRaw(i));
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(foreignSource_)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, foreignSource_);
    }
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(foreginId_)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(2, foreginId_);
    }
    if (nodeId_ != 0) {
      size += com.google.protobuf.CodedOutputStream
        .computeUInt32Size(3, nodeId_);
    }
    {
      int dataSize = 0;
      for (int i = 0; i < categories_.size(); i++) {
        dataSize += computeStringSizeNoTag(categories_.getRaw(i));
      }
      size += dataSize;
      size += 1 * getCategoriesList().size();
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
    if (!(obj instanceof org.opennms.netmgt.flows.persistence.model.NodeInfo)) {
      return super.equals(obj);
    }
    org.opennms.netmgt.flows.persistence.model.NodeInfo other = (org.opennms.netmgt.flows.persistence.model.NodeInfo) obj;

    if (!getForeignSource()
        .equals(other.getForeignSource())) return false;
    if (!getForeginId()
        .equals(other.getForeginId())) return false;
    if (getNodeId()
        != other.getNodeId()) return false;
    if (!getCategoriesList()
        .equals(other.getCategoriesList())) return false;
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
    hash = (37 * hash) + FOREIGN_SOURCE_FIELD_NUMBER;
    hash = (53 * hash) + getForeignSource().hashCode();
    hash = (37 * hash) + FOREGIN_ID_FIELD_NUMBER;
    hash = (53 * hash) + getForeginId().hashCode();
    hash = (37 * hash) + NODE_ID_FIELD_NUMBER;
    hash = (53 * hash) + getNodeId();
    if (getCategoriesCount() > 0) {
      hash = (37 * hash) + CATEGORIES_FIELD_NUMBER;
      hash = (53 * hash) + getCategoriesList().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static org.opennms.netmgt.flows.persistence.model.NodeInfo parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.opennms.netmgt.flows.persistence.model.NodeInfo parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.opennms.netmgt.flows.persistence.model.NodeInfo parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.opennms.netmgt.flows.persistence.model.NodeInfo parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.opennms.netmgt.flows.persistence.model.NodeInfo parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.opennms.netmgt.flows.persistence.model.NodeInfo parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.opennms.netmgt.flows.persistence.model.NodeInfo parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static org.opennms.netmgt.flows.persistence.model.NodeInfo parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static org.opennms.netmgt.flows.persistence.model.NodeInfo parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static org.opennms.netmgt.flows.persistence.model.NodeInfo parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static org.opennms.netmgt.flows.persistence.model.NodeInfo parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static org.opennms.netmgt.flows.persistence.model.NodeInfo parseFrom(
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
  public static Builder newBuilder(org.opennms.netmgt.flows.persistence.model.NodeInfo prototype) {
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
   * Protobuf type {@code NodeInfo}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:NodeInfo)
      org.opennms.netmgt.flows.persistence.model.NodeInfoOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return org.opennms.netmgt.flows.persistence.model.EnrichedFlowProtos.internal_static_NodeInfo_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return org.opennms.netmgt.flows.persistence.model.EnrichedFlowProtos.internal_static_NodeInfo_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              org.opennms.netmgt.flows.persistence.model.NodeInfo.class, org.opennms.netmgt.flows.persistence.model.NodeInfo.Builder.class);
    }

    // Construct using org.opennms.netmgt.flows.persistence.model.NodeInfo.newBuilder()
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
      foreignSource_ = "";
      foreginId_ = "";
      nodeId_ = 0;
      categories_ =
          com.google.protobuf.LazyStringArrayList.emptyList();
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return org.opennms.netmgt.flows.persistence.model.EnrichedFlowProtos.internal_static_NodeInfo_descriptor;
    }

    @java.lang.Override
    public org.opennms.netmgt.flows.persistence.model.NodeInfo getDefaultInstanceForType() {
      return org.opennms.netmgt.flows.persistence.model.NodeInfo.getDefaultInstance();
    }

    @java.lang.Override
    public org.opennms.netmgt.flows.persistence.model.NodeInfo build() {
      org.opennms.netmgt.flows.persistence.model.NodeInfo result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public org.opennms.netmgt.flows.persistence.model.NodeInfo buildPartial() {
      org.opennms.netmgt.flows.persistence.model.NodeInfo result = new org.opennms.netmgt.flows.persistence.model.NodeInfo(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(org.opennms.netmgt.flows.persistence.model.NodeInfo result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.foreignSource_ = foreignSource_;
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.foreginId_ = foreginId_;
      }
      if (((from_bitField0_ & 0x00000004) != 0)) {
        result.nodeId_ = nodeId_;
      }
      if (((from_bitField0_ & 0x00000008) != 0)) {
        categories_.makeImmutable();
        result.categories_ = categories_;
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
      if (other instanceof org.opennms.netmgt.flows.persistence.model.NodeInfo) {
        return mergeFrom((org.opennms.netmgt.flows.persistence.model.NodeInfo)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(org.opennms.netmgt.flows.persistence.model.NodeInfo other) {
      if (other == org.opennms.netmgt.flows.persistence.model.NodeInfo.getDefaultInstance()) return this;
      if (!other.getForeignSource().isEmpty()) {
        foreignSource_ = other.foreignSource_;
        bitField0_ |= 0x00000001;
        onChanged();
      }
      if (!other.getForeginId().isEmpty()) {
        foreginId_ = other.foreginId_;
        bitField0_ |= 0x00000002;
        onChanged();
      }
      if (other.getNodeId() != 0) {
        setNodeId(other.getNodeId());
      }
      if (!other.categories_.isEmpty()) {
        if (categories_.isEmpty()) {
          categories_ = other.categories_;
          bitField0_ |= 0x00000008;
        } else {
          ensureCategoriesIsMutable();
          categories_.addAll(other.categories_);
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
              foreignSource_ = input.readStringRequireUtf8();
              bitField0_ |= 0x00000001;
              break;
            } // case 10
            case 18: {
              foreginId_ = input.readStringRequireUtf8();
              bitField0_ |= 0x00000002;
              break;
            } // case 18
            case 24: {
              nodeId_ = input.readUInt32();
              bitField0_ |= 0x00000004;
              break;
            } // case 24
            case 34: {
              java.lang.String s = input.readStringRequireUtf8();
              ensureCategoriesIsMutable();
              categories_.add(s);
              break;
            } // case 34
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

    private java.lang.Object foreignSource_ = "";
    /**
     * <code>string foreign_source = 1;</code>
     * @return The foreignSource.
     */
    public java.lang.String getForeignSource() {
      java.lang.Object ref = foreignSource_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        foreignSource_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string foreign_source = 1;</code>
     * @return The bytes for foreignSource.
     */
    public com.google.protobuf.ByteString
        getForeignSourceBytes() {
      java.lang.Object ref = foreignSource_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        foreignSource_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string foreign_source = 1;</code>
     * @param value The foreignSource to set.
     * @return This builder for chaining.
     */
    public Builder setForeignSource(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      foreignSource_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>string foreign_source = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearForeignSource() {
      foreignSource_ = getDefaultInstance().getForeignSource();
      bitField0_ = (bitField0_ & ~0x00000001);
      onChanged();
      return this;
    }
    /**
     * <code>string foreign_source = 1;</code>
     * @param value The bytes for foreignSource to set.
     * @return This builder for chaining.
     */
    public Builder setForeignSourceBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      foreignSource_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }

    private java.lang.Object foreginId_ = "";
    /**
     * <code>string foregin_id = 2;</code>
     * @return The foreginId.
     */
    public java.lang.String getForeginId() {
      java.lang.Object ref = foreginId_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        foreginId_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string foregin_id = 2;</code>
     * @return The bytes for foreginId.
     */
    public com.google.protobuf.ByteString
        getForeginIdBytes() {
      java.lang.Object ref = foreginId_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        foreginId_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string foregin_id = 2;</code>
     * @param value The foreginId to set.
     * @return This builder for chaining.
     */
    public Builder setForeginId(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      foreginId_ = value;
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>string foregin_id = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearForeginId() {
      foreginId_ = getDefaultInstance().getForeginId();
      bitField0_ = (bitField0_ & ~0x00000002);
      onChanged();
      return this;
    }
    /**
     * <code>string foregin_id = 2;</code>
     * @param value The bytes for foreginId to set.
     * @return This builder for chaining.
     */
    public Builder setForeginIdBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      foreginId_ = value;
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }

    private int nodeId_ ;
    /**
     * <code>uint32 node_id = 3;</code>
     * @return The nodeId.
     */
    @java.lang.Override
    public int getNodeId() {
      return nodeId_;
    }
    /**
     * <code>uint32 node_id = 3;</code>
     * @param value The nodeId to set.
     * @return This builder for chaining.
     */
    public Builder setNodeId(int value) {

      nodeId_ = value;
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }
    /**
     * <code>uint32 node_id = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearNodeId() {
      bitField0_ = (bitField0_ & ~0x00000004);
      nodeId_ = 0;
      onChanged();
      return this;
    }

    private com.google.protobuf.LazyStringArrayList categories_ =
        com.google.protobuf.LazyStringArrayList.emptyList();
    private void ensureCategoriesIsMutable() {
      if (!categories_.isModifiable()) {
        categories_ = new com.google.protobuf.LazyStringArrayList(categories_);
      }
      bitField0_ |= 0x00000008;
    }
    /**
     * <code>repeated string categories = 4;</code>
     * @return A list containing the categories.
     */
    public com.google.protobuf.ProtocolStringList
        getCategoriesList() {
      categories_.makeImmutable();
      return categories_;
    }
    /**
     * <code>repeated string categories = 4;</code>
     * @return The count of categories.
     */
    public int getCategoriesCount() {
      return categories_.size();
    }
    /**
     * <code>repeated string categories = 4;</code>
     * @param index The index of the element to return.
     * @return The categories at the given index.
     */
    public java.lang.String getCategories(int index) {
      return categories_.get(index);
    }
    /**
     * <code>repeated string categories = 4;</code>
     * @param index The index of the value to return.
     * @return The bytes of the categories at the given index.
     */
    public com.google.protobuf.ByteString
        getCategoriesBytes(int index) {
      return categories_.getByteString(index);
    }
    /**
     * <code>repeated string categories = 4;</code>
     * @param index The index to set the value at.
     * @param value The categories to set.
     * @return This builder for chaining.
     */
    public Builder setCategories(
        int index, java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      ensureCategoriesIsMutable();
      categories_.set(index, value);
      bitField0_ |= 0x00000008;
      onChanged();
      return this;
    }
    /**
     * <code>repeated string categories = 4;</code>
     * @param value The categories to add.
     * @return This builder for chaining.
     */
    public Builder addCategories(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      ensureCategoriesIsMutable();
      categories_.add(value);
      bitField0_ |= 0x00000008;
      onChanged();
      return this;
    }
    /**
     * <code>repeated string categories = 4;</code>
     * @param values The categories to add.
     * @return This builder for chaining.
     */
    public Builder addAllCategories(
        java.lang.Iterable<java.lang.String> values) {
      ensureCategoriesIsMutable();
      com.google.protobuf.AbstractMessageLite.Builder.addAll(
          values, categories_);
      bitField0_ |= 0x00000008;
      onChanged();
      return this;
    }
    /**
     * <code>repeated string categories = 4;</code>
     * @return This builder for chaining.
     */
    public Builder clearCategories() {
      categories_ =
        com.google.protobuf.LazyStringArrayList.emptyList();
      bitField0_ = (bitField0_ & ~0x00000008);;
      onChanged();
      return this;
    }
    /**
     * <code>repeated string categories = 4;</code>
     * @param value The bytes of the categories to add.
     * @return This builder for chaining.
     */
    public Builder addCategoriesBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      ensureCategoriesIsMutable();
      categories_.add(value);
      bitField0_ |= 0x00000008;
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


    // @@protoc_insertion_point(builder_scope:NodeInfo)
  }

  // @@protoc_insertion_point(class_scope:NodeInfo)
  private static final org.opennms.netmgt.flows.persistence.model.NodeInfo DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new org.opennms.netmgt.flows.persistence.model.NodeInfo();
  }

  public static org.opennms.netmgt.flows.persistence.model.NodeInfo getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<NodeInfo>
      PARSER = new com.google.protobuf.AbstractParser<NodeInfo>() {
    @java.lang.Override
    public NodeInfo parsePartialFrom(
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

  public static com.google.protobuf.Parser<NodeInfo> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<NodeInfo> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public org.opennms.netmgt.flows.persistence.model.NodeInfo getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

