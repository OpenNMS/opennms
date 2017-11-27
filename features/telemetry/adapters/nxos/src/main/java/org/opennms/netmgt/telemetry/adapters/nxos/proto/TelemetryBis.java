/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
// source: telemetry_bis.proto

package org.opennms.netmgt.telemetry.adapters.nxos.proto;

public final class TelemetryBis {
  private TelemetryBis() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  public interface TelemetryOrBuilder extends
      // @@protoc_insertion_point(interface_extends:Telemetry)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <pre>
     *  bytes node_id_uuid = 2;              // not produced
     * </pre>
     *
     * <code>string node_id_str = 1;</code>
     */
    java.lang.String getNodeIdStr();
    /**
     * <pre>
     *  bytes node_id_uuid = 2;              // not produced
     * </pre>
     *
     * <code>string node_id_str = 1;</code>
     */
    com.google.protobuf.ByteString
        getNodeIdStrBytes();

    /**
     * <pre>
     *  uint32   subscription_id = 4;        // not produced
     * </pre>
     *
     * <code>string subscription_id_str = 3;</code>
     */
    java.lang.String getSubscriptionIdStr();
    /**
     * <pre>
     *  uint32   subscription_id = 4;        // not produced
     * </pre>
     *
     * <code>string subscription_id_str = 3;</code>
     */
    com.google.protobuf.ByteString
        getSubscriptionIdStrBytes();

    /**
     * <pre>
     * string   sensor_path = 5;               // not produced
     * </pre>
     *
     * <code>string encoding_path = 6;</code>
     */
    java.lang.String getEncodingPath();
    /**
     * <pre>
     * string   sensor_path = 5;               // not produced
     * </pre>
     *
     * <code>string encoding_path = 6;</code>
     */
    com.google.protobuf.ByteString
        getEncodingPathBytes();

    /**
     * <pre>
     * string   model_version = 7;             // not produced
     * </pre>
     *
     * <code>uint64 collection_id = 8;</code>
     */
    long getCollectionId();

    /**
     * <code>uint64 collection_start_time = 9;</code>
     */
    long getCollectionStartTime();

    /**
     * <code>uint64 msg_timestamp = 10;</code>
     */
    long getMsgTimestamp();

    /**
     * <code>repeated .TelemetryField data_gpbkv = 11;</code>
     */
    java.util.List<TelemetryBis.TelemetryField> 
        getDataGpbkvList();
    /**
     * <code>repeated .TelemetryField data_gpbkv = 11;</code>
     */
    TelemetryBis.TelemetryField getDataGpbkv(int index);
    /**
     * <code>repeated .TelemetryField data_gpbkv = 11;</code>
     */
    int getDataGpbkvCount();
    /**
     * <code>repeated .TelemetryField data_gpbkv = 11;</code>
     */
    java.util.List<? extends TelemetryBis.TelemetryFieldOrBuilder> 
        getDataGpbkvOrBuilderList();
    /**
     * <code>repeated .TelemetryField data_gpbkv = 11;</code>
     */
    TelemetryBis.TelemetryFieldOrBuilder getDataGpbkvOrBuilder(
        int index);

    /**
     * <code>.TelemetryGPBTable data_gpb = 12;</code>
     */
    boolean hasDataGpb();
    /**
     * <code>.TelemetryGPBTable data_gpb = 12;</code>
     */
    TelemetryBis.TelemetryGPBTable getDataGpb();
    /**
     * <code>.TelemetryGPBTable data_gpb = 12;</code>
     */
    TelemetryBis.TelemetryGPBTableOrBuilder getDataGpbOrBuilder();

    /**
     * <pre>
     * uint64   heartbeat_sequence_number = 14; // not produced
     * </pre>
     *
     * <code>uint64 collection_end_time = 13;</code>
     */
    long getCollectionEndTime();

    public TelemetryBis.Telemetry.NodeIdCase getNodeIdCase();

