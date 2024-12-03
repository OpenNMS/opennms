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
package org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto;

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
     * @return Whether the nodeIdStr field is set.
     */
    boolean hasNodeIdStr();
    /**
     * <pre>
     *  bytes node_id_uuid = 2;              // not produced
     * </pre>
     *
     * <code>string node_id_str = 1;</code>
     * @return The nodeIdStr.
     */
    java.lang.String getNodeIdStr();
    /**
     * <pre>
     *  bytes node_id_uuid = 2;              // not produced
     * </pre>
     *
     * <code>string node_id_str = 1;</code>
     * @return The bytes for nodeIdStr.
     */
    com.google.protobuf.ByteString
        getNodeIdStrBytes();

    /**
     * <pre>
     *  uint32   subscription_id = 4;        // not produced
     * </pre>
     *
     * <code>string subscription_id_str = 3;</code>
     * @return Whether the subscriptionIdStr field is set.
     */
    boolean hasSubscriptionIdStr();
    /**
     * <pre>
     *  uint32   subscription_id = 4;        // not produced
     * </pre>
     *
     * <code>string subscription_id_str = 3;</code>
     * @return The subscriptionIdStr.
     */
    java.lang.String getSubscriptionIdStr();
    /**
     * <pre>
     *  uint32   subscription_id = 4;        // not produced
     * </pre>
     *
     * <code>string subscription_id_str = 3;</code>
     * @return The bytes for subscriptionIdStr.
     */
    com.google.protobuf.ByteString
        getSubscriptionIdStrBytes();

    /**
     * <pre>
     * string   sensor_path = 5;               // not produced
     * </pre>
     *
     * <code>string encoding_path = 6;</code>
     * @return The encodingPath.
     */
    java.lang.String getEncodingPath();
    /**
     * <pre>
     * string   sensor_path = 5;               // not produced
     * </pre>
     *
     * <code>string encoding_path = 6;</code>
     * @return The bytes for encodingPath.
     */
    com.google.protobuf.ByteString
        getEncodingPathBytes();

    /**
     * <pre>
     * string   model_version = 7;             // not produced
     * </pre>
     *
     * <code>uint64 collection_id = 8;</code>
     * @return The collectionId.
     */
    long getCollectionId();

    /**
     * <code>uint64 collection_start_time = 9;</code>
     * @return The collectionStartTime.
     */
    long getCollectionStartTime();

    /**
     * <code>uint64 msg_timestamp = 10;</code>
     * @return The msgTimestamp.
     */
    long getMsgTimestamp();

    /**
     * <code>repeated .TelemetryField data_gpbkv = 11;</code>
     */
    java.util.List<org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField> 
        getDataGpbkvList();
    /**
     * <code>repeated .TelemetryField data_gpbkv = 11;</code>
     */
    org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField getDataGpbkv(int index);
    /**
     * <code>repeated .TelemetryField data_gpbkv = 11;</code>
     */
    int getDataGpbkvCount();
    /**
     * <code>repeated .TelemetryField data_gpbkv = 11;</code>
     */
    java.util.List<? extends org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryFieldOrBuilder> 
        getDataGpbkvOrBuilderList();
    /**
     * <code>repeated .TelemetryField data_gpbkv = 11;</code>
     */
    org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryFieldOrBuilder getDataGpbkvOrBuilder(
        int index);

    /**
     * <code>.TelemetryGPBTable data_gpb = 12;</code>
     * @return Whether the dataGpb field is set.
     */
    boolean hasDataGpb();
    /**
     * <code>.TelemetryGPBTable data_gpb = 12;</code>
     * @return The dataGpb.
     */
    org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable getDataGpb();
    /**
     * <code>.TelemetryGPBTable data_gpb = 12;</code>
     */
    org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTableOrBuilder getDataGpbOrBuilder();

    /**
     * <pre>
     * uint64   heartbeat_sequence_number = 14; // not produced
     * </pre>
     *
     * <code>uint64 collection_end_time = 13;</code>
     * @return The collectionEndTime.
     */
    long getCollectionEndTime();

    org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.Telemetry.NodeIdCase getNodeIdCase();

    org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.Telemetry.SubscriptionCase getSubscriptionCase();
  }
  /**
   * Protobuf type {@code Telemetry}
   */
  public static final class Telemetry extends
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
      dataGpbkv_ = java.util.Collections.emptyList();
    }

    @java.lang.Override
    @SuppressWarnings({"unused"})
    protected java.lang.Object newInstance(
        UnusedPrivateParameter unused) {
      return new Telemetry();
    }

    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.internal_static_Telemetry_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.internal_static_Telemetry_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.Telemetry.class, org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.Telemetry.Builder.class);
    }

    private int bitField0_;
    private int nodeIdCase_ = 0;
    @SuppressWarnings("serial")
    private java.lang.Object nodeId_;
    public enum NodeIdCase
        implements com.google.protobuf.Internal.EnumLite,
            com.google.protobuf.AbstractMessage.InternalOneOfEnum {
      NODE_ID_STR(1),
      NODEID_NOT_SET(0);
      private final int value;
      private NodeIdCase(int value) {
        this.value = value;
      }
      /**
       * @param value The number of the enum to look for.
       * @return The enum associated with the given number.
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
    @SuppressWarnings("serial")
    private java.lang.Object subscription_;
    public enum SubscriptionCase
        implements com.google.protobuf.Internal.EnumLite,
            com.google.protobuf.AbstractMessage.InternalOneOfEnum {
      SUBSCRIPTION_ID_STR(3),
      SUBSCRIPTION_NOT_SET(0);
      private final int value;
      private SubscriptionCase(int value) {
        this.value = value;
      }
      /**
       * @param value The number of the enum to look for.
       * @return The enum associated with the given number.
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
     * @return Whether the nodeIdStr field is set.
     */
    public boolean hasNodeIdStr() {
      return nodeIdCase_ == 1;
    }
    /**
     * <pre>
     *  bytes node_id_uuid = 2;              // not produced
     * </pre>
     *
     * <code>string node_id_str = 1;</code>
     * @return The nodeIdStr.
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
     * @return The bytes for nodeIdStr.
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
     * @return Whether the subscriptionIdStr field is set.
     */
    public boolean hasSubscriptionIdStr() {
      return subscriptionCase_ == 3;
    }
    /**
     * <pre>
     *  uint32   subscription_id = 4;        // not produced
     * </pre>
     *
     * <code>string subscription_id_str = 3;</code>
     * @return The subscriptionIdStr.
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
     * @return The bytes for subscriptionIdStr.
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
    @SuppressWarnings("serial")
    private volatile java.lang.Object encodingPath_ = "";
    /**
     * <pre>
     * string   sensor_path = 5;               // not produced
     * </pre>
     *
     * <code>string encoding_path = 6;</code>
     * @return The encodingPath.
     */
    @java.lang.Override
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
     * @return The bytes for encodingPath.
     */
    @java.lang.Override
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
    private long collectionId_ = 0L;
    /**
     * <pre>
     * string   model_version = 7;             // not produced
     * </pre>
     *
     * <code>uint64 collection_id = 8;</code>
     * @return The collectionId.
     */
    @java.lang.Override
    public long getCollectionId() {
      return collectionId_;
    }

    public static final int COLLECTION_START_TIME_FIELD_NUMBER = 9;
    private long collectionStartTime_ = 0L;
    /**
     * <code>uint64 collection_start_time = 9;</code>
     * @return The collectionStartTime.
     */
    @java.lang.Override
    public long getCollectionStartTime() {
      return collectionStartTime_;
    }

    public static final int MSG_TIMESTAMP_FIELD_NUMBER = 10;
    private long msgTimestamp_ = 0L;
    /**
     * <code>uint64 msg_timestamp = 10;</code>
     * @return The msgTimestamp.
     */
    @java.lang.Override
    public long getMsgTimestamp() {
      return msgTimestamp_;
    }

    public static final int DATA_GPBKV_FIELD_NUMBER = 11;
    @SuppressWarnings("serial")
    private java.util.List<org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField> dataGpbkv_;
    /**
     * <code>repeated .TelemetryField data_gpbkv = 11;</code>
     */
    @java.lang.Override
    public java.util.List<org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField> getDataGpbkvList() {
      return dataGpbkv_;
    }
    /**
     * <code>repeated .TelemetryField data_gpbkv = 11;</code>
     */
    @java.lang.Override
    public java.util.List<? extends org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryFieldOrBuilder> 
        getDataGpbkvOrBuilderList() {
      return dataGpbkv_;
    }
    /**
     * <code>repeated .TelemetryField data_gpbkv = 11;</code>
     */
    @java.lang.Override
    public int getDataGpbkvCount() {
      return dataGpbkv_.size();
    }
    /**
     * <code>repeated .TelemetryField data_gpbkv = 11;</code>
     */
    @java.lang.Override
    public org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField getDataGpbkv(int index) {
      return dataGpbkv_.get(index);
    }
    /**
     * <code>repeated .TelemetryField data_gpbkv = 11;</code>
     */
    @java.lang.Override
    public org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryFieldOrBuilder getDataGpbkvOrBuilder(
        int index) {
      return dataGpbkv_.get(index);
    }

    public static final int DATA_GPB_FIELD_NUMBER = 12;
    private org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable dataGpb_;
    /**
     * <code>.TelemetryGPBTable data_gpb = 12;</code>
     * @return Whether the dataGpb field is set.
     */
    @java.lang.Override
    public boolean hasDataGpb() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <code>.TelemetryGPBTable data_gpb = 12;</code>
     * @return The dataGpb.
     */
    @java.lang.Override
    public org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable getDataGpb() {
      return dataGpb_ == null ? org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable.getDefaultInstance() : dataGpb_;
    }
    /**
     * <code>.TelemetryGPBTable data_gpb = 12;</code>
     */
    @java.lang.Override
    public org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTableOrBuilder getDataGpbOrBuilder() {
      return dataGpb_ == null ? org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable.getDefaultInstance() : dataGpb_;
    }

    public static final int COLLECTION_END_TIME_FIELD_NUMBER = 13;
    private long collectionEndTime_ = 0L;
    /**
     * <pre>
     * uint64   heartbeat_sequence_number = 14; // not produced
     * </pre>
     *
     * <code>uint64 collection_end_time = 13;</code>
     * @return The collectionEndTime.
     */
    @java.lang.Override
    public long getCollectionEndTime() {
      return collectionEndTime_;
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
      if (nodeIdCase_ == 1) {
        com.google.protobuf.GeneratedMessageV3.writeString(output, 1, nodeId_);
      }
      if (subscriptionCase_ == 3) {
        com.google.protobuf.GeneratedMessageV3.writeString(output, 3, subscription_);
      }
      if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(encodingPath_)) {
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
      if (((bitField0_ & 0x00000001) != 0)) {
        output.writeMessage(12, getDataGpb());
      }
      if (collectionEndTime_ != 0L) {
        output.writeUInt64(13, collectionEndTime_);
      }
      getUnknownFields().writeTo(output);
    }

    @java.lang.Override
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
      if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(encodingPath_)) {
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
      if (((bitField0_ & 0x00000001) != 0)) {
        size += com.google.protobuf.CodedOutputStream
          .computeMessageSize(12, getDataGpb());
      }
      if (collectionEndTime_ != 0L) {
        size += com.google.protobuf.CodedOutputStream
          .computeUInt64Size(13, collectionEndTime_);
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
      if (!(obj instanceof org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.Telemetry)) {
        return super.equals(obj);
      }
      org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.Telemetry other = (org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.Telemetry) obj;

      if (!getEncodingPath()
          .equals(other.getEncodingPath())) return false;
      if (getCollectionId()
          != other.getCollectionId()) return false;
      if (getCollectionStartTime()
          != other.getCollectionStartTime()) return false;
      if (getMsgTimestamp()
          != other.getMsgTimestamp()) return false;
      if (!getDataGpbkvList()
          .equals(other.getDataGpbkvList())) return false;
      if (hasDataGpb() != other.hasDataGpb()) return false;
      if (hasDataGpb()) {
        if (!getDataGpb()
            .equals(other.getDataGpb())) return false;
      }
      if (getCollectionEndTime()
          != other.getCollectionEndTime()) return false;
      if (!getNodeIdCase().equals(other.getNodeIdCase())) return false;
      switch (nodeIdCase_) {
        case 1:
          if (!getNodeIdStr()
              .equals(other.getNodeIdStr())) return false;
          break;
        case 0:
        default:
      }
      if (!getSubscriptionCase().equals(other.getSubscriptionCase())) return false;
      switch (subscriptionCase_) {
        case 3:
          if (!getSubscriptionIdStr()
              .equals(other.getSubscriptionIdStr())) return false;
          break;
        case 0:
        default:
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
      hash = (29 * hash) + getUnknownFields().hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.Telemetry parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.Telemetry parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.Telemetry parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.Telemetry parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.Telemetry parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.Telemetry parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.Telemetry parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.Telemetry parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.Telemetry parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input);
    }

    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.Telemetry parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.Telemetry parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.Telemetry parseFrom(
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
    public static Builder newBuilder(org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.Telemetry prototype) {
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
     * Protobuf type {@code Telemetry}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:Telemetry)
        org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.internal_static_Telemetry_descriptor;
      }

      @java.lang.Override
      protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.internal_static_Telemetry_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.Telemetry.class, org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.Telemetry.Builder.class);
      }

      // Construct using org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.Telemetry.newBuilder()
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
          getDataGpbFieldBuilder();
        }
      }
      @java.lang.Override
      public Builder clear() {
        super.clear();
        bitField0_ = 0;
        encodingPath_ = "";
        collectionId_ = 0L;
        collectionStartTime_ = 0L;
        msgTimestamp_ = 0L;
        if (dataGpbkvBuilder_ == null) {
          dataGpbkv_ = java.util.Collections.emptyList();
        } else {
          dataGpbkv_ = null;
          dataGpbkvBuilder_.clear();
        }
        bitField0_ = (bitField0_ & ~0x00000040);
        dataGpb_ = null;
        if (dataGpbBuilder_ != null) {
          dataGpbBuilder_.dispose();
          dataGpbBuilder_ = null;
        }
        collectionEndTime_ = 0L;
        nodeIdCase_ = 0;
        nodeId_ = null;
        subscriptionCase_ = 0;
        subscription_ = null;
        return this;
      }

      @java.lang.Override
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.internal_static_Telemetry_descriptor;
      }

      @java.lang.Override
      public org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.Telemetry getDefaultInstanceForType() {
        return org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.Telemetry.getDefaultInstance();
      }

      @java.lang.Override
      public org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.Telemetry build() {
        org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.Telemetry result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      @java.lang.Override
      public org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.Telemetry buildPartial() {
        org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.Telemetry result = new org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.Telemetry(this);
        buildPartialRepeatedFields(result);
        if (bitField0_ != 0) { buildPartial0(result); }
        buildPartialOneofs(result);
        onBuilt();
        return result;
      }

      private void buildPartialRepeatedFields(org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.Telemetry result) {
        if (dataGpbkvBuilder_ == null) {
          if (((bitField0_ & 0x00000040) != 0)) {
            dataGpbkv_ = java.util.Collections.unmodifiableList(dataGpbkv_);
            bitField0_ = (bitField0_ & ~0x00000040);
          }
          result.dataGpbkv_ = dataGpbkv_;
        } else {
          result.dataGpbkv_ = dataGpbkvBuilder_.build();
        }
      }

      private void buildPartial0(org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.Telemetry result) {
        int from_bitField0_ = bitField0_;
        if (((from_bitField0_ & 0x00000004) != 0)) {
          result.encodingPath_ = encodingPath_;
        }
        if (((from_bitField0_ & 0x00000008) != 0)) {
          result.collectionId_ = collectionId_;
        }
        if (((from_bitField0_ & 0x00000010) != 0)) {
          result.collectionStartTime_ = collectionStartTime_;
        }
        if (((from_bitField0_ & 0x00000020) != 0)) {
          result.msgTimestamp_ = msgTimestamp_;
        }
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000080) != 0)) {
          result.dataGpb_ = dataGpbBuilder_ == null
              ? dataGpb_
              : dataGpbBuilder_.build();
          to_bitField0_ |= 0x00000001;
        }
        if (((from_bitField0_ & 0x00000100) != 0)) {
          result.collectionEndTime_ = collectionEndTime_;
        }
        result.bitField0_ |= to_bitField0_;
      }

      private void buildPartialOneofs(org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.Telemetry result) {
        result.nodeIdCase_ = nodeIdCase_;
        result.nodeId_ = this.nodeId_;
        result.subscriptionCase_ = subscriptionCase_;
        result.subscription_ = this.subscription_;
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
        if (other instanceof org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.Telemetry) {
          return mergeFrom((org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.Telemetry)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.Telemetry other) {
        if (other == org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.Telemetry.getDefaultInstance()) return this;
        if (!other.getEncodingPath().isEmpty()) {
          encodingPath_ = other.encodingPath_;
          bitField0_ |= 0x00000004;
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
                nodeIdCase_ = 1;
                nodeId_ = s;
                break;
              } // case 10
              case 26: {
                java.lang.String s = input.readStringRequireUtf8();
                subscriptionCase_ = 3;
                subscription_ = s;
                break;
              } // case 26
              case 50: {
                encodingPath_ = input.readStringRequireUtf8();
                bitField0_ |= 0x00000004;
                break;
              } // case 50
              case 64: {
                collectionId_ = input.readUInt64();
                bitField0_ |= 0x00000008;
                break;
              } // case 64
              case 72: {
                collectionStartTime_ = input.readUInt64();
                bitField0_ |= 0x00000010;
                break;
              } // case 72
              case 80: {
                msgTimestamp_ = input.readUInt64();
                bitField0_ |= 0x00000020;
                break;
              } // case 80
              case 90: {
                org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField m =
                    input.readMessage(
                        org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField.parser(),
                        extensionRegistry);
                if (dataGpbkvBuilder_ == null) {
                  ensureDataGpbkvIsMutable();
                  dataGpbkv_.add(m);
                } else {
                  dataGpbkvBuilder_.addMessage(m);
                }
                break;
              } // case 90
              case 98: {
                input.readMessage(
                    getDataGpbFieldBuilder().getBuilder(),
                    extensionRegistry);
                bitField0_ |= 0x00000080;
                break;
              } // case 98
              case 104: {
                collectionEndTime_ = input.readUInt64();
                bitField0_ |= 0x00000100;
                break;
              } // case 104
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
       * @return Whether the nodeIdStr field is set.
       */
      @java.lang.Override
      public boolean hasNodeIdStr() {
        return nodeIdCase_ == 1;
      }
      /**
       * <pre>
       *  bytes node_id_uuid = 2;              // not produced
       * </pre>
       *
       * <code>string node_id_str = 1;</code>
       * @return The nodeIdStr.
       */
      @java.lang.Override
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
       * @return The bytes for nodeIdStr.
       */
      @java.lang.Override
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
       * @param value The nodeIdStr to set.
       * @return This builder for chaining.
       */
      public Builder setNodeIdStr(
          java.lang.String value) {
        if (value == null) { throw new NullPointerException(); }
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
       * @return This builder for chaining.
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
       * @param value The bytes for nodeIdStr to set.
       * @return This builder for chaining.
       */
      public Builder setNodeIdStrBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) { throw new NullPointerException(); }
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
       * @return Whether the subscriptionIdStr field is set.
       */
      @java.lang.Override
      public boolean hasSubscriptionIdStr() {
        return subscriptionCase_ == 3;
      }
      /**
       * <pre>
       *  uint32   subscription_id = 4;        // not produced
       * </pre>
       *
       * <code>string subscription_id_str = 3;</code>
       * @return The subscriptionIdStr.
       */
      @java.lang.Override
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
       * @return The bytes for subscriptionIdStr.
       */
      @java.lang.Override
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
       * @param value The subscriptionIdStr to set.
       * @return This builder for chaining.
       */
      public Builder setSubscriptionIdStr(
          java.lang.String value) {
        if (value == null) { throw new NullPointerException(); }
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
       * @return This builder for chaining.
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
       * @param value The bytes for subscriptionIdStr to set.
       * @return This builder for chaining.
       */
      public Builder setSubscriptionIdStrBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) { throw new NullPointerException(); }
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
       * @return The encodingPath.
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
       * @return The bytes for encodingPath.
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
       * @param value The encodingPath to set.
       * @return This builder for chaining.
       */
      public Builder setEncodingPath(
          java.lang.String value) {
        if (value == null) { throw new NullPointerException(); }
        encodingPath_ = value;
        bitField0_ |= 0x00000004;
        onChanged();
        return this;
      }
      /**
       * <pre>
       * string   sensor_path = 5;               // not produced
       * </pre>
       *
       * <code>string encoding_path = 6;</code>
       * @return This builder for chaining.
       */
      public Builder clearEncodingPath() {
        encodingPath_ = getDefaultInstance().getEncodingPath();
        bitField0_ = (bitField0_ & ~0x00000004);
        onChanged();
        return this;
      }
      /**
       * <pre>
       * string   sensor_path = 5;               // not produced
       * </pre>
       *
       * <code>string encoding_path = 6;</code>
       * @param value The bytes for encodingPath to set.
       * @return This builder for chaining.
       */
      public Builder setEncodingPathBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) { throw new NullPointerException(); }
        checkByteStringIsUtf8(value);
        encodingPath_ = value;
        bitField0_ |= 0x00000004;
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
       * @return The collectionId.
       */
      @java.lang.Override
      public long getCollectionId() {
        return collectionId_;
      }
      /**
       * <pre>
       * string   model_version = 7;             // not produced
       * </pre>
       *
       * <code>uint64 collection_id = 8;</code>
       * @param value The collectionId to set.
       * @return This builder for chaining.
       */
      public Builder setCollectionId(long value) {

        collectionId_ = value;
        bitField0_ |= 0x00000008;
        onChanged();
        return this;
      }
      /**
       * <pre>
       * string   model_version = 7;             // not produced
       * </pre>
       *
       * <code>uint64 collection_id = 8;</code>
       * @return This builder for chaining.
       */
      public Builder clearCollectionId() {
        bitField0_ = (bitField0_ & ~0x00000008);
        collectionId_ = 0L;
        onChanged();
        return this;
      }

      private long collectionStartTime_ ;
      /**
       * <code>uint64 collection_start_time = 9;</code>
       * @return The collectionStartTime.
       */
      @java.lang.Override
      public long getCollectionStartTime() {
        return collectionStartTime_;
      }
      /**
       * <code>uint64 collection_start_time = 9;</code>
       * @param value The collectionStartTime to set.
       * @return This builder for chaining.
       */
      public Builder setCollectionStartTime(long value) {

        collectionStartTime_ = value;
        bitField0_ |= 0x00000010;
        onChanged();
        return this;
      }
      /**
       * <code>uint64 collection_start_time = 9;</code>
       * @return This builder for chaining.
       */
      public Builder clearCollectionStartTime() {
        bitField0_ = (bitField0_ & ~0x00000010);
        collectionStartTime_ = 0L;
        onChanged();
        return this;
      }

      private long msgTimestamp_ ;
      /**
       * <code>uint64 msg_timestamp = 10;</code>
       * @return The msgTimestamp.
       */
      @java.lang.Override
      public long getMsgTimestamp() {
        return msgTimestamp_;
      }
      /**
       * <code>uint64 msg_timestamp = 10;</code>
       * @param value The msgTimestamp to set.
       * @return This builder for chaining.
       */
      public Builder setMsgTimestamp(long value) {

        msgTimestamp_ = value;
        bitField0_ |= 0x00000020;
        onChanged();
        return this;
      }
      /**
       * <code>uint64 msg_timestamp = 10;</code>
       * @return This builder for chaining.
       */
      public Builder clearMsgTimestamp() {
        bitField0_ = (bitField0_ & ~0x00000020);
        msgTimestamp_ = 0L;
        onChanged();
        return this;
      }

      private java.util.List<org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField> dataGpbkv_ =
        java.util.Collections.emptyList();
      private void ensureDataGpbkvIsMutable() {
        if (!((bitField0_ & 0x00000040) != 0)) {
          dataGpbkv_ = new java.util.ArrayList<org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField>(dataGpbkv_);
          bitField0_ |= 0x00000040;
         }
      }

      private com.google.protobuf.RepeatedFieldBuilderV3<
          org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField, org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField.Builder, org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryFieldOrBuilder> dataGpbkvBuilder_;

      /**
       * <code>repeated .TelemetryField data_gpbkv = 11;</code>
       */
      public java.util.List<org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField> getDataGpbkvList() {
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
      public org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField getDataGpbkv(int index) {
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
          int index, org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField value) {
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
          int index, org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField.Builder builderForValue) {
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
      public Builder addDataGpbkv(org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField value) {
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
          int index, org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField value) {
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
          org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField.Builder builderForValue) {
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
          int index, org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField.Builder builderForValue) {
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
          java.lang.Iterable<? extends org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField> values) {
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
      public org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField.Builder getDataGpbkvBuilder(
          int index) {
        return getDataGpbkvFieldBuilder().getBuilder(index);
      }
      /**
       * <code>repeated .TelemetryField data_gpbkv = 11;</code>
       */
      public org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryFieldOrBuilder getDataGpbkvOrBuilder(
          int index) {
        if (dataGpbkvBuilder_ == null) {
          return dataGpbkv_.get(index);  } else {
          return dataGpbkvBuilder_.getMessageOrBuilder(index);
        }
      }
      /**
       * <code>repeated .TelemetryField data_gpbkv = 11;</code>
       */
      public java.util.List<? extends org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryFieldOrBuilder> 
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
      public org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField.Builder addDataGpbkvBuilder() {
        return getDataGpbkvFieldBuilder().addBuilder(
            org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField.getDefaultInstance());
      }
      /**
       * <code>repeated .TelemetryField data_gpbkv = 11;</code>
       */
      public org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField.Builder addDataGpbkvBuilder(
          int index) {
        return getDataGpbkvFieldBuilder().addBuilder(
            index, org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField.getDefaultInstance());
      }
      /**
       * <code>repeated .TelemetryField data_gpbkv = 11;</code>
       */
      public java.util.List<org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField.Builder> 
           getDataGpbkvBuilderList() {
        return getDataGpbkvFieldBuilder().getBuilderList();
      }
      private com.google.protobuf.RepeatedFieldBuilderV3<
          org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField, org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField.Builder, org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryFieldOrBuilder> 
          getDataGpbkvFieldBuilder() {
        if (dataGpbkvBuilder_ == null) {
          dataGpbkvBuilder_ = new com.google.protobuf.RepeatedFieldBuilderV3<
              org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField, org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField.Builder, org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryFieldOrBuilder>(
                  dataGpbkv_,
                  ((bitField0_ & 0x00000040) != 0),
                  getParentForChildren(),
                  isClean());
          dataGpbkv_ = null;
        }
        return dataGpbkvBuilder_;
      }

      private org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable dataGpb_;
      private com.google.protobuf.SingleFieldBuilderV3<
          org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable, org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable.Builder, org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTableOrBuilder> dataGpbBuilder_;
      /**
       * <code>.TelemetryGPBTable data_gpb = 12;</code>
       * @return Whether the dataGpb field is set.
       */
      public boolean hasDataGpb() {
        return ((bitField0_ & 0x00000080) != 0);
      }
      /**
       * <code>.TelemetryGPBTable data_gpb = 12;</code>
       * @return The dataGpb.
       */
      public org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable getDataGpb() {
        if (dataGpbBuilder_ == null) {
          return dataGpb_ == null ? org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable.getDefaultInstance() : dataGpb_;
        } else {
          return dataGpbBuilder_.getMessage();
        }
      }
      /**
       * <code>.TelemetryGPBTable data_gpb = 12;</code>
       */
      public Builder setDataGpb(org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable value) {
        if (dataGpbBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          dataGpb_ = value;
        } else {
          dataGpbBuilder_.setMessage(value);
        }
        bitField0_ |= 0x00000080;
        onChanged();
        return this;
      }
      /**
       * <code>.TelemetryGPBTable data_gpb = 12;</code>
       */
      public Builder setDataGpb(
          org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable.Builder builderForValue) {
        if (dataGpbBuilder_ == null) {
          dataGpb_ = builderForValue.build();
        } else {
          dataGpbBuilder_.setMessage(builderForValue.build());
        }
        bitField0_ |= 0x00000080;
        onChanged();
        return this;
      }
      /**
       * <code>.TelemetryGPBTable data_gpb = 12;</code>
       */
      public Builder mergeDataGpb(org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable value) {
        if (dataGpbBuilder_ == null) {
          if (((bitField0_ & 0x00000080) != 0) &&
            dataGpb_ != null &&
            dataGpb_ != org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable.getDefaultInstance()) {
            getDataGpbBuilder().mergeFrom(value);
          } else {
            dataGpb_ = value;
          }
        } else {
          dataGpbBuilder_.mergeFrom(value);
        }
        if (dataGpb_ != null) {
          bitField0_ |= 0x00000080;
          onChanged();
        }
        return this;
      }
      /**
       * <code>.TelemetryGPBTable data_gpb = 12;</code>
       */
      public Builder clearDataGpb() {
        bitField0_ = (bitField0_ & ~0x00000080);
        dataGpb_ = null;
        if (dataGpbBuilder_ != null) {
          dataGpbBuilder_.dispose();
          dataGpbBuilder_ = null;
        }
        onChanged();
        return this;
      }
      /**
       * <code>.TelemetryGPBTable data_gpb = 12;</code>
       */
      public org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable.Builder getDataGpbBuilder() {
        bitField0_ |= 0x00000080;
        onChanged();
        return getDataGpbFieldBuilder().getBuilder();
      }
      /**
       * <code>.TelemetryGPBTable data_gpb = 12;</code>
       */
      public org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTableOrBuilder getDataGpbOrBuilder() {
        if (dataGpbBuilder_ != null) {
          return dataGpbBuilder_.getMessageOrBuilder();
        } else {
          return dataGpb_ == null ?
              org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable.getDefaultInstance() : dataGpb_;
        }
      }
      /**
       * <code>.TelemetryGPBTable data_gpb = 12;</code>
       */
      private com.google.protobuf.SingleFieldBuilderV3<
          org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable, org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable.Builder, org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTableOrBuilder> 
          getDataGpbFieldBuilder() {
        if (dataGpbBuilder_ == null) {
          dataGpbBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
              org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable, org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable.Builder, org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTableOrBuilder>(
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
       * @return The collectionEndTime.
       */
      @java.lang.Override
      public long getCollectionEndTime() {
        return collectionEndTime_;
      }
      /**
       * <pre>
       * uint64   heartbeat_sequence_number = 14; // not produced
       * </pre>
       *
       * <code>uint64 collection_end_time = 13;</code>
       * @param value The collectionEndTime to set.
       * @return This builder for chaining.
       */
      public Builder setCollectionEndTime(long value) {

        collectionEndTime_ = value;
        bitField0_ |= 0x00000100;
        onChanged();
        return this;
      }
      /**
       * <pre>
       * uint64   heartbeat_sequence_number = 14; // not produced
       * </pre>
       *
       * <code>uint64 collection_end_time = 13;</code>
       * @return This builder for chaining.
       */
      public Builder clearCollectionEndTime() {
        bitField0_ = (bitField0_ & ~0x00000100);
        collectionEndTime_ = 0L;
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


      // @@protoc_insertion_point(builder_scope:Telemetry)
    }

    // @@protoc_insertion_point(class_scope:Telemetry)
    private static final org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.Telemetry DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.Telemetry();
    }

    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.Telemetry getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<Telemetry>
        PARSER = new com.google.protobuf.AbstractParser<Telemetry>() {
      @java.lang.Override
      public Telemetry parsePartialFrom(
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

    public static com.google.protobuf.Parser<Telemetry> parser() {
      return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<Telemetry> getParserForType() {
      return PARSER;
    }

    @java.lang.Override
    public org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.Telemetry getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  public interface TelemetryFieldOrBuilder extends
      // @@protoc_insertion_point(interface_extends:TelemetryField)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <code>uint64 timestamp = 1;</code>
     * @return The timestamp.
     */
    long getTimestamp();

    /**
     * <code>string name = 2;</code>
     * @return The name.
     */
    java.lang.String getName();
    /**
     * <code>string name = 2;</code>
     * @return The bytes for name.
     */
    com.google.protobuf.ByteString
        getNameBytes();

    /**
     * <code>bytes bytes_value = 4;</code>
     * @return Whether the bytesValue field is set.
     */
    boolean hasBytesValue();
    /**
     * <code>bytes bytes_value = 4;</code>
     * @return The bytesValue.
     */
    com.google.protobuf.ByteString getBytesValue();

    /**
     * <code>string string_value = 5;</code>
     * @return Whether the stringValue field is set.
     */
    boolean hasStringValue();
    /**
     * <code>string string_value = 5;</code>
     * @return The stringValue.
     */
    java.lang.String getStringValue();
    /**
     * <code>string string_value = 5;</code>
     * @return The bytes for stringValue.
     */
    com.google.protobuf.ByteString
        getStringValueBytes();

    /**
     * <code>bool bool_value = 6;</code>
     * @return Whether the boolValue field is set.
     */
    boolean hasBoolValue();
    /**
     * <code>bool bool_value = 6;</code>
     * @return The boolValue.
     */
    boolean getBoolValue();

    /**
     * <code>uint32 uint32_value = 7;</code>
     * @return Whether the uint32Value field is set.
     */
    boolean hasUint32Value();
    /**
     * <code>uint32 uint32_value = 7;</code>
     * @return The uint32Value.
     */
    int getUint32Value();

    /**
     * <code>uint64 uint64_value = 8;</code>
     * @return Whether the uint64Value field is set.
     */
    boolean hasUint64Value();
    /**
     * <code>uint64 uint64_value = 8;</code>
     * @return The uint64Value.
     */
    long getUint64Value();

    /**
     * <code>sint32 sint32_value = 9;</code>
     * @return Whether the sint32Value field is set.
     */
    boolean hasSint32Value();
    /**
     * <code>sint32 sint32_value = 9;</code>
     * @return The sint32Value.
     */
    int getSint32Value();

    /**
     * <code>sint64 sint64_value = 10;</code>
     * @return Whether the sint64Value field is set.
     */
    boolean hasSint64Value();
    /**
     * <code>sint64 sint64_value = 10;</code>
     * @return The sint64Value.
     */
    long getSint64Value();

    /**
     * <code>double double_value = 11;</code>
     * @return Whether the doubleValue field is set.
     */
    boolean hasDoubleValue();
    /**
     * <code>double double_value = 11;</code>
     * @return The doubleValue.
     */
    double getDoubleValue();

    /**
     * <code>float float_value = 12;</code>
     * @return Whether the floatValue field is set.
     */
    boolean hasFloatValue();
    /**
     * <code>float float_value = 12;</code>
     * @return The floatValue.
     */
    float getFloatValue();

    /**
     * <code>repeated .TelemetryField fields = 15;</code>
     */
    java.util.List<org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField> 
        getFieldsList();
    /**
     * <code>repeated .TelemetryField fields = 15;</code>
     */
    org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField getFields(int index);
    /**
     * <code>repeated .TelemetryField fields = 15;</code>
     */
    int getFieldsCount();
    /**
     * <code>repeated .TelemetryField fields = 15;</code>
     */
    java.util.List<? extends org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryFieldOrBuilder> 
        getFieldsOrBuilderList();
    /**
     * <code>repeated .TelemetryField fields = 15;</code>
     */
    org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryFieldOrBuilder getFieldsOrBuilder(
        int index);

    org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField.ValueByTypeCase getValueByTypeCase();
  }
  /**
   * Protobuf type {@code TelemetryField}
   */
  public static final class TelemetryField extends
      com.google.protobuf.GeneratedMessageV3 implements
      // @@protoc_insertion_point(message_implements:TelemetryField)
      TelemetryFieldOrBuilder {
  private static final long serialVersionUID = 0L;
    // Use TelemetryField.newBuilder() to construct.
    private TelemetryField(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
      super(builder);
    }
    private TelemetryField() {
      name_ = "";
      fields_ = java.util.Collections.emptyList();
    }

    @java.lang.Override
    @SuppressWarnings({"unused"})
    protected java.lang.Object newInstance(
        UnusedPrivateParameter unused) {
      return new TelemetryField();
    }

    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.internal_static_TelemetryField_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.internal_static_TelemetryField_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField.class, org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField.Builder.class);
    }

    private int valueByTypeCase_ = 0;
    @SuppressWarnings("serial")
    private java.lang.Object valueByType_;
    public enum ValueByTypeCase
        implements com.google.protobuf.Internal.EnumLite,
            com.google.protobuf.AbstractMessage.InternalOneOfEnum {
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
       * @param value The number of the enum to look for.
       * @return The enum associated with the given number.
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
    private long timestamp_ = 0L;
    /**
     * <code>uint64 timestamp = 1;</code>
     * @return The timestamp.
     */
    @java.lang.Override
    public long getTimestamp() {
      return timestamp_;
    }

    public static final int NAME_FIELD_NUMBER = 2;
    @SuppressWarnings("serial")
    private volatile java.lang.Object name_ = "";
    /**
     * <code>string name = 2;</code>
     * @return The name.
     */
    @java.lang.Override
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
     * @return The bytes for name.
     */
    @java.lang.Override
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
     * @return Whether the bytesValue field is set.
     */
    @java.lang.Override
    public boolean hasBytesValue() {
      return valueByTypeCase_ == 4;
    }
    /**
     * <code>bytes bytes_value = 4;</code>
     * @return The bytesValue.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString getBytesValue() {
      if (valueByTypeCase_ == 4) {
        return (com.google.protobuf.ByteString) valueByType_;
      }
      return com.google.protobuf.ByteString.EMPTY;
    }

    public static final int STRING_VALUE_FIELD_NUMBER = 5;
    /**
     * <code>string string_value = 5;</code>
     * @return Whether the stringValue field is set.
     */
    public boolean hasStringValue() {
      return valueByTypeCase_ == 5;
    }
    /**
     * <code>string string_value = 5;</code>
     * @return The stringValue.
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
     * @return The bytes for stringValue.
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
     * @return Whether the boolValue field is set.
     */
    @java.lang.Override
    public boolean hasBoolValue() {
      return valueByTypeCase_ == 6;
    }
    /**
     * <code>bool bool_value = 6;</code>
     * @return The boolValue.
     */
    @java.lang.Override
    public boolean getBoolValue() {
      if (valueByTypeCase_ == 6) {
        return (java.lang.Boolean) valueByType_;
      }
      return false;
    }

    public static final int UINT32_VALUE_FIELD_NUMBER = 7;
    /**
     * <code>uint32 uint32_value = 7;</code>
     * @return Whether the uint32Value field is set.
     */
    @java.lang.Override
    public boolean hasUint32Value() {
      return valueByTypeCase_ == 7;
    }
    /**
     * <code>uint32 uint32_value = 7;</code>
     * @return The uint32Value.
     */
    @java.lang.Override
    public int getUint32Value() {
      if (valueByTypeCase_ == 7) {
        return (java.lang.Integer) valueByType_;
      }
      return 0;
    }

    public static final int UINT64_VALUE_FIELD_NUMBER = 8;
    /**
     * <code>uint64 uint64_value = 8;</code>
     * @return Whether the uint64Value field is set.
     */
    @java.lang.Override
    public boolean hasUint64Value() {
      return valueByTypeCase_ == 8;
    }
    /**
     * <code>uint64 uint64_value = 8;</code>
     * @return The uint64Value.
     */
    @java.lang.Override
    public long getUint64Value() {
      if (valueByTypeCase_ == 8) {
        return (java.lang.Long) valueByType_;
      }
      return 0L;
    }

    public static final int SINT32_VALUE_FIELD_NUMBER = 9;
    /**
     * <code>sint32 sint32_value = 9;</code>
     * @return Whether the sint32Value field is set.
     */
    @java.lang.Override
    public boolean hasSint32Value() {
      return valueByTypeCase_ == 9;
    }
    /**
     * <code>sint32 sint32_value = 9;</code>
     * @return The sint32Value.
     */
    @java.lang.Override
    public int getSint32Value() {
      if (valueByTypeCase_ == 9) {
        return (java.lang.Integer) valueByType_;
      }
      return 0;
    }

    public static final int SINT64_VALUE_FIELD_NUMBER = 10;
    /**
     * <code>sint64 sint64_value = 10;</code>
     * @return Whether the sint64Value field is set.
     */
    @java.lang.Override
    public boolean hasSint64Value() {
      return valueByTypeCase_ == 10;
    }
    /**
     * <code>sint64 sint64_value = 10;</code>
     * @return The sint64Value.
     */
    @java.lang.Override
    public long getSint64Value() {
      if (valueByTypeCase_ == 10) {
        return (java.lang.Long) valueByType_;
      }
      return 0L;
    }

    public static final int DOUBLE_VALUE_FIELD_NUMBER = 11;
    /**
     * <code>double double_value = 11;</code>
     * @return Whether the doubleValue field is set.
     */
    @java.lang.Override
    public boolean hasDoubleValue() {
      return valueByTypeCase_ == 11;
    }
    /**
     * <code>double double_value = 11;</code>
     * @return The doubleValue.
     */
    @java.lang.Override
    public double getDoubleValue() {
      if (valueByTypeCase_ == 11) {
        return (java.lang.Double) valueByType_;
      }
      return 0D;
    }

    public static final int FLOAT_VALUE_FIELD_NUMBER = 12;
    /**
     * <code>float float_value = 12;</code>
     * @return Whether the floatValue field is set.
     */
    @java.lang.Override
    public boolean hasFloatValue() {
      return valueByTypeCase_ == 12;
    }
    /**
     * <code>float float_value = 12;</code>
     * @return The floatValue.
     */
    @java.lang.Override
    public float getFloatValue() {
      if (valueByTypeCase_ == 12) {
        return (java.lang.Float) valueByType_;
      }
      return 0F;
    }

    public static final int FIELDS_FIELD_NUMBER = 15;
    @SuppressWarnings("serial")
    private java.util.List<org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField> fields_;
    /**
     * <code>repeated .TelemetryField fields = 15;</code>
     */
    @java.lang.Override
    public java.util.List<org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField> getFieldsList() {
      return fields_;
    }
    /**
     * <code>repeated .TelemetryField fields = 15;</code>
     */
    @java.lang.Override
    public java.util.List<? extends org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryFieldOrBuilder> 
        getFieldsOrBuilderList() {
      return fields_;
    }
    /**
     * <code>repeated .TelemetryField fields = 15;</code>
     */
    @java.lang.Override
    public int getFieldsCount() {
      return fields_.size();
    }
    /**
     * <code>repeated .TelemetryField fields = 15;</code>
     */
    @java.lang.Override
    public org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField getFields(int index) {
      return fields_.get(index);
    }
    /**
     * <code>repeated .TelemetryField fields = 15;</code>
     */
    @java.lang.Override
    public org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryFieldOrBuilder getFieldsOrBuilder(
        int index) {
      return fields_.get(index);
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
      if (timestamp_ != 0L) {
        output.writeUInt64(1, timestamp_);
      }
      if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(name_)) {
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
      getUnknownFields().writeTo(output);
    }

    @java.lang.Override
    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      if (timestamp_ != 0L) {
        size += com.google.protobuf.CodedOutputStream
          .computeUInt64Size(1, timestamp_);
      }
      if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(name_)) {
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
      size += getUnknownFields().getSerializedSize();
      memoizedSize = size;
      return size;
    }

    @java.lang.Override
    public boolean equals(final java.lang.Object obj) {
      if (obj == this) {
       return true;
      }
      if (!(obj instanceof org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField)) {
        return super.equals(obj);
      }
      org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField other = (org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField) obj;

      if (getTimestamp()
          != other.getTimestamp()) return false;
      if (!getName()
          .equals(other.getName())) return false;
      if (!getFieldsList()
          .equals(other.getFieldsList())) return false;
      if (!getValueByTypeCase().equals(other.getValueByTypeCase())) return false;
      switch (valueByTypeCase_) {
        case 4:
          if (!getBytesValue()
              .equals(other.getBytesValue())) return false;
          break;
        case 5:
          if (!getStringValue()
              .equals(other.getStringValue())) return false;
          break;
        case 6:
          if (getBoolValue()
              != other.getBoolValue()) return false;
          break;
        case 7:
          if (getUint32Value()
              != other.getUint32Value()) return false;
          break;
        case 8:
          if (getUint64Value()
              != other.getUint64Value()) return false;
          break;
        case 9:
          if (getSint32Value()
              != other.getSint32Value()) return false;
          break;
        case 10:
          if (getSint64Value()
              != other.getSint64Value()) return false;
          break;
        case 11:
          if (java.lang.Double.doubleToLongBits(getDoubleValue())
              != java.lang.Double.doubleToLongBits(
                  other.getDoubleValue())) return false;
          break;
        case 12:
          if (java.lang.Float.floatToIntBits(getFloatValue())
              != java.lang.Float.floatToIntBits(
                  other.getFloatValue())) return false;
          break;
        case 0:
        default:
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
      hash = (29 * hash) + getUnknownFields().hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input);
    }

    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField parseFrom(
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
    public static Builder newBuilder(org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField prototype) {
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
     * Protobuf type {@code TelemetryField}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:TelemetryField)
        org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryFieldOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.internal_static_TelemetryField_descriptor;
      }

      @java.lang.Override
      protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.internal_static_TelemetryField_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField.class, org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField.Builder.class);
      }

      // Construct using org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField.newBuilder()
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
        timestamp_ = 0L;
        name_ = "";
        if (fieldsBuilder_ == null) {
          fields_ = java.util.Collections.emptyList();
        } else {
          fields_ = null;
          fieldsBuilder_.clear();
        }
        bitField0_ = (bitField0_ & ~0x00000800);
        valueByTypeCase_ = 0;
        valueByType_ = null;
        return this;
      }

      @java.lang.Override
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.internal_static_TelemetryField_descriptor;
      }

      @java.lang.Override
      public org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField getDefaultInstanceForType() {
        return org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField.getDefaultInstance();
      }

      @java.lang.Override
      public org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField build() {
        org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      @java.lang.Override
      public org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField buildPartial() {
        org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField result = new org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField(this);
        buildPartialRepeatedFields(result);
        if (bitField0_ != 0) { buildPartial0(result); }
        buildPartialOneofs(result);
        onBuilt();
        return result;
      }

      private void buildPartialRepeatedFields(org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField result) {
        if (fieldsBuilder_ == null) {
          if (((bitField0_ & 0x00000800) != 0)) {
            fields_ = java.util.Collections.unmodifiableList(fields_);
            bitField0_ = (bitField0_ & ~0x00000800);
          }
          result.fields_ = fields_;
        } else {
          result.fields_ = fieldsBuilder_.build();
        }
      }

      private void buildPartial0(org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField result) {
        int from_bitField0_ = bitField0_;
        if (((from_bitField0_ & 0x00000001) != 0)) {
          result.timestamp_ = timestamp_;
        }
        if (((from_bitField0_ & 0x00000002) != 0)) {
          result.name_ = name_;
        }
      }

      private void buildPartialOneofs(org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField result) {
        result.valueByTypeCase_ = valueByTypeCase_;
        result.valueByType_ = this.valueByType_;
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
        if (other instanceof org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField) {
          return mergeFrom((org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField other) {
        if (other == org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField.getDefaultInstance()) return this;
        if (other.getTimestamp() != 0L) {
          setTimestamp(other.getTimestamp());
        }
        if (!other.getName().isEmpty()) {
          name_ = other.name_;
          bitField0_ |= 0x00000002;
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
                timestamp_ = input.readUInt64();
                bitField0_ |= 0x00000001;
                break;
              } // case 8
              case 18: {
                name_ = input.readStringRequireUtf8();
                bitField0_ |= 0x00000002;
                break;
              } // case 18
              case 34: {
                valueByType_ = input.readBytes();
                valueByTypeCase_ = 4;
                break;
              } // case 34
              case 42: {
                java.lang.String s = input.readStringRequireUtf8();
                valueByTypeCase_ = 5;
                valueByType_ = s;
                break;
              } // case 42
              case 48: {
                valueByType_ = input.readBool();
                valueByTypeCase_ = 6;
                break;
              } // case 48
              case 56: {
                valueByType_ = input.readUInt32();
                valueByTypeCase_ = 7;
                break;
              } // case 56
              case 64: {
                valueByType_ = input.readUInt64();
                valueByTypeCase_ = 8;
                break;
              } // case 64
              case 72: {
                valueByType_ = input.readSInt32();
                valueByTypeCase_ = 9;
                break;
              } // case 72
              case 80: {
                valueByType_ = input.readSInt64();
                valueByTypeCase_ = 10;
                break;
              } // case 80
              case 89: {
                valueByType_ = input.readDouble();
                valueByTypeCase_ = 11;
                break;
              } // case 89
              case 101: {
                valueByType_ = input.readFloat();
                valueByTypeCase_ = 12;
                break;
              } // case 101
              case 122: {
                org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField m =
                    input.readMessage(
                        org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField.parser(),
                        extensionRegistry);
                if (fieldsBuilder_ == null) {
                  ensureFieldsIsMutable();
                  fields_.add(m);
                } else {
                  fieldsBuilder_.addMessage(m);
                }
                break;
              } // case 122
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
       * @return The timestamp.
       */
      @java.lang.Override
      public long getTimestamp() {
        return timestamp_;
      }
      /**
       * <code>uint64 timestamp = 1;</code>
       * @param value The timestamp to set.
       * @return This builder for chaining.
       */
      public Builder setTimestamp(long value) {

        timestamp_ = value;
        bitField0_ |= 0x00000001;
        onChanged();
        return this;
      }
      /**
       * <code>uint64 timestamp = 1;</code>
       * @return This builder for chaining.
       */
      public Builder clearTimestamp() {
        bitField0_ = (bitField0_ & ~0x00000001);
        timestamp_ = 0L;
        onChanged();
        return this;
      }

      private java.lang.Object name_ = "";
      /**
       * <code>string name = 2;</code>
       * @return The name.
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
       * @return The bytes for name.
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
       * @param value The name to set.
       * @return This builder for chaining.
       */
      public Builder setName(
          java.lang.String value) {
        if (value == null) { throw new NullPointerException(); }
        name_ = value;
        bitField0_ |= 0x00000002;
        onChanged();
        return this;
      }
      /**
       * <code>string name = 2;</code>
       * @return This builder for chaining.
       */
      public Builder clearName() {
        name_ = getDefaultInstance().getName();
        bitField0_ = (bitField0_ & ~0x00000002);
        onChanged();
        return this;
      }
      /**
       * <code>string name = 2;</code>
       * @param value The bytes for name to set.
       * @return This builder for chaining.
       */
      public Builder setNameBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) { throw new NullPointerException(); }
        checkByteStringIsUtf8(value);
        name_ = value;
        bitField0_ |= 0x00000002;
        onChanged();
        return this;
      }

      /**
       * <code>bytes bytes_value = 4;</code>
       * @return Whether the bytesValue field is set.
       */
      public boolean hasBytesValue() {
        return valueByTypeCase_ == 4;
      }
      /**
       * <code>bytes bytes_value = 4;</code>
       * @return The bytesValue.
       */
      public com.google.protobuf.ByteString getBytesValue() {
        if (valueByTypeCase_ == 4) {
          return (com.google.protobuf.ByteString) valueByType_;
        }
        return com.google.protobuf.ByteString.EMPTY;
      }
      /**
       * <code>bytes bytes_value = 4;</code>
       * @param value The bytesValue to set.
       * @return This builder for chaining.
       */
      public Builder setBytesValue(com.google.protobuf.ByteString value) {
        if (value == null) { throw new NullPointerException(); }
        valueByTypeCase_ = 4;
        valueByType_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>bytes bytes_value = 4;</code>
       * @return This builder for chaining.
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
       * @return Whether the stringValue field is set.
       */
      @java.lang.Override
      public boolean hasStringValue() {
        return valueByTypeCase_ == 5;
      }
      /**
       * <code>string string_value = 5;</code>
       * @return The stringValue.
       */
      @java.lang.Override
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
       * @return The bytes for stringValue.
       */
      @java.lang.Override
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
       * @param value The stringValue to set.
       * @return This builder for chaining.
       */
      public Builder setStringValue(
          java.lang.String value) {
        if (value == null) { throw new NullPointerException(); }
        valueByTypeCase_ = 5;
        valueByType_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>string string_value = 5;</code>
       * @return This builder for chaining.
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
       * @param value The bytes for stringValue to set.
       * @return This builder for chaining.
       */
      public Builder setStringValueBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) { throw new NullPointerException(); }
        checkByteStringIsUtf8(value);
        valueByTypeCase_ = 5;
        valueByType_ = value;
        onChanged();
        return this;
      }

      /**
       * <code>bool bool_value = 6;</code>
       * @return Whether the boolValue field is set.
       */
      public boolean hasBoolValue() {
        return valueByTypeCase_ == 6;
      }
      /**
       * <code>bool bool_value = 6;</code>
       * @return The boolValue.
       */
      public boolean getBoolValue() {
        if (valueByTypeCase_ == 6) {
          return (java.lang.Boolean) valueByType_;
        }
        return false;
      }
      /**
       * <code>bool bool_value = 6;</code>
       * @param value The boolValue to set.
       * @return This builder for chaining.
       */
      public Builder setBoolValue(boolean value) {

        valueByTypeCase_ = 6;
        valueByType_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>bool bool_value = 6;</code>
       * @return This builder for chaining.
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
       * @return Whether the uint32Value field is set.
       */
      public boolean hasUint32Value() {
        return valueByTypeCase_ == 7;
      }
      /**
       * <code>uint32 uint32_value = 7;</code>
       * @return The uint32Value.
       */
      public int getUint32Value() {
        if (valueByTypeCase_ == 7) {
          return (java.lang.Integer) valueByType_;
        }
        return 0;
      }
      /**
       * <code>uint32 uint32_value = 7;</code>
       * @param value The uint32Value to set.
       * @return This builder for chaining.
       */
      public Builder setUint32Value(int value) {

        valueByTypeCase_ = 7;
        valueByType_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>uint32 uint32_value = 7;</code>
       * @return This builder for chaining.
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
       * @return Whether the uint64Value field is set.
       */
      public boolean hasUint64Value() {
        return valueByTypeCase_ == 8;
      }
      /**
       * <code>uint64 uint64_value = 8;</code>
       * @return The uint64Value.
       */
      public long getUint64Value() {
        if (valueByTypeCase_ == 8) {
          return (java.lang.Long) valueByType_;
        }
        return 0L;
      }
      /**
       * <code>uint64 uint64_value = 8;</code>
       * @param value The uint64Value to set.
       * @return This builder for chaining.
       */
      public Builder setUint64Value(long value) {

        valueByTypeCase_ = 8;
        valueByType_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>uint64 uint64_value = 8;</code>
       * @return This builder for chaining.
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
       * @return Whether the sint32Value field is set.
       */
      public boolean hasSint32Value() {
        return valueByTypeCase_ == 9;
      }
      /**
       * <code>sint32 sint32_value = 9;</code>
       * @return The sint32Value.
       */
      public int getSint32Value() {
        if (valueByTypeCase_ == 9) {
          return (java.lang.Integer) valueByType_;
        }
        return 0;
      }
      /**
       * <code>sint32 sint32_value = 9;</code>
       * @param value The sint32Value to set.
       * @return This builder for chaining.
       */
      public Builder setSint32Value(int value) {

        valueByTypeCase_ = 9;
        valueByType_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>sint32 sint32_value = 9;</code>
       * @return This builder for chaining.
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
       * @return Whether the sint64Value field is set.
       */
      public boolean hasSint64Value() {
        return valueByTypeCase_ == 10;
      }
      /**
       * <code>sint64 sint64_value = 10;</code>
       * @return The sint64Value.
       */
      public long getSint64Value() {
        if (valueByTypeCase_ == 10) {
          return (java.lang.Long) valueByType_;
        }
        return 0L;
      }
      /**
       * <code>sint64 sint64_value = 10;</code>
       * @param value The sint64Value to set.
       * @return This builder for chaining.
       */
      public Builder setSint64Value(long value) {

        valueByTypeCase_ = 10;
        valueByType_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>sint64 sint64_value = 10;</code>
       * @return This builder for chaining.
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
       * @return Whether the doubleValue field is set.
       */
      public boolean hasDoubleValue() {
        return valueByTypeCase_ == 11;
      }
      /**
       * <code>double double_value = 11;</code>
       * @return The doubleValue.
       */
      public double getDoubleValue() {
        if (valueByTypeCase_ == 11) {
          return (java.lang.Double) valueByType_;
        }
        return 0D;
      }
      /**
       * <code>double double_value = 11;</code>
       * @param value The doubleValue to set.
       * @return This builder for chaining.
       */
      public Builder setDoubleValue(double value) {

        valueByTypeCase_ = 11;
        valueByType_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>double double_value = 11;</code>
       * @return This builder for chaining.
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
       * @return Whether the floatValue field is set.
       */
      public boolean hasFloatValue() {
        return valueByTypeCase_ == 12;
      }
      /**
       * <code>float float_value = 12;</code>
       * @return The floatValue.
       */
      public float getFloatValue() {
        if (valueByTypeCase_ == 12) {
          return (java.lang.Float) valueByType_;
        }
        return 0F;
      }
      /**
       * <code>float float_value = 12;</code>
       * @param value The floatValue to set.
       * @return This builder for chaining.
       */
      public Builder setFloatValue(float value) {

        valueByTypeCase_ = 12;
        valueByType_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>float float_value = 12;</code>
       * @return This builder for chaining.
       */
      public Builder clearFloatValue() {
        if (valueByTypeCase_ == 12) {
          valueByTypeCase_ = 0;
          valueByType_ = null;
          onChanged();
        }
        return this;
      }

      private java.util.List<org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField> fields_ =
        java.util.Collections.emptyList();
      private void ensureFieldsIsMutable() {
        if (!((bitField0_ & 0x00000800) != 0)) {
          fields_ = new java.util.ArrayList<org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField>(fields_);
          bitField0_ |= 0x00000800;
         }
      }

      private com.google.protobuf.RepeatedFieldBuilderV3<
          org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField, org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField.Builder, org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryFieldOrBuilder> fieldsBuilder_;

      /**
       * <code>repeated .TelemetryField fields = 15;</code>
       */
      public java.util.List<org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField> getFieldsList() {
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
      public org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField getFields(int index) {
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
          int index, org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField value) {
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
          int index, org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField.Builder builderForValue) {
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
      public Builder addFields(org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField value) {
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
          int index, org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField value) {
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
          org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField.Builder builderForValue) {
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
          int index, org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField.Builder builderForValue) {
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
          java.lang.Iterable<? extends org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField> values) {
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
      public org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField.Builder getFieldsBuilder(
          int index) {
        return getFieldsFieldBuilder().getBuilder(index);
      }
      /**
       * <code>repeated .TelemetryField fields = 15;</code>
       */
      public org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryFieldOrBuilder getFieldsOrBuilder(
          int index) {
        if (fieldsBuilder_ == null) {
          return fields_.get(index);  } else {
          return fieldsBuilder_.getMessageOrBuilder(index);
        }
      }
      /**
       * <code>repeated .TelemetryField fields = 15;</code>
       */
      public java.util.List<? extends org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryFieldOrBuilder> 
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
      public org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField.Builder addFieldsBuilder() {
        return getFieldsFieldBuilder().addBuilder(
            org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField.getDefaultInstance());
      }
      /**
       * <code>repeated .TelemetryField fields = 15;</code>
       */
      public org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField.Builder addFieldsBuilder(
          int index) {
        return getFieldsFieldBuilder().addBuilder(
            index, org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField.getDefaultInstance());
      }
      /**
       * <code>repeated .TelemetryField fields = 15;</code>
       */
      public java.util.List<org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField.Builder> 
           getFieldsBuilderList() {
        return getFieldsFieldBuilder().getBuilderList();
      }
      private com.google.protobuf.RepeatedFieldBuilderV3<
          org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField, org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField.Builder, org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryFieldOrBuilder> 
          getFieldsFieldBuilder() {
        if (fieldsBuilder_ == null) {
          fieldsBuilder_ = new com.google.protobuf.RepeatedFieldBuilderV3<
              org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField, org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField.Builder, org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryFieldOrBuilder>(
                  fields_,
                  ((bitField0_ & 0x00000800) != 0),
                  getParentForChildren(),
                  isClean());
          fields_ = null;
        }
        return fieldsBuilder_;
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


      // @@protoc_insertion_point(builder_scope:TelemetryField)
    }

    // @@protoc_insertion_point(class_scope:TelemetryField)
    private static final org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField();
    }

    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<TelemetryField>
        PARSER = new com.google.protobuf.AbstractParser<TelemetryField>() {
      @java.lang.Override
      public TelemetryField parsePartialFrom(
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

    public static com.google.protobuf.Parser<TelemetryField> parser() {
      return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<TelemetryField> getParserForType() {
      return PARSER;
    }

    @java.lang.Override
    public org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  public interface TelemetryGPBTableOrBuilder extends
      // @@protoc_insertion_point(interface_extends:TelemetryGPBTable)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <code>repeated .TelemetryRowGPB row = 1;</code>
     */
    java.util.List<org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB> 
        getRowList();
    /**
     * <code>repeated .TelemetryRowGPB row = 1;</code>
     */
    org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB getRow(int index);
    /**
     * <code>repeated .TelemetryRowGPB row = 1;</code>
     */
    int getRowCount();
    /**
     * <code>repeated .TelemetryRowGPB row = 1;</code>
     */
    java.util.List<? extends org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPBOrBuilder> 
        getRowOrBuilderList();
    /**
     * <code>repeated .TelemetryRowGPB row = 1;</code>
     */
    org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPBOrBuilder getRowOrBuilder(
        int index);
  }
  /**
   * Protobuf type {@code TelemetryGPBTable}
   */
  public static final class TelemetryGPBTable extends
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
    @SuppressWarnings({"unused"})
    protected java.lang.Object newInstance(
        UnusedPrivateParameter unused) {
      return new TelemetryGPBTable();
    }

    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.internal_static_TelemetryGPBTable_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.internal_static_TelemetryGPBTable_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable.class, org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable.Builder.class);
    }

    public static final int ROW_FIELD_NUMBER = 1;
    @SuppressWarnings("serial")
    private java.util.List<org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB> row_;
    /**
     * <code>repeated .TelemetryRowGPB row = 1;</code>
     */
    @java.lang.Override
    public java.util.List<org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB> getRowList() {
      return row_;
    }
    /**
     * <code>repeated .TelemetryRowGPB row = 1;</code>
     */
    @java.lang.Override
    public java.util.List<? extends org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPBOrBuilder> 
        getRowOrBuilderList() {
      return row_;
    }
    /**
     * <code>repeated .TelemetryRowGPB row = 1;</code>
     */
    @java.lang.Override
    public int getRowCount() {
      return row_.size();
    }
    /**
     * <code>repeated .TelemetryRowGPB row = 1;</code>
     */
    @java.lang.Override
    public org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB getRow(int index) {
      return row_.get(index);
    }
    /**
     * <code>repeated .TelemetryRowGPB row = 1;</code>
     */
    @java.lang.Override
    public org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPBOrBuilder getRowOrBuilder(
        int index) {
      return row_.get(index);
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
      for (int i = 0; i < row_.size(); i++) {
        output.writeMessage(1, row_.get(i));
      }
      getUnknownFields().writeTo(output);
    }

    @java.lang.Override
    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      for (int i = 0; i < row_.size(); i++) {
        size += com.google.protobuf.CodedOutputStream
          .computeMessageSize(1, row_.get(i));
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
      if (!(obj instanceof org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable)) {
        return super.equals(obj);
      }
      org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable other = (org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable) obj;

      if (!getRowList()
          .equals(other.getRowList())) return false;
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
      if (getRowCount() > 0) {
        hash = (37 * hash) + ROW_FIELD_NUMBER;
        hash = (53 * hash) + getRowList().hashCode();
      }
      hash = (29 * hash) + getUnknownFields().hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input);
    }

    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable parseFrom(
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
    public static Builder newBuilder(org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable prototype) {
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
     * Protobuf type {@code TelemetryGPBTable}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:TelemetryGPBTable)
        org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTableOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.internal_static_TelemetryGPBTable_descriptor;
      }

      @java.lang.Override
      protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.internal_static_TelemetryGPBTable_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable.class, org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable.Builder.class);
      }

      // Construct using org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable.newBuilder()
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
        if (rowBuilder_ == null) {
          row_ = java.util.Collections.emptyList();
        } else {
          row_ = null;
          rowBuilder_.clear();
        }
        bitField0_ = (bitField0_ & ~0x00000001);
        return this;
      }

      @java.lang.Override
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.internal_static_TelemetryGPBTable_descriptor;
      }

      @java.lang.Override
      public org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable getDefaultInstanceForType() {
        return org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable.getDefaultInstance();
      }

      @java.lang.Override
      public org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable build() {
        org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      @java.lang.Override
      public org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable buildPartial() {
        org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable result = new org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable(this);
        buildPartialRepeatedFields(result);
        if (bitField0_ != 0) { buildPartial0(result); }
        onBuilt();
        return result;
      }

      private void buildPartialRepeatedFields(org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable result) {
        if (rowBuilder_ == null) {
          if (((bitField0_ & 0x00000001) != 0)) {
            row_ = java.util.Collections.unmodifiableList(row_);
            bitField0_ = (bitField0_ & ~0x00000001);
          }
          result.row_ = row_;
        } else {
          result.row_ = rowBuilder_.build();
        }
      }

      private void buildPartial0(org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable result) {
        int from_bitField0_ = bitField0_;
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
        if (other instanceof org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable) {
          return mergeFrom((org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable other) {
        if (other == org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable.getDefaultInstance()) return this;
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
                org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB m =
                    input.readMessage(
                        org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB.parser(),
                        extensionRegistry);
                if (rowBuilder_ == null) {
                  ensureRowIsMutable();
                  row_.add(m);
                } else {
                  rowBuilder_.addMessage(m);
                }
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

      private java.util.List<org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB> row_ =
        java.util.Collections.emptyList();
      private void ensureRowIsMutable() {
        if (!((bitField0_ & 0x00000001) != 0)) {
          row_ = new java.util.ArrayList<org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB>(row_);
          bitField0_ |= 0x00000001;
         }
      }

      private com.google.protobuf.RepeatedFieldBuilderV3<
          org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB, org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB.Builder, org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPBOrBuilder> rowBuilder_;

      /**
       * <code>repeated .TelemetryRowGPB row = 1;</code>
       */
      public java.util.List<org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB> getRowList() {
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
      public org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB getRow(int index) {
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
          int index, org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB value) {
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
          int index, org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB.Builder builderForValue) {
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
      public Builder addRow(org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB value) {
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
          int index, org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB value) {
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
          org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB.Builder builderForValue) {
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
          int index, org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB.Builder builderForValue) {
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
          java.lang.Iterable<? extends org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB> values) {
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
      public org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB.Builder getRowBuilder(
          int index) {
        return getRowFieldBuilder().getBuilder(index);
      }
      /**
       * <code>repeated .TelemetryRowGPB row = 1;</code>
       */
      public org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPBOrBuilder getRowOrBuilder(
          int index) {
        if (rowBuilder_ == null) {
          return row_.get(index);  } else {
          return rowBuilder_.getMessageOrBuilder(index);
        }
      }
      /**
       * <code>repeated .TelemetryRowGPB row = 1;</code>
       */
      public java.util.List<? extends org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPBOrBuilder> 
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
      public org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB.Builder addRowBuilder() {
        return getRowFieldBuilder().addBuilder(
            org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB.getDefaultInstance());
      }
      /**
       * <code>repeated .TelemetryRowGPB row = 1;</code>
       */
      public org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB.Builder addRowBuilder(
          int index) {
        return getRowFieldBuilder().addBuilder(
            index, org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB.getDefaultInstance());
      }
      /**
       * <code>repeated .TelemetryRowGPB row = 1;</code>
       */
      public java.util.List<org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB.Builder> 
           getRowBuilderList() {
        return getRowFieldBuilder().getBuilderList();
      }
      private com.google.protobuf.RepeatedFieldBuilderV3<
          org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB, org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB.Builder, org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPBOrBuilder> 
          getRowFieldBuilder() {
        if (rowBuilder_ == null) {
          rowBuilder_ = new com.google.protobuf.RepeatedFieldBuilderV3<
              org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB, org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB.Builder, org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPBOrBuilder>(
                  row_,
                  ((bitField0_ & 0x00000001) != 0),
                  getParentForChildren(),
                  isClean());
          row_ = null;
        }
        return rowBuilder_;
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


      // @@protoc_insertion_point(builder_scope:TelemetryGPBTable)
    }

    // @@protoc_insertion_point(class_scope:TelemetryGPBTable)
    private static final org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable();
    }

    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<TelemetryGPBTable>
        PARSER = new com.google.protobuf.AbstractParser<TelemetryGPBTable>() {
      @java.lang.Override
      public TelemetryGPBTable parsePartialFrom(
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

    public static com.google.protobuf.Parser<TelemetryGPBTable> parser() {
      return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<TelemetryGPBTable> getParserForType() {
      return PARSER;
    }

    @java.lang.Override
    public org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryGPBTable getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  public interface TelemetryRowGPBOrBuilder extends
      // @@protoc_insertion_point(interface_extends:TelemetryRowGPB)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <code>uint64 timestamp = 1;</code>
     * @return The timestamp.
     */
    long getTimestamp();

    /**
     * <code>bytes keys = 10;</code>
     * @return The keys.
     */
    com.google.protobuf.ByteString getKeys();

    /**
     * <code>bytes content = 11;</code>
     * @return The content.
     */
    com.google.protobuf.ByteString getContent();
  }
  /**
   * Protobuf type {@code TelemetryRowGPB}
   */
  public static final class TelemetryRowGPB extends
      com.google.protobuf.GeneratedMessageV3 implements
      // @@protoc_insertion_point(message_implements:TelemetryRowGPB)
      TelemetryRowGPBOrBuilder {
  private static final long serialVersionUID = 0L;
    // Use TelemetryRowGPB.newBuilder() to construct.
    private TelemetryRowGPB(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
      super(builder);
    }
    private TelemetryRowGPB() {
      keys_ = com.google.protobuf.ByteString.EMPTY;
      content_ = com.google.protobuf.ByteString.EMPTY;
    }

    @java.lang.Override
    @SuppressWarnings({"unused"})
    protected java.lang.Object newInstance(
        UnusedPrivateParameter unused) {
      return new TelemetryRowGPB();
    }

    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.internal_static_TelemetryRowGPB_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.internal_static_TelemetryRowGPB_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB.class, org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB.Builder.class);
    }

    public static final int TIMESTAMP_FIELD_NUMBER = 1;
    private long timestamp_ = 0L;
    /**
     * <code>uint64 timestamp = 1;</code>
     * @return The timestamp.
     */
    @java.lang.Override
    public long getTimestamp() {
      return timestamp_;
    }

    public static final int KEYS_FIELD_NUMBER = 10;
    private com.google.protobuf.ByteString keys_ = com.google.protobuf.ByteString.EMPTY;
    /**
     * <code>bytes keys = 10;</code>
     * @return The keys.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString getKeys() {
      return keys_;
    }

    public static final int CONTENT_FIELD_NUMBER = 11;
    private com.google.protobuf.ByteString content_ = com.google.protobuf.ByteString.EMPTY;
    /**
     * <code>bytes content = 11;</code>
     * @return The content.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString getContent() {
      return content_;
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
      if (timestamp_ != 0L) {
        output.writeUInt64(1, timestamp_);
      }
      if (!keys_.isEmpty()) {
        output.writeBytes(10, keys_);
      }
      if (!content_.isEmpty()) {
        output.writeBytes(11, content_);
      }
      getUnknownFields().writeTo(output);
    }

    @java.lang.Override
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
      size += getUnknownFields().getSerializedSize();
      memoizedSize = size;
      return size;
    }

    @java.lang.Override
    public boolean equals(final java.lang.Object obj) {
      if (obj == this) {
       return true;
      }
      if (!(obj instanceof org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB)) {
        return super.equals(obj);
      }
      org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB other = (org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB) obj;

      if (getTimestamp()
          != other.getTimestamp()) return false;
      if (!getKeys()
          .equals(other.getKeys())) return false;
      if (!getContent()
          .equals(other.getContent())) return false;
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
      hash = (37 * hash) + TIMESTAMP_FIELD_NUMBER;
      hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
          getTimestamp());
      hash = (37 * hash) + KEYS_FIELD_NUMBER;
      hash = (53 * hash) + getKeys().hashCode();
      hash = (37 * hash) + CONTENT_FIELD_NUMBER;
      hash = (53 * hash) + getContent().hashCode();
      hash = (29 * hash) + getUnknownFields().hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input);
    }

    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB parseFrom(
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
    public static Builder newBuilder(org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB prototype) {
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
     * Protobuf type {@code TelemetryRowGPB}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:TelemetryRowGPB)
        org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPBOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.internal_static_TelemetryRowGPB_descriptor;
      }

      @java.lang.Override
      protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.internal_static_TelemetryRowGPB_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB.class, org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB.Builder.class);
      }

      // Construct using org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB.newBuilder()
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
        timestamp_ = 0L;
        keys_ = com.google.protobuf.ByteString.EMPTY;
        content_ = com.google.protobuf.ByteString.EMPTY;
        return this;
      }

      @java.lang.Override
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.internal_static_TelemetryRowGPB_descriptor;
      }

      @java.lang.Override
      public org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB getDefaultInstanceForType() {
        return org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB.getDefaultInstance();
      }

      @java.lang.Override
      public org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB build() {
        org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      @java.lang.Override
      public org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB buildPartial() {
        org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB result = new org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB(this);
        if (bitField0_ != 0) { buildPartial0(result); }
        onBuilt();
        return result;
      }

      private void buildPartial0(org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB result) {
        int from_bitField0_ = bitField0_;
        if (((from_bitField0_ & 0x00000001) != 0)) {
          result.timestamp_ = timestamp_;
        }
        if (((from_bitField0_ & 0x00000002) != 0)) {
          result.keys_ = keys_;
        }
        if (((from_bitField0_ & 0x00000004) != 0)) {
          result.content_ = content_;
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
        if (other instanceof org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB) {
          return mergeFrom((org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB other) {
        if (other == org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB.getDefaultInstance()) return this;
        if (other.getTimestamp() != 0L) {
          setTimestamp(other.getTimestamp());
        }
        if (other.getKeys() != com.google.protobuf.ByteString.EMPTY) {
          setKeys(other.getKeys());
        }
        if (other.getContent() != com.google.protobuf.ByteString.EMPTY) {
          setContent(other.getContent());
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
                timestamp_ = input.readUInt64();
                bitField0_ |= 0x00000001;
                break;
              } // case 8
              case 82: {
                keys_ = input.readBytes();
                bitField0_ |= 0x00000002;
                break;
              } // case 82
              case 90: {
                content_ = input.readBytes();
                bitField0_ |= 0x00000004;
                break;
              } // case 90
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
       * <code>uint64 timestamp = 1;</code>
       * @return The timestamp.
       */
      @java.lang.Override
      public long getTimestamp() {
        return timestamp_;
      }
      /**
       * <code>uint64 timestamp = 1;</code>
       * @param value The timestamp to set.
       * @return This builder for chaining.
       */
      public Builder setTimestamp(long value) {

        timestamp_ = value;
        bitField0_ |= 0x00000001;
        onChanged();
        return this;
      }
      /**
       * <code>uint64 timestamp = 1;</code>
       * @return This builder for chaining.
       */
      public Builder clearTimestamp() {
        bitField0_ = (bitField0_ & ~0x00000001);
        timestamp_ = 0L;
        onChanged();
        return this;
      }

      private com.google.protobuf.ByteString keys_ = com.google.protobuf.ByteString.EMPTY;
      /**
       * <code>bytes keys = 10;</code>
       * @return The keys.
       */
      @java.lang.Override
      public com.google.protobuf.ByteString getKeys() {
        return keys_;
      }
      /**
       * <code>bytes keys = 10;</code>
       * @param value The keys to set.
       * @return This builder for chaining.
       */
      public Builder setKeys(com.google.protobuf.ByteString value) {
        if (value == null) { throw new NullPointerException(); }
        keys_ = value;
        bitField0_ |= 0x00000002;
        onChanged();
        return this;
      }
      /**
       * <code>bytes keys = 10;</code>
       * @return This builder for chaining.
       */
      public Builder clearKeys() {
        bitField0_ = (bitField0_ & ~0x00000002);
        keys_ = getDefaultInstance().getKeys();
        onChanged();
        return this;
      }

      private com.google.protobuf.ByteString content_ = com.google.protobuf.ByteString.EMPTY;
      /**
       * <code>bytes content = 11;</code>
       * @return The content.
       */
      @java.lang.Override
      public com.google.protobuf.ByteString getContent() {
        return content_;
      }
      /**
       * <code>bytes content = 11;</code>
       * @param value The content to set.
       * @return This builder for chaining.
       */
      public Builder setContent(com.google.protobuf.ByteString value) {
        if (value == null) { throw new NullPointerException(); }
        content_ = value;
        bitField0_ |= 0x00000004;
        onChanged();
        return this;
      }
      /**
       * <code>bytes content = 11;</code>
       * @return This builder for chaining.
       */
      public Builder clearContent() {
        bitField0_ = (bitField0_ & ~0x00000004);
        content_ = getDefaultInstance().getContent();
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


      // @@protoc_insertion_point(builder_scope:TelemetryRowGPB)
    }

    // @@protoc_insertion_point(class_scope:TelemetryRowGPB)
    private static final org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB();
    }

    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<TelemetryRowGPB>
        PARSER = new com.google.protobuf.AbstractParser<TelemetryRowGPB>() {
      @java.lang.Override
      public TelemetryRowGPB parsePartialFrom(
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

    public static com.google.protobuf.Parser<TelemetryRowGPB> parser() {
      return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<TelemetryRowGPB> getParserForType() {
      return PARSER;
    }

    @java.lang.Override
    public org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryRowGPB getDefaultInstanceForType() {
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
      "\n\004name\030\002 \001(\t\022\025\n\013bytes_value\030\004 \001(\014H\000\022\026\n\014s" +
      "tring_value\030\005 \001(\tH\000\022\024\n\nbool_value\030\006 \001(\010H" +
      "\000\022\026\n\014uint32_value\030\007 \001(\rH\000\022\026\n\014uint64_valu" +
      "e\030\010 \001(\004H\000\022\026\n\014sint32_value\030\t \001(\021H\000\022\026\n\014sin" +
      "t64_value\030\n \001(\022H\000\022\026\n\014double_value\030\013 \001(\001H" +
      "\000\022\025\n\013float_value\030\014 \001(\002H\000\022\037\n\006fields\030\017 \003(\013" +
      "2\017.TelemetryFieldB\017\n\rvalue_by_type\"2\n\021Te" +
      "lemetryGPBTable\022\035\n\003row\030\001 \003(\0132\020.Telemetry" +
      "RowGPB\"C\n\017TelemetryRowGPB\022\021\n\ttimestamp\030\001" +
      " \001(\004\022\014\n\004keys\030\n \001(\014\022\017\n\007content\030\013 \001(\014BJ\n9o" +
      "rg.opennms.netmgt.telemetry.protocols.nx" +
      "os.adapter.protoZ\rtelemetry_bisb\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        });
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
