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
 * Protobuf type {@code ListValue}
 */
public final class ListValue extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:ListValue)
    ListValueOrBuilder {
private static final long serialVersionUID = 0L;
  // Use ListValue.newBuilder() to construct.
  private ListValue(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private ListValue() {
    semantic_ = 0;
    list_ = java.util.Collections.emptyList();
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new ListValue();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowProtos.internal_static_ListValue_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowProtos.internal_static_ListValue_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            org.opennms.netmgt.telemetry.protocols.netflow.transport.ListValue.class, org.opennms.netmgt.telemetry.protocols.netflow.transport.ListValue.Builder.class);
  }

  public static final int SEMANTIC_FIELD_NUMBER = 1;
  private int semantic_;
  /**
   * <code>.Semantic semantic = 1;</code>
   * @return The enum numeric value on the wire for semantic.
   */
  @java.lang.Override public int getSemanticValue() {
    return semantic_;
  }
  /**
   * <code>.Semantic semantic = 1;</code>
   * @return The semantic.
   */
  @java.lang.Override public org.opennms.netmgt.telemetry.protocols.netflow.transport.Semantic getSemantic() {
    @SuppressWarnings("deprecation")
    org.opennms.netmgt.telemetry.protocols.netflow.transport.Semantic result = org.opennms.netmgt.telemetry.protocols.netflow.transport.Semantic.valueOf(semantic_);
    return result == null ? org.opennms.netmgt.telemetry.protocols.netflow.transport.Semantic.UNRECOGNIZED : result;
  }

  public static final int LIST_FIELD_NUMBER = 2;
  private java.util.List<org.opennms.netmgt.telemetry.protocols.netflow.transport.List> list_;
  /**
   * <code>repeated .List list = 2;</code>
   */
  @java.lang.Override
  public java.util.List<org.opennms.netmgt.telemetry.protocols.netflow.transport.List> getListList() {
    return list_;
  }
  /**
   * <code>repeated .List list = 2;</code>
   */
  @java.lang.Override
  public java.util.List<? extends org.opennms.netmgt.telemetry.protocols.netflow.transport.ListOrBuilder> 
      getListOrBuilderList() {
    return list_;
  }
  /**
   * <code>repeated .List list = 2;</code>
   */
  @java.lang.Override
  public int getListCount() {
    return list_.size();
  }
  /**
   * <code>repeated .List list = 2;</code>
   */
  @java.lang.Override
  public org.opennms.netmgt.telemetry.protocols.netflow.transport.List getList(int index) {
    return list_.get(index);
  }
  /**
   * <code>repeated .List list = 2;</code>
   */
  @java.lang.Override
  public org.opennms.netmgt.telemetry.protocols.netflow.transport.ListOrBuilder getListOrBuilder(
      int index) {
    return list_.get(index);
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
    if (semantic_ != org.opennms.netmgt.telemetry.protocols.netflow.transport.Semantic.UNDEFINED.getNumber()) {
      output.writeEnum(1, semantic_);
    }
    for (int i = 0; i < list_.size(); i++) {
      output.writeMessage(2, list_.get(i));
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (semantic_ != org.opennms.netmgt.telemetry.protocols.netflow.transport.Semantic.UNDEFINED.getNumber()) {
      size += com.google.protobuf.CodedOutputStream
        .computeEnumSize(1, semantic_);
    }
    for (int i = 0; i < list_.size(); i++) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(2, list_.get(i));
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
    if (!(obj instanceof org.opennms.netmgt.telemetry.protocols.netflow.transport.ListValue)) {
      return super.equals(obj);
    }
    org.opennms.netmgt.telemetry.protocols.netflow.transport.ListValue other = (org.opennms.netmgt.telemetry.protocols.netflow.transport.ListValue) obj;

    if (semantic_ != other.semantic_) return false;
    if (!getListList()
        .equals(other.getListList())) return false;
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
    hash = (37 * hash) + SEMANTIC_FIELD_NUMBER;
    hash = (53 * hash) + semantic_;
    if (getListCount() > 0) {
      hash = (37 * hash) + LIST_FIELD_NUMBER;
      hash = (53 * hash) + getListList().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.ListValue parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.ListValue parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.ListValue parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.ListValue parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.ListValue parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.ListValue parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.ListValue parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.ListValue parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.ListValue parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.ListValue parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.ListValue parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.ListValue parseFrom(
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
  public static Builder newBuilder(org.opennms.netmgt.telemetry.protocols.netflow.transport.ListValue prototype) {
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
   * Protobuf type {@code ListValue}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:ListValue)
      org.opennms.netmgt.telemetry.protocols.netflow.transport.ListValueOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowProtos.internal_static_ListValue_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowProtos.internal_static_ListValue_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              org.opennms.netmgt.telemetry.protocols.netflow.transport.ListValue.class, org.opennms.netmgt.telemetry.protocols.netflow.transport.ListValue.Builder.class);
    }

    // Construct using org.opennms.netmgt.telemetry.protocols.netflow.transport.ListValue.newBuilder()
    private Builder() {

    }

    private Builder(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);

    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      semantic_ = 0;

      if (listBuilder_ == null) {
        list_ = java.util.Collections.emptyList();
      } else {
        list_ = null;
        listBuilder_.clear();
      }
      bitField0_ = (bitField0_ & ~0x00000001);
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowProtos.internal_static_ListValue_descriptor;
    }

    @java.lang.Override
    public org.opennms.netmgt.telemetry.protocols.netflow.transport.ListValue getDefaultInstanceForType() {
      return org.opennms.netmgt.telemetry.protocols.netflow.transport.ListValue.getDefaultInstance();
    }

    @java.lang.Override
    public org.opennms.netmgt.telemetry.protocols.netflow.transport.ListValue build() {
      org.opennms.netmgt.telemetry.protocols.netflow.transport.ListValue result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public org.opennms.netmgt.telemetry.protocols.netflow.transport.ListValue buildPartial() {
      org.opennms.netmgt.telemetry.protocols.netflow.transport.ListValue result = new org.opennms.netmgt.telemetry.protocols.netflow.transport.ListValue(this);
      int from_bitField0_ = bitField0_;
      result.semantic_ = semantic_;
      if (listBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0)) {
          list_ = java.util.Collections.unmodifiableList(list_);
          bitField0_ = (bitField0_ & ~0x00000001);
        }
        result.list_ = list_;
      } else {
        result.list_ = listBuilder_.build();
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
      if (other instanceof org.opennms.netmgt.telemetry.protocols.netflow.transport.ListValue) {
        return mergeFrom((org.opennms.netmgt.telemetry.protocols.netflow.transport.ListValue)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(org.opennms.netmgt.telemetry.protocols.netflow.transport.ListValue other) {
      if (other == org.opennms.netmgt.telemetry.protocols.netflow.transport.ListValue.getDefaultInstance()) return this;
      if (other.semantic_ != 0) {
        setSemanticValue(other.getSemanticValue());
      }
      if (listBuilder_ == null) {
        if (!other.list_.isEmpty()) {
          if (list_.isEmpty()) {
            list_ = other.list_;
            bitField0_ = (bitField0_ & ~0x00000001);
          } else {
            ensureListIsMutable();
            list_.addAll(other.list_);
          }
          onChanged();
        }
      } else {
        if (!other.list_.isEmpty()) {
          if (listBuilder_.isEmpty()) {
            listBuilder_.dispose();
            listBuilder_ = null;
            list_ = other.list_;
            bitField0_ = (bitField0_ & ~0x00000001);
            listBuilder_ = 
              com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders ?
                 getListFieldBuilder() : null;
          } else {
            listBuilder_.addAllMessages(other.list_);
          }
        }
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
            case 8: {
              semantic_ = input.readEnum();

              break;
            } // case 8
            case 18: {
              org.opennms.netmgt.telemetry.protocols.netflow.transport.List m =
                  input.readMessage(
                      org.opennms.netmgt.telemetry.protocols.netflow.transport.List.parser(),
                      extensionRegistry);
              if (listBuilder_ == null) {
                ensureListIsMutable();
                list_.add(m);
              } else {
                listBuilder_.addMessage(m);
              }
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

    private int semantic_ = 0;
    /**
     * <code>.Semantic semantic = 1;</code>
     * @return The enum numeric value on the wire for semantic.
     */
    @java.lang.Override public int getSemanticValue() {
      return semantic_;
    }
    /**
     * <code>.Semantic semantic = 1;</code>
     * @param value The enum numeric value on the wire for semantic to set.
     * @return This builder for chaining.
     */
    public Builder setSemanticValue(int value) {
      
      semantic_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>.Semantic semantic = 1;</code>
     * @return The semantic.
     */
    @java.lang.Override
    public org.opennms.netmgt.telemetry.protocols.netflow.transport.Semantic getSemantic() {
      @SuppressWarnings("deprecation")
      org.opennms.netmgt.telemetry.protocols.netflow.transport.Semantic result = org.opennms.netmgt.telemetry.protocols.netflow.transport.Semantic.valueOf(semantic_);
      return result == null ? org.opennms.netmgt.telemetry.protocols.netflow.transport.Semantic.UNRECOGNIZED : result;
    }
    /**
     * <code>.Semantic semantic = 1;</code>
     * @param value The semantic to set.
     * @return This builder for chaining.
     */
    public Builder setSemantic(org.opennms.netmgt.telemetry.protocols.netflow.transport.Semantic value) {
      if (value == null) {
        throw new NullPointerException();
      }
      
      semantic_ = value.getNumber();
      onChanged();
      return this;
    }
    /**
     * <code>.Semantic semantic = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearSemantic() {
      
      semantic_ = 0;
      onChanged();
      return this;
    }

    private java.util.List<org.opennms.netmgt.telemetry.protocols.netflow.transport.List> list_ =
      java.util.Collections.emptyList();
    private void ensureListIsMutable() {
      if (!((bitField0_ & 0x00000001) != 0)) {
        list_ = new java.util.ArrayList<org.opennms.netmgt.telemetry.protocols.netflow.transport.List>(list_);
        bitField0_ |= 0x00000001;
       }
    }

    private com.google.protobuf.RepeatedFieldBuilderV3<
        org.opennms.netmgt.telemetry.protocols.netflow.transport.List, org.opennms.netmgt.telemetry.protocols.netflow.transport.List.Builder, org.opennms.netmgt.telemetry.protocols.netflow.transport.ListOrBuilder> listBuilder_;

    /**
     * <code>repeated .List list = 2;</code>
     */
    public java.util.List<org.opennms.netmgt.telemetry.protocols.netflow.transport.List> getListList() {
      if (listBuilder_ == null) {
        return java.util.Collections.unmodifiableList(list_);
      } else {
        return listBuilder_.getMessageList();
      }
    }
    /**
     * <code>repeated .List list = 2;</code>
     */
    public int getListCount() {
      if (listBuilder_ == null) {
        return list_.size();
      } else {
        return listBuilder_.getCount();
      }
    }
    /**
     * <code>repeated .List list = 2;</code>
     */
    public org.opennms.netmgt.telemetry.protocols.netflow.transport.List getList(int index) {
      if (listBuilder_ == null) {
        return list_.get(index);
      } else {
        return listBuilder_.getMessage(index);
      }
    }
    /**
     * <code>repeated .List list = 2;</code>
     */
    public Builder setList(
        int index, org.opennms.netmgt.telemetry.protocols.netflow.transport.List value) {
      if (listBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureListIsMutable();
        list_.set(index, value);
        onChanged();
      } else {
        listBuilder_.setMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .List list = 2;</code>
     */
    public Builder setList(
        int index, org.opennms.netmgt.telemetry.protocols.netflow.transport.List.Builder builderForValue) {
      if (listBuilder_ == null) {
        ensureListIsMutable();
        list_.set(index, builderForValue.build());
        onChanged();
      } else {
        listBuilder_.setMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .List list = 2;</code>
     */
    public Builder addList(org.opennms.netmgt.telemetry.protocols.netflow.transport.List value) {
      if (listBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureListIsMutable();
        list_.add(value);
        onChanged();
      } else {
        listBuilder_.addMessage(value);
      }
      return this;
    }
    /**
     * <code>repeated .List list = 2;</code>
     */
    public Builder addList(
        int index, org.opennms.netmgt.telemetry.protocols.netflow.transport.List value) {
      if (listBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureListIsMutable();
        list_.add(index, value);
        onChanged();
      } else {
        listBuilder_.addMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .List list = 2;</code>
     */
    public Builder addList(
        org.opennms.netmgt.telemetry.protocols.netflow.transport.List.Builder builderForValue) {
      if (listBuilder_ == null) {
        ensureListIsMutable();
        list_.add(builderForValue.build());
        onChanged();
      } else {
        listBuilder_.addMessage(builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .List list = 2;</code>
     */
    public Builder addList(
        int index, org.opennms.netmgt.telemetry.protocols.netflow.transport.List.Builder builderForValue) {
      if (listBuilder_ == null) {
        ensureListIsMutable();
        list_.add(index, builderForValue.build());
        onChanged();
      } else {
        listBuilder_.addMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .List list = 2;</code>
     */
    public Builder addAllList(
        java.lang.Iterable<? extends org.opennms.netmgt.telemetry.protocols.netflow.transport.List> values) {
      if (listBuilder_ == null) {
        ensureListIsMutable();
        com.google.protobuf.AbstractMessageLite.Builder.addAll(
            values, list_);
        onChanged();
      } else {
        listBuilder_.addAllMessages(values);
      }
      return this;
    }
    /**
     * <code>repeated .List list = 2;</code>
     */
    public Builder clearList() {
      if (listBuilder_ == null) {
        list_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000001);
        onChanged();
      } else {
        listBuilder_.clear();
      }
      return this;
    }
    /**
     * <code>repeated .List list = 2;</code>
     */
    public Builder removeList(int index) {
      if (listBuilder_ == null) {
        ensureListIsMutable();
        list_.remove(index);
        onChanged();
      } else {
        listBuilder_.remove(index);
      }
      return this;
    }
    /**
     * <code>repeated .List list = 2;</code>
     */
    public org.opennms.netmgt.telemetry.protocols.netflow.transport.List.Builder getListBuilder(
        int index) {
      return getListFieldBuilder().getBuilder(index);
    }
    /**
     * <code>repeated .List list = 2;</code>
     */
    public org.opennms.netmgt.telemetry.protocols.netflow.transport.ListOrBuilder getListOrBuilder(
        int index) {
      if (listBuilder_ == null) {
        return list_.get(index);  } else {
        return listBuilder_.getMessageOrBuilder(index);
      }
    }
    /**
     * <code>repeated .List list = 2;</code>
     */
    public java.util.List<? extends org.opennms.netmgt.telemetry.protocols.netflow.transport.ListOrBuilder> 
         getListOrBuilderList() {
      if (listBuilder_ != null) {
        return listBuilder_.getMessageOrBuilderList();
      } else {
        return java.util.Collections.unmodifiableList(list_);
      }
    }
    /**
     * <code>repeated .List list = 2;</code>
     */
    public org.opennms.netmgt.telemetry.protocols.netflow.transport.List.Builder addListBuilder() {
      return getListFieldBuilder().addBuilder(
          org.opennms.netmgt.telemetry.protocols.netflow.transport.List.getDefaultInstance());
    }
    /**
     * <code>repeated .List list = 2;</code>
     */
    public org.opennms.netmgt.telemetry.protocols.netflow.transport.List.Builder addListBuilder(
        int index) {
      return getListFieldBuilder().addBuilder(
          index, org.opennms.netmgt.telemetry.protocols.netflow.transport.List.getDefaultInstance());
    }
    /**
     * <code>repeated .List list = 2;</code>
     */
    public java.util.List<org.opennms.netmgt.telemetry.protocols.netflow.transport.List.Builder> 
         getListBuilderList() {
      return getListFieldBuilder().getBuilderList();
    }
    private com.google.protobuf.RepeatedFieldBuilderV3<
        org.opennms.netmgt.telemetry.protocols.netflow.transport.List, org.opennms.netmgt.telemetry.protocols.netflow.transport.List.Builder, org.opennms.netmgt.telemetry.protocols.netflow.transport.ListOrBuilder> 
        getListFieldBuilder() {
      if (listBuilder_ == null) {
        listBuilder_ = new com.google.protobuf.RepeatedFieldBuilderV3<
            org.opennms.netmgt.telemetry.protocols.netflow.transport.List, org.opennms.netmgt.telemetry.protocols.netflow.transport.List.Builder, org.opennms.netmgt.telemetry.protocols.netflow.transport.ListOrBuilder>(
                list_,
                ((bitField0_ & 0x00000001) != 0),
                getParentForChildren(),
                isClean());
        list_ = null;
      }
      return listBuilder_;
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


    // @@protoc_insertion_point(builder_scope:ListValue)
  }

  // @@protoc_insertion_point(class_scope:ListValue)
  private static final org.opennms.netmgt.telemetry.protocols.netflow.transport.ListValue DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new org.opennms.netmgt.telemetry.protocols.netflow.transport.ListValue();
  }

  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.ListValue getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<ListValue>
      PARSER = new com.google.protobuf.AbstractParser<ListValue>() {
    @java.lang.Override
    public ListValue parsePartialFrom(
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

  public static com.google.protobuf.Parser<ListValue> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<ListValue> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public org.opennms.netmgt.telemetry.protocols.netflow.transport.ListValue getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