    public TelemetryBis.Telemetry.SubscriptionCase getSubscriptionCase();
  }
  /**
   * Protobuf type {@code Telemetry}
   */
  public  static final class Telemetry extends
      com.google.protobuf.GeneratedMessageV3 implements
      // @@protoc_insertion_point(message_implements:Telemetry)
      TelemetryOrBuilder {
  private static final long serialVersionUID = 0L;
    // Use Telemetry.newBuilder() to construct.
    private Telemetry(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
      super(builder);
    }
    private Telemetry() {
      encodingPath_ = "";
      collectionId_ = 0L;
      collectionStartTime_ = 0L;
      msgTimestamp_ = 0L;
      dataGpbkv_ = java.util.Collections.emptyList();
      collectionEndTime_ = 0L;
    }

    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return this.unknownFields;
    }
    private Telemetry(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      this();
      int mutable_bitField0_ = 0;
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            default: {
              if (!parseUnknownFieldProto3(
                  input, unknownFields, extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
            case 10: {
              java.lang.String s = input.readStringRequireUtf8();
              nodeIdCase_ = 1;
              nodeId_ = s;
              break;
            }
            case 26: {
              java.lang.String s = input.readStringRequireUtf8();
              subscriptionCase_ = 3;
              subscription_ = s;
              break;
            }
            case 50: {
              java.lang.String s = input.readStringRequireUtf8();

              encodingPath_ = s;
              break;
            }
            case 64: {

              collectionId_ = input.readUInt64();
              break;
            }
            case 72: {

              collectionStartTime_ = input.readUInt64();
              break;
            }
            case 80: {

              msgTimestamp_ = input.readUInt64();
              break;
            }
            case 90: {
              if (!((mutable_bitField0_ & 0x00000040) == 0x00000040)) {
                dataGpbkv_ = new java.util.ArrayList<TelemetryBis.TelemetryField>();
                mutable_bitField0_ |= 0x00000040;
              }
              dataGpbkv_.add(
                  input.readMessage(TelemetryBis.TelemetryField.parser(), extensionRegistry));
              break;
            }
            case 98: {
              TelemetryBis.TelemetryGPBTable.Builder subBuilder = null;
              if (dataGpb_ != null) {
                subBuilder = dataGpb_.toBuilder();
              }
              dataGpb_ = input.readMessage(TelemetryBis.TelemetryGPBTable.parser(), extensionRegistry);
              if (subBuilder != null) {
                subBuilder.mergeFrom(dataGpb_);
                dataGpb_ = subBuilder.buildPartial();
              }

              break;
            }
            case 104: {

              collectionEndTime_ = input.readUInt64();
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
            e).setUnfinishedMessage(this);
      } finally {
        if (((mutable_bitField0_ & 0x00000040) == 0x00000040)) {
          dataGpbkv_ = java.util.Collections.unmodifiableList(dataGpbkv_);
        }
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return TelemetryBis.internal_static_Telemetry_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return TelemetryBis.internal_static_Telemetry_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              TelemetryBis.Telemetry.class, TelemetryBis.Telemetry.Builder.class);
    }

    private int bitField0_;
    private int nodeIdCase_ = 0;
    private java.lang.Object nodeId_;
    public enum NodeIdCase
        implements com.google.protobuf.Internal.EnumLite {
      NODE_ID_STR(1),
      NODEID_NOT_SET(0);
      private final int value;
      private NodeIdCase(int value) {
        this.value = value;
      }
      /**
       * @deprecated Use {@link #forNumber(int)} instead.
       */
      @java.lang.Deprecated
      public static NodeIdCase valueOf(int value) {
        return forNumber(value);
      }

      public static NodeIdCase forNumber(int value) {
        switch (value) {
          case 1: return NODE_ID_STR;
          case 0: return NODEID_NOT_SET;
          default: return null;
        }
      }
      public int getNumber() {
        return this.value;
      }
    };

    public NodeIdCase
    getNodeIdCase() {
      return NodeIdCase.forNumber(
          nodeIdCase_);
    }

    private int subscriptionCase_ = 0;
    private java.lang.Object subscription_;
    public enum SubscriptionCase
        implements com.google.protobuf.Internal.EnumLite {
      SUBSCRIPTION_ID_STR(3),
      SUBSCRIPTION_NOT_SET(0);
      private final int value;
      private SubscriptionCase(int value) {
        this.value = value;
      }
      /**
       * @deprecated Use {@link #forNumber(int)} instead.
       */
      @java.lang.Deprecated
      public static SubscriptionCase valueOf(int value) {
        return forNumber(value);
      }

      public static SubscriptionCase forNumber(int value) {
        switch (value) {
          case 3: return SUBSCRIPTION_ID_STR;
          case 0: return SUBSCRIPTION_NOT_SET;
          default: return null;
        }
      }
      public int getNumber() {
        return this.value;
      }
    };

    public SubscriptionCase
    getSubscriptionCase() {
      return SubscriptionCase.forNumber(
          subscriptionCase_);
    }

    public static final int NODE_ID_STR_FIELD_NUMBER = 1;
    /**
     * <pre>
     *  bytes node_id_uuid = 2;              // not produced
     * </pre>
     *
     * <code>string node_id_str = 1;</code>
     */
    public java.lang.String getNodeIdStr() {
      java.lang.Object ref = "";
      if (nodeIdCase_ == 1) {
        ref = nodeId_;
      }
      if (ref instanceof java.lang.String) {
        return (java.lang.String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        if (nodeIdCase_ == 1) {
          nodeId_ = s;
        }
        return s;
      }
    }
    /**
     * <pre>
     *  bytes node_id_uuid = 2;              // not produced
     * </pre>
     *
     * <code>string node_id_str = 1;</code>
     */
    public com.google.protobuf.ByteString
        getNodeIdStrBytes() {
      java.lang.Object ref = "";
      if (nodeIdCase_ == 1) {
        ref = nodeId_;
      }
      if (ref instanceof java.lang.String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        if (nodeIdCase_ == 1) {
          nodeId_ = b;
        }
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    public static final int SUBSCRIPTION_ID_STR_FIELD_NUMBER = 3;
    /**
     * <pre>
     *  uint32   subscription_id = 4;        // not produced
     * </pre>
     *
     * <code>string subscription_id_str = 3;</code>
     */
    public java.lang.String getSubscriptionIdStr() {
      java.lang.Object ref = "";
      if (subscriptionCase_ == 3) {
        ref = subscription_;
      }
      if (ref instanceof java.lang.String) {
        return (java.lang.String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        if (subscriptionCase_ == 3) {
          subscription_ = s;
        }
        return s;
      }
    }
    /**
     * <pre>
     *  uint32   subscription_id = 4;        // not produced
     * </pre>
     *
     * <code>string subscription_id_str = 3;</code>
     */
    public com.google.protobuf.ByteString
        getSubscriptionIdStrBytes() {
      java.lang.Object ref = "";
      if (subscriptionCase_ == 3) {
        ref = subscription_;
      }
      if (ref instanceof java.lang.String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        if (subscriptionCase_ == 3) {
          subscription_ = b;
        }
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    public static final int ENCODING_PATH_FIELD_NUMBER = 6;
    private volatile java.lang.Object encodingPath_;
    /**
     * <pre>
     * string   sensor_path = 5;               // not produced
     * </pre>
     *
     * <code>string encoding_path = 6;</code>
     */
    public java.lang.String getEncodingPath() {
      java.lang.Object ref = encodingPath_;
      if (ref instanceof java.lang.String) {
        return (java.lang.String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        encodingPath_ = s;
        return s;
      }
    }
    /**
     * <pre>
     * string   sensor_path = 5;               // not produced
     * </pre>
     *
     * <code>string encoding_path = 6;</code>
     */
    public com.google.protobuf.ByteString
        getEncodingPathBytes() {
      java.lang.Object ref = encodingPath_;
      if (ref instanceof java.lang.String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        encodingPath_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    public static final int COLLECTION_ID_FIELD_NUMBER = 8;
    private long collectionId_;
    /**
     * <pre>
     * string   model_version = 7;             // not produced
     * </pre>
     *
     * <code>uint64 collection_id = 8;</code>
     */
    public long getCollectionId() {
      return collectionId_;
    }

    public static final int COLLECTION_START_TIME_FIELD_NUMBER = 9;
    private long collectionStartTime_;
    /**
     * <code>uint64 collection_start_time = 9;</code>
     */
    public long getCollectionStartTime() {
      return collectionStartTime_;
    }

    public static final int MSG_TIMESTAMP_FIELD_NUMBER = 10;
    private long msgTimestamp_;
    /**
     * <code>uint64 msg_timestamp = 10;</code>
     */
    public long getMsgTimestamp() {
      return msgTimestamp_;
    }

    public static final int DATA_GPBKV_FIELD_NUMBER = 11;
    private java.util.List<TelemetryBis.TelemetryField> dataGpbkv_;
    /**
     * <code>repeated .TelemetryField data_gpbkv = 11;</code>
     */
    public java.util.List<TelemetryBis.TelemetryField> getDataGpbkvList() {
      return dataGpbkv_;
    }
    /**
     * <code>repeated .TelemetryField data_gpbkv = 11;</code>
     */
    public java.util.List<? extends TelemetryBis.TelemetryFieldOrBuilder> 
        getDataGpbkvOrBuilderList() {
      return dataGpbkv_;
    }
    /**
     * <code>repeated .TelemetryField data_gpbkv = 11;</code>
     */
    public int getDataGpbkvCount() {
      return dataGpbkv_.size();
    }
    /**
     * <code>repeated .TelemetryField data_gpbkv = 11;</code>
     */
    public TelemetryBis.TelemetryField getDataGpbkv(int index) {
      return dataGpbkv_.get(index);
    }
    /**
     * <code>repeated .TelemetryField data_gpbkv = 11;</code>
     */
    public TelemetryBis.TelemetryFieldOrBuilder getDataGpbkvOrBuilder(
        int index) {
      return dataGpbkv_.get(index);
    }

    public static final int DATA_GPB_FIELD_NUMBER = 12;
    private TelemetryBis.TelemetryGPBTable dataGpb_;
    /**
     * <code>.TelemetryGPBTable data_gpb = 12;</code>
     */
    public boolean hasDataGpb() {
      return dataGpb_ != null;
    }
    /**
     * <code>.TelemetryGPBTable data_gpb = 12;</code>
     */
    public TelemetryBis.TelemetryGPBTable getDataGpb() {
      return dataGpb_ == null ? TelemetryBis.TelemetryGPBTable.getDefaultInstance() : dataGpb_;
    }
    /**
     * <code>.TelemetryGPBTable data_gpb = 12;</code>
     */
    public TelemetryBis.TelemetryGPBTableOrBuilder getDataGpbOrBuilder() {
      return getDataGpb();
    }

    public static final int COLLECTION_END_TIME_FIELD_NUMBER = 13;
    private long collectionEndTime_;
    /**
     * <pre>
     * uint64   heartbeat_sequence_number = 14; // not produced
     * </pre>
     *
     * <code>uint64 collection_end_time = 13;</code>
     */
    public long getCollectionEndTime() {
      return collectionEndTime_;
    }

    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      if (nodeIdCase_ == 1) {
        com.google.protobuf.GeneratedMessageV3.writeString(output, 1, nodeId_);
      }
      if (subscriptionCase_ == 3) {
        com.google.protobuf.GeneratedMessageV3.writeString(output, 3, subscription_);
      }
      if (!getEncodingPathBytes().isEmpty()) {
        com.google.protobuf.GeneratedMessageV3.writeString(output, 6, encodingPath_);
      }
      if (collectionId_ != 0L) {
        output.writeUInt64(8, collectionId_);
      }
      if (collectionStartTime_ != 0L) {
        output.writeUInt64(9, collectionStartTime_);
      }
      if (msgTimestamp_ != 0L) {
        output.writeUInt64(10, msgTimestamp_);
      }
      for (int i = 0; i < dataGpbkv_.size(); i++) {
        output.writeMessage(11, dataGpbkv_.get(i));
      }
      if (dataGpb_ != null) {
        output.writeMessage(12, getDataGpb());
      }
      if (collectionEndTime_ != 0L) {
        output.writeUInt64(13, collectionEndTime_);
      }
      unknownFields.writeTo(output);
    }

    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      if (nodeIdCase_ == 1) {
        size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, nodeId_);
      }
      if (subscriptionCase_ == 3) {
        size += com.google.protobuf.GeneratedMessageV3.computeStringSize(3, subscription_);
      }
      if (!getEncodingPathBytes().isEmpty()) {
        size += com.google.protobuf.GeneratedMessageV3.computeStringSize(6, encodingPath_);
      }
      if (collectionId_ != 0L) {
        size += com.google.protobuf.CodedOutputStream
          .computeUInt64Size(8, collectionId_);
      }
      if (collectionStartTime_ != 0L) {
        size += com.google.protobuf.CodedOutputStream
          .computeUInt64Size(9, collectionStartTime_);
      }
      if (msgTimestamp_ != 0L) {
        size += com.google.protobuf.CodedOutputStream
          .computeUInt64Size(10, msgTimestamp_);
      }
      for (int i = 0; i < dataGpbkv_.size(); i++) {
        size += com.google.protobuf.CodedOutputStream
          .computeMessageSize(11, dataGpbkv_.get(i));
      }
      if (dataGpb_ != null) {
        size += com.google.protobuf.CodedOutputStream
          .computeMessageSize(12, getDataGpb());
      }
      if (collectionEndTime_ != 0L) {
        size += com.google.protobuf.CodedOutputStream
          .computeUInt64Size(13, collectionEndTime_);
      }
      size += unknownFields.getSerializedSize();
      memoizedSize = size;
      return size;
    }

    @java.lang.Override
    public boolean equals(final java.lang.Object obj) {
      if (obj == this) {
       return true;
      }
      if (!(obj instanceof TelemetryBis.Telemetry)) {
        return super.equals(obj);
      }
      TelemetryBis.Telemetry other = (TelemetryBis.Telemetry) obj;

      boolean result = true;
      result = result && getEncodingPath()
          .equals(other.getEncodingPath());
      result = result && (getCollectionId()
          == other.getCollectionId());
      result = result && (getCollectionStartTime()
          == other.getCollectionStartTime());
      result = result && (getMsgTimestamp()
          == other.getMsgTimestamp());
      result = result && getDataGpbkvList()
          .equals(other.getDataGpbkvList());
      result = result && (hasDataGpb() == other.hasDataGpb());
      if (hasDataGpb()) {
        result = result && getDataGpb()
            .equals(other.getDataGpb());
      }
      result = result && (getCollectionEndTime()
          == other.getCollectionEndTime());
      result = result && getNodeIdCase().equals(
          other.getNodeIdCase());
      if (!result) return false;
      switch (nodeIdCase_) {
        case 1:
          result = result && getNodeIdStr()
              .equals(other.getNodeIdStr());
          break;
        case 0:
        default:
      }
      result = result && getSubscriptionCase().equals(
          other.getSubscriptionCase());
      if (!result) return false;
      switch (subscriptionCase_) {
        case 3:
          result = result && getSubscriptionIdStr()
              .equals(other.getSubscriptionIdStr());
          break;
        case 0:
        default:
      }
      result = result && unknownFields.equals(other.unknownFields);
      return result;
    }

    @java.lang.Override
    public int hashCode() {
      if (memoizedHashCode != 0) {
        return memoizedHashCode;
      }
      int hash = 41;
      hash = (19 * hash) + getDescriptor().hashCode();
      hash = (37 * hash) + ENCODING_PATH_FIELD_NUMBER;
      hash = (53 * hash) + getEncodingPath().hashCode();
      hash = (37 * hash) + COLLECTION_ID_FIELD_NUMBER;
      hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
          getCollectionId());
      hash = (37 * hash) + COLLECTION_START_TIME_FIELD_NUMBER;
      hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
          getCollectionStartTime());
      hash = (37 * hash) + MSG_TIMESTAMP_FIELD_NUMBER;
      hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
          getMsgTimestamp());
      if (getDataGpbkvCount() > 0) {
        hash = (37 * hash) + DATA_GPBKV_FIELD_NUMBER;
        hash = (53 * hash) + getDataGpbkvList().hashCode();
      }
      if (hasDataGpb()) {
        hash = (37 * hash) + DATA_GPB_FIELD_NUMBER;
        hash = (53 * hash) + getDataGpb().hashCode();
      }
      hash = (37 * hash) + COLLECTION_END_TIME_FIELD_NUMBER;
      hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
          getCollectionEndTime());
      switch (nodeIdCase_) {
        case 1:
          hash = (37 * hash) + NODE_ID_STR_FIELD_NUMBER;
          hash = (53 * hash) + getNodeIdStr().hashCode();
          break;
        case 0:
        default:
      }
      switch (subscriptionCase_) {
        case 3:
          hash = (37 * hash) + SUBSCRIPTION_ID_STR_FIELD_NUMBER;
          hash = (53 * hash) + getSubscriptionIdStr().hashCode();
          break;
        case 0:
        default:
      }
      hash = (29 * hash) + unknownFields.hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static TelemetryBis.Telemetry parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static TelemetryBis.Telemetry parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static TelemetryBis.Telemetry parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static TelemetryBis.Telemetry parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static TelemetryBis.Telemetry parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static TelemetryBis.Telemetry parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static TelemetryBis.Telemetry parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static TelemetryBis.Telemetry parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }
    public static TelemetryBis.Telemetry parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input);
    }
    public static TelemetryBis.Telemetry parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static TelemetryBis.Telemetry parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static TelemetryBis.Telemetry parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
    }
    public static Builder newBuilder(TelemetryBis.Telemetry prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }
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
     * Protobuf type {@code Telemetry}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:Telemetry)
        TelemetryBis.TelemetryOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return TelemetryBis.internal_static_Telemetry_descriptor;
      }

      protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return TelemetryBis.internal_static_Telemetry_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                TelemetryBis.Telemetry.class, TelemetryBis.Telemetry.Builder.class);
      }

      // Construct using TelemetryBis.Telemetry.newBuilder()
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
          getDataGpbkvFieldBuilder();
        }
      }
      public Builder clear() {
        super.clear();
        encodingPath_ = "";

        collectionId_ = 0L;

        collectionStartTime_ = 0L;

        msgTimestamp_ = 0L;

        if (dataGpbkvBuilder_ == null) {
          dataGpbkv_ = java.util.Collections.emptyList();
          bitField0_ = (bitField0_ & ~0x00000040);
        } else {
          dataGpbkvBuilder_.clear();
        }
        if (dataGpbBuilder_ == null) {
          dataGpb_ = null;
        } else {
          dataGpb_ = null;
          dataGpbBuilder_ = null;
        }
        collectionEndTime_ = 0L;

        nodeIdCase_ = 0;
        nodeId_ = null;
        subscriptionCase_ = 0;
        subscription_ = null;
        return this;
      }

      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return TelemetryBis.internal_static_Telemetry_descriptor;
      }

      public TelemetryBis.Telemetry getDefaultInstanceForType() {
        return TelemetryBis.Telemetry.getDefaultInstance();
      }

      public TelemetryBis.Telemetry build() {
        TelemetryBis.Telemetry result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public TelemetryBis.Telemetry buildPartial() {
        TelemetryBis.Telemetry result = new TelemetryBis.Telemetry(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (nodeIdCase_ == 1) {
          result.nodeId_ = nodeId_;
        }
        if (subscriptionCase_ == 3) {
          result.subscription_ = subscription_;
        }
        result.encodingPath_ = encodingPath_;
        result.collectionId_ = collectionId_;
        result.collectionStartTime_ = collectionStartTime_;
        result.msgTimestamp_ = msgTimestamp_;
        if (dataGpbkvBuilder_ == null) {
          if (((bitField0_ & 0x00000040) == 0x00000040)) {
            dataGpbkv_ = java.util.Collections.unmodifiableList(dataGpbkv_);
            bitField0_ = (bitField0_ & ~0x00000040);
          }
          result.dataGpbkv_ = dataGpbkv_;
        } else {
          result.dataGpbkv_ = dataGpbkvBuilder_.build();
        }
        if (dataGpbBuilder_ == null) {
          result.dataGpb_ = dataGpb_;
        } else {
          result.dataGpb_ = dataGpbBuilder_.build();
        }
        result.collectionEndTime_ = collectionEndTime_;
        result.bitField0_ = to_bitField0_;
        result.nodeIdCase_ = nodeIdCase_;
        result.subscriptionCase_ = subscriptionCase_;
        onBuilt();
        return result;
      }

      public Builder clone() {
        return (Builder) super.clone();
      }
      public Builder setField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return (Builder) super.setField(field, value);
      }
      public Builder clearField(
          com.google.protobuf.Descriptors.FieldDescriptor field) {
        return (Builder) super.clearField(field);
      }
      public Builder clearOneof(
          com.google.protobuf.Descriptors.OneofDescriptor oneof) {
        return (Builder) super.clearOneof(oneof);
      }
      public Builder setRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          int index, java.lang.Object value) {
        return (Builder) super.setRepeatedField(field, index, value);
      }
      public Builder addRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return (Builder) super.addRepeatedField(field, value);
      }
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof TelemetryBis.Telemetry) {
          return mergeFrom((TelemetryBis.Telemetry)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(TelemetryBis.Telemetry other) {
        if (other == TelemetryBis.Telemetry.getDefaultInstance()) return this;
        if (!other.getEncodingPath().isEmpty()) {
          encodingPath_ = other.encodingPath_;
          onChanged();
        }
        if (other.getCollectionId() != 0L) {
          setCollectionId(other.getCollectionId());
        }
        if (other.getCollectionStartTime() != 0L) {
          setCollectionStartTime(other.getCollectionStartTime());
        }
        if (other.getMsgTimestamp() != 0L) {
          setMsgTimestamp(other.getMsgTimestamp());
        }
        if (dataGpbkvBuilder_ == null) {
          if (!other.dataGpbkv_.isEmpty()) {
            if (dataGpbkv_.isEmpty()) {
              dataGpbkv_ = other.dataGpbkv_;
              bitField0_ = (bitField0_ & ~0x00000040);
            } else {
              ensureDataGpbkvIsMutable();
              dataGpbkv_.addAll(other.dataGpbkv_);
            }
            onChanged();
          }
        } else {
          if (!other.dataGpbkv_.isEmpty()) {
            if (dataGpbkvBuilder_.isEmpty()) {
              dataGpbkvBuilder_.dispose();
              dataGpbkvBuilder_ = null;
              dataGpbkv_ = other.dataGpbkv_;
              bitField0_ = (bitField0_ & ~0x00000040);
              dataGpbkvBuilder_ = 
                com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders ?
                   getDataGpbkvFieldBuilder() : null;
            } else {
              dataGpbkvBuilder_.addAllMessages(other.dataGpbkv_);
            }
          }
        }
        if (other.hasDataGpb()) {
          mergeDataGpb(other.getDataGpb());
        }
        if (other.getCollectionEndTime() != 0L) {
          setCollectionEndTime(other.getCollectionEndTime());
        }
        switch (other.getNodeIdCase()) {
          case NODE_ID_STR: {
            nodeIdCase_ = 1;
            nodeId_ = other.nodeId_;
            onChanged();
            break;
          }
          case NODEID_NOT_SET: {
            break;
          }
        }
        switch (other.getSubscriptionCase()) {
          case SUBSCRIPTION_ID_STR: {
            subscriptionCase_ = 3;
            subscription_ = other.subscription_;
            onChanged();
            break;
          }
          case SUBSCRIPTION_NOT_SET: {
            break;
          }
        }
        this.mergeUnknownFields(other.unknownFields);
        onChanged();
        return this;
      }

      public final boolean isInitialized() {
        return true;
      }

      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        TelemetryBis.Telemetry parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (TelemetryBis.Telemetry) e.getUnfinishedMessage();
          throw e.unwrapIOException();
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }
      private int nodeIdCase_ = 0;
      private java.lang.Object nodeId_;
      public NodeIdCase
          getNodeIdCase() {
        return NodeIdCase.forNumber(
            nodeIdCase_);
      }

      public Builder clearNodeId() {
        nodeIdCase_ = 0;
        nodeId_ = null;
        onChanged();
        return this;
      }

      private int subscriptionCase_ = 0;
      private java.lang.Object subscription_;
      public SubscriptionCase
          getSubscriptionCase() {
        return SubscriptionCase.forNumber(
            subscriptionCase_);
      }

      public Builder clearSubscription() {
        subscriptionCase_ = 0;
        subscription_ = null;
        onChanged();
        return this;
      }

      private int bitField0_;

      /**
       * <pre>
       *  bytes node_id_uuid = 2;              // not produced
       * </pre>
       *
       * <code>string node_id_str = 1;</code>
       */
      public java.lang.String getNodeIdStr() {
        java.lang.Object ref = "";
        if (nodeIdCase_ == 1) {
          ref = nodeId_;
        }
        if (!(ref instanceof java.lang.String)) {
          com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
          java.lang.String s = bs.toStringUtf8();
          if (nodeIdCase_ == 1) {
            nodeId_ = s;
          }
          return s;
        } else {
          return (java.lang.String) ref;
        }
      }
      /**
       * <pre>
       *  bytes node_id_uuid = 2;              // not produced
       * </pre>
       *
       * <code>string node_id_str = 1;</code>
       */
      public com.google.protobuf.ByteString
          getNodeIdStrBytes() {
        java.lang.Object ref = "";
        if (nodeIdCase_ == 1) {
          ref = nodeId_;
        }
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (java.lang.String) ref);
          if (nodeIdCase_ == 1) {
            nodeId_ = b;
          }
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <pre>
       *  bytes node_id_uuid = 2;              // not produced
       * </pre>
       *
       * <code>string node_id_str = 1;</code>
       */
      public Builder setNodeIdStr(
          java.lang.String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  nodeIdCase_ = 1;
        nodeId_ = value;
        onChanged();
        return this;
      }
      /**
       * <pre>
       *  bytes node_id_uuid = 2;              // not produced
       * </pre>
       *
       * <code>string node_id_str = 1;</code>
       */
      public Builder clearNodeIdStr() {
        if (nodeIdCase_ == 1) {
          nodeIdCase_ = 0;
          nodeId_ = null;
          onChanged();
        }
        return this;
      }
      /**
       * <pre>
       *  bytes node_id_uuid = 2;              // not produced
       * </pre>
       *
       * <code>string node_id_str = 1;</code>
       */
      public Builder setNodeIdStrBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
        nodeIdCase_ = 1;
        nodeId_ = value;
        onChanged();
        return this;
      }

      /**
       * <pre>
       *  uint32   subscription_id = 4;        // not produced
       * </pre>
       *
       * <code>string subscription_id_str = 3;</code>
       */
      public java.lang.String getSubscriptionIdStr() {
        java.lang.Object ref = "";
        if (subscriptionCase_ == 3) {
          ref = subscription_;
        }
        if (!(ref instanceof java.lang.String)) {
          com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
          java.lang.String s = bs.toStringUtf8();
          if (subscriptionCase_ == 3) {
            subscription_ = s;
          }
          return s;
        } else {
          return (java.lang.String) ref;
        }
      }
      /**
       * <pre>
       *  uint32   subscription_id = 4;        // not produced
       * </pre>
       *
       * <code>string subscription_id_str = 3;</code>
       */
      public com.google.protobuf.ByteString
          getSubscriptionIdStrBytes() {
        java.lang.Object ref = "";
        if (subscriptionCase_ == 3) {
          ref = subscription_;
        }
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (java.lang.String) ref);
          if (subscriptionCase_ == 3) {
            subscription_ = b;
          }
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <pre>
       *  uint32   subscription_id = 4;        // not produced
       * </pre>
       *
       * <code>string subscription_id_str = 3;</code>
       */
      public Builder setSubscriptionIdStr(
          java.lang.String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  subscriptionCase_ = 3;
        subscription_ = value;
        onChanged();
        return this;
      }
      /**
       * <pre>
       *  uint32   subscription_id = 4;        // not produced
       * </pre>
       *
       * <code>string subscription_id_str = 3;</code>
       */
      public Builder clearSubscriptionIdStr() {
        if (subscriptionCase_ == 3) {
          subscriptionCase_ = 0;
          subscription_ = null;
          onChanged();
        }
        return this;
      }
      /**
       * <pre>
       *  uint32   subscription_id = 4;        // not produced
       * </pre>
       *
       * <code>string subscription_id_str = 3;</code>
       */
      public Builder setSubscriptionIdStrBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
        subscriptionCase_ = 3;
        subscription_ = value;
        onChanged();
        return this;
      }

      private java.lang.Object encodingPath_ = "";
      /**
       * <pre>
       * string   sensor_path = 5;               // not produced
       * </pre>
       *
       * <code>string encoding_path = 6;</code>
       */
      public java.lang.String getEncodingPath() {
        java.lang.Object ref = encodingPath_;
        if (!(ref instanceof java.lang.String)) {
          com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
          java.lang.String s = bs.toStringUtf8();
          encodingPath_ = s;
          return s;
        } else {
          return (java.lang.String) ref;
        }
      }
      /**
       * <pre>
       * string   sensor_path = 5;               // not produced
       * </pre>
       *
       * <code>string encoding_path = 6;</code>
       */
      public com.google.protobuf.ByteString
          getEncodingPathBytes() {
        java.lang.Object ref = encodingPath_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (java.lang.String) ref);
          encodingPath_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <pre>
       * string   sensor_path = 5;               // not produced
       * </pre>
       *
       * <code>string encoding_path = 6;</code>
       */
      public Builder setEncodingPath(
          java.lang.String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  
        encodingPath_ = value;
        onChanged();
        return this;
      }
      /**
       * <pre>
       * string   sensor_path = 5;               // not produced
       * </pre>
       *
       * <code>string encoding_path = 6;</code>
       */
      public Builder clearEncodingPath() {
        
        encodingPath_ = getDefaultInstance().getEncodingPath();
        onChanged();
        return this;
      }
      /**
       * <pre>
       * string   sensor_path = 5;               // not produced
       * </pre>
       *
       * <code>string encoding_path = 6;</code>
       */
      public Builder setEncodingPathBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
        
        encodingPath_ = value;
        onChanged();
        return this;
      }

      private long collectionId_ ;
      /**
       * <pre>
       * string   model_version = 7;             // not produced
       * </pre>
       *
       * <code>uint64 collection_id = 8;</code>
       */
      public long getCollectionId() {
        return collectionId_;
      }
      /**
       * <pre>
       * string   model_version = 7;             // not produced
       * </pre>
       *
       * <code>uint64 collection_id = 8;</code>
       */
      public Builder setCollectionId(long value) {
        
        collectionId_ = value;
        onChanged();
        return this;
      }
      /**
       * <pre>
       * string   model_version = 7;             // not produced
       * </pre>
       *
       * <code>uint64 collection_id = 8;</code>
       */
      public Builder clearCollectionId() {
        
        collectionId_ = 0L;
        onChanged();
        return this;
      }

      private long collectionStartTime_ ;
      /**
       * <code>uint64 collection_start_time = 9;</code>
       */
      public long getCollectionStartTime() {
        return collectionStartTime_;
      }
      /**
       * <code>uint64 collection_start_time = 9;</code>
       */
      public Builder setCollectionStartTime(long value) {
        
        collectionStartTime_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>uint64 collection_start_time = 9;</code>
       */
      public Builder clearCollectionStartTime() {
        
        collectionStartTime_ = 0L;
        onChanged();
        return this;
      }

      private long msgTimestamp_ ;
      /**
       * <code>uint64 msg_timestamp = 10;</code>
       */
      public long getMsgTimestamp() {
        return msgTimestamp_;
      }
      /**
       * <code>uint64 msg_timestamp = 10;</code>
       */
      public Builder setMsgTimestamp(long value) {
        
        msgTimestamp_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>uint64 msg_timestamp = 10;</code>
       */
      public Builder clearMsgTimestamp() {
        
        msgTimestamp_ = 0L;
        onChanged();
        return this;
      }

      private java.util.List<TelemetryBis.TelemetryField> dataGpbkv_ =
        java.util.Collections.emptyList();
      private void ensureDataGpbkvIsMutable() {
        if (!((bitField0_ & 0x00000040) == 0x00000040)) {
          dataGpbkv_ = new java.util.ArrayList<TelemetryBis.TelemetryField>(dataGpbkv_);
          bitField0_ |= 0x00000040;
         }
      }

      private com.google.protobuf.RepeatedFieldBuilderV3<
          TelemetryBis.TelemetryField, TelemetryBis.TelemetryField.Builder, TelemetryBis.TelemetryFieldOrBuilder> dataGpbkvBuilder_;

      /**
       * <code>repeated .TelemetryField data_gpbkv = 11;</code>
       */
      public java.util.List<TelemetryBis.TelemetryField> getDataGpbkvList() {
        if (dataGpbkvBuilder_ == null) {
          return java.util.Collections.unmodifiableList(dataGpbkv_);
        } else {
          return dataGpbkvBuilder_.getMessageList();
        }
      }
      /**
       * <code>repeated .TelemetryField data_gpbkv = 11;</code>
       */
      public int getDataGpbkvCount() {
        if (dataGpbkvBuilder_ == null) {
          return dataGpbkv_.size();
        } else {
          return dataGpbkvBuilder_.getCount();
        }
      }
      /**
       * <code>repeated .TelemetryField data_gpbkv = 11;</code>
       */
      public TelemetryBis.TelemetryField getDataGpbkv(int index) {
        if (dataGpbkvBuilder_ == null) {
          return dataGpbkv_.get(index);
        } else {
          return dataGpbkvBuilder_.getMessage(index);
        }
      }
      /**
       * <code>repeated .TelemetryField data_gpbkv = 11;</code>
       */
      public Builder setDataGpbkv(
          int index, TelemetryBis.TelemetryField value) {
        if (dataGpbkvBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          ensureDataGpbkvIsMutable();
          dataGpbkv_.set(index, value);
          onChanged();
        } else {
          dataGpbkvBuilder_.setMessage(index, value);
        }
        return this;
      }
      /**
       * <code>repeated .TelemetryField data_gpbkv = 11;</code>
       */
      public Builder setDataGpbkv(
          int index, TelemetryBis.TelemetryField.Builder builderForValue) {
        if (dataGpbkvBuilder_ == null) {
          ensureDataGpbkvIsMutable();
          dataGpbkv_.set(index, builderForValue.build());
          onChanged();
        } else {
          dataGpbkvBuilder_.setMessage(index, builderForValue.build());
        }
        return this;
      }
      /**
       * <code>repeated .TelemetryField data_gpbkv = 11;</code>
       */
      public Builder addDataGpbkv(TelemetryBis.TelemetryField value) {
        if (dataGpbkvBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          ensureDataGpbkvIsMutable();
          dataGpbkv_.add(value);
          onChanged();
        } else {
          dataGpbkvBuilder_.addMessage(value);
        }
        return this;
      }
      /**
       * <code>repeated .TelemetryField data_gpbkv = 11;</code>
       */
      public Builder addDataGpbkv(
          int index, TelemetryBis.TelemetryField value) {
        if (dataGpbkvBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          ensureDataGpbkvIsMutable();
          dataGpbkv_.add(index, value);
          onChanged();
        } else {
          dataGpbkvBuilder_.addMessage(index, value);
        }
        return this;
      }
      /**
       * <code>repeated .TelemetryField data_gpbkv = 11;</code>
       */
      public Builder addDataGpbkv(
          TelemetryBis.TelemetryField.Builder builderForValue) {
        if (dataGpbkvBuilder_ == null) {
          ensureDataGpbkvIsMutable();
          dataGpbkv_.add(builderForValue.build());
          onChanged();
        } else {
          dataGpbkvBuilder_.addMessage(builderForValue.build());
        }
        return this;
      }
      /**
       * <code>repeated .TelemetryField data_gpbkv = 11;</code>
       */
      public Builder addDataGpbkv(
          int index, TelemetryBis.TelemetryField.Builder builderForValue) {
        if (dataGpbkvBuilder_ == null) {
          ensureDataGpbkvIsMutable();
          dataGpbkv_.add(index, builderForValue.build());
          onChanged();
        } else {
          dataGpbkvBuilder_.addMessage(index, builderForValue.build());
        }
        return this;
      }
      /**
       * <code>repeated .TelemetryField data_gpbkv = 11;</code>
       */
      public Builder addAllDataGpbkv(
          java.lang.Iterable<? extends TelemetryBis.TelemetryField> values) {
        if (dataGpbkvBuilder_ == null) {
          ensureDataGpbkvIsMutable();
          com.google.protobuf.AbstractMessageLite.Builder.addAll(
              values, dataGpbkv_);
          onChanged();
        } else {
          dataGpbkvBuilder_.addAllMessages(values);
        }
        return this;
      }
      /**
       * <code>repeated .TelemetryField data_gpbkv = 11;</code>
       */
      public Builder clearDataGpbkv() {
        if (dataGpbkvBuilder_ == null) {
          dataGpbkv_ = java.util.Collections.emptyList();
          bitField0_ = (bitField0_ & ~0x00000040);
          onChanged();
        } else {
          dataGpbkvBuilder_.clear();
        }
        return this;
      }
      /**
       * <code>repeated .TelemetryField data_gpbkv = 11;</code>
       */
      public Builder removeDataGpbkv(int index) {
        if (dataGpbkvBuilder_ == null) {
          ensureDataGpbkvIsMutable();
          dataGpbkv_.remove(index);
          onChanged();
        } else {
          dataGpbkvBuilder_.remove(index);
        }
        return this;
      }
      /**
       * <code>repeated .TelemetryField data_gpbkv = 11;</code>
       */
      public TelemetryBis.TelemetryField.Builder getDataGpbkvBuilder(
          int index) {
        return getDataGpbkvFieldBuilder().getBuilder(index);
      }
      /**
       * <code>repeated .TelemetryField data_gpbkv = 11;</code>
       */
      public TelemetryBis.TelemetryFieldOrBuilder getDataGpbkvOrBuilder(
          int index) {
        if (dataGpbkvBuilder_ == null) {
          return dataGpbkv_.get(index);  } else {
          return dataGpbkvBuilder_.getMessageOrBuilder(index);
        }
      }
      /**
       * <code>repeated .TelemetryField data_gpbkv = 11;</code>
       */
      public java.util.List<? extends TelemetryBis.TelemetryFieldOrBuilder> 
           getDataGpbkvOrBuilderList() {
        if (dataGpbkvBuilder_ != null) {
          return dataGpbkvBuilder_.getMessageOrBuilderList();
        } else {
          return java.util.Collections.unmodifiableList(dataGpbkv_);
        }
      }
      /**
       * <code>repeated .TelemetryField data_gpbkv = 11;</code>
       */
      public TelemetryBis.TelemetryField.Builder addDataGpbkvBuilder() {
        return getDataGpbkvFieldBuilder().addBuilder(
            TelemetryBis.TelemetryField.getDefaultInstance());
      }
      /**
       * <code>repeated .TelemetryField data_gpbkv = 11;</code>
       */
      public TelemetryBis.TelemetryField.Builder addDataGpbkvBuilder(
          int index) {
        return getDataGpbkvFieldBuilder().addBuilder(
            index, TelemetryBis.TelemetryField.getDefaultInstance());
      }
      /**
       * <code>repeated .TelemetryField data_gpbkv = 11;</code>
       */
      public java.util.List<TelemetryBis.TelemetryField.Builder> 
           getDataGpbkvBuilderList() {
        return getDataGpbkvFieldBuilder().getBuilderList();
      }
      private com.google.protobuf.RepeatedFieldBuilderV3<
          TelemetryBis.TelemetryField, TelemetryBis.TelemetryField.Builder, TelemetryBis.TelemetryFieldOrBuilder> 
          getDataGpbkvFieldBuilder() {
        if (dataGpbkvBuilder_ == null) {
          dataGpbkvBuilder_ = new com.google.protobuf.RepeatedFieldBuilderV3<
              TelemetryBis.TelemetryField, TelemetryBis.TelemetryField.Builder, TelemetryBis.TelemetryFieldOrBuilder>(
                  dataGpbkv_,
                  ((bitField0_ & 0x00000040) == 0x00000040),
                  getParentForChildren(),
                  isClean());
          dataGpbkv_ = null;
        }
        return dataGpbkvBuilder_;
      }

      private TelemetryBis.TelemetryGPBTable dataGpb_ = null;
      private com.google.protobuf.SingleFieldBuilderV3<
          TelemetryBis.TelemetryGPBTable, TelemetryBis.TelemetryGPBTable.Builder, TelemetryBis.TelemetryGPBTableOrBuilder> dataGpbBuilder_;
      /**
       * <code>.TelemetryGPBTable data_gpb = 12;</code>
       */
      public boolean hasDataGpb() {
        return dataGpbBuilder_ != null || dataGpb_ != null;
      }
      /**
       * <code>.TelemetryGPBTable data_gpb = 12;</code>
       */
      public TelemetryBis.TelemetryGPBTable getDataGpb() {
        if (dataGpbBuilder_ == null) {
          return dataGpb_ == null ? TelemetryBis.TelemetryGPBTable.getDefaultInstance() : dataGpb_;
        } else {
          return dataGpbBuilder_.getMessage();
        }
      }
      /**
       * <code>.TelemetryGPBTable data_gpb = 12;</code>
       */
      public Builder setDataGpb(TelemetryBis.TelemetryGPBTable value) {
        if (dataGpbBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          dataGpb_ = value;
          onChanged();
        } else {
          dataGpbBuilder_.setMessage(value);
        }

        return this;
      }
      /**
       * <code>.TelemetryGPBTable data_gpb = 12;</code>
       */
      public Builder setDataGpb(
          TelemetryBis.TelemetryGPBTable.Builder builderForValue) {
        if (dataGpbBuilder_ == null) {
          dataGpb_ = builderForValue.build();
          onChanged();
        } else {
          dataGpbBuilder_.setMessage(builderForValue.build());
        }

        return this;
      }
      /**
       * <code>.TelemetryGPBTable data_gpb = 12;</code>
       */
      public Builder mergeDataGpb(TelemetryBis.TelemetryGPBTable value) {
        if (dataGpbBuilder_ == null) {
          if (dataGpb_ != null) {
            dataGpb_ =
              TelemetryBis.TelemetryGPBTable.newBuilder(dataGpb_).mergeFrom(value).buildPartial();
          } else {
            dataGpb_ = value;
          }
          onChanged();
        } else {
          dataGpbBuilder_.mergeFrom(value);
        }

        return this;
      }
      /**
       * <code>.TelemetryGPBTable data_gpb = 12;</code>
       */
      public Builder clearDataGpb() {
        if (dataGpbBuilder_ == null) {
          dataGpb_ = null;
          onChanged();
        } else {
          dataGpb_ = null;
          dataGpbBuilder_ = null;
        }

        return this;
      }
      /**
       * <code>.TelemetryGPBTable data_gpb = 12;</code>
       */
      public TelemetryBis.TelemetryGPBTable.Builder getDataGpbBuilder() {
        
        onChanged();
        return getDataGpbFieldBuilder().getBuilder();
      }
      /**
       * <code>.TelemetryGPBTable data_gpb = 12;</code>
       */
      public TelemetryBis.TelemetryGPBTableOrBuilder getDataGpbOrBuilder() {
        if (dataGpbBuilder_ != null) {
          return dataGpbBuilder_.getMessageOrBuilder();
        } else {
          return dataGpb_ == null ?
              TelemetryBis.TelemetryGPBTable.getDefaultInstance() : dataGpb_;
        }
      }
      /**
       * <code>.TelemetryGPBTable data_gpb = 12;</code>
       */
      private com.google.protobuf.SingleFieldBuilderV3<
          TelemetryBis.TelemetryGPBTable, TelemetryBis.TelemetryGPBTable.Builder, TelemetryBis.TelemetryGPBTableOrBuilder> 
          getDataGpbFieldBuilder() {
        if (dataGpbBuilder_ == null) {
          dataGpbBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
              TelemetryBis.TelemetryGPBTable, TelemetryBis.TelemetryGPBTable.Builder, TelemetryBis.TelemetryGPBTableOrBuilder>(
                  getDataGpb(),
                  getParentForChildren(),
                  isClean());
          dataGpb_ = null;
        }
        return dataGpbBuilder_;
      }

      private long collectionEndTime_ ;
      /**
       * <pre>
       * uint64   heartbeat_sequence_number = 14; // not produced
       * </pre>
       *
       * <code>uint64 collection_end_time = 13;</code>
       */
      public long getCollectionEndTime() {
        return collectionEndTime_;
      }
      /**
       * <pre>
       * uint64   heartbeat_sequence_number = 14; // not produced
       * </pre>
       *
       * <code>uint64 collection_end_time = 13;</code>
       */
      public Builder setCollectionEndTime(long value) {
        
        collectionEndTime_ = value;
        onChanged();
        return this;
      }
      /**
       * <pre>
       * uint64   heartbeat_sequence_number = 14; // not produced
       * </pre>
       *
       * <code>uint64 collection_end_time = 13;</code>
       */
      public Builder clearCollectionEndTime() {
        
        collectionEndTime_ = 0L;
        onChanged();
        return this;
      }
      public final Builder setUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.setUnknownFieldsProto3(unknownFields);
      }

      public final Builder mergeUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.mergeUnknownFields(unknownFields);
      }


      // @@protoc_insertion_point(builder_scope:Telemetry)
    }

    // @@protoc_insertion_point(class_scope:Telemetry)
    private static final TelemetryBis.Telemetry DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new TelemetryBis.Telemetry();
    }

    public static TelemetryBis.Telemetry getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<Telemetry>
        PARSER = new com.google.protobuf.AbstractParser<Telemetry>() {
      public Telemetry parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
          return new Telemetry(input, extensionRegistry);
      }
    };

    public static com.google.protobuf.Parser<Telemetry> parser() {
      return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<Telemetry> getParserForType() {
      return PARSER;
    }

    public TelemetryBis.Telemetry getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  public interface TelemetryFieldOrBuilder extends
      // @@protoc_insertion_point(interface_extends:TelemetryField)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <code>uint64 timestamp = 1;</code>
     */
    long getTimestamp();

    /**
     * <code>string name = 2;</code>
     */
    java.lang.String getName();
    /**
     * <code>string name = 2;</code>
     */
    com.google.protobuf.ByteString
        getNameBytes();

    /**
     * <code>bytes bytes_value = 4;</code>
     */
    com.google.protobuf.ByteString getBytesValue();

    /**
     * <code>string string_value = 5;</code>
     */
    java.lang.String getStringValue();
    /**
     * <code>string string_value = 5;</code>
     */
    com.google.protobuf.ByteString
        getStringValueBytes();

    /**
     * <code>bool bool_value = 6;</code>
     */
    boolean getBoolValue();

    /**
     * <code>uint32 uint32_value = 7;</code>
     */
    int getUint32Value();

    /**
     * <code>uint64 uint64_value = 8;</code>
     */
    long getUint64Value();

    /**
     * <code>sint32 sint32_value = 9;</code>
     */
    int getSint32Value();

    /**
     * <code>sint64 sint64_value = 10;</code>
     */
    long getSint64Value();

    /**
     * <code>double double_value = 11;</code>
     */
    double getDoubleValue();

    /**
     * <code>float float_value = 12;</code>
     */
    float getFloatValue();

    /**
     * <code>repeated .TelemetryField fields = 15;</code>
     */
    java.util.List<TelemetryBis.TelemetryField> 
        getFieldsList();
    /**
     * <code>repeated .TelemetryField fields = 15;</code>
     */
    TelemetryBis.TelemetryField getFields(int index);
    /**
     * <code>repeated .TelemetryField fields = 15;</code>
     */
    int getFieldsCount();
    /**
     * <code>repeated .TelemetryField fields = 15;</code>
     */
    java.util.List<? extends TelemetryBis.TelemetryFieldOrBuilder> 
        getFieldsOrBuilderList();
    /**
     * <code>repeated .TelemetryField fields = 15;</code>
     */
    TelemetryBis.TelemetryFieldOrBuilder getFieldsOrBuilder(
        int index);

    public TelemetryBis.TelemetryField.ValueByTypeCase getValueByTypeCase();
  }
  /**
   * Protobuf type {@code TelemetryField}
   */
  public  static final class TelemetryField extends
      com.google.protobuf.GeneratedMessageV3 implements
      // @@protoc_insertion_point(message_implements:TelemetryField)
      TelemetryFieldOrBuilder {
  private static final long serialVersionUID = 0L;
    // Use TelemetryField.newBuilder() to construct.
    private TelemetryField(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
      super(builder);
    }
    private TelemetryField() {
      timestamp_ = 0L;
      name_ = "";
      fields_ = java.util.Collections.emptyList();
    }

    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return this.unknownFields;
    }
    private TelemetryField(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      this();
      int mutable_bitField0_ = 0;
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            default: {
              if (!parseUnknownFieldProto3(
                  input, unknownFields, extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
            case 8: {

              timestamp_ = input.readUInt64();
              break;
            }
            case 18: {
              java.lang.String s = input.readStringRequireUtf8();

              name_ = s;
              break;
            }
            case 34: {
              valueByTypeCase_ = 4;
              valueByType_ = input.readBytes();
              break;
            }
            case 42: {
              java.lang.String s = input.readStringRequireUtf8();
              valueByTypeCase_ = 5;
              valueByType_ = s;
              break;
            }
            case 48: {
              valueByTypeCase_ = 6;
              valueByType_ = input.readBool();
              break;
            }
            case 56: {
              valueByTypeCase_ = 7;
              valueByType_ = input.readUInt32();
              break;
            }
            case 64: {
              valueByTypeCase_ = 8;
              valueByType_ = input.readUInt64();
              break;
            }
            case 72: {
              valueByTypeCase_ = 9;
              valueByType_ = input.readSInt32();
              break;
            }
            case 80: {
              valueByTypeCase_ = 10;
              valueByType_ = input.readSInt64();
              break;
            }
            case 89: {
              valueByTypeCase_ = 11;
              valueByType_ = input.readDouble();
              break;
            }
            case 101: {
              valueByTypeCase_ = 12;
              valueByType_ = input.readFloat();
              break;
            }
            case 122: {
              if (!((mutable_bitField0_ & 0x00000800) == 0x00000800)) {
                fields_ = new java.util.ArrayList<TelemetryBis.TelemetryField>();
                mutable_bitField0_ |= 0x00000800;
              }
              fields_.add(
                  input.readMessage(TelemetryBis.TelemetryField.parser(), extensionRegistry));
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
            e).setUnfinishedMessage(this);
      } finally {
        if (((mutable_bitField0_ & 0x00000800) == 0x00000800)) {
          fields_ = java.util.Collections.unmodifiableList(fields_);
        }
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return TelemetryBis.internal_static_TelemetryField_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return TelemetryBis.internal_static_TelemetryField_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              TelemetryBis.TelemetryField.class, TelemetryBis.TelemetryField.Builder.class);
    }

    private int bitField0_;
    private int valueByTypeCase_ = 0;
    private java.lang.Object valueByType_;
    public enum ValueByTypeCase
        implements com.google.protobuf.Internal.EnumLite {
      BYTES_VALUE(4),
      STRING_VALUE(5),
      BOOL_VALUE(6),
      UINT32_VALUE(7),
      UINT64_VALUE(8),
      SINT32_VALUE(9),
      SINT64_VALUE(10),
      DOUBLE_VALUE(11),
      FLOAT_VALUE(12),
      VALUEBYTYPE_NOT_SET(0);
      private final int value;
      private ValueByTypeCase(int value) {
        this.value = value;
      }
      /**
       * @deprecated Use {@link #forNumber(int)} instead.
       */
      @java.lang.Deprecated
      public static ValueByTypeCase valueOf(int value) {
        return forNumber(value);
      }

      public static ValueByTypeCase forNumber(int value) {
        switch (value) {
          case 4: return BYTES_VALUE;
          case 5: return STRING_VALUE;
          case 6: return BOOL_VALUE;
          case 7: return UINT32_VALUE;
          case 8: return UINT64_VALUE;
          case 9: return SINT32_VALUE;
          case 10: return SINT64_VALUE;
          case 11: return DOUBLE_VALUE;
          case 12: return FLOAT_VALUE;
          case 0: return VALUEBYTYPE_NOT_SET;
          default: return null;
        }
      }
      public int getNumber() {
        return this.value;
      }
    };

    public ValueByTypeCase
    getValueByTypeCase() {
      return ValueByTypeCase.forNumber(
          valueByTypeCase_);
    }

    public static final int TIMESTAMP_FIELD_NUMBER = 1;
    private long timestamp_;
    /**
     * <code>uint64 timestamp = 1;</code>
     */
    public long getTimestamp() {
      return timestamp_;
    }

    public static final int NAME_FIELD_NUMBER = 2;
    private volatile java.lang.Object name_;
    /**
     * <code>string name = 2;</code>
     */
    public java.lang.String getName() {
      java.lang.Object ref = name_;
      if (ref instanceof java.lang.String) {
        return (java.lang.String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        name_ = s;
        return s;
      }
    }
    /**
     * <code>string name = 2;</code>
     */
    public com.google.protobuf.ByteString
        getNameBytes() {
      java.lang.Object ref = name_;
      if (ref instanceof java.lang.String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        name_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    public static final int BYTES_VALUE_FIELD_NUMBER = 4;
    /**
     * <code>bytes bytes_value = 4;</code>
     */
    public com.google.protobuf.ByteString getBytesValue() {
      if (valueByTypeCase_ == 4) {
        return (com.google.protobuf.ByteString) valueByType_;
      }
      return com.google.protobuf.ByteString.EMPTY;
    }

    public static final int STRING_VALUE_FIELD_NUMBER = 5;
    /**
     * <code>string string_value = 5;</code>
     */
    public java.lang.String getStringValue() {
      java.lang.Object ref = "";
      if (valueByTypeCase_ == 5) {
        ref = valueByType_;
      }
      if (ref instanceof java.lang.String) {
        return (java.lang.String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        if (valueByTypeCase_ == 5) {
          valueByType_ = s;
        }
        return s;
      }
    }
    /**
     * <code>string string_value = 5;</code>
     */
    public com.google.protobuf.ByteString
        getStringValueBytes() {
      java.lang.Object ref = "";
      if (valueByTypeCase_ == 5) {
        ref = valueByType_;
      }
      if (ref instanceof java.lang.String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        if (valueByTypeCase_ == 5) {
          valueByType_ = b;
        }
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    public static final int BOOL_VALUE_FIELD_NUMBER = 6;
    /**
     * <code>bool bool_value = 6;</code>
     */
    public boolean getBoolValue() {
      if (valueByTypeCase_ == 6) {
        return (java.lang.Boolean) valueByType_;
      }
      return false;
    }

    public static final int UINT32_VALUE_FIELD_NUMBER = 7;
    /**
     * <code>uint32 uint32_value = 7;</code>
     */
    public int getUint32Value() {
      if (valueByTypeCase_ == 7) {
        return (java.lang.Integer) valueByType_;
      }
      return 0;
    }

    public static final int UINT64_VALUE_FIELD_NUMBER = 8;
    /**
     * <code>uint64 uint64_value = 8;</code>
     */
    public long getUint64Value() {
      if (valueByTypeCase_ == 8) {
        return (java.lang.Long) valueByType_;
      }
      return 0L;
    }

    public static final int SINT32_VALUE_FIELD_NUMBER = 9;
    /**
     * <code>sint32 sint32_value = 9;</code>
     */
    public int getSint32Value() {
      if (valueByTypeCase_ == 9) {
        return (java.lang.Integer) valueByType_;
      }
      return 0;
    }

    public static final int SINT64_VALUE_FIELD_NUMBER = 10;
    /**
     * <code>sint64 sint64_value = 10;</code>
     */
    public long getSint64Value() {
      if (valueByTypeCase_ == 10) {
        return (java.lang.Long) valueByType_;
      }
      return 0L;
    }

    public static final int DOUBLE_VALUE_FIELD_NUMBER = 11;
    /**
     * <code>double double_value = 11;</code>
     */
    public double getDoubleValue() {
      if (valueByTypeCase_ == 11) {
        return (java.lang.Double) valueByType_;
      }
      return 0D;
    }

    public static final int FLOAT_VALUE_FIELD_NUMBER = 12;
    /**
     * <code>float float_value = 12;</code>
     */
    public float getFloatValue() {
      if (valueByTypeCase_ == 12) {
        return (java.lang.Float) valueByType_;
      }
      return 0F;
    }

    public static final int FIELDS_FIELD_NUMBER = 15;
    private java.util.List<TelemetryBis.TelemetryField> fields_;
    /**
     * <code>repeated .TelemetryField fields = 15;</code>
     */
    public java.util.List<TelemetryBis.TelemetryField> getFieldsList() {
      return fields_;
    }
    /**
     * <code>repeated .TelemetryField fields = 15;</code>
     */
    public java.util.List<? extends TelemetryBis.TelemetryFieldOrBuilder> 
        getFieldsOrBuilderList() {
      return fields_;
    }
    /**
     * <code>repeated .TelemetryField fields = 15;</code>
     */
    public int getFieldsCount() {
      return fields_.size();
    }
    /**
     * <code>repeated .TelemetryField fields = 15;</code>
     */
    public TelemetryBis.TelemetryField getFields(int index) {
      return fields_.get(index);
    }
    /**
     * <code>repeated .TelemetryField fields = 15;</code>
     */
    public TelemetryBis.TelemetryFieldOrBuilder getFieldsOrBuilder(
        int index) {
      return fields_.get(index);
    }

    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      if (timestamp_ != 0L) {
        output.writeUInt64(1, timestamp_);
      }
      if (!getNameBytes().isEmpty()) {
        com.google.protobuf.GeneratedMessageV3.writeString(output, 2, name_);
      }
      if (valueByTypeCase_ == 4) {
        output.writeBytes(
            4, (com.google.protobuf.ByteString) valueByType_);
      }
      if (valueByTypeCase_ == 5) {
        com.google.protobuf.GeneratedMessageV3.writeString(output, 5, valueByType_);
      }
      if (valueByTypeCase_ == 6) {
        output.writeBool(
            6, (boolean)((java.lang.Boolean) valueByType_));
      }
      if (valueByTypeCase_ == 7) {
        output.writeUInt32(
            7, (int)((java.lang.Integer) valueByType_));
      }
      if (valueByTypeCase_ == 8) {
        output.writeUInt64(
            8, (long)((java.lang.Long) valueByType_));
      }
      if (valueByTypeCase_ == 9) {
        output.writeSInt32(
            9, (int)((java.lang.Integer) valueByType_));
      }
      if (valueByTypeCase_ == 10) {
        output.writeSInt64(
            10, (long)((java.lang.Long) valueByType_));
      }
      if (valueByTypeCase_ == 11) {
        output.writeDouble(
            11, (double)((java.lang.Double) valueByType_));
      }
      if (valueByTypeCase_ == 12) {
        output.writeFloat(
            12, (float)((java.lang.Float) valueByType_));
      }
      for (int i = 0; i < fields_.size(); i++) {
        output.writeMessage(15, fields_.get(i));
      }
      unknownFields.writeTo(output);
    }

    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      if (timestamp_ != 0L) {
        size += com.google.protobuf.CodedOutputStream
          .computeUInt64Size(1, timestamp_);
      }
      if (!getNameBytes().isEmpty()) {
        size += com.google.protobuf.GeneratedMessageV3.computeStringSize(2, name_);
      }
      if (valueByTypeCase_ == 4) {
        size += com.google.protobuf.CodedOutputStream
          .computeBytesSize(
              4, (com.google.protobuf.ByteString) valueByType_);
      }
      if (valueByTypeCase_ == 5) {
        size += com.google.protobuf.GeneratedMessageV3.computeStringSize(5, valueByType_);
      }
      if (valueByTypeCase_ == 6) {
        size += com.google.protobuf.CodedOutputStream
          .computeBoolSize(
              6, (boolean)((java.lang.Boolean) valueByType_));
      }
      if (valueByTypeCase_ == 7) {
        size += com.google.protobuf.CodedOutputStream
          .computeUInt32Size(
              7, (int)((java.lang.Integer) valueByType_));
      }
      if (valueByTypeCase_ == 8) {
        size += com.google.protobuf.CodedOutputStream
          .computeUInt64Size(
              8, (long)((java.lang.Long) valueByType_));
      }
      if (valueByTypeCase_ == 9) {
        size += com.google.protobuf.CodedOutputStream
          .computeSInt32Size(
              9, (int)((java.lang.Integer) valueByType_));
      }
      if (valueByTypeCase_ == 10) {
        size += com.google.protobuf.CodedOutputStream
          .computeSInt64Size(
              10, (long)((java.lang.Long) valueByType_));
      }
      if (valueByTypeCase_ == 11) {
        size += com.google.protobuf.CodedOutputStream
          .computeDoubleSize(
              11, (double)((java.lang.Double) valueByType_));
      }
      if (valueByTypeCase_ == 12) {
        size += com.google.protobuf.CodedOutputStream
          .computeFloatSize(
              12, (float)((java.lang.Float) valueByType_));
      }
      for (int i = 0; i < fields_.size(); i++) {
        size += com.google.protobuf.CodedOutputStream
          .computeMessageSize(15, fields_.get(i));
      }
      size += unknownFields.getSerializedSize();
      memoizedSize = size;
      return size;
    }

    @java.lang.Override
    public boolean equals(final java.lang.Object obj) {
      if (obj == this) {
       return true;
      }
      if (!(obj instanceof TelemetryBis.TelemetryField)) {
        return super.equals(obj);
      }
      TelemetryBis.TelemetryField other = (TelemetryBis.TelemetryField) obj;

      boolean result = true;
      result = result && (getTimestamp()
          == other.getTimestamp());
      result = result && getName()
          .equals(other.getName());
      result = result && getFieldsList()
          .equals(other.getFieldsList());
      result = result && getValueByTypeCase().equals(
          other.getValueByTypeCase());
      if (!result) return false;
      switch (valueByTypeCase_) {
        case 4:
          result = result && getBytesValue()
              .equals(other.getBytesValue());
          break;
        case 5:
          result = result && getStringValue()
              .equals(other.getStringValue());
          break;
        case 6:
          result = result && (getBoolValue()
              == other.getBoolValue());
          break;
        case 7:
          result = result && (getUint32Value()
              == other.getUint32Value());
          break;
        case 8:
          result = result && (getUint64Value()
              == other.getUint64Value());
          break;
        case 9:
          result = result && (getSint32Value()
              == other.getSint32Value());
          break;
        case 10:
          result = result && (getSint64Value()
              == other.getSint64Value());
          break;
        case 11:
          result = result && (
              java.lang.Double.doubleToLongBits(getDoubleValue())
              == java.lang.Double.doubleToLongBits(
                  other.getDoubleValue()));
          break;
        case 12:
          result = result && (
              java.lang.Float.floatToIntBits(getFloatValue())
              == java.lang.Float.floatToIntBits(
                  other.getFloatValue()));
          break;
        case 0:
        default:
      }
      result = result && unknownFields.equals(other.unknownFields);
      return result;
    }

    @java.lang.Override
    public int hashCode() {
      if (memoizedHashCode != 0) {
        return memoizedHashCode;
      }
      int hash = 41;
      hash = (19 * hash) + getDescriptor().hashCode();
      hash = (37 * hash) + TIMESTAMP_FIELD_NUMBER;
      hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
          getTimestamp());
      hash = (37 * hash) + NAME_FIELD_NUMBER;
      hash = (53 * hash) + getName().hashCode();
      if (getFieldsCount() > 0) {
        hash = (37 * hash) + FIELDS_FIELD_NUMBER;
        hash = (53 * hash) + getFieldsList().hashCode();
      }
      switch (valueByTypeCase_) {
        case 4:
          hash = (37 * hash) + BYTES_VALUE_FIELD_NUMBER;
          hash = (53 * hash) + getBytesValue().hashCode();
          break;
        case 5:
          hash = (37 * hash) + STRING_VALUE_FIELD_NUMBER;
          hash = (53 * hash) + getStringValue().hashCode();
          break;
        case 6:
          hash = (37 * hash) + BOOL_VALUE_FIELD_NUMBER;
          hash = (53 * hash) + com.google.protobuf.Internal.hashBoolean(
              getBoolValue());
          break;
        case 7:
          hash = (37 * hash) + UINT32_VALUE_FIELD_NUMBER;
          hash = (53 * hash) + getUint32Value();
          break;
        case 8:
          hash = (37 * hash) + UINT64_VALUE_FIELD_NUMBER;
          hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
              getUint64Value());
          break;
        case 9:
          hash = (37 * hash) + SINT32_VALUE_FIELD_NUMBER;
          hash = (53 * hash) + getSint32Value();
          break;
        case 10:
          hash = (37 * hash) + SINT64_VALUE_FIELD_NUMBER;
          hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
              getSint64Value());
          break;
        case 11:
          hash = (37 * hash) + DOUBLE_VALUE_FIELD_NUMBER;
          hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
              java.lang.Double.doubleToLongBits(getDoubleValue()));
          break;
        case 12:
          hash = (37 * hash) + FLOAT_VALUE_FIELD_NUMBER;
          hash = (53 * hash) + java.lang.Float.floatToIntBits(
              getFloatValue());
          break;
        case 0:
        default:
      }
      hash = (29 * hash) + unknownFields.hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static TelemetryBis.TelemetryField parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static TelemetryBis.TelemetryField parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static TelemetryBis.TelemetryField parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static TelemetryBis.TelemetryField parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static TelemetryBis.TelemetryField parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static TelemetryBis.TelemetryField parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static TelemetryBis.TelemetryField parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static TelemetryBis.TelemetryField parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }
    public static TelemetryBis.TelemetryField parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input);
    }
    public static TelemetryBis.TelemetryField parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static TelemetryBis.TelemetryField parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static TelemetryBis.TelemetryField parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
    }
    public static Builder newBuilder(TelemetryBis.TelemetryField prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }
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
     * Protobuf type {@code TelemetryField}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:TelemetryField)
        TelemetryBis.TelemetryFieldOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return TelemetryBis.internal_static_TelemetryField_descriptor;
      }

      protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return TelemetryBis.internal_static_TelemetryField_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                TelemetryBis.TelemetryField.class, TelemetryBis.TelemetryField.Builder.class);
      }

      // Construct using TelemetryBis.TelemetryField.newBuilder()
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
          getFieldsFieldBuilder();
        }
      }
      public Builder clear() {
        super.clear();
        timestamp_ = 0L;

        name_ = "";

        if (fieldsBuilder_ == null) {
          fields_ = java.util.Collections.emptyList();
          bitField0_ = (bitField0_ & ~0x00000800);
        } else {
          fieldsBuilder_.clear();
        }
        valueByTypeCase_ = 0;
        valueByType_ = null;
        return this;
      }

      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return TelemetryBis.internal_static_TelemetryField_descriptor;
      }

      public TelemetryBis.TelemetryField getDefaultInstanceForType() {
        return TelemetryBis.TelemetryField.getDefaultInstance();
      }

      public TelemetryBis.TelemetryField build() {
        TelemetryBis.TelemetryField result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public TelemetryBis.TelemetryField buildPartial() {
        TelemetryBis.TelemetryField result = new TelemetryBis.TelemetryField(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        result.timestamp_ = timestamp_;
        result.name_ = name_;
        if (valueByTypeCase_ == 4) {
          result.valueByType_ = valueByType_;
        }
        if (valueByTypeCase_ == 5) {
          result.valueByType_ = valueByType_;
        }
        if (valueByTypeCase_ == 6) {
          result.valueByType_ = valueByType_;
        }
        if (valueByTypeCase_ == 7) {
          result.valueByType_ = valueByType_;
        }
        if (valueByTypeCase_ == 8) {
          result.valueByType_ = valueByType_;
        }
        if (valueByTypeCase_ == 9) {
          result.valueByType_ = valueByType_;
        }
        if (valueByTypeCase_ == 10) {
          result.valueByType_ = valueByType_;
        }
        if (valueByTypeCase_ == 11) {
          result.valueByType_ = valueByType_;
        }
        if (valueByTypeCase_ == 12) {
          result.valueByType_ = valueByType_;
        }
        if (fieldsBuilder_ == null) {
          if (((bitField0_ & 0x00000800) == 0x00000800)) {
            fields_ = java.util.Collections.unmodifiableList(fields_);
            bitField0_ = (bitField0_ & ~0x00000800);
          }
          result.fields_ = fields_;
        } else {
          result.fields_ = fieldsBuilder_.build();
        }
        result.bitField0_ = to_bitField0_;
        result.valueByTypeCase_ = valueByTypeCase_;
        onBuilt();
        return result;
      }

      public Builder clone() {
        return (Builder) super.clone();
      }
      public Builder setField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return (Builder) super.setField(field, value);
      }
      public Builder clearField(
          com.google.protobuf.Descriptors.FieldDescriptor field) {
        return (Builder) super.clearField(field);
      }
      public Builder clearOneof(
          com.google.protobuf.Descriptors.OneofDescriptor oneof) {
        return (Builder) super.clearOneof(oneof);
      }
      public Builder setRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          int index, java.lang.Object value) {
        return (Builder) super.setRepeatedField(field, index, value);
      }
      public Builder addRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return (Builder) super.addRepeatedField(field, value);
      }
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof TelemetryBis.TelemetryField) {
          return mergeFrom((TelemetryBis.TelemetryField)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(TelemetryBis.TelemetryField other) {
        if (other == TelemetryBis.TelemetryField.getDefaultInstance()) return this;
        if (other.getTimestamp() != 0L) {
          setTimestamp(other.getTimestamp());
        }
        if (!other.getName().isEmpty()) {
          name_ = other.name_;
          onChanged();
        }
        if (fieldsBuilder_ == null) {
          if (!other.fields_.isEmpty()) {
            if (fields_.isEmpty()) {
              fields_ = other.fields_;
              bitField0_ = (bitField0_ & ~0x00000800);
            } else {
              ensureFieldsIsMutable();
              fields_.addAll(other.fields_);
            }
            onChanged();
          }
        } else {
          if (!other.fields_.isEmpty()) {
            if (fieldsBuilder_.isEmpty()) {
              fieldsBuilder_.dispose();
              fieldsBuilder_ = null;
              fields_ = other.fields_;
              bitField0_ = (bitField0_ & ~0x00000800);
              fieldsBuilder_ = 
                com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders ?
                   getFieldsFieldBuilder() : null;
            } else {
              fieldsBuilder_.addAllMessages(other.fields_);
            }
          }
        }
        switch (other.getValueByTypeCase()) {
          case BYTES_VALUE: {
            setBytesValue(other.getBytesValue());
            break;
          }
          case STRING_VALUE: {
            valueByTypeCase_ = 5;
            valueByType_ = other.valueByType_;
            onChanged();
            break;
          }
          case BOOL_VALUE: {
            setBoolValue(other.getBoolValue());
            break;
          }
          case UINT32_VALUE: {
            setUint32Value(other.getUint32Value());
            break;
          }
          case UINT64_VALUE: {
            setUint64Value(other.getUint64Value());
            break;
          }
          case SINT32_VALUE: {
            setSint32Value(other.getSint32Value());
            break;
          }
          case SINT64_VALUE: {
            setSint64Value(other.getSint64Value());
            break;
          }
          case DOUBLE_VALUE: {
            setDoubleValue(other.getDoubleValue());
            break;
          }
          case FLOAT_VALUE: {
            setFloatValue(other.getFloatValue());
            break;
          }
          case VALUEBYTYPE_NOT_SET: {
            break;
          }
        }
        this.mergeUnknownFields(other.unknownFields);
        onChanged();
        return this;
      }

      public final boolean isInitialized() {
        return true;
      }

      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        TelemetryBis.TelemetryField parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (TelemetryBis.TelemetryField) e.getUnfinishedMessage();
          throw e.unwrapIOException();
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }
      private int valueByTypeCase_ = 0;
      private java.lang.Object valueByType_;
      public ValueByTypeCase
          getValueByTypeCase() {
        return ValueByTypeCase.forNumber(
            valueByTypeCase_);
      }

      public Builder clearValueByType() {
        valueByTypeCase_ = 0;
        valueByType_ = null;
        onChanged();
        return this;
      }

      private int bitField0_;

      private long timestamp_ ;
      /**
       * <code>uint64 timestamp = 1;</code>
       */
      public long getTimestamp() {
        return timestamp_;
      }
      /**
       * <code>uint64 timestamp = 1;</code>
       */
      public Builder setTimestamp(long value) {
        
        timestamp_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>uint64 timestamp = 1;</code>
       */
      public Builder clearTimestamp() {
        
        timestamp_ = 0L;
        onChanged();
        return this;
      }

      private java.lang.Object name_ = "";
      /**
       * <code>string name = 2;</code>
       */
      public java.lang.String getName() {
        java.lang.Object ref = name_;
        if (!(ref instanceof java.lang.String)) {
          com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
          java.lang.String s = bs.toStringUtf8();
          name_ = s;
          return s;
        } else {
          return (java.lang.String) ref;
        }
      }
      /**
       * <code>string name = 2;</code>
       */
      public com.google.protobuf.ByteString
          getNameBytes() {
        java.lang.Object ref = name_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (java.lang.String) ref);
          name_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>string name = 2;</code>
       */
      public Builder setName(
          java.lang.String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  
        name_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>string name = 2;</code>
       */
      public Builder clearName() {
        
        name_ = getDefaultInstance().getName();
        onChanged();
        return this;
      }
      /**
       * <code>string name = 2;</code>
       */
      public Builder setNameBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
        
        name_ = value;
        onChanged();
        return this;
      }

      /**
       * <code>bytes bytes_value = 4;</code>
       */
      public com.google.protobuf.ByteString getBytesValue() {
        if (valueByTypeCase_ == 4) {
          return (com.google.protobuf.ByteString) valueByType_;
        }
        return com.google.protobuf.ByteString.EMPTY;
      }
      /**
       * <code>bytes bytes_value = 4;</code>
       */
      public Builder setBytesValue(com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  valueByTypeCase_ = 4;
        valueByType_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>bytes bytes_value = 4;</code>
       */
      public Builder clearBytesValue() {
        if (valueByTypeCase_ == 4) {
          valueByTypeCase_ = 0;
          valueByType_ = null;
          onChanged();
        }
        return this;
      }

      /**
       * <code>string string_value = 5;</code>
       */
      public java.lang.String getStringValue() {
        java.lang.Object ref = "";
        if (valueByTypeCase_ == 5) {
          ref = valueByType_;
        }
        if (!(ref instanceof java.lang.String)) {
          com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
          java.lang.String s = bs.toStringUtf8();
          if (valueByTypeCase_ == 5) {
            valueByType_ = s;
          }
          return s;
        } else {
          return (java.lang.String) ref;
        }
      }
      /**
       * <code>string string_value = 5;</code>
       */
      public com.google.protobuf.ByteString
          getStringValueBytes() {
        java.lang.Object ref = "";
        if (valueByTypeCase_ == 5) {
          ref = valueByType_;
        }
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (java.lang.String) ref);
          if (valueByTypeCase_ == 5) {
            valueByType_ = b;
          }
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>string string_value = 5;</code>
       */
      public Builder setStringValue(
          java.lang.String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  valueByTypeCase_ = 5;
        valueByType_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>string string_value = 5;</code>
       */
      public Builder clearStringValue() {
        if (valueByTypeCase_ == 5) {
          valueByTypeCase_ = 0;
          valueByType_ = null;
          onChanged();
        }
        return this;
      }
      /**
       * <code>string string_value = 5;</code>
       */
      public Builder setStringValueBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
        valueByTypeCase_ = 5;
        valueByType_ = value;
        onChanged();
        return this;
      }

      /**
       * <code>bool bool_value = 6;</code>
       */
      public boolean getBoolValue() {
        if (valueByTypeCase_ == 6) {
          return (java.lang.Boolean) valueByType_;
        }
        return false;
      }
      /**
       * <code>bool bool_value = 6;</code>
       */
      public Builder setBoolValue(boolean value) {
        valueByTypeCase_ = 6;
        valueByType_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>bool bool_value = 6;</code>
       */
      public Builder clearBoolValue() {
        if (valueByTypeCase_ == 6) {
          valueByTypeCase_ = 0;
          valueByType_ = null;
          onChanged();
        }
        return this;
      }

      /**
       * <code>uint32 uint32_value = 7;</code>
       */
      public int getUint32Value() {
        if (valueByTypeCase_ == 7) {
          return (java.lang.Integer) valueByType_;
        }
        return 0;
      }
      /**
       * <code>uint32 uint32_value = 7;</code>
       */
      public Builder setUint32Value(int value) {
        valueByTypeCase_ = 7;
        valueByType_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>uint32 uint32_value = 7;</code>
       */
      public Builder clearUint32Value() {
        if (valueByTypeCase_ == 7) {
          valueByTypeCase_ = 0;
          valueByType_ = null;
          onChanged();
        }
        return this;
      }

      /**
       * <code>uint64 uint64_value = 8;</code>
       */
      public long getUint64Value() {
        if (valueByTypeCase_ == 8) {
          return (java.lang.Long) valueByType_;
        }
        return 0L;
      }
      /**
       * <code>uint64 uint64_value = 8;</code>
       */
      public Builder setUint64Value(long value) {
        valueByTypeCase_ = 8;
        valueByType_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>uint64 uint64_value = 8;</code>
       */
      public Builder clearUint64Value() {
        if (valueByTypeCase_ == 8) {
          valueByTypeCase_ = 0;
          valueByType_ = null;
          onChanged();
        }
        return this;
      }

      /**
       * <code>sint32 sint32_value = 9;</code>
       */
      public int getSint32Value() {
        if (valueByTypeCase_ == 9) {
          return (java.lang.Integer) valueByType_;
        }
        return 0;
      }
      /**
       * <code>sint32 sint32_value = 9;</code>
       */
      public Builder setSint32Value(int value) {
        valueByTypeCase_ = 9;
        valueByType_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>sint32 sint32_value = 9;</code>
       */
      public Builder clearSint32Value() {
        if (valueByTypeCase_ == 9) {
          valueByTypeCase_ = 0;
          valueByType_ = null;
          onChanged();
        }
        return this;
      }

      /**
       * <code>sint64 sint64_value = 10;</code>
       */
      public long getSint64Value() {
        if (valueByTypeCase_ == 10) {
          return (java.lang.Long) valueByType_;
        }
        return 0L;
      }
      /**
       * <code>sint64 sint64_value = 10;</code>
       */
      public Builder setSint64Value(long value) {
        valueByTypeCase_ = 10;
        valueByType_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>sint64 sint64_value = 10;</code>
       */
      public Builder clearSint64Value() {
        if (valueByTypeCase_ == 10) {
          valueByTypeCase_ = 0;
          valueByType_ = null;
          onChanged();
        }
        return this;
      }

      /**
       * <code>double double_value = 11;</code>
       */
      public double getDoubleValue() {
        if (valueByTypeCase_ == 11) {
          return (java.lang.Double) valueByType_;
        }
        return 0D;
      }
      /**
       * <code>double double_value = 11;</code>
       */
      public Builder setDoubleValue(double value) {
        valueByTypeCase_ = 11;
        valueByType_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>double double_value = 11;</code>
       */
      public Builder clearDoubleValue() {
        if (valueByTypeCase_ == 11) {
          valueByTypeCase_ = 0;
          valueByType_ = null;
          onChanged();
        }
        return this;
      }

      /**
       * <code>float float_value = 12;</code>
       */
      public float getFloatValue() {
        if (valueByTypeCase_ == 12) {
          return (java.lang.Float) valueByType_;
        }
        return 0F;
      }
      /**
       * <code>float float_value = 12;</code>
       */
      public Builder setFloatValue(float value) {
        valueByTypeCase_ = 12;
        valueByType_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>float float_value = 12;</code>
       */
      public Builder clearFloatValue() {
        if (valueByTypeCase_ == 12) {
          valueByTypeCase_ = 0;
          valueByType_ = null;
          onChanged();
        }
        return this;
      }

      private java.util.List<TelemetryBis.TelemetryField> fields_ =
        java.util.Collections.emptyList();
      private void ensureFieldsIsMutable() {
        if (!((bitField0_ & 0x00000800) == 0x00000800)) {
          fields_ = new java.util.ArrayList<TelemetryBis.TelemetryField>(fields_);
          bitField0_ |= 0x00000800;
         }
      }

      private com.google.protobuf.RepeatedFieldBuilderV3<
          TelemetryBis.TelemetryField, TelemetryBis.TelemetryField.Builder, TelemetryBis.TelemetryFieldOrBuilder> fieldsBuilder_;

      /**
       * <code>repeated .TelemetryField fields = 15;</code>
       */
      public java.util.List<TelemetryBis.TelemetryField> getFieldsList() {
        if (fieldsBuilder_ == null) {
          return java.util.Collections.unmodifiableList(fields_);
        } else {
          return fieldsBuilder_.getMessageList();
        }
      }
      /**
       * <code>repeated .TelemetryField fields = 15;</code>
       */
      public int getFieldsCount() {
        if (fieldsBuilder_ == null) {
          return fields_.size();
        } else {
          return fieldsBuilder_.getCount();
        }
      }
      /**
       * <code>repeated .TelemetryField fields = 15;</code>
       */
      public TelemetryBis.TelemetryField getFields(int index) {
        if (fieldsBuilder_ == null) {
          return fields_.get(index);
        } else {
          return fieldsBuilder_.getMessage(index);
        }
      }
      /**
       * <code>repeated .TelemetryField fields = 15;</code>
       */
      public Builder setFields(
          int index, TelemetryBis.TelemetryField value) {
        if (fieldsBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          ensureFieldsIsMutable();
          fields_.set(index, value);
          onChanged();
        } else {
          fieldsBuilder_.setMessage(index, value);
        }
        return this;
      }
      /**
       * <code>repeated .TelemetryField fields = 15;</code>
       */
      public Builder setFields(
          int index, TelemetryBis.TelemetryField.Builder builderForValue) {
        if (fieldsBuilder_ == null) {
          ensureFieldsIsMutable();
          fields_.set(index, builderForValue.build());
          onChanged();
        } else {
          fieldsBuilder_.setMessage(index, builderForValue.build());
        }
        return this;
      }
      /**
       * <code>repeated .TelemetryField fields = 15;</code>
       */
      public Builder addFields(TelemetryBis.TelemetryField value) {
        if (fieldsBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          ensureFieldsIsMutable();
          fields_.add(value);
          onChanged();
        } else {
          fieldsBuilder_.addMessage(value);
        }
        return this;
      }
      /**
       * <code>repeated .TelemetryField fields = 15;</code>
       */
      public Builder addFields(
          int index, TelemetryBis.TelemetryField value) {
        if (fieldsBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          ensureFieldsIsMutable();
          fields_.add(index, value);
          onChanged();
        } else {
          fieldsBuilder_.addMessage(index, value);
        }
        return this;
      }
      /**
       * <code>repeated .TelemetryField fields = 15;</code>
       */
      public Builder addFields(
          TelemetryBis.TelemetryField.Builder builderForValue) {
        if (fieldsBuilder_ == null) {
          ensureFieldsIsMutable();
          fields_.add(builderForValue.build());
          onChanged();
        } else {
          fieldsBuilder_.addMessage(builderForValue.build());
        }
        return this;
      }
      /**
       * <code>repeated .TelemetryField fields = 15;</code>
       */
      public Builder addFields(
          int index, TelemetryBis.TelemetryField.Builder builderForValue) {
        if (fieldsBuilder_ == null) {
          ensureFieldsIsMutable();
          fields_.add(index, builderForValue.build());
          onChanged();
        } else {
          fieldsBuilder_.addMessage(index, builderForValue.build());
        }
        return this;
      }
      /**
       * <code>repeated .TelemetryField fields = 15;</code>
       */
      public Builder addAllFields(
          java.lang.Iterable<? extends TelemetryBis.TelemetryField> values) {
        if (fieldsBuilder_ == null) {
          ensureFieldsIsMutable();
          com.google.protobuf.AbstractMessageLite.Builder.addAll(
              values, fields_);
          onChanged();
        } else {
          fieldsBuilder_.addAllMessages(values);
        }
        return this;
      }
      /**
       * <code>repeated .TelemetryField fields = 15;</code>
       */
      public Builder clearFields() {
        if (fieldsBuilder_ == null) {
          fields_ = java.util.Collections.emptyList();
          bitField0_ = (bitField0_ & ~0x00000800);
          onChanged();
        } else {
          fieldsBuilder_.clear();
        }
        return this;
      }
      /**
       * <code>repeated .TelemetryField fields = 15;</code>
       */
      public Builder removeFields(int index) {
        if (fieldsBuilder_ == null) {
          ensureFieldsIsMutable();
          fields_.remove(index);
          onChanged();
        } else {
          fieldsBuilder_.remove(index);
        }
        return this;
      }
      /**
       * <code>repeated .TelemetryField fields = 15;</code>
       */
      public TelemetryBis.TelemetryField.Builder getFieldsBuilder(
          int index) {
        return getFieldsFieldBuilder().getBuilder(index);
      }
      /**
       * <code>repeated .TelemetryField fields = 15;</code>
       */
      public TelemetryBis.TelemetryFieldOrBuilder getFieldsOrBuilder(
          int index) {
        if (fieldsBuilder_ == null) {
          return fields_.get(index);  } else {
          return fieldsBuilder_.getMessageOrBuilder(index);
        }
      }
      /**
       * <code>repeated .TelemetryField fields = 15;</code>
       */
      public java.util.List<? extends TelemetryBis.TelemetryFieldOrBuilder> 
           getFieldsOrBuilderList() {
        if (fieldsBuilder_ != null) {
          return fieldsBuilder_.getMessageOrBuilderList();
        } else {
          return java.util.Collections.unmodifiableList(fields_);
        }
      }
      /**
       * <code>repeated .TelemetryField fields = 15;</code>
       */
      public TelemetryBis.TelemetryField.Builder addFieldsBuilder() {
        return getFieldsFieldBuilder().addBuilder(
            TelemetryBis.TelemetryField.getDefaultInstance());
      }
      /**
       * <code>repeated .TelemetryField fields = 15;</code>
       */
      public TelemetryBis.TelemetryField.Builder addFieldsBuilder(
          int index) {
        return getFieldsFieldBuilder().addBuilder(
            index, TelemetryBis.TelemetryField.getDefaultInstance());
      }
      /**
       * <code>repeated .TelemetryField fields = 15;</code>
       */
      public java.util.List<TelemetryBis.TelemetryField.Builder> 
           getFieldsBuilderList() {
        return getFieldsFieldBuilder().getBuilderList();
      }
      private com.google.protobuf.RepeatedFieldBuilderV3<
          TelemetryBis.TelemetryField, TelemetryBis.TelemetryField.Builder, TelemetryBis.TelemetryFieldOrBuilder> 
          getFieldsFieldBuilder() {
        if (fieldsBuilder_ == null) {
          fieldsBuilder_ = new com.google.protobuf.RepeatedFieldBuilderV3<
              TelemetryBis.TelemetryField, TelemetryBis.TelemetryField.Builder, TelemetryBis.TelemetryFieldOrBuilder>(
                  fields_,
                  ((bitField0_ & 0x00000800) == 0x00000800),
                  getParentForChildren(),
                  isClean());
          fields_ = null;
        }
        return fieldsBuilder_;
      }
      public final Builder setUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.setUnknownFieldsProto3(unknownFields);
      }

      public final Builder mergeUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.mergeUnknownFields(unknownFields);
      }


      // @@protoc_insertion_point(builder_scope:TelemetryField)
    }

    // @@protoc_insertion_point(class_scope:TelemetryField)
    private static final TelemetryBis.TelemetryField DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new TelemetryBis.TelemetryField();
    }

    public static TelemetryBis.TelemetryField getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<TelemetryField>
        PARSER = new com.google.protobuf.AbstractParser<TelemetryField>() {
      public TelemetryField parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
          return new TelemetryField(input, extensionRegistry);
      }
    };

    public static com.google.protobuf.Parser<TelemetryField> parser() {
      return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<TelemetryField> getParserForType() {
      return PARSER;
    }

    public TelemetryBis.TelemetryField getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  public interface TelemetryGPBTableOrBuilder extends
      // @@protoc_insertion_point(interface_extends:TelemetryGPBTable)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <code>repeated .TelemetryRowGPB row = 1;</code>
     */
    java.util.List<TelemetryBis.TelemetryRowGPB> 
        getRowList();
    /**
     * <code>repeated .TelemetryRowGPB row = 1;</code>
     */
    TelemetryBis.TelemetryRowGPB getRow(int index);
    /**
     * <code>repeated .TelemetryRowGPB row = 1;</code>
     */
    int getRowCount();
    /**
     * <code>repeated .TelemetryRowGPB row = 1;</code>
     */
    java.util.List<? extends TelemetryBis.TelemetryRowGPBOrBuilder> 
        getRowOrBuilderList();
    /**
     * <code>repeated .TelemetryRowGPB row = 1;</code>
     */
    TelemetryBis.TelemetryRowGPBOrBuilder getRowOrBuilder(
        int index);
  }
  /**
   * Protobuf type {@code TelemetryGPBTable}
   */
  public  static final class TelemetryGPBTable extends
      com.google.protobuf.GeneratedMessageV3 implements
      // @@protoc_insertion_point(message_implements:TelemetryGPBTable)
      TelemetryGPBTableOrBuilder {
  private static final long serialVersionUID = 0L;
    // Use TelemetryGPBTable.newBuilder() to construct.
    private TelemetryGPBTable(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
      super(builder);
    }
    private TelemetryGPBTable() {
      row_ = java.util.Collections.emptyList();
    }

    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return this.unknownFields;
    }
    private TelemetryGPBTable(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      this();
      int mutable_bitField0_ = 0;
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            default: {
              if (!parseUnknownFieldProto3(
                  input, unknownFields, extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
            case 10: {
              if (!((mutable_bitField0_ & 0x00000001) == 0x00000001)) {
                row_ = new java.util.ArrayList<TelemetryBis.TelemetryRowGPB>();
                mutable_bitField0_ |= 0x00000001;
              }
              row_.add(
                  input.readMessage(TelemetryBis.TelemetryRowGPB.parser(), extensionRegistry));
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
            e).setUnfinishedMessage(this);
      } finally {
        if (((mutable_bitField0_ & 0x00000001) == 0x00000001)) {
          row_ = java.util.Collections.unmodifiableList(row_);
        }
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return TelemetryBis.internal_static_TelemetryGPBTable_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return TelemetryBis.internal_static_TelemetryGPBTable_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              TelemetryBis.TelemetryGPBTable.class, TelemetryBis.TelemetryGPBTable.Builder.class);
    }

    public static final int ROW_FIELD_NUMBER = 1;
    private java.util.List<TelemetryBis.TelemetryRowGPB> row_;
    /**
     * <code>repeated .TelemetryRowGPB row = 1;</code>
     */
    public java.util.List<TelemetryBis.TelemetryRowGPB> getRowList() {
      return row_;
    }
    /**
     * <code>repeated .TelemetryRowGPB row = 1;</code>
     */
    public java.util.List<? extends TelemetryBis.TelemetryRowGPBOrBuilder> 
        getRowOrBuilderList() {
      return row_;
    }
    /**
     * <code>repeated .TelemetryRowGPB row = 1;</code>
     */
    public int getRowCount() {
      return row_.size();
    }
    /**
     * <code>repeated .TelemetryRowGPB row = 1;</code>
     */
    public TelemetryBis.TelemetryRowGPB getRow(int index) {
      return row_.get(index);
    }
    /**
     * <code>repeated .TelemetryRowGPB row = 1;</code>
     */
    public TelemetryBis.TelemetryRowGPBOrBuilder getRowOrBuilder(
        int index) {
      return row_.get(index);
    }

    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      for (int i = 0; i < row_.size(); i++) {
        output.writeMessage(1, row_.get(i));
      }
      unknownFields.writeTo(output);
    }

    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      for (int i = 0; i < row_.size(); i++) {
        size += com.google.protobuf.CodedOutputStream
          .computeMessageSize(1, row_.get(i));
      }
      size += unknownFields.getSerializedSize();
      memoizedSize = size;
      return size;
    }

    @java.lang.Override
    public boolean equals(final java.lang.Object obj) {
      if (obj == this) {
       return true;
      }
      if (!(obj instanceof TelemetryBis.TelemetryGPBTable)) {
        return super.equals(obj);
      }
      TelemetryBis.TelemetryGPBTable other = (TelemetryBis.TelemetryGPBTable) obj;

      boolean result = true;
      result = result && getRowList()
          .equals(other.getRowList());
      result = result && unknownFields.equals(other.unknownFields);
      return result;
    }

    @java.lang.Override
    public int hashCode() {
      if (memoizedHashCode != 0) {
        return memoizedHashCode;
      }
      int hash = 41;
      hash = (19 * hash) + getDescriptor().hashCode();
      if (getRowCount() > 0) {
        hash = (37 * hash) + ROW_FIELD_NUMBER;
        hash = (53 * hash) + getRowList().hashCode();
      }
      hash = (29 * hash) + unknownFields.hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static TelemetryBis.TelemetryGPBTable parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static TelemetryBis.TelemetryGPBTable parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static TelemetryBis.TelemetryGPBTable parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static TelemetryBis.TelemetryGPBTable parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static TelemetryBis.TelemetryGPBTable parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static TelemetryBis.TelemetryGPBTable parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static TelemetryBis.TelemetryGPBTable parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static TelemetryBis.TelemetryGPBTable parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }
    public static TelemetryBis.TelemetryGPBTable parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input);
    }
    public static TelemetryBis.TelemetryGPBTable parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static TelemetryBis.TelemetryGPBTable parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static TelemetryBis.TelemetryGPBTable parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
    }
    public static Builder newBuilder(TelemetryBis.TelemetryGPBTable prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }
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
     * Protobuf type {@code TelemetryGPBTable}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:TelemetryGPBTable)
        TelemetryBis.TelemetryGPBTableOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return TelemetryBis.internal_static_TelemetryGPBTable_descriptor;
      }

      protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return TelemetryBis.internal_static_TelemetryGPBTable_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                TelemetryBis.TelemetryGPBTable.class, TelemetryBis.TelemetryGPBTable.Builder.class);
      }

      // Construct using TelemetryBis.TelemetryGPBTable.newBuilder()
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
          getRowFieldBuilder();
        }
      }
      public Builder clear() {
        super.clear();
        if (rowBuilder_ == null) {
          row_ = java.util.Collections.emptyList();
          bitField0_ = (bitField0_ & ~0x00000001);
        } else {
          rowBuilder_.clear();
        }
        return this;
      }

      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return TelemetryBis.internal_static_TelemetryGPBTable_descriptor;
      }

      public TelemetryBis.TelemetryGPBTable getDefaultInstanceForType() {
        return TelemetryBis.TelemetryGPBTable.getDefaultInstance();
      }

      public TelemetryBis.TelemetryGPBTable build() {
        TelemetryBis.TelemetryGPBTable result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public TelemetryBis.TelemetryGPBTable buildPartial() {
        TelemetryBis.TelemetryGPBTable result = new TelemetryBis.TelemetryGPBTable(this);
        int from_bitField0_ = bitField0_;
        if (rowBuilder_ == null) {
          if (((bitField0_ & 0x00000001) == 0x00000001)) {
            row_ = java.util.Collections.unmodifiableList(row_);
            bitField0_ = (bitField0_ & ~0x00000001);
          }
          result.row_ = row_;
        } else {
          result.row_ = rowBuilder_.build();
        }
        onBuilt();
        return result;
      }

      public Builder clone() {
        return (Builder) super.clone();
      }
      public Builder setField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return (Builder) super.setField(field, value);
      }
      public Builder clearField(
          com.google.protobuf.Descriptors.FieldDescriptor field) {
        return (Builder) super.clearField(field);
      }
      public Builder clearOneof(
          com.google.protobuf.Descriptors.OneofDescriptor oneof) {
        return (Builder) super.clearOneof(oneof);
      }
      public Builder setRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          int index, java.lang.Object value) {
        return (Builder) super.setRepeatedField(field, index, value);
      }
      public Builder addRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return (Builder) super.addRepeatedField(field, value);
      }
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof TelemetryBis.TelemetryGPBTable) {
          return mergeFrom((TelemetryBis.TelemetryGPBTable)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(TelemetryBis.TelemetryGPBTable other) {
        if (other == TelemetryBis.TelemetryGPBTable.getDefaultInstance()) return this;
        if (rowBuilder_ == null) {
          if (!other.row_.isEmpty()) {
            if (row_.isEmpty()) {
              row_ = other.row_;
              bitField0_ = (bitField0_ & ~0x00000001);
            } else {
              ensureRowIsMutable();
              row_.addAll(other.row_);
            }
            onChanged();
          }
        } else {
          if (!other.row_.isEmpty()) {
            if (rowBuilder_.isEmpty()) {
              rowBuilder_.dispose();
              rowBuilder_ = null;
              row_ = other.row_;
              bitField0_ = (bitField0_ & ~0x00000001);
              rowBuilder_ = 
                com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders ?
                   getRowFieldBuilder() : null;
            } else {
              rowBuilder_.addAllMessages(other.row_);
            }
          }
        }
        this.mergeUnknownFields(other.unknownFields);
        onChanged();
        return this;
      }

      public final boolean isInitialized() {
        return true;
      }

      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        TelemetryBis.TelemetryGPBTable parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (TelemetryBis.TelemetryGPBTable) e.getUnfinishedMessage();
          throw e.unwrapIOException();
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }
      private int bitField0_;

      private java.util.List<TelemetryBis.TelemetryRowGPB> row_ =
        java.util.Collections.emptyList();
      private void ensureRowIsMutable() {
        if (!((bitField0_ & 0x00000001) == 0x00000001)) {
          row_ = new java.util.ArrayList<TelemetryBis.TelemetryRowGPB>(row_);
          bitField0_ |= 0x00000001;
         }
      }

      private com.google.protobuf.RepeatedFieldBuilderV3<
          TelemetryBis.TelemetryRowGPB, TelemetryBis.TelemetryRowGPB.Builder, TelemetryBis.TelemetryRowGPBOrBuilder> rowBuilder_;

      /**
       * <code>repeated .TelemetryRowGPB row = 1;</code>
       */
      public java.util.List<TelemetryBis.TelemetryRowGPB> getRowList() {
        if (rowBuilder_ == null) {
          return java.util.Collections.unmodifiableList(row_);
        } else {
          return rowBuilder_.getMessageList();
        }
      }
      /**
       * <code>repeated .TelemetryRowGPB row = 1;</code>
       */
      public int getRowCount() {
        if (rowBuilder_ == null) {
          return row_.size();
        } else {
          return rowBuilder_.getCount();
        }
      }
      /**
       * <code>repeated .TelemetryRowGPB row = 1;</code>
       */
      public TelemetryBis.TelemetryRowGPB getRow(int index) {
        if (rowBuilder_ == null) {
          return row_.get(index);
        } else {
          return rowBuilder_.getMessage(index);
        }
      }
      /**
       * <code>repeated .TelemetryRowGPB row = 1;</code>
       */
      public Builder setRow(
          int index, TelemetryBis.TelemetryRowGPB value) {
        if (rowBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          ensureRowIsMutable();
          row_.set(index, value);
          onChanged();
        } else {
          rowBuilder_.setMessage(index, value);
        }
        return this;
      }
      /**
       * <code>repeated .TelemetryRowGPB row = 1;</code>
       */
      public Builder setRow(
          int index, TelemetryBis.TelemetryRowGPB.Builder builderForValue) {
        if (rowBuilder_ == null) {
          ensureRowIsMutable();
          row_.set(index, builderForValue.build());
          onChanged();
        } else {
          rowBuilder_.setMessage(index, builderForValue.build());
        }
        return this;
      }
      /**
       * <code>repeated .TelemetryRowGPB row = 1;</code>
       */
      public Builder addRow(TelemetryBis.TelemetryRowGPB value) {
        if (rowBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          ensureRowIsMutable();
          row_.add(value);
          onChanged();
        } else {
          rowBuilder_.addMessage(value);
        }
        return this;
      }
      /**
       * <code>repeated .TelemetryRowGPB row = 1;</code>
       */
      public Builder addRow(
          int index, TelemetryBis.TelemetryRowGPB value) {
        if (rowBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          ensureRowIsMutable();
          row_.add(index, value);
          onChanged();
        } else {
          rowBuilder_.addMessage(index, value);
        }
        return this;
      }
      /**
       * <code>repeated .TelemetryRowGPB row = 1;</code>
       */
      public Builder addRow(
          TelemetryBis.TelemetryRowGPB.Builder builderForValue) {
        if (rowBuilder_ == null) {
          ensureRowIsMutable();
          row_.add(builderForValue.build());
          onChanged();
        } else {
          rowBuilder_.addMessage(builderForValue.build());
        }
        return this;
      }
      /**
       * <code>repeated .TelemetryRowGPB row = 1;</code>
       */
      public Builder addRow(
          int index, TelemetryBis.TelemetryRowGPB.Builder builderForValue) {
        if (rowBuilder_ == null) {
          ensureRowIsMutable();
          row_.add(index, builderForValue.build());
          onChanged();
        } else {
          rowBuilder_.addMessage(index, builderForValue.build());
        }
        return this;
      }
      /**
       * <code>repeated .TelemetryRowGPB row = 1;</code>
       */
      public Builder addAllRow(
          java.lang.Iterable<? extends TelemetryBis.TelemetryRowGPB> values) {
        if (rowBuilder_ == null) {
          ensureRowIsMutable();
          com.google.protobuf.AbstractMessageLite.Builder.addAll(
              values, row_);
          onChanged();
        } else {
          rowBuilder_.addAllMessages(values);
        }
        return this;
      }
      /**
       * <code>repeated .TelemetryRowGPB row = 1;</code>
       */
      public Builder clearRow() {
        if (rowBuilder_ == null) {
          row_ = java.util.Collections.emptyList();
          bitField0_ = (bitField0_ & ~0x00000001);
          onChanged();
        } else {
          rowBuilder_.clear();
        }
        return this;
      }
      /**
       * <code>repeated .TelemetryRowGPB row = 1;</code>
       */
      public Builder removeRow(int index) {
        if (rowBuilder_ == null) {
          ensureRowIsMutable();
          row_.remove(index);
          onChanged();
        } else {
          rowBuilder_.remove(index);
        }
        return this;
      }
      /**
       * <code>repeated .TelemetryRowGPB row = 1;</code>
       */
      public TelemetryBis.TelemetryRowGPB.Builder getRowBuilder(
          int index) {
        return getRowFieldBuilder().getBuilder(index);
      }
      /**
       * <code>repeated .TelemetryRowGPB row = 1;</code>
       */
      public TelemetryBis.TelemetryRowGPBOrBuilder getRowOrBuilder(
          int index) {
        if (rowBuilder_ == null) {
          return row_.get(index);  } else {
          return rowBuilder_.getMessageOrBuilder(index);
        }
      }
      /**
       * <code>repeated .TelemetryRowGPB row = 1;</code>
       */
      public java.util.List<? extends TelemetryBis.TelemetryRowGPBOrBuilder> 
           getRowOrBuilderList() {
        if (rowBuilder_ != null) {
          return rowBuilder_.getMessageOrBuilderList();
        } else {
          return java.util.Collections.unmodifiableList(row_);
        }
      }
      /**
       * <code>repeated .TelemetryRowGPB row = 1;</code>
       */
      public TelemetryBis.TelemetryRowGPB.Builder addRowBuilder() {
        return getRowFieldBuilder().addBuilder(
            TelemetryBis.TelemetryRowGPB.getDefaultInstance());
      }
      /**
       * <code>repeated .TelemetryRowGPB row = 1;</code>
       */
      public TelemetryBis.TelemetryRowGPB.Builder addRowBuilder(
          int index) {
        return getRowFieldBuilder().addBuilder(
            index, TelemetryBis.TelemetryRowGPB.getDefaultInstance());
      }
      /**
       * <code>repeated .TelemetryRowGPB row = 1;</code>
       */
      public java.util.List<TelemetryBis.TelemetryRowGPB.Builder> 
           getRowBuilderList() {
        return getRowFieldBuilder().getBuilderList();
      }
      private com.google.protobuf.RepeatedFieldBuilderV3<
          TelemetryBis.TelemetryRowGPB, TelemetryBis.TelemetryRowGPB.Builder, TelemetryBis.TelemetryRowGPBOrBuilder> 
          getRowFieldBuilder() {
        if (rowBuilder_ == null) {
          rowBuilder_ = new com.google.protobuf.RepeatedFieldBuilderV3<
              TelemetryBis.TelemetryRowGPB, TelemetryBis.TelemetryRowGPB.Builder, TelemetryBis.TelemetryRowGPBOrBuilder>(
                  row_,
                  ((bitField0_ & 0x00000001) == 0x00000001),
                  getParentForChildren(),
                  isClean());
          row_ = null;
        }
        return rowBuilder_;
      }
      public final Builder setUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.setUnknownFieldsProto3(unknownFields);
      }

      public final Builder mergeUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.mergeUnknownFields(unknownFields);
      }


      // @@protoc_insertion_point(builder_scope:TelemetryGPBTable)
    }

    // @@protoc_insertion_point(class_scope:TelemetryGPBTable)
    private static final TelemetryBis.TelemetryGPBTable DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new TelemetryBis.TelemetryGPBTable();
    }

    public static TelemetryBis.TelemetryGPBTable getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<TelemetryGPBTable>
        PARSER = new com.google.protobuf.AbstractParser<TelemetryGPBTable>() {
      public TelemetryGPBTable parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
          return new TelemetryGPBTable(input, extensionRegistry);
      }
    };

    public static com.google.protobuf.Parser<TelemetryGPBTable> parser() {
      return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<TelemetryGPBTable> getParserForType() {
      return PARSER;
    }

    public TelemetryBis.TelemetryGPBTable getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  public interface TelemetryRowGPBOrBuilder extends
      // @@protoc_insertion_point(interface_extends:TelemetryRowGPB)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <code>uint64 timestamp = 1;</code>
     */
    long getTimestamp();

    /**
     * <code>bytes keys = 10;</code>
     */
    com.google.protobuf.ByteString getKeys();

    /**
     * <code>bytes content = 11;</code>
     */
    com.google.protobuf.ByteString getContent();
  }
  /**
   * Protobuf type {@code TelemetryRowGPB}
   */
  public  static final class TelemetryRowGPB extends
      com.google.protobuf.GeneratedMessageV3 implements
      // @@protoc_insertion_point(message_implements:TelemetryRowGPB)
      TelemetryRowGPBOrBuilder {
  private static final long serialVersionUID = 0L;
    // Use TelemetryRowGPB.newBuilder() to construct.
    private TelemetryRowGPB(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
      super(builder);
    }
    private TelemetryRowGPB() {
      timestamp_ = 0L;
      keys_ = com.google.protobuf.ByteString.EMPTY;
      content_ = com.google.protobuf.ByteString.EMPTY;
    }

    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return this.unknownFields;
    }
    private TelemetryRowGPB(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      this();
      int mutable_bitField0_ = 0;
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            default: {
              if (!parseUnknownFieldProto3(
                  input, unknownFields, extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
            case 8: {

              timestamp_ = input.readUInt64();
              break;
            }
            case 82: {

              keys_ = input.readBytes();
              break;
            }
            case 90: {

              content_ = input.readBytes();
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
            e).setUnfinishedMessage(this);
      } finally {
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return TelemetryBis.internal_static_TelemetryRowGPB_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return TelemetryBis.internal_static_TelemetryRowGPB_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              TelemetryBis.TelemetryRowGPB.class, TelemetryBis.TelemetryRowGPB.Builder.class);
    }

    public static final int TIMESTAMP_FIELD_NUMBER = 1;
    private long timestamp_;
    /**
     * <code>uint64 timestamp = 1;</code>
     */
    public long getTimestamp() {
      return timestamp_;
    }

    public static final int KEYS_FIELD_NUMBER = 10;
    private com.google.protobuf.ByteString keys_;
    /**
     * <code>bytes keys = 10;</code>
     */
    public com.google.protobuf.ByteString getKeys() {
      return keys_;
    }

    public static final int CONTENT_FIELD_NUMBER = 11;
    private com.google.protobuf.ByteString content_;
    /**
     * <code>bytes content = 11;</code>
     */
    public com.google.protobuf.ByteString getContent() {
      return content_;
    }

    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      if (timestamp_ != 0L) {
        output.writeUInt64(1, timestamp_);
      }
      if (!keys_.isEmpty()) {
        output.writeBytes(10, keys_);
      }
      if (!content_.isEmpty()) {
        output.writeBytes(11, content_);
      }
      unknownFields.writeTo(output);
    }

    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      if (timestamp_ != 0L) {
        size += com.google.protobuf.CodedOutputStream
          .computeUInt64Size(1, timestamp_);
      }
      if (!keys_.isEmpty()) {
        size += com.google.protobuf.CodedOutputStream
          .computeBytesSize(10, keys_);
      }
      if (!content_.isEmpty()) {
        size += com.google.protobuf.CodedOutputStream
          .computeBytesSize(11, content_);
      }
      size += unknownFields.getSerializedSize();
      memoizedSize = size;
      return size;
    }

    @java.lang.Override
    public boolean equals(final java.lang.Object obj) {
      if (obj == this) {
       return true;
      }
      if (!(obj instanceof TelemetryBis.TelemetryRowGPB)) {
        return super.equals(obj);
      }
      TelemetryBis.TelemetryRowGPB other = (TelemetryBis.TelemetryRowGPB) obj;

      boolean result = true;
      result = result && (getTimestamp()
          == other.getTimestamp());
      result = result && getKeys()
          .equals(other.getKeys());
      result = result && getContent()
          .equals(other.getContent());
      result = result && unknownFields.equals(other.unknownFields);
      return result;
    }

    @java.lang.Override
    public int hashCode() {
      if (memoizedHashCode != 0) {
        return memoizedHashCode;
      }
      int hash = 41;
      hash = (19 * hash) + getDescriptor().hashCode();
      hash = (37 * hash) + TIMESTAMP_FIELD_NUMBER;
      hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
          getTimestamp());
      hash = (37 * hash) + KEYS_FIELD_NUMBER;
      hash = (53 * hash) + getKeys().hashCode();
      hash = (37 * hash) + CONTENT_FIELD_NUMBER;
      hash = (53 * hash) + getContent().hashCode();
      hash = (29 * hash) + unknownFields.hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static TelemetryBis.TelemetryRowGPB parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static TelemetryBis.TelemetryRowGPB parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static TelemetryBis.TelemetryRowGPB parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static TelemetryBis.TelemetryRowGPB parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static TelemetryBis.TelemetryRowGPB parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static TelemetryBis.TelemetryRowGPB parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static TelemetryBis.TelemetryRowGPB parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static TelemetryBis.TelemetryRowGPB parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }
    public static TelemetryBis.TelemetryRowGPB parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input);
    }
    public static TelemetryBis.TelemetryRowGPB parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static TelemetryBis.TelemetryRowGPB parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static TelemetryBis.TelemetryRowGPB parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
    }
    public static Builder newBuilder(TelemetryBis.TelemetryRowGPB prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }
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
     * Protobuf type {@code TelemetryRowGPB}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:TelemetryRowGPB)
        TelemetryBis.TelemetryRowGPBOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return TelemetryBis.internal_static_TelemetryRowGPB_descriptor;
      }

      protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return TelemetryBis.internal_static_TelemetryRowGPB_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                TelemetryBis.TelemetryRowGPB.class, TelemetryBis.TelemetryRowGPB.Builder.class);
      }

      // Construct using TelemetryBis.TelemetryRowGPB.newBuilder()
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
        }
      }
      public Builder clear() {
        super.clear();
        timestamp_ = 0L;

        keys_ = com.google.protobuf.ByteString.EMPTY;

        content_ = com.google.protobuf.ByteString.EMPTY;

        return this;
      }

      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return TelemetryBis.internal_static_TelemetryRowGPB_descriptor;
      }

      public TelemetryBis.TelemetryRowGPB getDefaultInstanceForType() {
        return TelemetryBis.TelemetryRowGPB.getDefaultInstance();
      }

      public TelemetryBis.TelemetryRowGPB build() {
        TelemetryBis.TelemetryRowGPB result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public TelemetryBis.TelemetryRowGPB buildPartial() {
        TelemetryBis.TelemetryRowGPB result = new TelemetryBis.TelemetryRowGPB(this);
        result.timestamp_ = timestamp_;
        result.keys_ = keys_;
        result.content_ = content_;
        onBuilt();
        return result;
      }

      public Builder clone() {
        return (Builder) super.clone();
      }
      public Builder setField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return (Builder) super.setField(field, value);
      }
      public Builder clearField(
          com.google.protobuf.Descriptors.FieldDescriptor field) {
        return (Builder) super.clearField(field);
      }
      public Builder clearOneof(
          com.google.protobuf.Descriptors.OneofDescriptor oneof) {
        return (Builder) super.clearOneof(oneof);
      }
      public Builder setRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          int index, java.lang.Object value) {
        return (Builder) super.setRepeatedField(field, index, value);
      }
      public Builder addRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return (Builder) super.addRepeatedField(field, value);
      }
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof TelemetryBis.TelemetryRowGPB) {
          return mergeFrom((TelemetryBis.TelemetryRowGPB)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(TelemetryBis.TelemetryRowGPB other) {
        if (other == TelemetryBis.TelemetryRowGPB.getDefaultInstance()) return this;
        if (other.getTimestamp() != 0L) {
          setTimestamp(other.getTimestamp());
        }
        if (other.getKeys() != com.google.protobuf.ByteString.EMPTY) {
          setKeys(other.getKeys());
        }
        if (other.getContent() != com.google.protobuf.ByteString.EMPTY) {
          setContent(other.getContent());
        }
        this.mergeUnknownFields(other.unknownFields);
        onChanged();
        return this;
      }

      public final boolean isInitialized() {
        return true;
      }

      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        TelemetryBis.TelemetryRowGPB parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (TelemetryBis.TelemetryRowGPB) e.getUnfinishedMessage();
          throw e.unwrapIOException();
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }

      private long timestamp_ ;
      /**
       * <code>uint64 timestamp = 1;</code>
       */
      public long getTimestamp() {
        return timestamp_;
      }
      /**
       * <code>uint64 timestamp = 1;</code>
       */
      public Builder setTimestamp(long value) {
        
        timestamp_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>uint64 timestamp = 1;</code>
       */
      public Builder clearTimestamp() {
        
        timestamp_ = 0L;
        onChanged();
        return this;
      }

      private com.google.protobuf.ByteString keys_ = com.google.protobuf.ByteString.EMPTY;
      /**
       * <code>bytes keys = 10;</code>
       */
      public com.google.protobuf.ByteString getKeys() {
        return keys_;
      }
      /**
       * <code>bytes keys = 10;</code>
       */
      public Builder setKeys(com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  
        keys_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>bytes keys = 10;</code>
       */
      public Builder clearKeys() {
        
        keys_ = getDefaultInstance().getKeys();
        onChanged();
        return this;
      }

      private com.google.protobuf.ByteString content_ = com.google.protobuf.ByteString.EMPTY;
      /**
       * <code>bytes content = 11;</code>
       */
      public com.google.protobuf.ByteString getContent() {
        return content_;
      }
      /**
       * <code>bytes content = 11;</code>
       */
      public Builder setContent(com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  
        content_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>bytes content = 11;</code>
       */
      public Builder clearContent() {
        
        content_ = getDefaultInstance().getContent();
        onChanged();
        return this;
      }
      public final Builder setUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.setUnknownFieldsProto3(unknownFields);
      }

      public final Builder mergeUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.mergeUnknownFields(unknownFields);
      }


      // @@protoc_insertion_point(builder_scope:TelemetryRowGPB)
    }

    // @@protoc_insertion_point(class_scope:TelemetryRowGPB)
    private static final TelemetryBis.TelemetryRowGPB DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new TelemetryBis.TelemetryRowGPB();
    }

    public static TelemetryBis.TelemetryRowGPB getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<TelemetryRowGPB>
        PARSER = new com.google.protobuf.AbstractParser<TelemetryRowGPB>() {
      public TelemetryRowGPB parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
          return new TelemetryRowGPB(input, extensionRegistry);
      }
    };

    public static com.google.protobuf.Parser<TelemetryRowGPB> parser() {
      return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<TelemetryRowGPB> getParserForType() {
      return PARSER;
    }

    public TelemetryBis.TelemetryRowGPB getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_Telemetry_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_Telemetry_fieldAccessorTable;
  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_TelemetryField_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_TelemetryField_fieldAccessorTable;
  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_TelemetryGPBTable_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_TelemetryGPBTable_fieldAccessorTable;
  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_TelemetryRowGPB_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_TelemetryRowGPB_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\023telemetry_bis.proto\"\250\002\n\tTelemetry\022\025\n\013n" +
      "ode_id_str\030\001 \001(\tH\000\022\035\n\023subscription_id_st" +
      "r\030\003 \001(\tH\001\022\025\n\rencoding_path\030\006 \001(\t\022\025\n\rcoll" +
      "ection_id\030\010 \001(\004\022\035\n\025collection_start_time" +
      "\030\t \001(\004\022\025\n\rmsg_timestamp\030\n \001(\004\022#\n\ndata_gp" +
      "bkv\030\013 \003(\0132\017.TelemetryField\022$\n\010data_gpb\030\014" +
      " \001(\0132\022.TelemetryGPBTable\022\033\n\023collection_e" +
      "nd_time\030\r \001(\004B\t\n\007node_idB\016\n\014subscription" +
      "\"\267\002\n\016TelemetryField\022\021\n\ttimestamp\030\001 \001(\004\022\014" +
      "\n\004name\030\002 \001(\t\022\025\n\013bytes_value\030\004 \001(\014H\000\022\026\n\014s",
      "tring_value\030\005 \001(\tH\000\022\024\n\nbool_value\030\006 \001(\010H" +
      "\000\022\026\n\014uint32_value\030\007 \001(\rH\000\022\026\n\014uint64_valu" +
      "e\030\010 \001(\004H\000\022\026\n\014sint32_value\030\t \001(\021H\000\022\026\n\014sin" +
      "t64_value\030\n \001(\022H\000\022\026\n\014double_value\030\013 \001(\001H" +
      "\000\022\025\n\013float_value\030\014 \001(\002H\000\022\037\n\006fields\030\017 \003(\013" +
      "2\017.TelemetryFieldB\017\n\rvalue_by_type\"2\n\021Te" +
      "lemetryGPBTable\022\035\n\003row\030\001 \003(\0132\020.Telemetry" +
      "RowGPB\"C\n\017TelemetryRowGPB\022\021\n\ttimestamp\030\001" +
      " \001(\004\022\014\n\004keys\030\n \001(\014\022\017\n\007content\030\013 \001(\014B\017Z\rt" +
      "elemetry_bisb\006proto3"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
        new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
          public com.google.protobuf.ExtensionRegistry assignDescriptors(
              com.google.protobuf.Descriptors.FileDescriptor root) {
            descriptor = root;
            return null;
          }
        };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
    internal_static_Telemetry_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_Telemetry_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_Telemetry_descriptor,
        new java.lang.String[] { "NodeIdStr", "SubscriptionIdStr", "EncodingPath", "CollectionId", "CollectionStartTime", "MsgTimestamp", "DataGpbkv", "DataGpb", "CollectionEndTime", "NodeId", "Subscription", });
    internal_static_TelemetryField_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_TelemetryField_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_TelemetryField_descriptor,
        new java.lang.String[] { "Timestamp", "Name", "BytesValue", "StringValue", "BoolValue", "Uint32Value", "Uint64Value", "Sint32Value", "Sint64Value", "DoubleValue", "FloatValue", "Fields", "ValueByType", });
    internal_static_TelemetryGPBTable_descriptor =
      getDescriptor().getMessageTypes().get(2);
    internal_static_TelemetryGPBTable_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_TelemetryGPBTable_descriptor,
        new java.lang.String[] { "Row", });
    internal_static_TelemetryRowGPB_descriptor =
      getDescriptor().getMessageTypes().get(3);
    internal_static_TelemetryRowGPB_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_TelemetryRowGPB_descriptor,
        new java.lang.String[] { "Timestamp", "Keys", "Content", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}
