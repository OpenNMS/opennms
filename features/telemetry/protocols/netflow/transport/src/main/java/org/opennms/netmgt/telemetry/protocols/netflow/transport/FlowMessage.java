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
package org.opennms.netmgt.telemetry.protocols.netflow.transport;

/**
 * Protobuf type {@code FlowMessage}
 */
public final class FlowMessage extends
        com.google.protobuf.GeneratedMessageV3 implements
        // @@protoc_insertion_point(message_implements:FlowMessage)
        FlowMessageOrBuilder {
  private static final long serialVersionUID = 0L;
  // Use FlowMessage.newBuilder() to construct.
  private FlowMessage(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private FlowMessage() {
    direction_ = 0;
    dstAddress_ = "";
    dstHostname_ = "";
    nextHopAddress_ = "";
    nextHopHostname_ = "";
    samplingAlgorithm_ = 0;
    srcAddress_ = "";
    srcHostname_ = "";
    netflowVersion_ = 0;
    nodeIdentifier_ = "";
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
          UnusedPrivateParameter unused) {
    return new FlowMessage();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
  getDescriptor() {
    return org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowProtos.internal_static_FlowMessage_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
  internalGetFieldAccessorTable() {
    return org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowProtos.internal_static_FlowMessage_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                    org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage.class, org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage.Builder.class);
  }

  private int bitField0_;
  public static final int TIMESTAMP_FIELD_NUMBER = 1;
  private long timestamp_ = 0L;
  /**
   * <pre>
   * Flow timestamp in milliseconds.
   * </pre>
   *
   * <code>uint64 timestamp = 1;</code>
   * @return The timestamp.
   */
  @java.lang.Override
  public long getTimestamp() {
    return timestamp_;
  }

  public static final int NUM_BYTES_FIELD_NUMBER = 2;
  private com.google.protobuf.UInt64Value numBytes_;
  /**
   * <pre>
   * Number of bytes transferred in the flow
   * </pre>
   *
   * <code>.google.protobuf.UInt64Value num_bytes = 2;</code>
   * @return Whether the numBytes field is set.
   */
  @java.lang.Override
  public boolean hasNumBytes() {
    return ((bitField0_ & 0x00000001) != 0);
  }
  /**
   * <pre>
   * Number of bytes transferred in the flow
   * </pre>
   *
   * <code>.google.protobuf.UInt64Value num_bytes = 2;</code>
   * @return The numBytes.
   */
  @java.lang.Override
  public com.google.protobuf.UInt64Value getNumBytes() {
    return numBytes_ == null ? com.google.protobuf.UInt64Value.getDefaultInstance() : numBytes_;
  }
  /**
   * <pre>
   * Number of bytes transferred in the flow
   * </pre>
   *
   * <code>.google.protobuf.UInt64Value num_bytes = 2;</code>
   */
  @java.lang.Override
  public com.google.protobuf.UInt64ValueOrBuilder getNumBytesOrBuilder() {
    return numBytes_ == null ? com.google.protobuf.UInt64Value.getDefaultInstance() : numBytes_;
  }

  public static final int DIRECTION_FIELD_NUMBER = 3;
  private int direction_ = 0;
  /**
   * <pre>
   * Direction of the flow (egress vs ingress)
   * </pre>
   *
   * <code>.Direction direction = 3;</code>
   * @return The enum numeric value on the wire for direction.
   */
  @java.lang.Override public int getDirectionValue() {
    return direction_;
  }
  /**
   * <pre>
   * Direction of the flow (egress vs ingress)
   * </pre>
   *
   * <code>.Direction direction = 3;</code>
   * @return The direction.
   */
  @java.lang.Override public org.opennms.netmgt.telemetry.protocols.netflow.transport.Direction getDirection() {
    org.opennms.netmgt.telemetry.protocols.netflow.transport.Direction result = org.opennms.netmgt.telemetry.protocols.netflow.transport.Direction.forNumber(direction_);
    return result == null ? org.opennms.netmgt.telemetry.protocols.netflow.transport.Direction.UNRECOGNIZED : result;
  }

  public static final int DST_ADDRESS_FIELD_NUMBER = 4;
  @SuppressWarnings("serial")
  private volatile java.lang.Object dstAddress_ = "";
  /**
   * <pre>
   *  Destination address.
   * </pre>
   *
   * <code>string dst_address = 4;</code>
   * @return The dstAddress.
   */
  @java.lang.Override
  public java.lang.String getDstAddress() {
    java.lang.Object ref = dstAddress_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      dstAddress_ = s;
      return s;
    }
  }
  /**
   * <pre>
   *  Destination address.
   * </pre>
   *
   * <code>string dst_address = 4;</code>
   * @return The bytes for dstAddress.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
  getDstAddressBytes() {
    java.lang.Object ref = dstAddress_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b =
              com.google.protobuf.ByteString.copyFromUtf8(
                      (java.lang.String) ref);
      dstAddress_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int DST_HOSTNAME_FIELD_NUMBER = 5;
  @SuppressWarnings("serial")
  private volatile java.lang.Object dstHostname_ = "";
  /**
   * <pre>
   * Destination address hostname.
   * </pre>
   *
   * <code>string dst_hostname = 5;</code>
   * @return The dstHostname.
   */
  @java.lang.Override
  public java.lang.String getDstHostname() {
    java.lang.Object ref = dstHostname_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      dstHostname_ = s;
      return s;
    }
  }
  /**
   * <pre>
   * Destination address hostname.
   * </pre>
   *
   * <code>string dst_hostname = 5;</code>
   * @return The bytes for dstHostname.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
  getDstHostnameBytes() {
    java.lang.Object ref = dstHostname_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b =
              com.google.protobuf.ByteString.copyFromUtf8(
                      (java.lang.String) ref);
      dstHostname_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int DST_AS_FIELD_NUMBER = 6;
  private com.google.protobuf.UInt64Value dstAs_;
  /**
   * <pre>
   * Destination autonomous system (AS).
   * </pre>
   *
   * <code>.google.protobuf.UInt64Value dst_as = 6;</code>
   * @return Whether the dstAs field is set.
   */
  @java.lang.Override
  public boolean hasDstAs() {
    return ((bitField0_ & 0x00000002) != 0);
  }
  /**
   * <pre>
   * Destination autonomous system (AS).
   * </pre>
   *
   * <code>.google.protobuf.UInt64Value dst_as = 6;</code>
   * @return The dstAs.
   */
  @java.lang.Override
  public com.google.protobuf.UInt64Value getDstAs() {
    return dstAs_ == null ? com.google.protobuf.UInt64Value.getDefaultInstance() : dstAs_;
  }
  /**
   * <pre>
   * Destination autonomous system (AS).
   * </pre>
   *
   * <code>.google.protobuf.UInt64Value dst_as = 6;</code>
   */
  @java.lang.Override
  public com.google.protobuf.UInt64ValueOrBuilder getDstAsOrBuilder() {
    return dstAs_ == null ? com.google.protobuf.UInt64Value.getDefaultInstance() : dstAs_;
  }

  public static final int DST_MASK_LEN_FIELD_NUMBER = 7;
  private com.google.protobuf.UInt32Value dstMaskLen_;
  /**
   * <pre>
   * The number of contiguous bits in the source address subnet mask.
   * </pre>
   *
   * <code>.google.protobuf.UInt32Value dst_mask_len = 7;</code>
   * @return Whether the dstMaskLen field is set.
   */
  @java.lang.Override
  public boolean hasDstMaskLen() {
    return ((bitField0_ & 0x00000004) != 0);
  }
  /**
   * <pre>
   * The number of contiguous bits in the source address subnet mask.
   * </pre>
   *
   * <code>.google.protobuf.UInt32Value dst_mask_len = 7;</code>
   * @return The dstMaskLen.
   */
  @java.lang.Override
  public com.google.protobuf.UInt32Value getDstMaskLen() {
    return dstMaskLen_ == null ? com.google.protobuf.UInt32Value.getDefaultInstance() : dstMaskLen_;
  }
  /**
   * <pre>
   * The number of contiguous bits in the source address subnet mask.
   * </pre>
   *
   * <code>.google.protobuf.UInt32Value dst_mask_len = 7;</code>
   */
  @java.lang.Override
  public com.google.protobuf.UInt32ValueOrBuilder getDstMaskLenOrBuilder() {
    return dstMaskLen_ == null ? com.google.protobuf.UInt32Value.getDefaultInstance() : dstMaskLen_;
  }

  public static final int DST_PORT_FIELD_NUMBER = 8;
  private com.google.protobuf.UInt32Value dstPort_;
  /**
   * <pre>
   * Destination port.
   * </pre>
   *
   * <code>.google.protobuf.UInt32Value dst_port = 8;</code>
   * @return Whether the dstPort field is set.
   */
  @java.lang.Override
  public boolean hasDstPort() {
    return ((bitField0_ & 0x00000008) != 0);
  }
  /**
   * <pre>
   * Destination port.
   * </pre>
   *
   * <code>.google.protobuf.UInt32Value dst_port = 8;</code>
   * @return The dstPort.
   */
  @java.lang.Override
  public com.google.protobuf.UInt32Value getDstPort() {
    return dstPort_ == null ? com.google.protobuf.UInt32Value.getDefaultInstance() : dstPort_;
  }
  /**
   * <pre>
   * Destination port.
   * </pre>
   *
   * <code>.google.protobuf.UInt32Value dst_port = 8;</code>
   */
  @java.lang.Override
  public com.google.protobuf.UInt32ValueOrBuilder getDstPortOrBuilder() {
    return dstPort_ == null ? com.google.protobuf.UInt32Value.getDefaultInstance() : dstPort_;
  }

  public static final int ENGINE_ID_FIELD_NUMBER = 9;
  private com.google.protobuf.UInt32Value engineId_;
  /**
   * <pre>
   * Slot number of the flow-switching engine.
   * </pre>
   *
   * <code>.google.protobuf.UInt32Value engine_id = 9;</code>
   * @return Whether the engineId field is set.
   */
  @java.lang.Override
  public boolean hasEngineId() {
    return ((bitField0_ & 0x00000010) != 0);
  }
  /**
   * <pre>
   * Slot number of the flow-switching engine.
   * </pre>
   *
   * <code>.google.protobuf.UInt32Value engine_id = 9;</code>
   * @return The engineId.
   */
  @java.lang.Override
  public com.google.protobuf.UInt32Value getEngineId() {
    return engineId_ == null ? com.google.protobuf.UInt32Value.getDefaultInstance() : engineId_;
  }
  /**
   * <pre>
   * Slot number of the flow-switching engine.
   * </pre>
   *
   * <code>.google.protobuf.UInt32Value engine_id = 9;</code>
   */
  @java.lang.Override
  public com.google.protobuf.UInt32ValueOrBuilder getEngineIdOrBuilder() {
    return engineId_ == null ? com.google.protobuf.UInt32Value.getDefaultInstance() : engineId_;
  }

  public static final int ENGINE_TYPE_FIELD_NUMBER = 10;
  private com.google.protobuf.UInt32Value engineType_;
  /**
   * <pre>
   * Type of flow-switching engine.
   * </pre>
   *
   * <code>.google.protobuf.UInt32Value engine_type = 10;</code>
   * @return Whether the engineType field is set.
   */
  @java.lang.Override
  public boolean hasEngineType() {
    return ((bitField0_ & 0x00000020) != 0);
  }
  /**
   * <pre>
   * Type of flow-switching engine.
   * </pre>
   *
   * <code>.google.protobuf.UInt32Value engine_type = 10;</code>
   * @return The engineType.
   */
  @java.lang.Override
  public com.google.protobuf.UInt32Value getEngineType() {
    return engineType_ == null ? com.google.protobuf.UInt32Value.getDefaultInstance() : engineType_;
  }
  /**
   * <pre>
   * Type of flow-switching engine.
   * </pre>
   *
   * <code>.google.protobuf.UInt32Value engine_type = 10;</code>
   */
  @java.lang.Override
  public com.google.protobuf.UInt32ValueOrBuilder getEngineTypeOrBuilder() {
    return engineType_ == null ? com.google.protobuf.UInt32Value.getDefaultInstance() : engineType_;
  }

  public static final int DELTA_SWITCHED_FIELD_NUMBER = 11;
  private com.google.protobuf.UInt64Value deltaSwitched_;
  /**
   * <pre>
   * Unix timestamp in ms at which the previous exported packet-
   * </pre>
   *
   * <code>.google.protobuf.UInt64Value delta_switched = 11;</code>
   * @return Whether the deltaSwitched field is set.
   */
  @java.lang.Override
  public boolean hasDeltaSwitched() {
    return ((bitField0_ & 0x00000040) != 0);
  }
  /**
   * <pre>
   * Unix timestamp in ms at which the previous exported packet-
   * </pre>
   *
   * <code>.google.protobuf.UInt64Value delta_switched = 11;</code>
   * @return The deltaSwitched.
   */
  @java.lang.Override
  public com.google.protobuf.UInt64Value getDeltaSwitched() {
    return deltaSwitched_ == null ? com.google.protobuf.UInt64Value.getDefaultInstance() : deltaSwitched_;
  }
  /**
   * <pre>
   * Unix timestamp in ms at which the previous exported packet-
   * </pre>
   *
   * <code>.google.protobuf.UInt64Value delta_switched = 11;</code>
   */
  @java.lang.Override
  public com.google.protobuf.UInt64ValueOrBuilder getDeltaSwitchedOrBuilder() {
    return deltaSwitched_ == null ? com.google.protobuf.UInt64Value.getDefaultInstance() : deltaSwitched_;
  }

  public static final int FIRST_SWITCHED_FIELD_NUMBER = 12;
  private com.google.protobuf.UInt64Value firstSwitched_;
  /**
   * <pre>
   * -associated with this flow was switched.
   * </pre>
   *
   * <code>.google.protobuf.UInt64Value first_switched = 12;</code>
   * @return Whether the firstSwitched field is set.
   */
  @java.lang.Override
  public boolean hasFirstSwitched() {
    return ((bitField0_ & 0x00000080) != 0);
  }
  /**
   * <pre>
   * -associated with this flow was switched.
   * </pre>
   *
   * <code>.google.protobuf.UInt64Value first_switched = 12;</code>
   * @return The firstSwitched.
   */
  @java.lang.Override
  public com.google.protobuf.UInt64Value getFirstSwitched() {
    return firstSwitched_ == null ? com.google.protobuf.UInt64Value.getDefaultInstance() : firstSwitched_;
  }
  /**
   * <pre>
   * -associated with this flow was switched.
   * </pre>
   *
   * <code>.google.protobuf.UInt64Value first_switched = 12;</code>
   */
  @java.lang.Override
  public com.google.protobuf.UInt64ValueOrBuilder getFirstSwitchedOrBuilder() {
    return firstSwitched_ == null ? com.google.protobuf.UInt64Value.getDefaultInstance() : firstSwitched_;
  }

  public static final int LAST_SWITCHED_FIELD_NUMBER = 13;
  private com.google.protobuf.UInt64Value lastSwitched_;
  /**
   * <pre>
   * -associated with this flow was switched.
   * </pre>
   *
   * <code>.google.protobuf.UInt64Value last_switched = 13;</code>
   * @return Whether the lastSwitched field is set.
   */
  @java.lang.Override
  public boolean hasLastSwitched() {
    return ((bitField0_ & 0x00000100) != 0);
  }
  /**
   * <pre>
   * -associated with this flow was switched.
   * </pre>
   *
   * <code>.google.protobuf.UInt64Value last_switched = 13;</code>
   * @return The lastSwitched.
   */
  @java.lang.Override
  public com.google.protobuf.UInt64Value getLastSwitched() {
    return lastSwitched_ == null ? com.google.protobuf.UInt64Value.getDefaultInstance() : lastSwitched_;
  }
  /**
   * <pre>
   * -associated with this flow was switched.
   * </pre>
   *
   * <code>.google.protobuf.UInt64Value last_switched = 13;</code>
   */
  @java.lang.Override
  public com.google.protobuf.UInt64ValueOrBuilder getLastSwitchedOrBuilder() {
    return lastSwitched_ == null ? com.google.protobuf.UInt64Value.getDefaultInstance() : lastSwitched_;
  }

  public static final int NUM_FLOW_RECORDS_FIELD_NUMBER = 14;
  private com.google.protobuf.UInt32Value numFlowRecords_;
  /**
   * <pre>
   * Number of flow records in the associated packet.
   * </pre>
   *
   * <code>.google.protobuf.UInt32Value num_flow_records = 14;</code>
   * @return Whether the numFlowRecords field is set.
   */
  @java.lang.Override
  public boolean hasNumFlowRecords() {
    return ((bitField0_ & 0x00000200) != 0);
  }
  /**
   * <pre>
   * Number of flow records in the associated packet.
   * </pre>
   *
   * <code>.google.protobuf.UInt32Value num_flow_records = 14;</code>
   * @return The numFlowRecords.
   */
  @java.lang.Override
  public com.google.protobuf.UInt32Value getNumFlowRecords() {
    return numFlowRecords_ == null ? com.google.protobuf.UInt32Value.getDefaultInstance() : numFlowRecords_;
  }
  /**
   * <pre>
   * Number of flow records in the associated packet.
   * </pre>
   *
   * <code>.google.protobuf.UInt32Value num_flow_records = 14;</code>
   */
  @java.lang.Override
  public com.google.protobuf.UInt32ValueOrBuilder getNumFlowRecordsOrBuilder() {
    return numFlowRecords_ == null ? com.google.protobuf.UInt32Value.getDefaultInstance() : numFlowRecords_;
  }

  public static final int NUM_PACKETS_FIELD_NUMBER = 15;
  private com.google.protobuf.UInt64Value numPackets_;
  /**
   * <pre>
   * Number of packets in the flow.
   * </pre>
   *
   * <code>.google.protobuf.UInt64Value num_packets = 15;</code>
   * @return Whether the numPackets field is set.
   */
  @java.lang.Override
  public boolean hasNumPackets() {
    return ((bitField0_ & 0x00000400) != 0);
  }
  /**
   * <pre>
   * Number of packets in the flow.
   * </pre>
   *
   * <code>.google.protobuf.UInt64Value num_packets = 15;</code>
   * @return The numPackets.
   */
  @java.lang.Override
  public com.google.protobuf.UInt64Value getNumPackets() {
    return numPackets_ == null ? com.google.protobuf.UInt64Value.getDefaultInstance() : numPackets_;
  }
  /**
   * <pre>
   * Number of packets in the flow.
   * </pre>
   *
   * <code>.google.protobuf.UInt64Value num_packets = 15;</code>
   */
  @java.lang.Override
  public com.google.protobuf.UInt64ValueOrBuilder getNumPacketsOrBuilder() {
    return numPackets_ == null ? com.google.protobuf.UInt64Value.getDefaultInstance() : numPackets_;
  }

  public static final int FLOW_SEQ_NUM_FIELD_NUMBER = 16;
  private com.google.protobuf.UInt64Value flowSeqNum_;
  /**
   * <pre>
   * Flow packet sequence number.
   * </pre>
   *
   * <code>.google.protobuf.UInt64Value flow_seq_num = 16;</code>
   * @return Whether the flowSeqNum field is set.
   */
  @java.lang.Override
  public boolean hasFlowSeqNum() {
    return ((bitField0_ & 0x00000800) != 0);
  }
  /**
   * <pre>
   * Flow packet sequence number.
   * </pre>
   *
   * <code>.google.protobuf.UInt64Value flow_seq_num = 16;</code>
   * @return The flowSeqNum.
   */
  @java.lang.Override
  public com.google.protobuf.UInt64Value getFlowSeqNum() {
    return flowSeqNum_ == null ? com.google.protobuf.UInt64Value.getDefaultInstance() : flowSeqNum_;
  }
  /**
   * <pre>
   * Flow packet sequence number.
   * </pre>
   *
   * <code>.google.protobuf.UInt64Value flow_seq_num = 16;</code>
   */
  @java.lang.Override
  public com.google.protobuf.UInt64ValueOrBuilder getFlowSeqNumOrBuilder() {
    return flowSeqNum_ == null ? com.google.protobuf.UInt64Value.getDefaultInstance() : flowSeqNum_;
  }

  public static final int INPUT_SNMP_IFINDEX_FIELD_NUMBER = 17;
  private com.google.protobuf.UInt32Value inputSnmpIfindex_;
  /**
   * <pre>
   * Input SNMP ifIndex.
   * </pre>
   *
   * <code>.google.protobuf.UInt32Value input_snmp_ifindex = 17;</code>
   * @return Whether the inputSnmpIfindex field is set.
   */
  @java.lang.Override
  public boolean hasInputSnmpIfindex() {
    return ((bitField0_ & 0x00001000) != 0);
  }
  /**
   * <pre>
   * Input SNMP ifIndex.
   * </pre>
   *
   * <code>.google.protobuf.UInt32Value input_snmp_ifindex = 17;</code>
   * @return The inputSnmpIfindex.
   */
  @java.lang.Override
  public com.google.protobuf.UInt32Value getInputSnmpIfindex() {
    return inputSnmpIfindex_ == null ? com.google.protobuf.UInt32Value.getDefaultInstance() : inputSnmpIfindex_;
  }
  /**
   * <pre>
   * Input SNMP ifIndex.
   * </pre>
   *
   * <code>.google.protobuf.UInt32Value input_snmp_ifindex = 17;</code>
   */
  @java.lang.Override
  public com.google.protobuf.UInt32ValueOrBuilder getInputSnmpIfindexOrBuilder() {
    return inputSnmpIfindex_ == null ? com.google.protobuf.UInt32Value.getDefaultInstance() : inputSnmpIfindex_;
  }

  public static final int OUTPUT_SNMP_IFINDEX_FIELD_NUMBER = 18;
  private com.google.protobuf.UInt32Value outputSnmpIfindex_;
  /**
   * <pre>
   * Output SNMP ifIndex.
   * </pre>
   *
   * <code>.google.protobuf.UInt32Value output_snmp_ifindex = 18;</code>
   * @return Whether the outputSnmpIfindex field is set.
   */
  @java.lang.Override
  public boolean hasOutputSnmpIfindex() {
    return ((bitField0_ & 0x00002000) != 0);
  }
  /**
   * <pre>
   * Output SNMP ifIndex.
   * </pre>
   *
   * <code>.google.protobuf.UInt32Value output_snmp_ifindex = 18;</code>
   * @return The outputSnmpIfindex.
   */
  @java.lang.Override
  public com.google.protobuf.UInt32Value getOutputSnmpIfindex() {
    return outputSnmpIfindex_ == null ? com.google.protobuf.UInt32Value.getDefaultInstance() : outputSnmpIfindex_;
  }
  /**
   * <pre>
   * Output SNMP ifIndex.
   * </pre>
   *
   * <code>.google.protobuf.UInt32Value output_snmp_ifindex = 18;</code>
   */
  @java.lang.Override
  public com.google.protobuf.UInt32ValueOrBuilder getOutputSnmpIfindexOrBuilder() {
    return outputSnmpIfindex_ == null ? com.google.protobuf.UInt32Value.getDefaultInstance() : outputSnmpIfindex_;
  }

  public static final int IP_PROTOCOL_VERSION_FIELD_NUMBER = 19;
  private com.google.protobuf.UInt32Value ipProtocolVersion_;
  /**
   * <pre>
   * IPv4 vs IPv6.
   * </pre>
   *
   * <code>.google.protobuf.UInt32Value ip_protocol_version = 19;</code>
   * @return Whether the ipProtocolVersion field is set.
   */
  @java.lang.Override
  public boolean hasIpProtocolVersion() {
    return ((bitField0_ & 0x00004000) != 0);
  }
  /**
   * <pre>
   * IPv4 vs IPv6.
   * </pre>
   *
   * <code>.google.protobuf.UInt32Value ip_protocol_version = 19;</code>
   * @return The ipProtocolVersion.
   */
  @java.lang.Override
  public com.google.protobuf.UInt32Value getIpProtocolVersion() {
    return ipProtocolVersion_ == null ? com.google.protobuf.UInt32Value.getDefaultInstance() : ipProtocolVersion_;
  }
  /**
   * <pre>
   * IPv4 vs IPv6.
   * </pre>
   *
   * <code>.google.protobuf.UInt32Value ip_protocol_version = 19;</code>
   */
  @java.lang.Override
  public com.google.protobuf.UInt32ValueOrBuilder getIpProtocolVersionOrBuilder() {
    return ipProtocolVersion_ == null ? com.google.protobuf.UInt32Value.getDefaultInstance() : ipProtocolVersion_;
  }

  public static final int NEXT_HOP_ADDRESS_FIELD_NUMBER = 20;
  @SuppressWarnings("serial")
  private volatile java.lang.Object nextHopAddress_ = "";
  /**
   * <pre>
   * Next hop IpAddress.
   * </pre>
   *
   * <code>string next_hop_address = 20;</code>
   * @return The nextHopAddress.
   */
  @java.lang.Override
  public java.lang.String getNextHopAddress() {
    java.lang.Object ref = nextHopAddress_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      nextHopAddress_ = s;
      return s;
    }
  }
  /**
   * <pre>
   * Next hop IpAddress.
   * </pre>
   *
   * <code>string next_hop_address = 20;</code>
   * @return The bytes for nextHopAddress.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
  getNextHopAddressBytes() {
    java.lang.Object ref = nextHopAddress_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b =
              com.google.protobuf.ByteString.copyFromUtf8(
                      (java.lang.String) ref);
      nextHopAddress_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int NEXT_HOP_HOSTNAME_FIELD_NUMBER = 21;
  @SuppressWarnings("serial")
  private volatile java.lang.Object nextHopHostname_ = "";
  /**
   * <pre>
   * Next hop hostname.
   * </pre>
   *
   * <code>string next_hop_hostname = 21;</code>
   * @return The nextHopHostname.
   */
  @java.lang.Override
  public java.lang.String getNextHopHostname() {
    java.lang.Object ref = nextHopHostname_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      nextHopHostname_ = s;
      return s;
    }
  }
  /**
   * <pre>
   * Next hop hostname.
   * </pre>
   *
   * <code>string next_hop_hostname = 21;</code>
   * @return The bytes for nextHopHostname.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
  getNextHopHostnameBytes() {
    java.lang.Object ref = nextHopHostname_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b =
              com.google.protobuf.ByteString.copyFromUtf8(
                      (java.lang.String) ref);
      nextHopHostname_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int PROTOCOL_FIELD_NUMBER = 22;
  private com.google.protobuf.UInt32Value protocol_;
  /**
   * <pre>
   * IP protocol number i.e 6 for TCP, 17 for UDP
   * </pre>
   *
   * <code>.google.protobuf.UInt32Value protocol = 22;</code>
   * @return Whether the protocol field is set.
   */
  @java.lang.Override
  public boolean hasProtocol() {
    return ((bitField0_ & 0x00008000) != 0);
  }
  /**
   * <pre>
   * IP protocol number i.e 6 for TCP, 17 for UDP
   * </pre>
   *
   * <code>.google.protobuf.UInt32Value protocol = 22;</code>
   * @return The protocol.
   */
  @java.lang.Override
  public com.google.protobuf.UInt32Value getProtocol() {
    return protocol_ == null ? com.google.protobuf.UInt32Value.getDefaultInstance() : protocol_;
  }
  /**
   * <pre>
   * IP protocol number i.e 6 for TCP, 17 for UDP
   * </pre>
   *
   * <code>.google.protobuf.UInt32Value protocol = 22;</code>
   */
  @java.lang.Override
  public com.google.protobuf.UInt32ValueOrBuilder getProtocolOrBuilder() {
    return protocol_ == null ? com.google.protobuf.UInt32Value.getDefaultInstance() : protocol_;
  }

  public static final int SAMPLING_ALGORITHM_FIELD_NUMBER = 23;
  private int samplingAlgorithm_ = 0;
  /**
   * <pre>
   * Sampling algorithm ID.
   * </pre>
   *
   * <code>.SamplingAlgorithm sampling_algorithm = 23;</code>
   * @return The enum numeric value on the wire for samplingAlgorithm.
   */
  @java.lang.Override public int getSamplingAlgorithmValue() {
    return samplingAlgorithm_;
  }
  /**
   * <pre>
   * Sampling algorithm ID.
   * </pre>
   *
   * <code>.SamplingAlgorithm sampling_algorithm = 23;</code>
   * @return The samplingAlgorithm.
   */
  @java.lang.Override public org.opennms.netmgt.telemetry.protocols.netflow.transport.SamplingAlgorithm getSamplingAlgorithm() {
    org.opennms.netmgt.telemetry.protocols.netflow.transport.SamplingAlgorithm result = org.opennms.netmgt.telemetry.protocols.netflow.transport.SamplingAlgorithm.forNumber(samplingAlgorithm_);
    return result == null ? org.opennms.netmgt.telemetry.protocols.netflow.transport.SamplingAlgorithm.UNRECOGNIZED : result;
  }

  public static final int SAMPLING_INTERVAL_FIELD_NUMBER = 24;
  private com.google.protobuf.DoubleValue samplingInterval_;
  /**
   * <pre>
   * Sampling interval.
   * </pre>
   *
   * <code>.google.protobuf.DoubleValue sampling_interval = 24;</code>
   * @return Whether the samplingInterval field is set.
   */
  @java.lang.Override
  public boolean hasSamplingInterval() {
    return ((bitField0_ & 0x00010000) != 0);
  }
  /**
   * <pre>
   * Sampling interval.
   * </pre>
   *
   * <code>.google.protobuf.DoubleValue sampling_interval = 24;</code>
   * @return The samplingInterval.
   */
  @java.lang.Override
  public com.google.protobuf.DoubleValue getSamplingInterval() {
    return samplingInterval_ == null ? com.google.protobuf.DoubleValue.getDefaultInstance() : samplingInterval_;
  }
  /**
   * <pre>
   * Sampling interval.
   * </pre>
   *
   * <code>.google.protobuf.DoubleValue sampling_interval = 24;</code>
   */
  @java.lang.Override
  public com.google.protobuf.DoubleValueOrBuilder getSamplingIntervalOrBuilder() {
    return samplingInterval_ == null ? com.google.protobuf.DoubleValue.getDefaultInstance() : samplingInterval_;
  }

  public static final int SRC_ADDRESS_FIELD_NUMBER = 26;
  @SuppressWarnings("serial")
  private volatile java.lang.Object srcAddress_ = "";
  /**
   * <pre>
   * Source address.
   * </pre>
   *
   * <code>string src_address = 26;</code>
   * @return The srcAddress.
   */
  @java.lang.Override
  public java.lang.String getSrcAddress() {
    java.lang.Object ref = srcAddress_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      srcAddress_ = s;
      return s;
    }
  }
  /**
   * <pre>
   * Source address.
   * </pre>
   *
   * <code>string src_address = 26;</code>
   * @return The bytes for srcAddress.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
  getSrcAddressBytes() {
    java.lang.Object ref = srcAddress_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b =
              com.google.protobuf.ByteString.copyFromUtf8(
                      (java.lang.String) ref);
      srcAddress_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int SRC_HOSTNAME_FIELD_NUMBER = 27;
  @SuppressWarnings("serial")
  private volatile java.lang.Object srcHostname_ = "";
  /**
   * <pre>
   * Source hostname.
   * </pre>
   *
   * <code>string src_hostname = 27;</code>
   * @return The srcHostname.
   */
  @java.lang.Override
  public java.lang.String getSrcHostname() {
    java.lang.Object ref = srcHostname_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      srcHostname_ = s;
      return s;
    }
  }
  /**
   * <pre>
   * Source hostname.
   * </pre>
   *
   * <code>string src_hostname = 27;</code>
   * @return The bytes for srcHostname.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
  getSrcHostnameBytes() {
    java.lang.Object ref = srcHostname_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b =
              com.google.protobuf.ByteString.copyFromUtf8(
                      (java.lang.String) ref);
      srcHostname_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int SRC_AS_FIELD_NUMBER = 28;
  private com.google.protobuf.UInt64Value srcAs_;
  /**
   * <pre>
   * Source AS number.
   * </pre>
   *
   * <code>.google.protobuf.UInt64Value src_as = 28;</code>
   * @return Whether the srcAs field is set.
   */
  @java.lang.Override
  public boolean hasSrcAs() {
    return ((bitField0_ & 0x00020000) != 0);
  }
  /**
   * <pre>
   * Source AS number.
   * </pre>
   *
   * <code>.google.protobuf.UInt64Value src_as = 28;</code>
   * @return The srcAs.
   */
  @java.lang.Override
  public com.google.protobuf.UInt64Value getSrcAs() {
    return srcAs_ == null ? com.google.protobuf.UInt64Value.getDefaultInstance() : srcAs_;
  }
  /**
   * <pre>
   * Source AS number.
   * </pre>
   *
   * <code>.google.protobuf.UInt64Value src_as = 28;</code>
   */
  @java.lang.Override
  public com.google.protobuf.UInt64ValueOrBuilder getSrcAsOrBuilder() {
    return srcAs_ == null ? com.google.protobuf.UInt64Value.getDefaultInstance() : srcAs_;
  }

  public static final int SRC_MASK_LEN_FIELD_NUMBER = 29;
  private com.google.protobuf.UInt32Value srcMaskLen_;
  /**
   * <pre>
   * The number of contiguous bits in the destination address subnet mask.
   * </pre>
   *
   * <code>.google.protobuf.UInt32Value src_mask_len = 29;</code>
   * @return Whether the srcMaskLen field is set.
   */
  @java.lang.Override
  public boolean hasSrcMaskLen() {
    return ((bitField0_ & 0x00040000) != 0);
  }
  /**
   * <pre>
   * The number of contiguous bits in the destination address subnet mask.
   * </pre>
   *
   * <code>.google.protobuf.UInt32Value src_mask_len = 29;</code>
   * @return The srcMaskLen.
   */
  @java.lang.Override
  public com.google.protobuf.UInt32Value getSrcMaskLen() {
    return srcMaskLen_ == null ? com.google.protobuf.UInt32Value.getDefaultInstance() : srcMaskLen_;
  }
  /**
   * <pre>
   * The number of contiguous bits in the destination address subnet mask.
   * </pre>
   *
   * <code>.google.protobuf.UInt32Value src_mask_len = 29;</code>
   */
  @java.lang.Override
  public com.google.protobuf.UInt32ValueOrBuilder getSrcMaskLenOrBuilder() {
    return srcMaskLen_ == null ? com.google.protobuf.UInt32Value.getDefaultInstance() : srcMaskLen_;
  }

  public static final int SRC_PORT_FIELD_NUMBER = 30;
  private com.google.protobuf.UInt32Value srcPort_;
  /**
   * <pre>
   * Source port.
   * </pre>
   *
   * <code>.google.protobuf.UInt32Value src_port = 30;</code>
   * @return Whether the srcPort field is set.
   */
  @java.lang.Override
  public boolean hasSrcPort() {
    return ((bitField0_ & 0x00080000) != 0);
  }
  /**
   * <pre>
   * Source port.
   * </pre>
   *
   * <code>.google.protobuf.UInt32Value src_port = 30;</code>
   * @return The srcPort.
   */
  @java.lang.Override
  public com.google.protobuf.UInt32Value getSrcPort() {
    return srcPort_ == null ? com.google.protobuf.UInt32Value.getDefaultInstance() : srcPort_;
  }
  /**
   * <pre>
   * Source port.
   * </pre>
   *
   * <code>.google.protobuf.UInt32Value src_port = 30;</code>
   */
  @java.lang.Override
  public com.google.protobuf.UInt32ValueOrBuilder getSrcPortOrBuilder() {
    return srcPort_ == null ? com.google.protobuf.UInt32Value.getDefaultInstance() : srcPort_;
  }

  public static final int TCP_FLAGS_FIELD_NUMBER = 31;
  private com.google.protobuf.UInt32Value tcpFlags_;
  /**
   * <pre>
   * TCP Flags.
   * </pre>
   *
   * <code>.google.protobuf.UInt32Value tcp_flags = 31;</code>
   * @return Whether the tcpFlags field is set.
   */
  @java.lang.Override
  public boolean hasTcpFlags() {
    return ((bitField0_ & 0x00100000) != 0);
  }
  /**
   * <pre>
   * TCP Flags.
   * </pre>
   *
   * <code>.google.protobuf.UInt32Value tcp_flags = 31;</code>
   * @return The tcpFlags.
   */
  @java.lang.Override
  public com.google.protobuf.UInt32Value getTcpFlags() {
    return tcpFlags_ == null ? com.google.protobuf.UInt32Value.getDefaultInstance() : tcpFlags_;
  }
  /**
   * <pre>
   * TCP Flags.
   * </pre>
   *
   * <code>.google.protobuf.UInt32Value tcp_flags = 31;</code>
   */
  @java.lang.Override
  public com.google.protobuf.UInt32ValueOrBuilder getTcpFlagsOrBuilder() {
    return tcpFlags_ == null ? com.google.protobuf.UInt32Value.getDefaultInstance() : tcpFlags_;
  }

  public static final int TOS_FIELD_NUMBER = 32;
  private com.google.protobuf.UInt32Value tos_;
  /**
   * <pre>
   * TOS
   * </pre>
   *
   * <code>.google.protobuf.UInt32Value tos = 32;</code>
   * @return Whether the tos field is set.
   */
  @java.lang.Override
  public boolean hasTos() {
    return ((bitField0_ & 0x00200000) != 0);
  }
  /**
   * <pre>
   * TOS
   * </pre>
   *
   * <code>.google.protobuf.UInt32Value tos = 32;</code>
   * @return The tos.
   */
  @java.lang.Override
  public com.google.protobuf.UInt32Value getTos() {
    return tos_ == null ? com.google.protobuf.UInt32Value.getDefaultInstance() : tos_;
  }
  /**
   * <pre>
   * TOS
   * </pre>
   *
   * <code>.google.protobuf.UInt32Value tos = 32;</code>
   */
  @java.lang.Override
  public com.google.protobuf.UInt32ValueOrBuilder getTosOrBuilder() {
    return tos_ == null ? com.google.protobuf.UInt32Value.getDefaultInstance() : tos_;
  }

  public static final int NETFLOW_VERSION_FIELD_NUMBER = 33;
  private int netflowVersion_ = 0;
  /**
   * <pre>
   * Netflow version
   * </pre>
   *
   * <code>.NetflowVersion netflow_version = 33;</code>
   * @return The enum numeric value on the wire for netflowVersion.
   */
  @java.lang.Override public int getNetflowVersionValue() {
    return netflowVersion_;
  }
  /**
   * <pre>
   * Netflow version
   * </pre>
   *
   * <code>.NetflowVersion netflow_version = 33;</code>
   * @return The netflowVersion.
   */
  @java.lang.Override public org.opennms.netmgt.telemetry.protocols.netflow.transport.NetflowVersion getNetflowVersion() {
    org.opennms.netmgt.telemetry.protocols.netflow.transport.NetflowVersion result = org.opennms.netmgt.telemetry.protocols.netflow.transport.NetflowVersion.forNumber(netflowVersion_);
    return result == null ? org.opennms.netmgt.telemetry.protocols.netflow.transport.NetflowVersion.UNRECOGNIZED : result;
  }

  public static final int VLAN_FIELD_NUMBER = 34;
  private com.google.protobuf.UInt32Value vlan_;
  /**
   * <pre>
   * VLAN ID.
   * </pre>
   *
   * <code>.google.protobuf.UInt32Value vlan = 34;</code>
   * @return Whether the vlan field is set.
   */
  @java.lang.Override
  public boolean hasVlan() {
    return ((bitField0_ & 0x00400000) != 0);
  }
  /**
   * <pre>
   * VLAN ID.
   * </pre>
   *
   * <code>.google.protobuf.UInt32Value vlan = 34;</code>
   * @return The vlan.
   */
  @java.lang.Override
  public com.google.protobuf.UInt32Value getVlan() {
    return vlan_ == null ? com.google.protobuf.UInt32Value.getDefaultInstance() : vlan_;
  }
  /**
   * <pre>
   * VLAN ID.
   * </pre>
   *
   * <code>.google.protobuf.UInt32Value vlan = 34;</code>
   */
  @java.lang.Override
  public com.google.protobuf.UInt32ValueOrBuilder getVlanOrBuilder() {
    return vlan_ == null ? com.google.protobuf.UInt32Value.getDefaultInstance() : vlan_;
  }

  public static final int NODE_IDENTIFIER_FIELD_NUMBER = 35;
  @SuppressWarnings("serial")
  private volatile java.lang.Object nodeIdentifier_ = "";
  /**
   * <pre>
   * node lookup identifier.
   * </pre>
   *
   * <code>string node_identifier = 35;</code>
   * @return The nodeIdentifier.
   */
  @java.lang.Override
  public java.lang.String getNodeIdentifier() {
    java.lang.Object ref = nodeIdentifier_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      nodeIdentifier_ = s;
      return s;
    }
  }
  /**
   * <pre>
   * node lookup identifier.
   * </pre>
   *
   * <code>string node_identifier = 35;</code>
   * @return The bytes for nodeIdentifier.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
  getNodeIdentifierBytes() {
    java.lang.Object ref = nodeIdentifier_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b =
              com.google.protobuf.ByteString.copyFromUtf8(
                      (java.lang.String) ref);
      nodeIdentifier_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
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
    if (((bitField0_ & 0x00000001) != 0)) {
      output.writeMessage(2, getNumBytes());
    }
    if (direction_ != org.opennms.netmgt.telemetry.protocols.netflow.transport.Direction.INGRESS.getNumber()) {
      output.writeEnum(3, direction_);
    }
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(dstAddress_)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 4, dstAddress_);
    }
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(dstHostname_)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 5, dstHostname_);
    }
    if (((bitField0_ & 0x00000002) != 0)) {
      output.writeMessage(6, getDstAs());
    }
    if (((bitField0_ & 0x00000004) != 0)) {
      output.writeMessage(7, getDstMaskLen());
    }
    if (((bitField0_ & 0x00000008) != 0)) {
      output.writeMessage(8, getDstPort());
    }
    if (((bitField0_ & 0x00000010) != 0)) {
      output.writeMessage(9, getEngineId());
    }
    if (((bitField0_ & 0x00000020) != 0)) {
      output.writeMessage(10, getEngineType());
    }
    if (((bitField0_ & 0x00000040) != 0)) {
      output.writeMessage(11, getDeltaSwitched());
    }
    if (((bitField0_ & 0x00000080) != 0)) {
      output.writeMessage(12, getFirstSwitched());
    }
    if (((bitField0_ & 0x00000100) != 0)) {
      output.writeMessage(13, getLastSwitched());
    }
    if (((bitField0_ & 0x00000200) != 0)) {
      output.writeMessage(14, getNumFlowRecords());
    }
    if (((bitField0_ & 0x00000400) != 0)) {
      output.writeMessage(15, getNumPackets());
    }
    if (((bitField0_ & 0x00000800) != 0)) {
      output.writeMessage(16, getFlowSeqNum());
    }
    if (((bitField0_ & 0x00001000) != 0)) {
      output.writeMessage(17, getInputSnmpIfindex());
    }
    if (((bitField0_ & 0x00002000) != 0)) {
      output.writeMessage(18, getOutputSnmpIfindex());
    }
    if (((bitField0_ & 0x00004000) != 0)) {
      output.writeMessage(19, getIpProtocolVersion());
    }
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(nextHopAddress_)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 20, nextHopAddress_);
    }
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(nextHopHostname_)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 21, nextHopHostname_);
    }
    if (((bitField0_ & 0x00008000) != 0)) {
      output.writeMessage(22, getProtocol());
    }
    if (samplingAlgorithm_ != org.opennms.netmgt.telemetry.protocols.netflow.transport.SamplingAlgorithm.UNASSIGNED.getNumber()) {
      output.writeEnum(23, samplingAlgorithm_);
    }
    if (((bitField0_ & 0x00010000) != 0)) {
      output.writeMessage(24, getSamplingInterval());
    }
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(srcAddress_)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 26, srcAddress_);
    }
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(srcHostname_)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 27, srcHostname_);
    }
    if (((bitField0_ & 0x00020000) != 0)) {
      output.writeMessage(28, getSrcAs());
    }
    if (((bitField0_ & 0x00040000) != 0)) {
      output.writeMessage(29, getSrcMaskLen());
    }
    if (((bitField0_ & 0x00080000) != 0)) {
      output.writeMessage(30, getSrcPort());
    }
    if (((bitField0_ & 0x00100000) != 0)) {
      output.writeMessage(31, getTcpFlags());
    }
    if (((bitField0_ & 0x00200000) != 0)) {
      output.writeMessage(32, getTos());
    }
    if (netflowVersion_ != org.opennms.netmgt.telemetry.protocols.netflow.transport.NetflowVersion.V5.getNumber()) {
      output.writeEnum(33, netflowVersion_);
    }
    if (((bitField0_ & 0x00400000) != 0)) {
      output.writeMessage(34, getVlan());
    }
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(nodeIdentifier_)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 35, nodeIdentifier_);
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
    if (((bitField0_ & 0x00000001) != 0)) {
      size += com.google.protobuf.CodedOutputStream
              .computeMessageSize(2, getNumBytes());
    }
    if (direction_ != org.opennms.netmgt.telemetry.protocols.netflow.transport.Direction.INGRESS.getNumber()) {
      size += com.google.protobuf.CodedOutputStream
              .computeEnumSize(3, direction_);
    }
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(dstAddress_)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(4, dstAddress_);
    }
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(dstHostname_)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(5, dstHostname_);
    }
    if (((bitField0_ & 0x00000002) != 0)) {
      size += com.google.protobuf.CodedOutputStream
              .computeMessageSize(6, getDstAs());
    }
    if (((bitField0_ & 0x00000004) != 0)) {
      size += com.google.protobuf.CodedOutputStream
              .computeMessageSize(7, getDstMaskLen());
    }
    if (((bitField0_ & 0x00000008) != 0)) {
      size += com.google.protobuf.CodedOutputStream
              .computeMessageSize(8, getDstPort());
    }
    if (((bitField0_ & 0x00000010) != 0)) {
      size += com.google.protobuf.CodedOutputStream
              .computeMessageSize(9, getEngineId());
    }
    if (((bitField0_ & 0x00000020) != 0)) {
      size += com.google.protobuf.CodedOutputStream
              .computeMessageSize(10, getEngineType());
    }
    if (((bitField0_ & 0x00000040) != 0)) {
      size += com.google.protobuf.CodedOutputStream
              .computeMessageSize(11, getDeltaSwitched());
    }
    if (((bitField0_ & 0x00000080) != 0)) {
      size += com.google.protobuf.CodedOutputStream
              .computeMessageSize(12, getFirstSwitched());
    }
    if (((bitField0_ & 0x00000100) != 0)) {
      size += com.google.protobuf.CodedOutputStream
              .computeMessageSize(13, getLastSwitched());
    }
    if (((bitField0_ & 0x00000200) != 0)) {
      size += com.google.protobuf.CodedOutputStream
              .computeMessageSize(14, getNumFlowRecords());
    }
    if (((bitField0_ & 0x00000400) != 0)) {
      size += com.google.protobuf.CodedOutputStream
              .computeMessageSize(15, getNumPackets());
    }
    if (((bitField0_ & 0x00000800) != 0)) {
      size += com.google.protobuf.CodedOutputStream
              .computeMessageSize(16, getFlowSeqNum());
    }
    if (((bitField0_ & 0x00001000) != 0)) {
      size += com.google.protobuf.CodedOutputStream
              .computeMessageSize(17, getInputSnmpIfindex());
    }
    if (((bitField0_ & 0x00002000) != 0)) {
      size += com.google.protobuf.CodedOutputStream
              .computeMessageSize(18, getOutputSnmpIfindex());
    }
    if (((bitField0_ & 0x00004000) != 0)) {
      size += com.google.protobuf.CodedOutputStream
              .computeMessageSize(19, getIpProtocolVersion());
    }
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(nextHopAddress_)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(20, nextHopAddress_);
    }
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(nextHopHostname_)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(21, nextHopHostname_);
    }
    if (((bitField0_ & 0x00008000) != 0)) {
      size += com.google.protobuf.CodedOutputStream
              .computeMessageSize(22, getProtocol());
    }
    if (samplingAlgorithm_ != org.opennms.netmgt.telemetry.protocols.netflow.transport.SamplingAlgorithm.UNASSIGNED.getNumber()) {
      size += com.google.protobuf.CodedOutputStream
              .computeEnumSize(23, samplingAlgorithm_);
    }
    if (((bitField0_ & 0x00010000) != 0)) {
      size += com.google.protobuf.CodedOutputStream
              .computeMessageSize(24, getSamplingInterval());
    }
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(srcAddress_)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(26, srcAddress_);
    }
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(srcHostname_)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(27, srcHostname_);
    }
    if (((bitField0_ & 0x00020000) != 0)) {
      size += com.google.protobuf.CodedOutputStream
              .computeMessageSize(28, getSrcAs());
    }
    if (((bitField0_ & 0x00040000) != 0)) {
      size += com.google.protobuf.CodedOutputStream
              .computeMessageSize(29, getSrcMaskLen());
    }
    if (((bitField0_ & 0x00080000) != 0)) {
      size += com.google.protobuf.CodedOutputStream
              .computeMessageSize(30, getSrcPort());
    }
    if (((bitField0_ & 0x00100000) != 0)) {
      size += com.google.protobuf.CodedOutputStream
              .computeMessageSize(31, getTcpFlags());
    }
    if (((bitField0_ & 0x00200000) != 0)) {
      size += com.google.protobuf.CodedOutputStream
              .computeMessageSize(32, getTos());
    }
    if (netflowVersion_ != org.opennms.netmgt.telemetry.protocols.netflow.transport.NetflowVersion.V5.getNumber()) {
      size += com.google.protobuf.CodedOutputStream
              .computeEnumSize(33, netflowVersion_);
    }
    if (((bitField0_ & 0x00400000) != 0)) {
      size += com.google.protobuf.CodedOutputStream
              .computeMessageSize(34, getVlan());
    }
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(nodeIdentifier_)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(35, nodeIdentifier_);
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
    if (!(obj instanceof org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage)) {
      return super.equals(obj);
    }
    org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage other = (org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage) obj;

    if (getTimestamp()
            != other.getTimestamp()) return false;
    if (hasNumBytes() != other.hasNumBytes()) return false;
    if (hasNumBytes()) {
      if (!getNumBytes()
              .equals(other.getNumBytes())) return false;
    }
    if (direction_ != other.direction_) return false;
    if (!getDstAddress()
            .equals(other.getDstAddress())) return false;
    if (!getDstHostname()
            .equals(other.getDstHostname())) return false;
    if (hasDstAs() != other.hasDstAs()) return false;
    if (hasDstAs()) {
      if (!getDstAs()
              .equals(other.getDstAs())) return false;
    }
    if (hasDstMaskLen() != other.hasDstMaskLen()) return false;
    if (hasDstMaskLen()) {
      if (!getDstMaskLen()
              .equals(other.getDstMaskLen())) return false;
    }
    if (hasDstPort() != other.hasDstPort()) return false;
    if (hasDstPort()) {
      if (!getDstPort()
              .equals(other.getDstPort())) return false;
    }
    if (hasEngineId() != other.hasEngineId()) return false;
    if (hasEngineId()) {
      if (!getEngineId()
              .equals(other.getEngineId())) return false;
    }
    if (hasEngineType() != other.hasEngineType()) return false;
    if (hasEngineType()) {
      if (!getEngineType()
              .equals(other.getEngineType())) return false;
    }
    if (hasDeltaSwitched() != other.hasDeltaSwitched()) return false;
    if (hasDeltaSwitched()) {
      if (!getDeltaSwitched()
              .equals(other.getDeltaSwitched())) return false;
    }
    if (hasFirstSwitched() != other.hasFirstSwitched()) return false;
    if (hasFirstSwitched()) {
      if (!getFirstSwitched()
              .equals(other.getFirstSwitched())) return false;
    }
    if (hasLastSwitched() != other.hasLastSwitched()) return false;
    if (hasLastSwitched()) {
      if (!getLastSwitched()
              .equals(other.getLastSwitched())) return false;
    }
    if (hasNumFlowRecords() != other.hasNumFlowRecords()) return false;
    if (hasNumFlowRecords()) {
      if (!getNumFlowRecords()
              .equals(other.getNumFlowRecords())) return false;
    }
    if (hasNumPackets() != other.hasNumPackets()) return false;
    if (hasNumPackets()) {
      if (!getNumPackets()
              .equals(other.getNumPackets())) return false;
    }
    if (hasFlowSeqNum() != other.hasFlowSeqNum()) return false;
    if (hasFlowSeqNum()) {
      if (!getFlowSeqNum()
              .equals(other.getFlowSeqNum())) return false;
    }
    if (hasInputSnmpIfindex() != other.hasInputSnmpIfindex()) return false;
    if (hasInputSnmpIfindex()) {
      if (!getInputSnmpIfindex()
              .equals(other.getInputSnmpIfindex())) return false;
    }
    if (hasOutputSnmpIfindex() != other.hasOutputSnmpIfindex()) return false;
    if (hasOutputSnmpIfindex()) {
      if (!getOutputSnmpIfindex()
              .equals(other.getOutputSnmpIfindex())) return false;
    }
    if (hasIpProtocolVersion() != other.hasIpProtocolVersion()) return false;
    if (hasIpProtocolVersion()) {
      if (!getIpProtocolVersion()
              .equals(other.getIpProtocolVersion())) return false;
    }
    if (!getNextHopAddress()
            .equals(other.getNextHopAddress())) return false;
    if (!getNextHopHostname()
            .equals(other.getNextHopHostname())) return false;
    if (hasProtocol() != other.hasProtocol()) return false;
    if (hasProtocol()) {
      if (!getProtocol()
              .equals(other.getProtocol())) return false;
    }
    if (samplingAlgorithm_ != other.samplingAlgorithm_) return false;
    if (hasSamplingInterval() != other.hasSamplingInterval()) return false;
    if (hasSamplingInterval()) {
      if (!getSamplingInterval()
              .equals(other.getSamplingInterval())) return false;
    }
    if (!getSrcAddress()
            .equals(other.getSrcAddress())) return false;
    if (!getSrcHostname()
            .equals(other.getSrcHostname())) return false;
    if (hasSrcAs() != other.hasSrcAs()) return false;
    if (hasSrcAs()) {
      if (!getSrcAs()
              .equals(other.getSrcAs())) return false;
    }
    if (hasSrcMaskLen() != other.hasSrcMaskLen()) return false;
    if (hasSrcMaskLen()) {
      if (!getSrcMaskLen()
              .equals(other.getSrcMaskLen())) return false;
    }
    if (hasSrcPort() != other.hasSrcPort()) return false;
    if (hasSrcPort()) {
      if (!getSrcPort()
              .equals(other.getSrcPort())) return false;
    }
    if (hasTcpFlags() != other.hasTcpFlags()) return false;
    if (hasTcpFlags()) {
      if (!getTcpFlags()
              .equals(other.getTcpFlags())) return false;
    }
    if (hasTos() != other.hasTos()) return false;
    if (hasTos()) {
      if (!getTos()
              .equals(other.getTos())) return false;
    }
    if (netflowVersion_ != other.netflowVersion_) return false;
    if (hasVlan() != other.hasVlan()) return false;
    if (hasVlan()) {
      if (!getVlan()
              .equals(other.getVlan())) return false;
    }
    if (!getNodeIdentifier()
            .equals(other.getNodeIdentifier())) return false;
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
    if (hasNumBytes()) {
      hash = (37 * hash) + NUM_BYTES_FIELD_NUMBER;
      hash = (53 * hash) + getNumBytes().hashCode();
    }
    hash = (37 * hash) + DIRECTION_FIELD_NUMBER;
    hash = (53 * hash) + direction_;
    hash = (37 * hash) + DST_ADDRESS_FIELD_NUMBER;
    hash = (53 * hash) + getDstAddress().hashCode();
    hash = (37 * hash) + DST_HOSTNAME_FIELD_NUMBER;
    hash = (53 * hash) + getDstHostname().hashCode();
    if (hasDstAs()) {
      hash = (37 * hash) + DST_AS_FIELD_NUMBER;
      hash = (53 * hash) + getDstAs().hashCode();
    }
    if (hasDstMaskLen()) {
      hash = (37 * hash) + DST_MASK_LEN_FIELD_NUMBER;
      hash = (53 * hash) + getDstMaskLen().hashCode();
    }
    if (hasDstPort()) {
      hash = (37 * hash) + DST_PORT_FIELD_NUMBER;
      hash = (53 * hash) + getDstPort().hashCode();
    }
    if (hasEngineId()) {
      hash = (37 * hash) + ENGINE_ID_FIELD_NUMBER;
      hash = (53 * hash) + getEngineId().hashCode();
    }
    if (hasEngineType()) {
      hash = (37 * hash) + ENGINE_TYPE_FIELD_NUMBER;
      hash = (53 * hash) + getEngineType().hashCode();
    }
    if (hasDeltaSwitched()) {
      hash = (37 * hash) + DELTA_SWITCHED_FIELD_NUMBER;
      hash = (53 * hash) + getDeltaSwitched().hashCode();
    }
    if (hasFirstSwitched()) {
      hash = (37 * hash) + FIRST_SWITCHED_FIELD_NUMBER;
      hash = (53 * hash) + getFirstSwitched().hashCode();
    }
    if (hasLastSwitched()) {
      hash = (37 * hash) + LAST_SWITCHED_FIELD_NUMBER;
      hash = (53 * hash) + getLastSwitched().hashCode();
    }
    if (hasNumFlowRecords()) {
      hash = (37 * hash) + NUM_FLOW_RECORDS_FIELD_NUMBER;
      hash = (53 * hash) + getNumFlowRecords().hashCode();
    }
    if (hasNumPackets()) {
      hash = (37 * hash) + NUM_PACKETS_FIELD_NUMBER;
      hash = (53 * hash) + getNumPackets().hashCode();
    }
    if (hasFlowSeqNum()) {
      hash = (37 * hash) + FLOW_SEQ_NUM_FIELD_NUMBER;
      hash = (53 * hash) + getFlowSeqNum().hashCode();
    }
    if (hasInputSnmpIfindex()) {
      hash = (37 * hash) + INPUT_SNMP_IFINDEX_FIELD_NUMBER;
      hash = (53 * hash) + getInputSnmpIfindex().hashCode();
    }
    if (hasOutputSnmpIfindex()) {
      hash = (37 * hash) + OUTPUT_SNMP_IFINDEX_FIELD_NUMBER;
      hash = (53 * hash) + getOutputSnmpIfindex().hashCode();
    }
    if (hasIpProtocolVersion()) {
      hash = (37 * hash) + IP_PROTOCOL_VERSION_FIELD_NUMBER;
      hash = (53 * hash) + getIpProtocolVersion().hashCode();
    }
    hash = (37 * hash) + NEXT_HOP_ADDRESS_FIELD_NUMBER;
    hash = (53 * hash) + getNextHopAddress().hashCode();
    hash = (37 * hash) + NEXT_HOP_HOSTNAME_FIELD_NUMBER;
    hash = (53 * hash) + getNextHopHostname().hashCode();
    if (hasProtocol()) {
      hash = (37 * hash) + PROTOCOL_FIELD_NUMBER;
      hash = (53 * hash) + getProtocol().hashCode();
    }
    hash = (37 * hash) + SAMPLING_ALGORITHM_FIELD_NUMBER;
    hash = (53 * hash) + samplingAlgorithm_;
    if (hasSamplingInterval()) {
      hash = (37 * hash) + SAMPLING_INTERVAL_FIELD_NUMBER;
      hash = (53 * hash) + getSamplingInterval().hashCode();
    }
    hash = (37 * hash) + SRC_ADDRESS_FIELD_NUMBER;
    hash = (53 * hash) + getSrcAddress().hashCode();
    hash = (37 * hash) + SRC_HOSTNAME_FIELD_NUMBER;
    hash = (53 * hash) + getSrcHostname().hashCode();
    if (hasSrcAs()) {
      hash = (37 * hash) + SRC_AS_FIELD_NUMBER;
      hash = (53 * hash) + getSrcAs().hashCode();
    }
    if (hasSrcMaskLen()) {
      hash = (37 * hash) + SRC_MASK_LEN_FIELD_NUMBER;
      hash = (53 * hash) + getSrcMaskLen().hashCode();
    }
    if (hasSrcPort()) {
      hash = (37 * hash) + SRC_PORT_FIELD_NUMBER;
      hash = (53 * hash) + getSrcPort().hashCode();
    }
    if (hasTcpFlags()) {
      hash = (37 * hash) + TCP_FLAGS_FIELD_NUMBER;
      hash = (53 * hash) + getTcpFlags().hashCode();
    }
    if (hasTos()) {
      hash = (37 * hash) + TOS_FIELD_NUMBER;
      hash = (53 * hash) + getTos().hashCode();
    }
    hash = (37 * hash) + NETFLOW_VERSION_FIELD_NUMBER;
    hash = (53 * hash) + netflowVersion_;
    if (hasVlan()) {
      hash = (37 * hash) + VLAN_FIELD_NUMBER;
      hash = (53 * hash) + getVlan().hashCode();
    }
    hash = (37 * hash) + NODE_IDENTIFIER_FIELD_NUMBER;
    hash = (53 * hash) + getNodeIdentifier().hashCode();
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage parseFrom(
          java.nio.ByteBuffer data)
          throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage parseFrom(
          java.nio.ByteBuffer data,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage parseFrom(
          com.google.protobuf.ByteString data)
          throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage parseFrom(
          com.google.protobuf.ByteString data,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage parseFrom(byte[] data)
          throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage parseFrom(
          byte[] data,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage parseFrom(java.io.InputStream input)
          throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
            .parseWithIOException(PARSER, input);
  }
  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage parseFrom(
          java.io.InputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
            .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage parseDelimitedFrom(java.io.InputStream input)
          throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
            .parseDelimitedWithIOException(PARSER, input);
  }

  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage parseDelimitedFrom(
          java.io.InputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
            .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage parseFrom(
          com.google.protobuf.CodedInputStream input)
          throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
            .parseWithIOException(PARSER, input);
  }
  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage parseFrom(
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
  public static Builder newBuilder(org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage prototype) {
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
   * Protobuf type {@code FlowMessage}
   */
  public static final class Builder extends
          com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
          // @@protoc_insertion_point(builder_implements:FlowMessage)
          org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessageOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
    getDescriptor() {
      return org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowProtos.internal_static_FlowMessage_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
    internalGetFieldAccessorTable() {
      return org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowProtos.internal_static_FlowMessage_fieldAccessorTable
              .ensureFieldAccessorsInitialized(
                      org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage.class, org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage.Builder.class);
    }

    // Construct using org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage.newBuilder()
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
        getNumBytesFieldBuilder();
        getDstAsFieldBuilder();
        getDstMaskLenFieldBuilder();
        getDstPortFieldBuilder();
        getEngineIdFieldBuilder();
        getEngineTypeFieldBuilder();
        getDeltaSwitchedFieldBuilder();
        getFirstSwitchedFieldBuilder();
        getLastSwitchedFieldBuilder();
        getNumFlowRecordsFieldBuilder();
        getNumPacketsFieldBuilder();
        getFlowSeqNumFieldBuilder();
        getInputSnmpIfindexFieldBuilder();
        getOutputSnmpIfindexFieldBuilder();
        getIpProtocolVersionFieldBuilder();
        getProtocolFieldBuilder();
        getSamplingIntervalFieldBuilder();
        getSrcAsFieldBuilder();
        getSrcMaskLenFieldBuilder();
        getSrcPortFieldBuilder();
        getTcpFlagsFieldBuilder();
        getTosFieldBuilder();
        getVlanFieldBuilder();
      }
    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      bitField0_ = 0;
      bitField1_ = 0;
      timestamp_ = 0L;
      numBytes_ = null;
      if (numBytesBuilder_ != null) {
        numBytesBuilder_.dispose();
        numBytesBuilder_ = null;
      }
      direction_ = 0;
      dstAddress_ = "";
      dstHostname_ = "";
      dstAs_ = null;
      if (dstAsBuilder_ != null) {
        dstAsBuilder_.dispose();
        dstAsBuilder_ = null;
      }
      dstMaskLen_ = null;
      if (dstMaskLenBuilder_ != null) {
        dstMaskLenBuilder_.dispose();
        dstMaskLenBuilder_ = null;
      }
      dstPort_ = null;
      if (dstPortBuilder_ != null) {
        dstPortBuilder_.dispose();
        dstPortBuilder_ = null;
      }
      engineId_ = null;
      if (engineIdBuilder_ != null) {
        engineIdBuilder_.dispose();
        engineIdBuilder_ = null;
      }
      engineType_ = null;
      if (engineTypeBuilder_ != null) {
        engineTypeBuilder_.dispose();
        engineTypeBuilder_ = null;
      }
      deltaSwitched_ = null;
      if (deltaSwitchedBuilder_ != null) {
        deltaSwitchedBuilder_.dispose();
        deltaSwitchedBuilder_ = null;
      }
      firstSwitched_ = null;
      if (firstSwitchedBuilder_ != null) {
        firstSwitchedBuilder_.dispose();
        firstSwitchedBuilder_ = null;
      }
      lastSwitched_ = null;
      if (lastSwitchedBuilder_ != null) {
        lastSwitchedBuilder_.dispose();
        lastSwitchedBuilder_ = null;
      }
      numFlowRecords_ = null;
      if (numFlowRecordsBuilder_ != null) {
        numFlowRecordsBuilder_.dispose();
        numFlowRecordsBuilder_ = null;
      }
      numPackets_ = null;
      if (numPacketsBuilder_ != null) {
        numPacketsBuilder_.dispose();
        numPacketsBuilder_ = null;
      }
      flowSeqNum_ = null;
      if (flowSeqNumBuilder_ != null) {
        flowSeqNumBuilder_.dispose();
        flowSeqNumBuilder_ = null;
      }
      inputSnmpIfindex_ = null;
      if (inputSnmpIfindexBuilder_ != null) {
        inputSnmpIfindexBuilder_.dispose();
        inputSnmpIfindexBuilder_ = null;
      }
      outputSnmpIfindex_ = null;
      if (outputSnmpIfindexBuilder_ != null) {
        outputSnmpIfindexBuilder_.dispose();
        outputSnmpIfindexBuilder_ = null;
      }
      ipProtocolVersion_ = null;
      if (ipProtocolVersionBuilder_ != null) {
        ipProtocolVersionBuilder_.dispose();
        ipProtocolVersionBuilder_ = null;
      }
      nextHopAddress_ = "";
      nextHopHostname_ = "";
      protocol_ = null;
      if (protocolBuilder_ != null) {
        protocolBuilder_.dispose();
        protocolBuilder_ = null;
      }
      samplingAlgorithm_ = 0;
      samplingInterval_ = null;
      if (samplingIntervalBuilder_ != null) {
        samplingIntervalBuilder_.dispose();
        samplingIntervalBuilder_ = null;
      }
      srcAddress_ = "";
      srcHostname_ = "";
      srcAs_ = null;
      if (srcAsBuilder_ != null) {
        srcAsBuilder_.dispose();
        srcAsBuilder_ = null;
      }
      srcMaskLen_ = null;
      if (srcMaskLenBuilder_ != null) {
        srcMaskLenBuilder_.dispose();
        srcMaskLenBuilder_ = null;
      }
      srcPort_ = null;
      if (srcPortBuilder_ != null) {
        srcPortBuilder_.dispose();
        srcPortBuilder_ = null;
      }
      tcpFlags_ = null;
      if (tcpFlagsBuilder_ != null) {
        tcpFlagsBuilder_.dispose();
        tcpFlagsBuilder_ = null;
      }
      tos_ = null;
      if (tosBuilder_ != null) {
        tosBuilder_.dispose();
        tosBuilder_ = null;
      }
      netflowVersion_ = 0;
      vlan_ = null;
      if (vlanBuilder_ != null) {
        vlanBuilder_.dispose();
        vlanBuilder_ = null;
      }
      nodeIdentifier_ = "";
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
    getDescriptorForType() {
      return org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowProtos.internal_static_FlowMessage_descriptor;
    }

    @java.lang.Override
    public org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage getDefaultInstanceForType() {
      return org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage.getDefaultInstance();
    }

    @java.lang.Override
    public org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage build() {
      org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage buildPartial() {
      org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage result = new org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      if (bitField1_ != 0) { buildPartial1(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.timestamp_ = timestamp_;
      }
      int to_bitField0_ = 0;
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.numBytes_ = numBytesBuilder_ == null
                ? numBytes_
                : numBytesBuilder_.build();
        to_bitField0_ |= 0x00000001;
      }
      if (((from_bitField0_ & 0x00000004) != 0)) {
        result.direction_ = direction_;
      }
      if (((from_bitField0_ & 0x00000008) != 0)) {
        result.dstAddress_ = dstAddress_;
      }
      if (((from_bitField0_ & 0x00000010) != 0)) {
        result.dstHostname_ = dstHostname_;
      }
      if (((from_bitField0_ & 0x00000020) != 0)) {
        result.dstAs_ = dstAsBuilder_ == null
                ? dstAs_
                : dstAsBuilder_.build();
        to_bitField0_ |= 0x00000002;
      }
      if (((from_bitField0_ & 0x00000040) != 0)) {
        result.dstMaskLen_ = dstMaskLenBuilder_ == null
                ? dstMaskLen_
                : dstMaskLenBuilder_.build();
        to_bitField0_ |= 0x00000004;
      }
      if (((from_bitField0_ & 0x00000080) != 0)) {
        result.dstPort_ = dstPortBuilder_ == null
                ? dstPort_
                : dstPortBuilder_.build();
        to_bitField0_ |= 0x00000008;
      }
      if (((from_bitField0_ & 0x00000100) != 0)) {
        result.engineId_ = engineIdBuilder_ == null
                ? engineId_
                : engineIdBuilder_.build();
        to_bitField0_ |= 0x00000010;
      }
      if (((from_bitField0_ & 0x00000200) != 0)) {
        result.engineType_ = engineTypeBuilder_ == null
                ? engineType_
                : engineTypeBuilder_.build();
        to_bitField0_ |= 0x00000020;
      }
      if (((from_bitField0_ & 0x00000400) != 0)) {
        result.deltaSwitched_ = deltaSwitchedBuilder_ == null
                ? deltaSwitched_
                : deltaSwitchedBuilder_.build();
        to_bitField0_ |= 0x00000040;
      }
      if (((from_bitField0_ & 0x00000800) != 0)) {
        result.firstSwitched_ = firstSwitchedBuilder_ == null
                ? firstSwitched_
                : firstSwitchedBuilder_.build();
        to_bitField0_ |= 0x00000080;
      }
      if (((from_bitField0_ & 0x00001000) != 0)) {
        result.lastSwitched_ = lastSwitchedBuilder_ == null
                ? lastSwitched_
                : lastSwitchedBuilder_.build();
        to_bitField0_ |= 0x00000100;
      }
      if (((from_bitField0_ & 0x00002000) != 0)) {
        result.numFlowRecords_ = numFlowRecordsBuilder_ == null
                ? numFlowRecords_
                : numFlowRecordsBuilder_.build();
        to_bitField0_ |= 0x00000200;
      }
      if (((from_bitField0_ & 0x00004000) != 0)) {
        result.numPackets_ = numPacketsBuilder_ == null
                ? numPackets_
                : numPacketsBuilder_.build();
        to_bitField0_ |= 0x00000400;
      }
      if (((from_bitField0_ & 0x00008000) != 0)) {
        result.flowSeqNum_ = flowSeqNumBuilder_ == null
                ? flowSeqNum_
                : flowSeqNumBuilder_.build();
        to_bitField0_ |= 0x00000800;
      }
      if (((from_bitField0_ & 0x00010000) != 0)) {
        result.inputSnmpIfindex_ = inputSnmpIfindexBuilder_ == null
                ? inputSnmpIfindex_
                : inputSnmpIfindexBuilder_.build();
        to_bitField0_ |= 0x00001000;
      }
      if (((from_bitField0_ & 0x00020000) != 0)) {
        result.outputSnmpIfindex_ = outputSnmpIfindexBuilder_ == null
                ? outputSnmpIfindex_
                : outputSnmpIfindexBuilder_.build();
        to_bitField0_ |= 0x00002000;
      }
      if (((from_bitField0_ & 0x00040000) != 0)) {
        result.ipProtocolVersion_ = ipProtocolVersionBuilder_ == null
                ? ipProtocolVersion_
                : ipProtocolVersionBuilder_.build();
        to_bitField0_ |= 0x00004000;
      }
      if (((from_bitField0_ & 0x00080000) != 0)) {
        result.nextHopAddress_ = nextHopAddress_;
      }
      if (((from_bitField0_ & 0x00100000) != 0)) {
        result.nextHopHostname_ = nextHopHostname_;
      }
      if (((from_bitField0_ & 0x00200000) != 0)) {
        result.protocol_ = protocolBuilder_ == null
                ? protocol_
                : protocolBuilder_.build();
        to_bitField0_ |= 0x00008000;
      }
      if (((from_bitField0_ & 0x00400000) != 0)) {
        result.samplingAlgorithm_ = samplingAlgorithm_;
      }
      if (((from_bitField0_ & 0x00800000) != 0)) {
        result.samplingInterval_ = samplingIntervalBuilder_ == null
                ? samplingInterval_
                : samplingIntervalBuilder_.build();
        to_bitField0_ |= 0x00010000;
      }
      if (((from_bitField0_ & 0x01000000) != 0)) {
        result.srcAddress_ = srcAddress_;
      }
      if (((from_bitField0_ & 0x02000000) != 0)) {
        result.srcHostname_ = srcHostname_;
      }
      if (((from_bitField0_ & 0x04000000) != 0)) {
        result.srcAs_ = srcAsBuilder_ == null
                ? srcAs_
                : srcAsBuilder_.build();
        to_bitField0_ |= 0x00020000;
      }
      if (((from_bitField0_ & 0x08000000) != 0)) {
        result.srcMaskLen_ = srcMaskLenBuilder_ == null
                ? srcMaskLen_
                : srcMaskLenBuilder_.build();
        to_bitField0_ |= 0x00040000;
      }
      if (((from_bitField0_ & 0x10000000) != 0)) {
        result.srcPort_ = srcPortBuilder_ == null
                ? srcPort_
                : srcPortBuilder_.build();
        to_bitField0_ |= 0x00080000;
      }
      if (((from_bitField0_ & 0x20000000) != 0)) {
        result.tcpFlags_ = tcpFlagsBuilder_ == null
                ? tcpFlags_
                : tcpFlagsBuilder_.build();
        to_bitField0_ |= 0x00100000;
      }
      if (((from_bitField0_ & 0x40000000) != 0)) {
        result.tos_ = tosBuilder_ == null
                ? tos_
                : tosBuilder_.build();
        to_bitField0_ |= 0x00200000;
      }
      if (((from_bitField0_ & 0x80000000) != 0)) {
        result.netflowVersion_ = netflowVersion_;
      }
      result.bitField0_ |= to_bitField0_;
    }

    private void buildPartial1(org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage result) {
      int from_bitField1_ = bitField1_;
      int to_bitField0_ = 0;
      if (((from_bitField1_ & 0x00000001) != 0)) {
        result.vlan_ = vlanBuilder_ == null
                ? vlan_
                : vlanBuilder_.build();
        to_bitField0_ |= 0x00400000;
      }
      if (((from_bitField1_ & 0x00000002) != 0)) {
        result.nodeIdentifier_ = nodeIdentifier_;
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
      if (other instanceof org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage) {
        return mergeFrom((org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage other) {
      if (other == org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage.getDefaultInstance()) return this;
      if (other.getTimestamp() != 0L) {
        setTimestamp(other.getTimestamp());
      }
      if (other.hasNumBytes()) {
        mergeNumBytes(other.getNumBytes());
      }
      if (other.direction_ != 0) {
        setDirectionValue(other.getDirectionValue());
      }
      if (!other.getDstAddress().isEmpty()) {
        dstAddress_ = other.dstAddress_;
        bitField0_ |= 0x00000008;
        onChanged();
      }
      if (!other.getDstHostname().isEmpty()) {
        dstHostname_ = other.dstHostname_;
        bitField0_ |= 0x00000010;
        onChanged();
      }
      if (other.hasDstAs()) {
        mergeDstAs(other.getDstAs());
      }
      if (other.hasDstMaskLen()) {
        mergeDstMaskLen(other.getDstMaskLen());
      }
      if (other.hasDstPort()) {
        mergeDstPort(other.getDstPort());
      }
      if (other.hasEngineId()) {
        mergeEngineId(other.getEngineId());
      }
      if (other.hasEngineType()) {
        mergeEngineType(other.getEngineType());
      }
      if (other.hasDeltaSwitched()) {
        mergeDeltaSwitched(other.getDeltaSwitched());
      }
      if (other.hasFirstSwitched()) {
        mergeFirstSwitched(other.getFirstSwitched());
      }
      if (other.hasLastSwitched()) {
        mergeLastSwitched(other.getLastSwitched());
      }
      if (other.hasNumFlowRecords()) {
        mergeNumFlowRecords(other.getNumFlowRecords());
      }
      if (other.hasNumPackets()) {
        mergeNumPackets(other.getNumPackets());
      }
      if (other.hasFlowSeqNum()) {
        mergeFlowSeqNum(other.getFlowSeqNum());
      }
      if (other.hasInputSnmpIfindex()) {
        mergeInputSnmpIfindex(other.getInputSnmpIfindex());
      }
      if (other.hasOutputSnmpIfindex()) {
        mergeOutputSnmpIfindex(other.getOutputSnmpIfindex());
      }
      if (other.hasIpProtocolVersion()) {
        mergeIpProtocolVersion(other.getIpProtocolVersion());
      }
      if (!other.getNextHopAddress().isEmpty()) {
        nextHopAddress_ = other.nextHopAddress_;
        bitField0_ |= 0x00080000;
        onChanged();
      }
      if (!other.getNextHopHostname().isEmpty()) {
        nextHopHostname_ = other.nextHopHostname_;
        bitField0_ |= 0x00100000;
        onChanged();
      }
      if (other.hasProtocol()) {
        mergeProtocol(other.getProtocol());
      }
      if (other.samplingAlgorithm_ != 0) {
        setSamplingAlgorithmValue(other.getSamplingAlgorithmValue());
      }
      if (other.hasSamplingInterval()) {
        mergeSamplingInterval(other.getSamplingInterval());
      }
      if (!other.getSrcAddress().isEmpty()) {
        srcAddress_ = other.srcAddress_;
        bitField0_ |= 0x01000000;
        onChanged();
      }
      if (!other.getSrcHostname().isEmpty()) {
        srcHostname_ = other.srcHostname_;
        bitField0_ |= 0x02000000;
        onChanged();
      }
      if (other.hasSrcAs()) {
        mergeSrcAs(other.getSrcAs());
      }
      if (other.hasSrcMaskLen()) {
        mergeSrcMaskLen(other.getSrcMaskLen());
      }
      if (other.hasSrcPort()) {
        mergeSrcPort(other.getSrcPort());
      }
      if (other.hasTcpFlags()) {
        mergeTcpFlags(other.getTcpFlags());
      }
      if (other.hasTos()) {
        mergeTos(other.getTos());
      }
      if (other.netflowVersion_ != 0) {
        setNetflowVersionValue(other.getNetflowVersionValue());
      }
      if (other.hasVlan()) {
        mergeVlan(other.getVlan());
      }
      if (!other.getNodeIdentifier().isEmpty()) {
        nodeIdentifier_ = other.nodeIdentifier_;
        bitField1_ |= 0x00000002;
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
            case 8: {
              timestamp_ = input.readUInt64();
              bitField0_ |= 0x00000001;
              break;
            } // case 8
            case 18: {
              input.readMessage(
                      getNumBytesFieldBuilder().getBuilder(),
                      extensionRegistry);
              bitField0_ |= 0x00000002;
              break;
            } // case 18
            case 24: {
              direction_ = input.readEnum();
              bitField0_ |= 0x00000004;
              break;
            } // case 24
            case 34: {
              dstAddress_ = input.readStringRequireUtf8();
              bitField0_ |= 0x00000008;
              break;
            } // case 34
            case 42: {
              dstHostname_ = input.readStringRequireUtf8();
              bitField0_ |= 0x00000010;
              break;
            } // case 42
            case 50: {
              input.readMessage(
                      getDstAsFieldBuilder().getBuilder(),
                      extensionRegistry);
              bitField0_ |= 0x00000020;
              break;
            } // case 50
            case 58: {
              input.readMessage(
                      getDstMaskLenFieldBuilder().getBuilder(),
                      extensionRegistry);
              bitField0_ |= 0x00000040;
              break;
            } // case 58
            case 66: {
              input.readMessage(
                      getDstPortFieldBuilder().getBuilder(),
                      extensionRegistry);
              bitField0_ |= 0x00000080;
              break;
            } // case 66
            case 74: {
              input.readMessage(
                      getEngineIdFieldBuilder().getBuilder(),
                      extensionRegistry);
              bitField0_ |= 0x00000100;
              break;
            } // case 74
            case 82: {
              input.readMessage(
                      getEngineTypeFieldBuilder().getBuilder(),
                      extensionRegistry);
              bitField0_ |= 0x00000200;
              break;
            } // case 82
            case 90: {
              input.readMessage(
                      getDeltaSwitchedFieldBuilder().getBuilder(),
                      extensionRegistry);
              bitField0_ |= 0x00000400;
              break;
            } // case 90
            case 98: {
              input.readMessage(
                      getFirstSwitchedFieldBuilder().getBuilder(),
                      extensionRegistry);
              bitField0_ |= 0x00000800;
              break;
            } // case 98
            case 106: {
              input.readMessage(
                      getLastSwitchedFieldBuilder().getBuilder(),
                      extensionRegistry);
              bitField0_ |= 0x00001000;
              break;
            } // case 106
            case 114: {
              input.readMessage(
                      getNumFlowRecordsFieldBuilder().getBuilder(),
                      extensionRegistry);
              bitField0_ |= 0x00002000;
              break;
            } // case 114
            case 122: {
              input.readMessage(
                      getNumPacketsFieldBuilder().getBuilder(),
                      extensionRegistry);
              bitField0_ |= 0x00004000;
              break;
            } // case 122
            case 130: {
              input.readMessage(
                      getFlowSeqNumFieldBuilder().getBuilder(),
                      extensionRegistry);
              bitField0_ |= 0x00008000;
              break;
            } // case 130
            case 138: {
              input.readMessage(
                      getInputSnmpIfindexFieldBuilder().getBuilder(),
                      extensionRegistry);
              bitField0_ |= 0x00010000;
              break;
            } // case 138
            case 146: {
              input.readMessage(
                      getOutputSnmpIfindexFieldBuilder().getBuilder(),
                      extensionRegistry);
              bitField0_ |= 0x00020000;
              break;
            } // case 146
            case 154: {
              input.readMessage(
                      getIpProtocolVersionFieldBuilder().getBuilder(),
                      extensionRegistry);
              bitField0_ |= 0x00040000;
              break;
            } // case 154
            case 162: {
              nextHopAddress_ = input.readStringRequireUtf8();
              bitField0_ |= 0x00080000;
              break;
            } // case 162
            case 170: {
              nextHopHostname_ = input.readStringRequireUtf8();
              bitField0_ |= 0x00100000;
              break;
            } // case 170
            case 178: {
              input.readMessage(
                      getProtocolFieldBuilder().getBuilder(),
                      extensionRegistry);
              bitField0_ |= 0x00200000;
              break;
            } // case 178
            case 184: {
              samplingAlgorithm_ = input.readEnum();
              bitField0_ |= 0x00400000;
              break;
            } // case 184
            case 194: {
              input.readMessage(
                      getSamplingIntervalFieldBuilder().getBuilder(),
                      extensionRegistry);
              bitField0_ |= 0x00800000;
              break;
            } // case 194
            case 210: {
              srcAddress_ = input.readStringRequireUtf8();
              bitField0_ |= 0x01000000;
              break;
            } // case 210
            case 218: {
              srcHostname_ = input.readStringRequireUtf8();
              bitField0_ |= 0x02000000;
              break;
            } // case 218
            case 226: {
              input.readMessage(
                      getSrcAsFieldBuilder().getBuilder(),
                      extensionRegistry);
              bitField0_ |= 0x04000000;
              break;
            } // case 226
            case 234: {
              input.readMessage(
                      getSrcMaskLenFieldBuilder().getBuilder(),
                      extensionRegistry);
              bitField0_ |= 0x08000000;
              break;
            } // case 234
            case 242: {
              input.readMessage(
                      getSrcPortFieldBuilder().getBuilder(),
                      extensionRegistry);
              bitField0_ |= 0x10000000;
              break;
            } // case 242
            case 250: {
              input.readMessage(
                      getTcpFlagsFieldBuilder().getBuilder(),
                      extensionRegistry);
              bitField0_ |= 0x20000000;
              break;
            } // case 250
            case 258: {
              input.readMessage(
                      getTosFieldBuilder().getBuilder(),
                      extensionRegistry);
              bitField0_ |= 0x40000000;
              break;
            } // case 258
            case 264: {
              netflowVersion_ = input.readEnum();
              bitField0_ |= 0x80000000;
              break;
            } // case 264
            case 274: {
              input.readMessage(
                      getVlanFieldBuilder().getBuilder(),
                      extensionRegistry);
              bitField1_ |= 0x00000001;
              break;
            } // case 274
            case 282: {
              nodeIdentifier_ = input.readStringRequireUtf8();
              bitField1_ |= 0x00000002;
              break;
            } // case 282
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
    private int bitField1_;

    private long timestamp_ ;
    /**
     * <pre>
     * Flow timestamp in milliseconds.
     * </pre>
     *
     * <code>uint64 timestamp = 1;</code>
     * @return The timestamp.
     */
    @java.lang.Override
    public long getTimestamp() {
      return timestamp_;
    }
    /**
     * <pre>
     * Flow timestamp in milliseconds.
     * </pre>
     *
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
     * <pre>
     * Flow timestamp in milliseconds.
     * </pre>
     *
     * <code>uint64 timestamp = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearTimestamp() {
      bitField0_ = (bitField0_ & ~0x00000001);
      timestamp_ = 0L;
      onChanged();
      return this;
    }

    private com.google.protobuf.UInt64Value numBytes_;
    private com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.UInt64Value, com.google.protobuf.UInt64Value.Builder, com.google.protobuf.UInt64ValueOrBuilder> numBytesBuilder_;
    /**
     * <pre>
     * Number of bytes transferred in the flow
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value num_bytes = 2;</code>
     * @return Whether the numBytes field is set.
     */
    public boolean hasNumBytes() {
      return ((bitField0_ & 0x00000002) != 0);
    }
    /**
     * <pre>
     * Number of bytes transferred in the flow
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value num_bytes = 2;</code>
     * @return The numBytes.
     */
    public com.google.protobuf.UInt64Value getNumBytes() {
      if (numBytesBuilder_ == null) {
        return numBytes_ == null ? com.google.protobuf.UInt64Value.getDefaultInstance() : numBytes_;
      } else {
        return numBytesBuilder_.getMessage();
      }
    }
    /**
     * <pre>
     * Number of bytes transferred in the flow
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value num_bytes = 2;</code>
     */
    public Builder setNumBytes(com.google.protobuf.UInt64Value value) {
      if (numBytesBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        numBytes_ = value;
      } else {
        numBytesBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Number of bytes transferred in the flow
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value num_bytes = 2;</code>
     */
    public Builder setNumBytes(
            com.google.protobuf.UInt64Value.Builder builderForValue) {
      if (numBytesBuilder_ == null) {
        numBytes_ = builderForValue.build();
      } else {
        numBytesBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Number of bytes transferred in the flow
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value num_bytes = 2;</code>
     */
    public Builder mergeNumBytes(com.google.protobuf.UInt64Value value) {
      if (numBytesBuilder_ == null) {
        if (((bitField0_ & 0x00000002) != 0) &&
                numBytes_ != null &&
                numBytes_ != com.google.protobuf.UInt64Value.getDefaultInstance()) {
          getNumBytesBuilder().mergeFrom(value);
        } else {
          numBytes_ = value;
        }
      } else {
        numBytesBuilder_.mergeFrom(value);
      }
      if (numBytes_ != null) {
        bitField0_ |= 0x00000002;
        onChanged();
      }
      return this;
    }
    /**
     * <pre>
     * Number of bytes transferred in the flow
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value num_bytes = 2;</code>
     */
    public Builder clearNumBytes() {
      bitField0_ = (bitField0_ & ~0x00000002);
      numBytes_ = null;
      if (numBytesBuilder_ != null) {
        numBytesBuilder_.dispose();
        numBytesBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Number of bytes transferred in the flow
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value num_bytes = 2;</code>
     */
    public com.google.protobuf.UInt64Value.Builder getNumBytesBuilder() {
      bitField0_ |= 0x00000002;
      onChanged();
      return getNumBytesFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * Number of bytes transferred in the flow
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value num_bytes = 2;</code>
     */
    public com.google.protobuf.UInt64ValueOrBuilder getNumBytesOrBuilder() {
      if (numBytesBuilder_ != null) {
        return numBytesBuilder_.getMessageOrBuilder();
      } else {
        return numBytes_ == null ?
                com.google.protobuf.UInt64Value.getDefaultInstance() : numBytes_;
      }
    }
    /**
     * <pre>
     * Number of bytes transferred in the flow
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value num_bytes = 2;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.UInt64Value, com.google.protobuf.UInt64Value.Builder, com.google.protobuf.UInt64ValueOrBuilder>
    getNumBytesFieldBuilder() {
      if (numBytesBuilder_ == null) {
        numBytesBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
                com.google.protobuf.UInt64Value, com.google.protobuf.UInt64Value.Builder, com.google.protobuf.UInt64ValueOrBuilder>(
                getNumBytes(),
                getParentForChildren(),
                isClean());
        numBytes_ = null;
      }
      return numBytesBuilder_;
    }

    private int direction_ = 0;
    /**
     * <pre>
     * Direction of the flow (egress vs ingress)
     * </pre>
     *
     * <code>.Direction direction = 3;</code>
     * @return The enum numeric value on the wire for direction.
     */
    @java.lang.Override public int getDirectionValue() {
      return direction_;
    }
    /**
     * <pre>
     * Direction of the flow (egress vs ingress)
     * </pre>
     *
     * <code>.Direction direction = 3;</code>
     * @param value The enum numeric value on the wire for direction to set.
     * @return This builder for chaining.
     */
    public Builder setDirectionValue(int value) {
      direction_ = value;
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Direction of the flow (egress vs ingress)
     * </pre>
     *
     * <code>.Direction direction = 3;</code>
     * @return The direction.
     */
    @java.lang.Override
    public org.opennms.netmgt.telemetry.protocols.netflow.transport.Direction getDirection() {
      org.opennms.netmgt.telemetry.protocols.netflow.transport.Direction result = org.opennms.netmgt.telemetry.protocols.netflow.transport.Direction.forNumber(direction_);
      return result == null ? org.opennms.netmgt.telemetry.protocols.netflow.transport.Direction.UNRECOGNIZED : result;
    }
    /**
     * <pre>
     * Direction of the flow (egress vs ingress)
     * </pre>
     *
     * <code>.Direction direction = 3;</code>
     * @param value The direction to set.
     * @return This builder for chaining.
     */
    public Builder setDirection(org.opennms.netmgt.telemetry.protocols.netflow.transport.Direction value) {
      if (value == null) {
        throw new NullPointerException();
      }
      bitField0_ |= 0x00000004;
      direction_ = value.getNumber();
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Direction of the flow (egress vs ingress)
     * </pre>
     *
     * <code>.Direction direction = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearDirection() {
      bitField0_ = (bitField0_ & ~0x00000004);
      direction_ = 0;
      onChanged();
      return this;
    }

    private java.lang.Object dstAddress_ = "";
    /**
     * <pre>
     *  Destination address.
     * </pre>
     *
     * <code>string dst_address = 4;</code>
     * @return The dstAddress.
     */
    public java.lang.String getDstAddress() {
      java.lang.Object ref = dstAddress_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
                (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        dstAddress_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <pre>
     *  Destination address.
     * </pre>
     *
     * <code>string dst_address = 4;</code>
     * @return The bytes for dstAddress.
     */
    public com.google.protobuf.ByteString
    getDstAddressBytes() {
      java.lang.Object ref = dstAddress_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b =
                com.google.protobuf.ByteString.copyFromUtf8(
                        (java.lang.String) ref);
        dstAddress_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <pre>
     *  Destination address.
     * </pre>
     *
     * <code>string dst_address = 4;</code>
     * @param value The dstAddress to set.
     * @return This builder for chaining.
     */
    public Builder setDstAddress(
            java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      dstAddress_ = value;
      bitField0_ |= 0x00000008;
      onChanged();
      return this;
    }
    /**
     * <pre>
     *  Destination address.
     * </pre>
     *
     * <code>string dst_address = 4;</code>
     * @return This builder for chaining.
     */
    public Builder clearDstAddress() {
      dstAddress_ = getDefaultInstance().getDstAddress();
      bitField0_ = (bitField0_ & ~0x00000008);
      onChanged();
      return this;
    }
    /**
     * <pre>
     *  Destination address.
     * </pre>
     *
     * <code>string dst_address = 4;</code>
     * @param value The bytes for dstAddress to set.
     * @return This builder for chaining.
     */
    public Builder setDstAddressBytes(
            com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      dstAddress_ = value;
      bitField0_ |= 0x00000008;
      onChanged();
      return this;
    }

    private java.lang.Object dstHostname_ = "";
    /**
     * <pre>
     * Destination address hostname.
     * </pre>
     *
     * <code>string dst_hostname = 5;</code>
     * @return The dstHostname.
     */
    public java.lang.String getDstHostname() {
      java.lang.Object ref = dstHostname_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
                (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        dstHostname_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <pre>
     * Destination address hostname.
     * </pre>
     *
     * <code>string dst_hostname = 5;</code>
     * @return The bytes for dstHostname.
     */
    public com.google.protobuf.ByteString
    getDstHostnameBytes() {
      java.lang.Object ref = dstHostname_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b =
                com.google.protobuf.ByteString.copyFromUtf8(
                        (java.lang.String) ref);
        dstHostname_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <pre>
     * Destination address hostname.
     * </pre>
     *
     * <code>string dst_hostname = 5;</code>
     * @param value The dstHostname to set.
     * @return This builder for chaining.
     */
    public Builder setDstHostname(
            java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      dstHostname_ = value;
      bitField0_ |= 0x00000010;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Destination address hostname.
     * </pre>
     *
     * <code>string dst_hostname = 5;</code>
     * @return This builder for chaining.
     */
    public Builder clearDstHostname() {
      dstHostname_ = getDefaultInstance().getDstHostname();
      bitField0_ = (bitField0_ & ~0x00000010);
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Destination address hostname.
     * </pre>
     *
     * <code>string dst_hostname = 5;</code>
     * @param value The bytes for dstHostname to set.
     * @return This builder for chaining.
     */
    public Builder setDstHostnameBytes(
            com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      dstHostname_ = value;
      bitField0_ |= 0x00000010;
      onChanged();
      return this;
    }

    private com.google.protobuf.UInt64Value dstAs_;
    private com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.UInt64Value, com.google.protobuf.UInt64Value.Builder, com.google.protobuf.UInt64ValueOrBuilder> dstAsBuilder_;
    /**
     * <pre>
     * Destination autonomous system (AS).
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value dst_as = 6;</code>
     * @return Whether the dstAs field is set.
     */
    public boolean hasDstAs() {
      return ((bitField0_ & 0x00000020) != 0);
    }
    /**
     * <pre>
     * Destination autonomous system (AS).
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value dst_as = 6;</code>
     * @return The dstAs.
     */
    public com.google.protobuf.UInt64Value getDstAs() {
      if (dstAsBuilder_ == null) {
        return dstAs_ == null ? com.google.protobuf.UInt64Value.getDefaultInstance() : dstAs_;
      } else {
        return dstAsBuilder_.getMessage();
      }
    }
    /**
     * <pre>
     * Destination autonomous system (AS).
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value dst_as = 6;</code>
     */
    public Builder setDstAs(com.google.protobuf.UInt64Value value) {
      if (dstAsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        dstAs_ = value;
      } else {
        dstAsBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000020;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Destination autonomous system (AS).
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value dst_as = 6;</code>
     */
    public Builder setDstAs(
            com.google.protobuf.UInt64Value.Builder builderForValue) {
      if (dstAsBuilder_ == null) {
        dstAs_ = builderForValue.build();
      } else {
        dstAsBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000020;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Destination autonomous system (AS).
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value dst_as = 6;</code>
     */
    public Builder mergeDstAs(com.google.protobuf.UInt64Value value) {
      if (dstAsBuilder_ == null) {
        if (((bitField0_ & 0x00000020) != 0) &&
                dstAs_ != null &&
                dstAs_ != com.google.protobuf.UInt64Value.getDefaultInstance()) {
          getDstAsBuilder().mergeFrom(value);
        } else {
          dstAs_ = value;
        }
      } else {
        dstAsBuilder_.mergeFrom(value);
      }
      if (dstAs_ != null) {
        bitField0_ |= 0x00000020;
        onChanged();
      }
      return this;
    }
    /**
     * <pre>
     * Destination autonomous system (AS).
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value dst_as = 6;</code>
     */
    public Builder clearDstAs() {
      bitField0_ = (bitField0_ & ~0x00000020);
      dstAs_ = null;
      if (dstAsBuilder_ != null) {
        dstAsBuilder_.dispose();
        dstAsBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Destination autonomous system (AS).
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value dst_as = 6;</code>
     */
    public com.google.protobuf.UInt64Value.Builder getDstAsBuilder() {
      bitField0_ |= 0x00000020;
      onChanged();
      return getDstAsFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * Destination autonomous system (AS).
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value dst_as = 6;</code>
     */
    public com.google.protobuf.UInt64ValueOrBuilder getDstAsOrBuilder() {
      if (dstAsBuilder_ != null) {
        return dstAsBuilder_.getMessageOrBuilder();
      } else {
        return dstAs_ == null ?
                com.google.protobuf.UInt64Value.getDefaultInstance() : dstAs_;
      }
    }
    /**
     * <pre>
     * Destination autonomous system (AS).
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value dst_as = 6;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.UInt64Value, com.google.protobuf.UInt64Value.Builder, com.google.protobuf.UInt64ValueOrBuilder>
    getDstAsFieldBuilder() {
      if (dstAsBuilder_ == null) {
        dstAsBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
                com.google.protobuf.UInt64Value, com.google.protobuf.UInt64Value.Builder, com.google.protobuf.UInt64ValueOrBuilder>(
                getDstAs(),
                getParentForChildren(),
                isClean());
        dstAs_ = null;
      }
      return dstAsBuilder_;
    }

    private com.google.protobuf.UInt32Value dstMaskLen_;
    private com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.UInt32Value, com.google.protobuf.UInt32Value.Builder, com.google.protobuf.UInt32ValueOrBuilder> dstMaskLenBuilder_;
    /**
     * <pre>
     * The number of contiguous bits in the source address subnet mask.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value dst_mask_len = 7;</code>
     * @return Whether the dstMaskLen field is set.
     */
    public boolean hasDstMaskLen() {
      return ((bitField0_ & 0x00000040) != 0);
    }
    /**
     * <pre>
     * The number of contiguous bits in the source address subnet mask.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value dst_mask_len = 7;</code>
     * @return The dstMaskLen.
     */
    public com.google.protobuf.UInt32Value getDstMaskLen() {
      if (dstMaskLenBuilder_ == null) {
        return dstMaskLen_ == null ? com.google.protobuf.UInt32Value.getDefaultInstance() : dstMaskLen_;
      } else {
        return dstMaskLenBuilder_.getMessage();
      }
    }
    /**
     * <pre>
     * The number of contiguous bits in the source address subnet mask.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value dst_mask_len = 7;</code>
     */
    public Builder setDstMaskLen(com.google.protobuf.UInt32Value value) {
      if (dstMaskLenBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        dstMaskLen_ = value;
      } else {
        dstMaskLenBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000040;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The number of contiguous bits in the source address subnet mask.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value dst_mask_len = 7;</code>
     */
    public Builder setDstMaskLen(
            com.google.protobuf.UInt32Value.Builder builderForValue) {
      if (dstMaskLenBuilder_ == null) {
        dstMaskLen_ = builderForValue.build();
      } else {
        dstMaskLenBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000040;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The number of contiguous bits in the source address subnet mask.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value dst_mask_len = 7;</code>
     */
    public Builder mergeDstMaskLen(com.google.protobuf.UInt32Value value) {
      if (dstMaskLenBuilder_ == null) {
        if (((bitField0_ & 0x00000040) != 0) &&
                dstMaskLen_ != null &&
                dstMaskLen_ != com.google.protobuf.UInt32Value.getDefaultInstance()) {
          getDstMaskLenBuilder().mergeFrom(value);
        } else {
          dstMaskLen_ = value;
        }
      } else {
        dstMaskLenBuilder_.mergeFrom(value);
      }
      if (dstMaskLen_ != null) {
        bitField0_ |= 0x00000040;
        onChanged();
      }
      return this;
    }
    /**
     * <pre>
     * The number of contiguous bits in the source address subnet mask.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value dst_mask_len = 7;</code>
     */
    public Builder clearDstMaskLen() {
      bitField0_ = (bitField0_ & ~0x00000040);
      dstMaskLen_ = null;
      if (dstMaskLenBuilder_ != null) {
        dstMaskLenBuilder_.dispose();
        dstMaskLenBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The number of contiguous bits in the source address subnet mask.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value dst_mask_len = 7;</code>
     */
    public com.google.protobuf.UInt32Value.Builder getDstMaskLenBuilder() {
      bitField0_ |= 0x00000040;
      onChanged();
      return getDstMaskLenFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * The number of contiguous bits in the source address subnet mask.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value dst_mask_len = 7;</code>
     */
    public com.google.protobuf.UInt32ValueOrBuilder getDstMaskLenOrBuilder() {
      if (dstMaskLenBuilder_ != null) {
        return dstMaskLenBuilder_.getMessageOrBuilder();
      } else {
        return dstMaskLen_ == null ?
                com.google.protobuf.UInt32Value.getDefaultInstance() : dstMaskLen_;
      }
    }
    /**
     * <pre>
     * The number of contiguous bits in the source address subnet mask.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value dst_mask_len = 7;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.UInt32Value, com.google.protobuf.UInt32Value.Builder, com.google.protobuf.UInt32ValueOrBuilder>
    getDstMaskLenFieldBuilder() {
      if (dstMaskLenBuilder_ == null) {
        dstMaskLenBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
                com.google.protobuf.UInt32Value, com.google.protobuf.UInt32Value.Builder, com.google.protobuf.UInt32ValueOrBuilder>(
                getDstMaskLen(),
                getParentForChildren(),
                isClean());
        dstMaskLen_ = null;
      }
      return dstMaskLenBuilder_;
    }

    private com.google.protobuf.UInt32Value dstPort_;
    private com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.UInt32Value, com.google.protobuf.UInt32Value.Builder, com.google.protobuf.UInt32ValueOrBuilder> dstPortBuilder_;
    /**
     * <pre>
     * Destination port.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value dst_port = 8;</code>
     * @return Whether the dstPort field is set.
     */
    public boolean hasDstPort() {
      return ((bitField0_ & 0x00000080) != 0);
    }
    /**
     * <pre>
     * Destination port.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value dst_port = 8;</code>
     * @return The dstPort.
     */
    public com.google.protobuf.UInt32Value getDstPort() {
      if (dstPortBuilder_ == null) {
        return dstPort_ == null ? com.google.protobuf.UInt32Value.getDefaultInstance() : dstPort_;
      } else {
        return dstPortBuilder_.getMessage();
      }
    }
    /**
     * <pre>
     * Destination port.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value dst_port = 8;</code>
     */
    public Builder setDstPort(com.google.protobuf.UInt32Value value) {
      if (dstPortBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        dstPort_ = value;
      } else {
        dstPortBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000080;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Destination port.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value dst_port = 8;</code>
     */
    public Builder setDstPort(
            com.google.protobuf.UInt32Value.Builder builderForValue) {
      if (dstPortBuilder_ == null) {
        dstPort_ = builderForValue.build();
      } else {
        dstPortBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000080;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Destination port.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value dst_port = 8;</code>
     */
    public Builder mergeDstPort(com.google.protobuf.UInt32Value value) {
      if (dstPortBuilder_ == null) {
        if (((bitField0_ & 0x00000080) != 0) &&
                dstPort_ != null &&
                dstPort_ != com.google.protobuf.UInt32Value.getDefaultInstance()) {
          getDstPortBuilder().mergeFrom(value);
        } else {
          dstPort_ = value;
        }
      } else {
        dstPortBuilder_.mergeFrom(value);
      }
      if (dstPort_ != null) {
        bitField0_ |= 0x00000080;
        onChanged();
      }
      return this;
    }
    /**
     * <pre>
     * Destination port.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value dst_port = 8;</code>
     */
    public Builder clearDstPort() {
      bitField0_ = (bitField0_ & ~0x00000080);
      dstPort_ = null;
      if (dstPortBuilder_ != null) {
        dstPortBuilder_.dispose();
        dstPortBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Destination port.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value dst_port = 8;</code>
     */
    public com.google.protobuf.UInt32Value.Builder getDstPortBuilder() {
      bitField0_ |= 0x00000080;
      onChanged();
      return getDstPortFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * Destination port.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value dst_port = 8;</code>
     */
    public com.google.protobuf.UInt32ValueOrBuilder getDstPortOrBuilder() {
      if (dstPortBuilder_ != null) {
        return dstPortBuilder_.getMessageOrBuilder();
      } else {
        return dstPort_ == null ?
                com.google.protobuf.UInt32Value.getDefaultInstance() : dstPort_;
      }
    }
    /**
     * <pre>
     * Destination port.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value dst_port = 8;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.UInt32Value, com.google.protobuf.UInt32Value.Builder, com.google.protobuf.UInt32ValueOrBuilder>
    getDstPortFieldBuilder() {
      if (dstPortBuilder_ == null) {
        dstPortBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
                com.google.protobuf.UInt32Value, com.google.protobuf.UInt32Value.Builder, com.google.protobuf.UInt32ValueOrBuilder>(
                getDstPort(),
                getParentForChildren(),
                isClean());
        dstPort_ = null;
      }
      return dstPortBuilder_;
    }

    private com.google.protobuf.UInt32Value engineId_;
    private com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.UInt32Value, com.google.protobuf.UInt32Value.Builder, com.google.protobuf.UInt32ValueOrBuilder> engineIdBuilder_;
    /**
     * <pre>
     * Slot number of the flow-switching engine.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value engine_id = 9;</code>
     * @return Whether the engineId field is set.
     */
    public boolean hasEngineId() {
      return ((bitField0_ & 0x00000100) != 0);
    }
    /**
     * <pre>
     * Slot number of the flow-switching engine.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value engine_id = 9;</code>
     * @return The engineId.
     */
    public com.google.protobuf.UInt32Value getEngineId() {
      if (engineIdBuilder_ == null) {
        return engineId_ == null ? com.google.protobuf.UInt32Value.getDefaultInstance() : engineId_;
      } else {
        return engineIdBuilder_.getMessage();
      }
    }
    /**
     * <pre>
     * Slot number of the flow-switching engine.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value engine_id = 9;</code>
     */
    public Builder setEngineId(com.google.protobuf.UInt32Value value) {
      if (engineIdBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        engineId_ = value;
      } else {
        engineIdBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000100;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Slot number of the flow-switching engine.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value engine_id = 9;</code>
     */
    public Builder setEngineId(
            com.google.protobuf.UInt32Value.Builder builderForValue) {
      if (engineIdBuilder_ == null) {
        engineId_ = builderForValue.build();
      } else {
        engineIdBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000100;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Slot number of the flow-switching engine.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value engine_id = 9;</code>
     */
    public Builder mergeEngineId(com.google.protobuf.UInt32Value value) {
      if (engineIdBuilder_ == null) {
        if (((bitField0_ & 0x00000100) != 0) &&
                engineId_ != null &&
                engineId_ != com.google.protobuf.UInt32Value.getDefaultInstance()) {
          getEngineIdBuilder().mergeFrom(value);
        } else {
          engineId_ = value;
        }
      } else {
        engineIdBuilder_.mergeFrom(value);
      }
      if (engineId_ != null) {
        bitField0_ |= 0x00000100;
        onChanged();
      }
      return this;
    }
    /**
     * <pre>
     * Slot number of the flow-switching engine.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value engine_id = 9;</code>
     */
    public Builder clearEngineId() {
      bitField0_ = (bitField0_ & ~0x00000100);
      engineId_ = null;
      if (engineIdBuilder_ != null) {
        engineIdBuilder_.dispose();
        engineIdBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Slot number of the flow-switching engine.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value engine_id = 9;</code>
     */
    public com.google.protobuf.UInt32Value.Builder getEngineIdBuilder() {
      bitField0_ |= 0x00000100;
      onChanged();
      return getEngineIdFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * Slot number of the flow-switching engine.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value engine_id = 9;</code>
     */
    public com.google.protobuf.UInt32ValueOrBuilder getEngineIdOrBuilder() {
      if (engineIdBuilder_ != null) {
        return engineIdBuilder_.getMessageOrBuilder();
      } else {
        return engineId_ == null ?
                com.google.protobuf.UInt32Value.getDefaultInstance() : engineId_;
      }
    }
    /**
     * <pre>
     * Slot number of the flow-switching engine.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value engine_id = 9;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.UInt32Value, com.google.protobuf.UInt32Value.Builder, com.google.protobuf.UInt32ValueOrBuilder>
    getEngineIdFieldBuilder() {
      if (engineIdBuilder_ == null) {
        engineIdBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
                com.google.protobuf.UInt32Value, com.google.protobuf.UInt32Value.Builder, com.google.protobuf.UInt32ValueOrBuilder>(
                getEngineId(),
                getParentForChildren(),
                isClean());
        engineId_ = null;
      }
      return engineIdBuilder_;
    }

    private com.google.protobuf.UInt32Value engineType_;
    private com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.UInt32Value, com.google.protobuf.UInt32Value.Builder, com.google.protobuf.UInt32ValueOrBuilder> engineTypeBuilder_;
    /**
     * <pre>
     * Type of flow-switching engine.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value engine_type = 10;</code>
     * @return Whether the engineType field is set.
     */
    public boolean hasEngineType() {
      return ((bitField0_ & 0x00000200) != 0);
    }
    /**
     * <pre>
     * Type of flow-switching engine.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value engine_type = 10;</code>
     * @return The engineType.
     */
    public com.google.protobuf.UInt32Value getEngineType() {
      if (engineTypeBuilder_ == null) {
        return engineType_ == null ? com.google.protobuf.UInt32Value.getDefaultInstance() : engineType_;
      } else {
        return engineTypeBuilder_.getMessage();
      }
    }
    /**
     * <pre>
     * Type of flow-switching engine.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value engine_type = 10;</code>
     */
    public Builder setEngineType(com.google.protobuf.UInt32Value value) {
      if (engineTypeBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        engineType_ = value;
      } else {
        engineTypeBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000200;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Type of flow-switching engine.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value engine_type = 10;</code>
     */
    public Builder setEngineType(
            com.google.protobuf.UInt32Value.Builder builderForValue) {
      if (engineTypeBuilder_ == null) {
        engineType_ = builderForValue.build();
      } else {
        engineTypeBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000200;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Type of flow-switching engine.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value engine_type = 10;</code>
     */
    public Builder mergeEngineType(com.google.protobuf.UInt32Value value) {
      if (engineTypeBuilder_ == null) {
        if (((bitField0_ & 0x00000200) != 0) &&
                engineType_ != null &&
                engineType_ != com.google.protobuf.UInt32Value.getDefaultInstance()) {
          getEngineTypeBuilder().mergeFrom(value);
        } else {
          engineType_ = value;
        }
      } else {
        engineTypeBuilder_.mergeFrom(value);
      }
      if (engineType_ != null) {
        bitField0_ |= 0x00000200;
        onChanged();
      }
      return this;
    }
    /**
     * <pre>
     * Type of flow-switching engine.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value engine_type = 10;</code>
     */
    public Builder clearEngineType() {
      bitField0_ = (bitField0_ & ~0x00000200);
      engineType_ = null;
      if (engineTypeBuilder_ != null) {
        engineTypeBuilder_.dispose();
        engineTypeBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Type of flow-switching engine.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value engine_type = 10;</code>
     */
    public com.google.protobuf.UInt32Value.Builder getEngineTypeBuilder() {
      bitField0_ |= 0x00000200;
      onChanged();
      return getEngineTypeFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * Type of flow-switching engine.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value engine_type = 10;</code>
     */
    public com.google.protobuf.UInt32ValueOrBuilder getEngineTypeOrBuilder() {
      if (engineTypeBuilder_ != null) {
        return engineTypeBuilder_.getMessageOrBuilder();
      } else {
        return engineType_ == null ?
                com.google.protobuf.UInt32Value.getDefaultInstance() : engineType_;
      }
    }
    /**
     * <pre>
     * Type of flow-switching engine.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value engine_type = 10;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.UInt32Value, com.google.protobuf.UInt32Value.Builder, com.google.protobuf.UInt32ValueOrBuilder>
    getEngineTypeFieldBuilder() {
      if (engineTypeBuilder_ == null) {
        engineTypeBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
                com.google.protobuf.UInt32Value, com.google.protobuf.UInt32Value.Builder, com.google.protobuf.UInt32ValueOrBuilder>(
                getEngineType(),
                getParentForChildren(),
                isClean());
        engineType_ = null;
      }
      return engineTypeBuilder_;
    }

    private com.google.protobuf.UInt64Value deltaSwitched_;
    private com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.UInt64Value, com.google.protobuf.UInt64Value.Builder, com.google.protobuf.UInt64ValueOrBuilder> deltaSwitchedBuilder_;
    /**
     * <pre>
     * Unix timestamp in ms at which the previous exported packet-
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value delta_switched = 11;</code>
     * @return Whether the deltaSwitched field is set.
     */
    public boolean hasDeltaSwitched() {
      return ((bitField0_ & 0x00000400) != 0);
    }
    /**
     * <pre>
     * Unix timestamp in ms at which the previous exported packet-
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value delta_switched = 11;</code>
     * @return The deltaSwitched.
     */
    public com.google.protobuf.UInt64Value getDeltaSwitched() {
      if (deltaSwitchedBuilder_ == null) {
        return deltaSwitched_ == null ? com.google.protobuf.UInt64Value.getDefaultInstance() : deltaSwitched_;
      } else {
        return deltaSwitchedBuilder_.getMessage();
      }
    }
    /**
     * <pre>
     * Unix timestamp in ms at which the previous exported packet-
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value delta_switched = 11;</code>
     */
    public Builder setDeltaSwitched(com.google.protobuf.UInt64Value value) {
      if (deltaSwitchedBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        deltaSwitched_ = value;
      } else {
        deltaSwitchedBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000400;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Unix timestamp in ms at which the previous exported packet-
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value delta_switched = 11;</code>
     */
    public Builder setDeltaSwitched(
            com.google.protobuf.UInt64Value.Builder builderForValue) {
      if (deltaSwitchedBuilder_ == null) {
        deltaSwitched_ = builderForValue.build();
      } else {
        deltaSwitchedBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000400;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Unix timestamp in ms at which the previous exported packet-
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value delta_switched = 11;</code>
     */
    public Builder mergeDeltaSwitched(com.google.protobuf.UInt64Value value) {
      if (deltaSwitchedBuilder_ == null) {
        if (((bitField0_ & 0x00000400) != 0) &&
                deltaSwitched_ != null &&
                deltaSwitched_ != com.google.protobuf.UInt64Value.getDefaultInstance()) {
          getDeltaSwitchedBuilder().mergeFrom(value);
        } else {
          deltaSwitched_ = value;
        }
      } else {
        deltaSwitchedBuilder_.mergeFrom(value);
      }
      if (deltaSwitched_ != null) {
        bitField0_ |= 0x00000400;
        onChanged();
      }
      return this;
    }
    /**
     * <pre>
     * Unix timestamp in ms at which the previous exported packet-
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value delta_switched = 11;</code>
     */
    public Builder clearDeltaSwitched() {
      bitField0_ = (bitField0_ & ~0x00000400);
      deltaSwitched_ = null;
      if (deltaSwitchedBuilder_ != null) {
        deltaSwitchedBuilder_.dispose();
        deltaSwitchedBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Unix timestamp in ms at which the previous exported packet-
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value delta_switched = 11;</code>
     */
    public com.google.protobuf.UInt64Value.Builder getDeltaSwitchedBuilder() {
      bitField0_ |= 0x00000400;
      onChanged();
      return getDeltaSwitchedFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * Unix timestamp in ms at which the previous exported packet-
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value delta_switched = 11;</code>
     */
    public com.google.protobuf.UInt64ValueOrBuilder getDeltaSwitchedOrBuilder() {
      if (deltaSwitchedBuilder_ != null) {
        return deltaSwitchedBuilder_.getMessageOrBuilder();
      } else {
        return deltaSwitched_ == null ?
                com.google.protobuf.UInt64Value.getDefaultInstance() : deltaSwitched_;
      }
    }
    /**
     * <pre>
     * Unix timestamp in ms at which the previous exported packet-
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value delta_switched = 11;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.UInt64Value, com.google.protobuf.UInt64Value.Builder, com.google.protobuf.UInt64ValueOrBuilder>
    getDeltaSwitchedFieldBuilder() {
      if (deltaSwitchedBuilder_ == null) {
        deltaSwitchedBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
                com.google.protobuf.UInt64Value, com.google.protobuf.UInt64Value.Builder, com.google.protobuf.UInt64ValueOrBuilder>(
                getDeltaSwitched(),
                getParentForChildren(),
                isClean());
        deltaSwitched_ = null;
      }
      return deltaSwitchedBuilder_;
    }

    private com.google.protobuf.UInt64Value firstSwitched_;
    private com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.UInt64Value, com.google.protobuf.UInt64Value.Builder, com.google.protobuf.UInt64ValueOrBuilder> firstSwitchedBuilder_;
    /**
     * <pre>
     * -associated with this flow was switched.
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value first_switched = 12;</code>
     * @return Whether the firstSwitched field is set.
     */
    public boolean hasFirstSwitched() {
      return ((bitField0_ & 0x00000800) != 0);
    }
    /**
     * <pre>
     * -associated with this flow was switched.
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value first_switched = 12;</code>
     * @return The firstSwitched.
     */
    public com.google.protobuf.UInt64Value getFirstSwitched() {
      if (firstSwitchedBuilder_ == null) {
        return firstSwitched_ == null ? com.google.protobuf.UInt64Value.getDefaultInstance() : firstSwitched_;
      } else {
        return firstSwitchedBuilder_.getMessage();
      }
    }
    /**
     * <pre>
     * -associated with this flow was switched.
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value first_switched = 12;</code>
     */
    public Builder setFirstSwitched(com.google.protobuf.UInt64Value value) {
      if (firstSwitchedBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        firstSwitched_ = value;
      } else {
        firstSwitchedBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000800;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * -associated with this flow was switched.
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value first_switched = 12;</code>
     */
    public Builder setFirstSwitched(
            com.google.protobuf.UInt64Value.Builder builderForValue) {
      if (firstSwitchedBuilder_ == null) {
        firstSwitched_ = builderForValue.build();
      } else {
        firstSwitchedBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000800;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * -associated with this flow was switched.
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value first_switched = 12;</code>
     */
    public Builder mergeFirstSwitched(com.google.protobuf.UInt64Value value) {
      if (firstSwitchedBuilder_ == null) {
        if (((bitField0_ & 0x00000800) != 0) &&
                firstSwitched_ != null &&
                firstSwitched_ != com.google.protobuf.UInt64Value.getDefaultInstance()) {
          getFirstSwitchedBuilder().mergeFrom(value);
        } else {
          firstSwitched_ = value;
        }
      } else {
        firstSwitchedBuilder_.mergeFrom(value);
      }
      if (firstSwitched_ != null) {
        bitField0_ |= 0x00000800;
        onChanged();
      }
      return this;
    }
    /**
     * <pre>
     * -associated with this flow was switched.
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value first_switched = 12;</code>
     */
    public Builder clearFirstSwitched() {
      bitField0_ = (bitField0_ & ~0x00000800);
      firstSwitched_ = null;
      if (firstSwitchedBuilder_ != null) {
        firstSwitchedBuilder_.dispose();
        firstSwitchedBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <pre>
     * -associated with this flow was switched.
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value first_switched = 12;</code>
     */
    public com.google.protobuf.UInt64Value.Builder getFirstSwitchedBuilder() {
      bitField0_ |= 0x00000800;
      onChanged();
      return getFirstSwitchedFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * -associated with this flow was switched.
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value first_switched = 12;</code>
     */
    public com.google.protobuf.UInt64ValueOrBuilder getFirstSwitchedOrBuilder() {
      if (firstSwitchedBuilder_ != null) {
        return firstSwitchedBuilder_.getMessageOrBuilder();
      } else {
        return firstSwitched_ == null ?
                com.google.protobuf.UInt64Value.getDefaultInstance() : firstSwitched_;
      }
    }
    /**
     * <pre>
     * -associated with this flow was switched.
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value first_switched = 12;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.UInt64Value, com.google.protobuf.UInt64Value.Builder, com.google.protobuf.UInt64ValueOrBuilder>
    getFirstSwitchedFieldBuilder() {
      if (firstSwitchedBuilder_ == null) {
        firstSwitchedBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
                com.google.protobuf.UInt64Value, com.google.protobuf.UInt64Value.Builder, com.google.protobuf.UInt64ValueOrBuilder>(
                getFirstSwitched(),
                getParentForChildren(),
                isClean());
        firstSwitched_ = null;
      }
      return firstSwitchedBuilder_;
    }

    private com.google.protobuf.UInt64Value lastSwitched_;
    private com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.UInt64Value, com.google.protobuf.UInt64Value.Builder, com.google.protobuf.UInt64ValueOrBuilder> lastSwitchedBuilder_;
    /**
     * <pre>
     * -associated with this flow was switched.
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value last_switched = 13;</code>
     * @return Whether the lastSwitched field is set.
     */
    public boolean hasLastSwitched() {
      return ((bitField0_ & 0x00001000) != 0);
    }
    /**
     * <pre>
     * -associated with this flow was switched.
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value last_switched = 13;</code>
     * @return The lastSwitched.
     */
    public com.google.protobuf.UInt64Value getLastSwitched() {
      if (lastSwitchedBuilder_ == null) {
        return lastSwitched_ == null ? com.google.protobuf.UInt64Value.getDefaultInstance() : lastSwitched_;
      } else {
        return lastSwitchedBuilder_.getMessage();
      }
    }
    /**
     * <pre>
     * -associated with this flow was switched.
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value last_switched = 13;</code>
     */
    public Builder setLastSwitched(com.google.protobuf.UInt64Value value) {
      if (lastSwitchedBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        lastSwitched_ = value;
      } else {
        lastSwitchedBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00001000;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * -associated with this flow was switched.
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value last_switched = 13;</code>
     */
    public Builder setLastSwitched(
            com.google.protobuf.UInt64Value.Builder builderForValue) {
      if (lastSwitchedBuilder_ == null) {
        lastSwitched_ = builderForValue.build();
      } else {
        lastSwitchedBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00001000;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * -associated with this flow was switched.
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value last_switched = 13;</code>
     */
    public Builder mergeLastSwitched(com.google.protobuf.UInt64Value value) {
      if (lastSwitchedBuilder_ == null) {
        if (((bitField0_ & 0x00001000) != 0) &&
                lastSwitched_ != null &&
                lastSwitched_ != com.google.protobuf.UInt64Value.getDefaultInstance()) {
          getLastSwitchedBuilder().mergeFrom(value);
        } else {
          lastSwitched_ = value;
        }
      } else {
        lastSwitchedBuilder_.mergeFrom(value);
      }
      if (lastSwitched_ != null) {
        bitField0_ |= 0x00001000;
        onChanged();
      }
      return this;
    }
    /**
     * <pre>
     * -associated with this flow was switched.
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value last_switched = 13;</code>
     */
    public Builder clearLastSwitched() {
      bitField0_ = (bitField0_ & ~0x00001000);
      lastSwitched_ = null;
      if (lastSwitchedBuilder_ != null) {
        lastSwitchedBuilder_.dispose();
        lastSwitchedBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <pre>
     * -associated with this flow was switched.
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value last_switched = 13;</code>
     */
    public com.google.protobuf.UInt64Value.Builder getLastSwitchedBuilder() {
      bitField0_ |= 0x00001000;
      onChanged();
      return getLastSwitchedFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * -associated with this flow was switched.
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value last_switched = 13;</code>
     */
    public com.google.protobuf.UInt64ValueOrBuilder getLastSwitchedOrBuilder() {
      if (lastSwitchedBuilder_ != null) {
        return lastSwitchedBuilder_.getMessageOrBuilder();
      } else {
        return lastSwitched_ == null ?
                com.google.protobuf.UInt64Value.getDefaultInstance() : lastSwitched_;
      }
    }
    /**
     * <pre>
     * -associated with this flow was switched.
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value last_switched = 13;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.UInt64Value, com.google.protobuf.UInt64Value.Builder, com.google.protobuf.UInt64ValueOrBuilder>
    getLastSwitchedFieldBuilder() {
      if (lastSwitchedBuilder_ == null) {
        lastSwitchedBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
                com.google.protobuf.UInt64Value, com.google.protobuf.UInt64Value.Builder, com.google.protobuf.UInt64ValueOrBuilder>(
                getLastSwitched(),
                getParentForChildren(),
                isClean());
        lastSwitched_ = null;
      }
      return lastSwitchedBuilder_;
    }

    private com.google.protobuf.UInt32Value numFlowRecords_;
    private com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.UInt32Value, com.google.protobuf.UInt32Value.Builder, com.google.protobuf.UInt32ValueOrBuilder> numFlowRecordsBuilder_;
    /**
     * <pre>
     * Number of flow records in the associated packet.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value num_flow_records = 14;</code>
     * @return Whether the numFlowRecords field is set.
     */
    public boolean hasNumFlowRecords() {
      return ((bitField0_ & 0x00002000) != 0);
    }
    /**
     * <pre>
     * Number of flow records in the associated packet.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value num_flow_records = 14;</code>
     * @return The numFlowRecords.
     */
    public com.google.protobuf.UInt32Value getNumFlowRecords() {
      if (numFlowRecordsBuilder_ == null) {
        return numFlowRecords_ == null ? com.google.protobuf.UInt32Value.getDefaultInstance() : numFlowRecords_;
      } else {
        return numFlowRecordsBuilder_.getMessage();
      }
    }
    /**
     * <pre>
     * Number of flow records in the associated packet.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value num_flow_records = 14;</code>
     */
    public Builder setNumFlowRecords(com.google.protobuf.UInt32Value value) {
      if (numFlowRecordsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        numFlowRecords_ = value;
      } else {
        numFlowRecordsBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00002000;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Number of flow records in the associated packet.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value num_flow_records = 14;</code>
     */
    public Builder setNumFlowRecords(
            com.google.protobuf.UInt32Value.Builder builderForValue) {
      if (numFlowRecordsBuilder_ == null) {
        numFlowRecords_ = builderForValue.build();
      } else {
        numFlowRecordsBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00002000;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Number of flow records in the associated packet.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value num_flow_records = 14;</code>
     */
    public Builder mergeNumFlowRecords(com.google.protobuf.UInt32Value value) {
      if (numFlowRecordsBuilder_ == null) {
        if (((bitField0_ & 0x00002000) != 0) &&
                numFlowRecords_ != null &&
                numFlowRecords_ != com.google.protobuf.UInt32Value.getDefaultInstance()) {
          getNumFlowRecordsBuilder().mergeFrom(value);
        } else {
          numFlowRecords_ = value;
        }
      } else {
        numFlowRecordsBuilder_.mergeFrom(value);
      }
      if (numFlowRecords_ != null) {
        bitField0_ |= 0x00002000;
        onChanged();
      }
      return this;
    }
    /**
     * <pre>
     * Number of flow records in the associated packet.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value num_flow_records = 14;</code>
     */
    public Builder clearNumFlowRecords() {
      bitField0_ = (bitField0_ & ~0x00002000);
      numFlowRecords_ = null;
      if (numFlowRecordsBuilder_ != null) {
        numFlowRecordsBuilder_.dispose();
        numFlowRecordsBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Number of flow records in the associated packet.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value num_flow_records = 14;</code>
     */
    public com.google.protobuf.UInt32Value.Builder getNumFlowRecordsBuilder() {
      bitField0_ |= 0x00002000;
      onChanged();
      return getNumFlowRecordsFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * Number of flow records in the associated packet.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value num_flow_records = 14;</code>
     */
    public com.google.protobuf.UInt32ValueOrBuilder getNumFlowRecordsOrBuilder() {
      if (numFlowRecordsBuilder_ != null) {
        return numFlowRecordsBuilder_.getMessageOrBuilder();
      } else {
        return numFlowRecords_ == null ?
                com.google.protobuf.UInt32Value.getDefaultInstance() : numFlowRecords_;
      }
    }
    /**
     * <pre>
     * Number of flow records in the associated packet.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value num_flow_records = 14;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.UInt32Value, com.google.protobuf.UInt32Value.Builder, com.google.protobuf.UInt32ValueOrBuilder>
    getNumFlowRecordsFieldBuilder() {
      if (numFlowRecordsBuilder_ == null) {
        numFlowRecordsBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
                com.google.protobuf.UInt32Value, com.google.protobuf.UInt32Value.Builder, com.google.protobuf.UInt32ValueOrBuilder>(
                getNumFlowRecords(),
                getParentForChildren(),
                isClean());
        numFlowRecords_ = null;
      }
      return numFlowRecordsBuilder_;
    }

    private com.google.protobuf.UInt64Value numPackets_;
    private com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.UInt64Value, com.google.protobuf.UInt64Value.Builder, com.google.protobuf.UInt64ValueOrBuilder> numPacketsBuilder_;
    /**
     * <pre>
     * Number of packets in the flow.
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value num_packets = 15;</code>
     * @return Whether the numPackets field is set.
     */
    public boolean hasNumPackets() {
      return ((bitField0_ & 0x00004000) != 0);
    }
    /**
     * <pre>
     * Number of packets in the flow.
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value num_packets = 15;</code>
     * @return The numPackets.
     */
    public com.google.protobuf.UInt64Value getNumPackets() {
      if (numPacketsBuilder_ == null) {
        return numPackets_ == null ? com.google.protobuf.UInt64Value.getDefaultInstance() : numPackets_;
      } else {
        return numPacketsBuilder_.getMessage();
      }
    }
    /**
     * <pre>
     * Number of packets in the flow.
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value num_packets = 15;</code>
     */
    public Builder setNumPackets(com.google.protobuf.UInt64Value value) {
      if (numPacketsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        numPackets_ = value;
      } else {
        numPacketsBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00004000;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Number of packets in the flow.
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value num_packets = 15;</code>
     */
    public Builder setNumPackets(
            com.google.protobuf.UInt64Value.Builder builderForValue) {
      if (numPacketsBuilder_ == null) {
        numPackets_ = builderForValue.build();
      } else {
        numPacketsBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00004000;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Number of packets in the flow.
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value num_packets = 15;</code>
     */
    public Builder mergeNumPackets(com.google.protobuf.UInt64Value value) {
      if (numPacketsBuilder_ == null) {
        if (((bitField0_ & 0x00004000) != 0) &&
                numPackets_ != null &&
                numPackets_ != com.google.protobuf.UInt64Value.getDefaultInstance()) {
          getNumPacketsBuilder().mergeFrom(value);
        } else {
          numPackets_ = value;
        }
      } else {
        numPacketsBuilder_.mergeFrom(value);
      }
      if (numPackets_ != null) {
        bitField0_ |= 0x00004000;
        onChanged();
      }
      return this;
    }
    /**
     * <pre>
     * Number of packets in the flow.
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value num_packets = 15;</code>
     */
    public Builder clearNumPackets() {
      bitField0_ = (bitField0_ & ~0x00004000);
      numPackets_ = null;
      if (numPacketsBuilder_ != null) {
        numPacketsBuilder_.dispose();
        numPacketsBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Number of packets in the flow.
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value num_packets = 15;</code>
     */
    public com.google.protobuf.UInt64Value.Builder getNumPacketsBuilder() {
      bitField0_ |= 0x00004000;
      onChanged();
      return getNumPacketsFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * Number of packets in the flow.
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value num_packets = 15;</code>
     */
    public com.google.protobuf.UInt64ValueOrBuilder getNumPacketsOrBuilder() {
      if (numPacketsBuilder_ != null) {
        return numPacketsBuilder_.getMessageOrBuilder();
      } else {
        return numPackets_ == null ?
                com.google.protobuf.UInt64Value.getDefaultInstance() : numPackets_;
      }
    }
    /**
     * <pre>
     * Number of packets in the flow.
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value num_packets = 15;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.UInt64Value, com.google.protobuf.UInt64Value.Builder, com.google.protobuf.UInt64ValueOrBuilder>
    getNumPacketsFieldBuilder() {
      if (numPacketsBuilder_ == null) {
        numPacketsBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
                com.google.protobuf.UInt64Value, com.google.protobuf.UInt64Value.Builder, com.google.protobuf.UInt64ValueOrBuilder>(
                getNumPackets(),
                getParentForChildren(),
                isClean());
        numPackets_ = null;
      }
      return numPacketsBuilder_;
    }

    private com.google.protobuf.UInt64Value flowSeqNum_;
    private com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.UInt64Value, com.google.protobuf.UInt64Value.Builder, com.google.protobuf.UInt64ValueOrBuilder> flowSeqNumBuilder_;
    /**
     * <pre>
     * Flow packet sequence number.
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value flow_seq_num = 16;</code>
     * @return Whether the flowSeqNum field is set.
     */
    public boolean hasFlowSeqNum() {
      return ((bitField0_ & 0x00008000) != 0);
    }
    /**
     * <pre>
     * Flow packet sequence number.
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value flow_seq_num = 16;</code>
     * @return The flowSeqNum.
     */
    public com.google.protobuf.UInt64Value getFlowSeqNum() {
      if (flowSeqNumBuilder_ == null) {
        return flowSeqNum_ == null ? com.google.protobuf.UInt64Value.getDefaultInstance() : flowSeqNum_;
      } else {
        return flowSeqNumBuilder_.getMessage();
      }
    }
    /**
     * <pre>
     * Flow packet sequence number.
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value flow_seq_num = 16;</code>
     */
    public Builder setFlowSeqNum(com.google.protobuf.UInt64Value value) {
      if (flowSeqNumBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        flowSeqNum_ = value;
      } else {
        flowSeqNumBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00008000;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Flow packet sequence number.
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value flow_seq_num = 16;</code>
     */
    public Builder setFlowSeqNum(
            com.google.protobuf.UInt64Value.Builder builderForValue) {
      if (flowSeqNumBuilder_ == null) {
        flowSeqNum_ = builderForValue.build();
      } else {
        flowSeqNumBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00008000;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Flow packet sequence number.
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value flow_seq_num = 16;</code>
     */
    public Builder mergeFlowSeqNum(com.google.protobuf.UInt64Value value) {
      if (flowSeqNumBuilder_ == null) {
        if (((bitField0_ & 0x00008000) != 0) &&
                flowSeqNum_ != null &&
                flowSeqNum_ != com.google.protobuf.UInt64Value.getDefaultInstance()) {
          getFlowSeqNumBuilder().mergeFrom(value);
        } else {
          flowSeqNum_ = value;
        }
      } else {
        flowSeqNumBuilder_.mergeFrom(value);
      }
      if (flowSeqNum_ != null) {
        bitField0_ |= 0x00008000;
        onChanged();
      }
      return this;
    }
    /**
     * <pre>
     * Flow packet sequence number.
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value flow_seq_num = 16;</code>
     */
    public Builder clearFlowSeqNum() {
      bitField0_ = (bitField0_ & ~0x00008000);
      flowSeqNum_ = null;
      if (flowSeqNumBuilder_ != null) {
        flowSeqNumBuilder_.dispose();
        flowSeqNumBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Flow packet sequence number.
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value flow_seq_num = 16;</code>
     */
    public com.google.protobuf.UInt64Value.Builder getFlowSeqNumBuilder() {
      bitField0_ |= 0x00008000;
      onChanged();
      return getFlowSeqNumFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * Flow packet sequence number.
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value flow_seq_num = 16;</code>
     */
    public com.google.protobuf.UInt64ValueOrBuilder getFlowSeqNumOrBuilder() {
      if (flowSeqNumBuilder_ != null) {
        return flowSeqNumBuilder_.getMessageOrBuilder();
      } else {
        return flowSeqNum_ == null ?
                com.google.protobuf.UInt64Value.getDefaultInstance() : flowSeqNum_;
      }
    }
    /**
     * <pre>
     * Flow packet sequence number.
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value flow_seq_num = 16;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.UInt64Value, com.google.protobuf.UInt64Value.Builder, com.google.protobuf.UInt64ValueOrBuilder>
    getFlowSeqNumFieldBuilder() {
      if (flowSeqNumBuilder_ == null) {
        flowSeqNumBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
                com.google.protobuf.UInt64Value, com.google.protobuf.UInt64Value.Builder, com.google.protobuf.UInt64ValueOrBuilder>(
                getFlowSeqNum(),
                getParentForChildren(),
                isClean());
        flowSeqNum_ = null;
      }
      return flowSeqNumBuilder_;
    }

    private com.google.protobuf.UInt32Value inputSnmpIfindex_;
    private com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.UInt32Value, com.google.protobuf.UInt32Value.Builder, com.google.protobuf.UInt32ValueOrBuilder> inputSnmpIfindexBuilder_;
    /**
     * <pre>
     * Input SNMP ifIndex.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value input_snmp_ifindex = 17;</code>
     * @return Whether the inputSnmpIfindex field is set.
     */
    public boolean hasInputSnmpIfindex() {
      return ((bitField0_ & 0x00010000) != 0);
    }
    /**
     * <pre>
     * Input SNMP ifIndex.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value input_snmp_ifindex = 17;</code>
     * @return The inputSnmpIfindex.
     */
    public com.google.protobuf.UInt32Value getInputSnmpIfindex() {
      if (inputSnmpIfindexBuilder_ == null) {
        return inputSnmpIfindex_ == null ? com.google.protobuf.UInt32Value.getDefaultInstance() : inputSnmpIfindex_;
      } else {
        return inputSnmpIfindexBuilder_.getMessage();
      }
    }
    /**
     * <pre>
     * Input SNMP ifIndex.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value input_snmp_ifindex = 17;</code>
     */
    public Builder setInputSnmpIfindex(com.google.protobuf.UInt32Value value) {
      if (inputSnmpIfindexBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        inputSnmpIfindex_ = value;
      } else {
        inputSnmpIfindexBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00010000;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Input SNMP ifIndex.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value input_snmp_ifindex = 17;</code>
     */
    public Builder setInputSnmpIfindex(
            com.google.protobuf.UInt32Value.Builder builderForValue) {
      if (inputSnmpIfindexBuilder_ == null) {
        inputSnmpIfindex_ = builderForValue.build();
      } else {
        inputSnmpIfindexBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00010000;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Input SNMP ifIndex.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value input_snmp_ifindex = 17;</code>
     */
    public Builder mergeInputSnmpIfindex(com.google.protobuf.UInt32Value value) {
      if (inputSnmpIfindexBuilder_ == null) {
        if (((bitField0_ & 0x00010000) != 0) &&
                inputSnmpIfindex_ != null &&
                inputSnmpIfindex_ != com.google.protobuf.UInt32Value.getDefaultInstance()) {
          getInputSnmpIfindexBuilder().mergeFrom(value);
        } else {
          inputSnmpIfindex_ = value;
        }
      } else {
        inputSnmpIfindexBuilder_.mergeFrom(value);
      }
      if (inputSnmpIfindex_ != null) {
        bitField0_ |= 0x00010000;
        onChanged();
      }
      return this;
    }
    /**
     * <pre>
     * Input SNMP ifIndex.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value input_snmp_ifindex = 17;</code>
     */
    public Builder clearInputSnmpIfindex() {
      bitField0_ = (bitField0_ & ~0x00010000);
      inputSnmpIfindex_ = null;
      if (inputSnmpIfindexBuilder_ != null) {
        inputSnmpIfindexBuilder_.dispose();
        inputSnmpIfindexBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Input SNMP ifIndex.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value input_snmp_ifindex = 17;</code>
     */
    public com.google.protobuf.UInt32Value.Builder getInputSnmpIfindexBuilder() {
      bitField0_ |= 0x00010000;
      onChanged();
      return getInputSnmpIfindexFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * Input SNMP ifIndex.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value input_snmp_ifindex = 17;</code>
     */
    public com.google.protobuf.UInt32ValueOrBuilder getInputSnmpIfindexOrBuilder() {
      if (inputSnmpIfindexBuilder_ != null) {
        return inputSnmpIfindexBuilder_.getMessageOrBuilder();
      } else {
        return inputSnmpIfindex_ == null ?
                com.google.protobuf.UInt32Value.getDefaultInstance() : inputSnmpIfindex_;
      }
    }
    /**
     * <pre>
     * Input SNMP ifIndex.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value input_snmp_ifindex = 17;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.UInt32Value, com.google.protobuf.UInt32Value.Builder, com.google.protobuf.UInt32ValueOrBuilder>
    getInputSnmpIfindexFieldBuilder() {
      if (inputSnmpIfindexBuilder_ == null) {
        inputSnmpIfindexBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
                com.google.protobuf.UInt32Value, com.google.protobuf.UInt32Value.Builder, com.google.protobuf.UInt32ValueOrBuilder>(
                getInputSnmpIfindex(),
                getParentForChildren(),
                isClean());
        inputSnmpIfindex_ = null;
      }
      return inputSnmpIfindexBuilder_;
    }

    private com.google.protobuf.UInt32Value outputSnmpIfindex_;
    private com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.UInt32Value, com.google.protobuf.UInt32Value.Builder, com.google.protobuf.UInt32ValueOrBuilder> outputSnmpIfindexBuilder_;
    /**
     * <pre>
     * Output SNMP ifIndex.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value output_snmp_ifindex = 18;</code>
     * @return Whether the outputSnmpIfindex field is set.
     */
    public boolean hasOutputSnmpIfindex() {
      return ((bitField0_ & 0x00020000) != 0);
    }
    /**
     * <pre>
     * Output SNMP ifIndex.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value output_snmp_ifindex = 18;</code>
     * @return The outputSnmpIfindex.
     */
    public com.google.protobuf.UInt32Value getOutputSnmpIfindex() {
      if (outputSnmpIfindexBuilder_ == null) {
        return outputSnmpIfindex_ == null ? com.google.protobuf.UInt32Value.getDefaultInstance() : outputSnmpIfindex_;
      } else {
        return outputSnmpIfindexBuilder_.getMessage();
      }
    }
    /**
     * <pre>
     * Output SNMP ifIndex.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value output_snmp_ifindex = 18;</code>
     */
    public Builder setOutputSnmpIfindex(com.google.protobuf.UInt32Value value) {
      if (outputSnmpIfindexBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        outputSnmpIfindex_ = value;
      } else {
        outputSnmpIfindexBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00020000;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Output SNMP ifIndex.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value output_snmp_ifindex = 18;</code>
     */
    public Builder setOutputSnmpIfindex(
            com.google.protobuf.UInt32Value.Builder builderForValue) {
      if (outputSnmpIfindexBuilder_ == null) {
        outputSnmpIfindex_ = builderForValue.build();
      } else {
        outputSnmpIfindexBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00020000;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Output SNMP ifIndex.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value output_snmp_ifindex = 18;</code>
     */
    public Builder mergeOutputSnmpIfindex(com.google.protobuf.UInt32Value value) {
      if (outputSnmpIfindexBuilder_ == null) {
        if (((bitField0_ & 0x00020000) != 0) &&
                outputSnmpIfindex_ != null &&
                outputSnmpIfindex_ != com.google.protobuf.UInt32Value.getDefaultInstance()) {
          getOutputSnmpIfindexBuilder().mergeFrom(value);
        } else {
          outputSnmpIfindex_ = value;
        }
      } else {
        outputSnmpIfindexBuilder_.mergeFrom(value);
      }
      if (outputSnmpIfindex_ != null) {
        bitField0_ |= 0x00020000;
        onChanged();
      }
      return this;
    }
    /**
     * <pre>
     * Output SNMP ifIndex.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value output_snmp_ifindex = 18;</code>
     */
    public Builder clearOutputSnmpIfindex() {
      bitField0_ = (bitField0_ & ~0x00020000);
      outputSnmpIfindex_ = null;
      if (outputSnmpIfindexBuilder_ != null) {
        outputSnmpIfindexBuilder_.dispose();
        outputSnmpIfindexBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Output SNMP ifIndex.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value output_snmp_ifindex = 18;</code>
     */
    public com.google.protobuf.UInt32Value.Builder getOutputSnmpIfindexBuilder() {
      bitField0_ |= 0x00020000;
      onChanged();
      return getOutputSnmpIfindexFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * Output SNMP ifIndex.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value output_snmp_ifindex = 18;</code>
     */
    public com.google.protobuf.UInt32ValueOrBuilder getOutputSnmpIfindexOrBuilder() {
      if (outputSnmpIfindexBuilder_ != null) {
        return outputSnmpIfindexBuilder_.getMessageOrBuilder();
      } else {
        return outputSnmpIfindex_ == null ?
                com.google.protobuf.UInt32Value.getDefaultInstance() : outputSnmpIfindex_;
      }
    }
    /**
     * <pre>
     * Output SNMP ifIndex.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value output_snmp_ifindex = 18;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.UInt32Value, com.google.protobuf.UInt32Value.Builder, com.google.protobuf.UInt32ValueOrBuilder>
    getOutputSnmpIfindexFieldBuilder() {
      if (outputSnmpIfindexBuilder_ == null) {
        outputSnmpIfindexBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
                com.google.protobuf.UInt32Value, com.google.protobuf.UInt32Value.Builder, com.google.protobuf.UInt32ValueOrBuilder>(
                getOutputSnmpIfindex(),
                getParentForChildren(),
                isClean());
        outputSnmpIfindex_ = null;
      }
      return outputSnmpIfindexBuilder_;
    }

    private com.google.protobuf.UInt32Value ipProtocolVersion_;
    private com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.UInt32Value, com.google.protobuf.UInt32Value.Builder, com.google.protobuf.UInt32ValueOrBuilder> ipProtocolVersionBuilder_;
    /**
     * <pre>
     * IPv4 vs IPv6.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value ip_protocol_version = 19;</code>
     * @return Whether the ipProtocolVersion field is set.
     */
    public boolean hasIpProtocolVersion() {
      return ((bitField0_ & 0x00040000) != 0);
    }
    /**
     * <pre>
     * IPv4 vs IPv6.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value ip_protocol_version = 19;</code>
     * @return The ipProtocolVersion.
     */
    public com.google.protobuf.UInt32Value getIpProtocolVersion() {
      if (ipProtocolVersionBuilder_ == null) {
        return ipProtocolVersion_ == null ? com.google.protobuf.UInt32Value.getDefaultInstance() : ipProtocolVersion_;
      } else {
        return ipProtocolVersionBuilder_.getMessage();
      }
    }
    /**
     * <pre>
     * IPv4 vs IPv6.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value ip_protocol_version = 19;</code>
     */
    public Builder setIpProtocolVersion(com.google.protobuf.UInt32Value value) {
      if (ipProtocolVersionBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ipProtocolVersion_ = value;
      } else {
        ipProtocolVersionBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00040000;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * IPv4 vs IPv6.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value ip_protocol_version = 19;</code>
     */
    public Builder setIpProtocolVersion(
            com.google.protobuf.UInt32Value.Builder builderForValue) {
      if (ipProtocolVersionBuilder_ == null) {
        ipProtocolVersion_ = builderForValue.build();
      } else {
        ipProtocolVersionBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00040000;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * IPv4 vs IPv6.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value ip_protocol_version = 19;</code>
     */
    public Builder mergeIpProtocolVersion(com.google.protobuf.UInt32Value value) {
      if (ipProtocolVersionBuilder_ == null) {
        if (((bitField0_ & 0x00040000) != 0) &&
                ipProtocolVersion_ != null &&
                ipProtocolVersion_ != com.google.protobuf.UInt32Value.getDefaultInstance()) {
          getIpProtocolVersionBuilder().mergeFrom(value);
        } else {
          ipProtocolVersion_ = value;
        }
      } else {
        ipProtocolVersionBuilder_.mergeFrom(value);
      }
      if (ipProtocolVersion_ != null) {
        bitField0_ |= 0x00040000;
        onChanged();
      }
      return this;
    }
    /**
     * <pre>
     * IPv4 vs IPv6.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value ip_protocol_version = 19;</code>
     */
    public Builder clearIpProtocolVersion() {
      bitField0_ = (bitField0_ & ~0x00040000);
      ipProtocolVersion_ = null;
      if (ipProtocolVersionBuilder_ != null) {
        ipProtocolVersionBuilder_.dispose();
        ipProtocolVersionBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <pre>
     * IPv4 vs IPv6.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value ip_protocol_version = 19;</code>
     */
    public com.google.protobuf.UInt32Value.Builder getIpProtocolVersionBuilder() {
      bitField0_ |= 0x00040000;
      onChanged();
      return getIpProtocolVersionFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * IPv4 vs IPv6.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value ip_protocol_version = 19;</code>
     */
    public com.google.protobuf.UInt32ValueOrBuilder getIpProtocolVersionOrBuilder() {
      if (ipProtocolVersionBuilder_ != null) {
        return ipProtocolVersionBuilder_.getMessageOrBuilder();
      } else {
        return ipProtocolVersion_ == null ?
                com.google.protobuf.UInt32Value.getDefaultInstance() : ipProtocolVersion_;
      }
    }
    /**
     * <pre>
     * IPv4 vs IPv6.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value ip_protocol_version = 19;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.UInt32Value, com.google.protobuf.UInt32Value.Builder, com.google.protobuf.UInt32ValueOrBuilder>
    getIpProtocolVersionFieldBuilder() {
      if (ipProtocolVersionBuilder_ == null) {
        ipProtocolVersionBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
                com.google.protobuf.UInt32Value, com.google.protobuf.UInt32Value.Builder, com.google.protobuf.UInt32ValueOrBuilder>(
                getIpProtocolVersion(),
                getParentForChildren(),
                isClean());
        ipProtocolVersion_ = null;
      }
      return ipProtocolVersionBuilder_;
    }

    private java.lang.Object nextHopAddress_ = "";
    /**
     * <pre>
     * Next hop IpAddress.
     * </pre>
     *
     * <code>string next_hop_address = 20;</code>
     * @return The nextHopAddress.
     */
    public java.lang.String getNextHopAddress() {
      java.lang.Object ref = nextHopAddress_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
                (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        nextHopAddress_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <pre>
     * Next hop IpAddress.
     * </pre>
     *
     * <code>string next_hop_address = 20;</code>
     * @return The bytes for nextHopAddress.
     */
    public com.google.protobuf.ByteString
    getNextHopAddressBytes() {
      java.lang.Object ref = nextHopAddress_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b =
                com.google.protobuf.ByteString.copyFromUtf8(
                        (java.lang.String) ref);
        nextHopAddress_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <pre>
     * Next hop IpAddress.
     * </pre>
     *
     * <code>string next_hop_address = 20;</code>
     * @param value The nextHopAddress to set.
     * @return This builder for chaining.
     */
    public Builder setNextHopAddress(
            java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      nextHopAddress_ = value;
      bitField0_ |= 0x00080000;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Next hop IpAddress.
     * </pre>
     *
     * <code>string next_hop_address = 20;</code>
     * @return This builder for chaining.
     */
    public Builder clearNextHopAddress() {
      nextHopAddress_ = getDefaultInstance().getNextHopAddress();
      bitField0_ = (bitField0_ & ~0x00080000);
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Next hop IpAddress.
     * </pre>
     *
     * <code>string next_hop_address = 20;</code>
     * @param value The bytes for nextHopAddress to set.
     * @return This builder for chaining.
     */
    public Builder setNextHopAddressBytes(
            com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      nextHopAddress_ = value;
      bitField0_ |= 0x00080000;
      onChanged();
      return this;
    }

    private java.lang.Object nextHopHostname_ = "";
    /**
     * <pre>
     * Next hop hostname.
     * </pre>
     *
     * <code>string next_hop_hostname = 21;</code>
     * @return The nextHopHostname.
     */
    public java.lang.String getNextHopHostname() {
      java.lang.Object ref = nextHopHostname_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
                (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        nextHopHostname_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <pre>
     * Next hop hostname.
     * </pre>
     *
     * <code>string next_hop_hostname = 21;</code>
     * @return The bytes for nextHopHostname.
     */
    public com.google.protobuf.ByteString
    getNextHopHostnameBytes() {
      java.lang.Object ref = nextHopHostname_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b =
                com.google.protobuf.ByteString.copyFromUtf8(
                        (java.lang.String) ref);
        nextHopHostname_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <pre>
     * Next hop hostname.
     * </pre>
     *
     * <code>string next_hop_hostname = 21;</code>
     * @param value The nextHopHostname to set.
     * @return This builder for chaining.
     */
    public Builder setNextHopHostname(
            java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      nextHopHostname_ = value;
      bitField0_ |= 0x00100000;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Next hop hostname.
     * </pre>
     *
     * <code>string next_hop_hostname = 21;</code>
     * @return This builder for chaining.
     */
    public Builder clearNextHopHostname() {
      nextHopHostname_ = getDefaultInstance().getNextHopHostname();
      bitField0_ = (bitField0_ & ~0x00100000);
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Next hop hostname.
     * </pre>
     *
     * <code>string next_hop_hostname = 21;</code>
     * @param value The bytes for nextHopHostname to set.
     * @return This builder for chaining.
     */
    public Builder setNextHopHostnameBytes(
            com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      nextHopHostname_ = value;
      bitField0_ |= 0x00100000;
      onChanged();
      return this;
    }

    private com.google.protobuf.UInt32Value protocol_;
    private com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.UInt32Value, com.google.protobuf.UInt32Value.Builder, com.google.protobuf.UInt32ValueOrBuilder> protocolBuilder_;
    /**
     * <pre>
     * IP protocol number i.e 6 for TCP, 17 for UDP
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value protocol = 22;</code>
     * @return Whether the protocol field is set.
     */
    public boolean hasProtocol() {
      return ((bitField0_ & 0x00200000) != 0);
    }
    /**
     * <pre>
     * IP protocol number i.e 6 for TCP, 17 for UDP
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value protocol = 22;</code>
     * @return The protocol.
     */
    public com.google.protobuf.UInt32Value getProtocol() {
      if (protocolBuilder_ == null) {
        return protocol_ == null ? com.google.protobuf.UInt32Value.getDefaultInstance() : protocol_;
      } else {
        return protocolBuilder_.getMessage();
      }
    }
    /**
     * <pre>
     * IP protocol number i.e 6 for TCP, 17 for UDP
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value protocol = 22;</code>
     */
    public Builder setProtocol(com.google.protobuf.UInt32Value value) {
      if (protocolBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        protocol_ = value;
      } else {
        protocolBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00200000;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * IP protocol number i.e 6 for TCP, 17 for UDP
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value protocol = 22;</code>
     */
    public Builder setProtocol(
            com.google.protobuf.UInt32Value.Builder builderForValue) {
      if (protocolBuilder_ == null) {
        protocol_ = builderForValue.build();
      } else {
        protocolBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00200000;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * IP protocol number i.e 6 for TCP, 17 for UDP
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value protocol = 22;</code>
     */
    public Builder mergeProtocol(com.google.protobuf.UInt32Value value) {
      if (protocolBuilder_ == null) {
        if (((bitField0_ & 0x00200000) != 0) &&
                protocol_ != null &&
                protocol_ != com.google.protobuf.UInt32Value.getDefaultInstance()) {
          getProtocolBuilder().mergeFrom(value);
        } else {
          protocol_ = value;
        }
      } else {
        protocolBuilder_.mergeFrom(value);
      }
      if (protocol_ != null) {
        bitField0_ |= 0x00200000;
        onChanged();
      }
      return this;
    }
    /**
     * <pre>
     * IP protocol number i.e 6 for TCP, 17 for UDP
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value protocol = 22;</code>
     */
    public Builder clearProtocol() {
      bitField0_ = (bitField0_ & ~0x00200000);
      protocol_ = null;
      if (protocolBuilder_ != null) {
        protocolBuilder_.dispose();
        protocolBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <pre>
     * IP protocol number i.e 6 for TCP, 17 for UDP
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value protocol = 22;</code>
     */
    public com.google.protobuf.UInt32Value.Builder getProtocolBuilder() {
      bitField0_ |= 0x00200000;
      onChanged();
      return getProtocolFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * IP protocol number i.e 6 for TCP, 17 for UDP
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value protocol = 22;</code>
     */
    public com.google.protobuf.UInt32ValueOrBuilder getProtocolOrBuilder() {
      if (protocolBuilder_ != null) {
        return protocolBuilder_.getMessageOrBuilder();
      } else {
        return protocol_ == null ?
                com.google.protobuf.UInt32Value.getDefaultInstance() : protocol_;
      }
    }
    /**
     * <pre>
     * IP protocol number i.e 6 for TCP, 17 for UDP
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value protocol = 22;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.UInt32Value, com.google.protobuf.UInt32Value.Builder, com.google.protobuf.UInt32ValueOrBuilder>
    getProtocolFieldBuilder() {
      if (protocolBuilder_ == null) {
        protocolBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
                com.google.protobuf.UInt32Value, com.google.protobuf.UInt32Value.Builder, com.google.protobuf.UInt32ValueOrBuilder>(
                getProtocol(),
                getParentForChildren(),
                isClean());
        protocol_ = null;
      }
      return protocolBuilder_;
    }

    private int samplingAlgorithm_ = 0;
    /**
     * <pre>
     * Sampling algorithm ID.
     * </pre>
     *
     * <code>.SamplingAlgorithm sampling_algorithm = 23;</code>
     * @return The enum numeric value on the wire for samplingAlgorithm.
     */
    @java.lang.Override public int getSamplingAlgorithmValue() {
      return samplingAlgorithm_;
    }
    /**
     * <pre>
     * Sampling algorithm ID.
     * </pre>
     *
     * <code>.SamplingAlgorithm sampling_algorithm = 23;</code>
     * @param value The enum numeric value on the wire for samplingAlgorithm to set.
     * @return This builder for chaining.
     */
    public Builder setSamplingAlgorithmValue(int value) {
      samplingAlgorithm_ = value;
      bitField0_ |= 0x00400000;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Sampling algorithm ID.
     * </pre>
     *
     * <code>.SamplingAlgorithm sampling_algorithm = 23;</code>
     * @return The samplingAlgorithm.
     */
    @java.lang.Override
    public org.opennms.netmgt.telemetry.protocols.netflow.transport.SamplingAlgorithm getSamplingAlgorithm() {
      org.opennms.netmgt.telemetry.protocols.netflow.transport.SamplingAlgorithm result = org.opennms.netmgt.telemetry.protocols.netflow.transport.SamplingAlgorithm.forNumber(samplingAlgorithm_);
      return result == null ? org.opennms.netmgt.telemetry.protocols.netflow.transport.SamplingAlgorithm.UNRECOGNIZED : result;
    }
    /**
     * <pre>
     * Sampling algorithm ID.
     * </pre>
     *
     * <code>.SamplingAlgorithm sampling_algorithm = 23;</code>
     * @param value The samplingAlgorithm to set.
     * @return This builder for chaining.
     */
    public Builder setSamplingAlgorithm(org.opennms.netmgt.telemetry.protocols.netflow.transport.SamplingAlgorithm value) {
      if (value == null) {
        throw new NullPointerException();
      }
      bitField0_ |= 0x00400000;
      samplingAlgorithm_ = value.getNumber();
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Sampling algorithm ID.
     * </pre>
     *
     * <code>.SamplingAlgorithm sampling_algorithm = 23;</code>
     * @return This builder for chaining.
     */
    public Builder clearSamplingAlgorithm() {
      bitField0_ = (bitField0_ & ~0x00400000);
      samplingAlgorithm_ = 0;
      onChanged();
      return this;
    }

    private com.google.protobuf.DoubleValue samplingInterval_;
    private com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.DoubleValue, com.google.protobuf.DoubleValue.Builder, com.google.protobuf.DoubleValueOrBuilder> samplingIntervalBuilder_;
    /**
     * <pre>
     * Sampling interval.
     * </pre>
     *
     * <code>.google.protobuf.DoubleValue sampling_interval = 24;</code>
     * @return Whether the samplingInterval field is set.
     */
    public boolean hasSamplingInterval() {
      return ((bitField0_ & 0x00800000) != 0);
    }
    /**
     * <pre>
     * Sampling interval.
     * </pre>
     *
     * <code>.google.protobuf.DoubleValue sampling_interval = 24;</code>
     * @return The samplingInterval.
     */
    public com.google.protobuf.DoubleValue getSamplingInterval() {
      if (samplingIntervalBuilder_ == null) {
        return samplingInterval_ == null ? com.google.protobuf.DoubleValue.getDefaultInstance() : samplingInterval_;
      } else {
        return samplingIntervalBuilder_.getMessage();
      }
    }
    /**
     * <pre>
     * Sampling interval.
     * </pre>
     *
     * <code>.google.protobuf.DoubleValue sampling_interval = 24;</code>
     */
    public Builder setSamplingInterval(com.google.protobuf.DoubleValue value) {
      if (samplingIntervalBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        samplingInterval_ = value;
      } else {
        samplingIntervalBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00800000;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Sampling interval.
     * </pre>
     *
     * <code>.google.protobuf.DoubleValue sampling_interval = 24;</code>
     */
    public Builder setSamplingInterval(
            com.google.protobuf.DoubleValue.Builder builderForValue) {
      if (samplingIntervalBuilder_ == null) {
        samplingInterval_ = builderForValue.build();
      } else {
        samplingIntervalBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00800000;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Sampling interval.
     * </pre>
     *
     * <code>.google.protobuf.DoubleValue sampling_interval = 24;</code>
     */
    public Builder mergeSamplingInterval(com.google.protobuf.DoubleValue value) {
      if (samplingIntervalBuilder_ == null) {
        if (((bitField0_ & 0x00800000) != 0) &&
                samplingInterval_ != null &&
                samplingInterval_ != com.google.protobuf.DoubleValue.getDefaultInstance()) {
          getSamplingIntervalBuilder().mergeFrom(value);
        } else {
          samplingInterval_ = value;
        }
      } else {
        samplingIntervalBuilder_.mergeFrom(value);
      }
      if (samplingInterval_ != null) {
        bitField0_ |= 0x00800000;
        onChanged();
      }
      return this;
    }
    /**
     * <pre>
     * Sampling interval.
     * </pre>
     *
     * <code>.google.protobuf.DoubleValue sampling_interval = 24;</code>
     */
    public Builder clearSamplingInterval() {
      bitField0_ = (bitField0_ & ~0x00800000);
      samplingInterval_ = null;
      if (samplingIntervalBuilder_ != null) {
        samplingIntervalBuilder_.dispose();
        samplingIntervalBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Sampling interval.
     * </pre>
     *
     * <code>.google.protobuf.DoubleValue sampling_interval = 24;</code>
     */
    public com.google.protobuf.DoubleValue.Builder getSamplingIntervalBuilder() {
      bitField0_ |= 0x00800000;
      onChanged();
      return getSamplingIntervalFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * Sampling interval.
     * </pre>
     *
     * <code>.google.protobuf.DoubleValue sampling_interval = 24;</code>
     */
    public com.google.protobuf.DoubleValueOrBuilder getSamplingIntervalOrBuilder() {
      if (samplingIntervalBuilder_ != null) {
        return samplingIntervalBuilder_.getMessageOrBuilder();
      } else {
        return samplingInterval_ == null ?
                com.google.protobuf.DoubleValue.getDefaultInstance() : samplingInterval_;
      }
    }
    /**
     * <pre>
     * Sampling interval.
     * </pre>
     *
     * <code>.google.protobuf.DoubleValue sampling_interval = 24;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.DoubleValue, com.google.protobuf.DoubleValue.Builder, com.google.protobuf.DoubleValueOrBuilder>
    getSamplingIntervalFieldBuilder() {
      if (samplingIntervalBuilder_ == null) {
        samplingIntervalBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
                com.google.protobuf.DoubleValue, com.google.protobuf.DoubleValue.Builder, com.google.protobuf.DoubleValueOrBuilder>(
                getSamplingInterval(),
                getParentForChildren(),
                isClean());
        samplingInterval_ = null;
      }
      return samplingIntervalBuilder_;
    }

    private java.lang.Object srcAddress_ = "";
    /**
     * <pre>
     * Source address.
     * </pre>
     *
     * <code>string src_address = 26;</code>
     * @return The srcAddress.
     */
    public java.lang.String getSrcAddress() {
      java.lang.Object ref = srcAddress_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
                (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        srcAddress_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <pre>
     * Source address.
     * </pre>
     *
     * <code>string src_address = 26;</code>
     * @return The bytes for srcAddress.
     */
    public com.google.protobuf.ByteString
    getSrcAddressBytes() {
      java.lang.Object ref = srcAddress_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b =
                com.google.protobuf.ByteString.copyFromUtf8(
                        (java.lang.String) ref);
        srcAddress_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <pre>
     * Source address.
     * </pre>
     *
     * <code>string src_address = 26;</code>
     * @param value The srcAddress to set.
     * @return This builder for chaining.
     */
    public Builder setSrcAddress(
            java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      srcAddress_ = value;
      bitField0_ |= 0x01000000;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Source address.
     * </pre>
     *
     * <code>string src_address = 26;</code>
     * @return This builder for chaining.
     */
    public Builder clearSrcAddress() {
      srcAddress_ = getDefaultInstance().getSrcAddress();
      bitField0_ = (bitField0_ & ~0x01000000);
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Source address.
     * </pre>
     *
     * <code>string src_address = 26;</code>
     * @param value The bytes for srcAddress to set.
     * @return This builder for chaining.
     */
    public Builder setSrcAddressBytes(
            com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      srcAddress_ = value;
      bitField0_ |= 0x01000000;
      onChanged();
      return this;
    }

    private java.lang.Object srcHostname_ = "";
    /**
     * <pre>
     * Source hostname.
     * </pre>
     *
     * <code>string src_hostname = 27;</code>
     * @return The srcHostname.
     */
    public java.lang.String getSrcHostname() {
      java.lang.Object ref = srcHostname_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
                (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        srcHostname_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <pre>
     * Source hostname.
     * </pre>
     *
     * <code>string src_hostname = 27;</code>
     * @return The bytes for srcHostname.
     */
    public com.google.protobuf.ByteString
    getSrcHostnameBytes() {
      java.lang.Object ref = srcHostname_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b =
                com.google.protobuf.ByteString.copyFromUtf8(
                        (java.lang.String) ref);
        srcHostname_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <pre>
     * Source hostname.
     * </pre>
     *
     * <code>string src_hostname = 27;</code>
     * @param value The srcHostname to set.
     * @return This builder for chaining.
     */
    public Builder setSrcHostname(
            java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      srcHostname_ = value;
      bitField0_ |= 0x02000000;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Source hostname.
     * </pre>
     *
     * <code>string src_hostname = 27;</code>
     * @return This builder for chaining.
     */
    public Builder clearSrcHostname() {
      srcHostname_ = getDefaultInstance().getSrcHostname();
      bitField0_ = (bitField0_ & ~0x02000000);
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Source hostname.
     * </pre>
     *
     * <code>string src_hostname = 27;</code>
     * @param value The bytes for srcHostname to set.
     * @return This builder for chaining.
     */
    public Builder setSrcHostnameBytes(
            com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      srcHostname_ = value;
      bitField0_ |= 0x02000000;
      onChanged();
      return this;
    }

    private com.google.protobuf.UInt64Value srcAs_;
    private com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.UInt64Value, com.google.protobuf.UInt64Value.Builder, com.google.protobuf.UInt64ValueOrBuilder> srcAsBuilder_;
    /**
     * <pre>
     * Source AS number.
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value src_as = 28;</code>
     * @return Whether the srcAs field is set.
     */
    public boolean hasSrcAs() {
      return ((bitField0_ & 0x04000000) != 0);
    }
    /**
     * <pre>
     * Source AS number.
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value src_as = 28;</code>
     * @return The srcAs.
     */
    public com.google.protobuf.UInt64Value getSrcAs() {
      if (srcAsBuilder_ == null) {
        return srcAs_ == null ? com.google.protobuf.UInt64Value.getDefaultInstance() : srcAs_;
      } else {
        return srcAsBuilder_.getMessage();
      }
    }
    /**
     * <pre>
     * Source AS number.
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value src_as = 28;</code>
     */
    public Builder setSrcAs(com.google.protobuf.UInt64Value value) {
      if (srcAsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        srcAs_ = value;
      } else {
        srcAsBuilder_.setMessage(value);
      }
      bitField0_ |= 0x04000000;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Source AS number.
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value src_as = 28;</code>
     */
    public Builder setSrcAs(
            com.google.protobuf.UInt64Value.Builder builderForValue) {
      if (srcAsBuilder_ == null) {
        srcAs_ = builderForValue.build();
      } else {
        srcAsBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x04000000;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Source AS number.
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value src_as = 28;</code>
     */
    public Builder mergeSrcAs(com.google.protobuf.UInt64Value value) {
      if (srcAsBuilder_ == null) {
        if (((bitField0_ & 0x04000000) != 0) &&
                srcAs_ != null &&
                srcAs_ != com.google.protobuf.UInt64Value.getDefaultInstance()) {
          getSrcAsBuilder().mergeFrom(value);
        } else {
          srcAs_ = value;
        }
      } else {
        srcAsBuilder_.mergeFrom(value);
      }
      if (srcAs_ != null) {
        bitField0_ |= 0x04000000;
        onChanged();
      }
      return this;
    }
    /**
     * <pre>
     * Source AS number.
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value src_as = 28;</code>
     */
    public Builder clearSrcAs() {
      bitField0_ = (bitField0_ & ~0x04000000);
      srcAs_ = null;
      if (srcAsBuilder_ != null) {
        srcAsBuilder_.dispose();
        srcAsBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Source AS number.
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value src_as = 28;</code>
     */
    public com.google.protobuf.UInt64Value.Builder getSrcAsBuilder() {
      bitField0_ |= 0x04000000;
      onChanged();
      return getSrcAsFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * Source AS number.
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value src_as = 28;</code>
     */
    public com.google.protobuf.UInt64ValueOrBuilder getSrcAsOrBuilder() {
      if (srcAsBuilder_ != null) {
        return srcAsBuilder_.getMessageOrBuilder();
      } else {
        return srcAs_ == null ?
                com.google.protobuf.UInt64Value.getDefaultInstance() : srcAs_;
      }
    }
    /**
     * <pre>
     * Source AS number.
     * </pre>
     *
     * <code>.google.protobuf.UInt64Value src_as = 28;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.UInt64Value, com.google.protobuf.UInt64Value.Builder, com.google.protobuf.UInt64ValueOrBuilder>
    getSrcAsFieldBuilder() {
      if (srcAsBuilder_ == null) {
        srcAsBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
                com.google.protobuf.UInt64Value, com.google.protobuf.UInt64Value.Builder, com.google.protobuf.UInt64ValueOrBuilder>(
                getSrcAs(),
                getParentForChildren(),
                isClean());
        srcAs_ = null;
      }
      return srcAsBuilder_;
    }

    private com.google.protobuf.UInt32Value srcMaskLen_;
    private com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.UInt32Value, com.google.protobuf.UInt32Value.Builder, com.google.protobuf.UInt32ValueOrBuilder> srcMaskLenBuilder_;
    /**
     * <pre>
     * The number of contiguous bits in the destination address subnet mask.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value src_mask_len = 29;</code>
     * @return Whether the srcMaskLen field is set.
     */
    public boolean hasSrcMaskLen() {
      return ((bitField0_ & 0x08000000) != 0);
    }
    /**
     * <pre>
     * The number of contiguous bits in the destination address subnet mask.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value src_mask_len = 29;</code>
     * @return The srcMaskLen.
     */
    public com.google.protobuf.UInt32Value getSrcMaskLen() {
      if (srcMaskLenBuilder_ == null) {
        return srcMaskLen_ == null ? com.google.protobuf.UInt32Value.getDefaultInstance() : srcMaskLen_;
      } else {
        return srcMaskLenBuilder_.getMessage();
      }
    }
    /**
     * <pre>
     * The number of contiguous bits in the destination address subnet mask.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value src_mask_len = 29;</code>
     */
    public Builder setSrcMaskLen(com.google.protobuf.UInt32Value value) {
      if (srcMaskLenBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        srcMaskLen_ = value;
      } else {
        srcMaskLenBuilder_.setMessage(value);
      }
      bitField0_ |= 0x08000000;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The number of contiguous bits in the destination address subnet mask.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value src_mask_len = 29;</code>
     */
    public Builder setSrcMaskLen(
            com.google.protobuf.UInt32Value.Builder builderForValue) {
      if (srcMaskLenBuilder_ == null) {
        srcMaskLen_ = builderForValue.build();
      } else {
        srcMaskLenBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x08000000;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The number of contiguous bits in the destination address subnet mask.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value src_mask_len = 29;</code>
     */
    public Builder mergeSrcMaskLen(com.google.protobuf.UInt32Value value) {
      if (srcMaskLenBuilder_ == null) {
        if (((bitField0_ & 0x08000000) != 0) &&
                srcMaskLen_ != null &&
                srcMaskLen_ != com.google.protobuf.UInt32Value.getDefaultInstance()) {
          getSrcMaskLenBuilder().mergeFrom(value);
        } else {
          srcMaskLen_ = value;
        }
      } else {
        srcMaskLenBuilder_.mergeFrom(value);
      }
      if (srcMaskLen_ != null) {
        bitField0_ |= 0x08000000;
        onChanged();
      }
      return this;
    }
    /**
     * <pre>
     * The number of contiguous bits in the destination address subnet mask.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value src_mask_len = 29;</code>
     */
    public Builder clearSrcMaskLen() {
      bitField0_ = (bitField0_ & ~0x08000000);
      srcMaskLen_ = null;
      if (srcMaskLenBuilder_ != null) {
        srcMaskLenBuilder_.dispose();
        srcMaskLenBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The number of contiguous bits in the destination address subnet mask.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value src_mask_len = 29;</code>
     */
    public com.google.protobuf.UInt32Value.Builder getSrcMaskLenBuilder() {
      bitField0_ |= 0x08000000;
      onChanged();
      return getSrcMaskLenFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * The number of contiguous bits in the destination address subnet mask.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value src_mask_len = 29;</code>
     */
    public com.google.protobuf.UInt32ValueOrBuilder getSrcMaskLenOrBuilder() {
      if (srcMaskLenBuilder_ != null) {
        return srcMaskLenBuilder_.getMessageOrBuilder();
      } else {
        return srcMaskLen_ == null ?
                com.google.protobuf.UInt32Value.getDefaultInstance() : srcMaskLen_;
      }
    }
    /**
     * <pre>
     * The number of contiguous bits in the destination address subnet mask.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value src_mask_len = 29;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.UInt32Value, com.google.protobuf.UInt32Value.Builder, com.google.protobuf.UInt32ValueOrBuilder>
    getSrcMaskLenFieldBuilder() {
      if (srcMaskLenBuilder_ == null) {
        srcMaskLenBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
                com.google.protobuf.UInt32Value, com.google.protobuf.UInt32Value.Builder, com.google.protobuf.UInt32ValueOrBuilder>(
                getSrcMaskLen(),
                getParentForChildren(),
                isClean());
        srcMaskLen_ = null;
      }
      return srcMaskLenBuilder_;
    }

    private com.google.protobuf.UInt32Value srcPort_;
    private com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.UInt32Value, com.google.protobuf.UInt32Value.Builder, com.google.protobuf.UInt32ValueOrBuilder> srcPortBuilder_;
    /**
     * <pre>
     * Source port.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value src_port = 30;</code>
     * @return Whether the srcPort field is set.
     */
    public boolean hasSrcPort() {
      return ((bitField0_ & 0x10000000) != 0);
    }
    /**
     * <pre>
     * Source port.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value src_port = 30;</code>
     * @return The srcPort.
     */
    public com.google.protobuf.UInt32Value getSrcPort() {
      if (srcPortBuilder_ == null) {
        return srcPort_ == null ? com.google.protobuf.UInt32Value.getDefaultInstance() : srcPort_;
      } else {
        return srcPortBuilder_.getMessage();
      }
    }
    /**
     * <pre>
     * Source port.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value src_port = 30;</code>
     */
    public Builder setSrcPort(com.google.protobuf.UInt32Value value) {
      if (srcPortBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        srcPort_ = value;
      } else {
        srcPortBuilder_.setMessage(value);
      }
      bitField0_ |= 0x10000000;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Source port.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value src_port = 30;</code>
     */
    public Builder setSrcPort(
            com.google.protobuf.UInt32Value.Builder builderForValue) {
      if (srcPortBuilder_ == null) {
        srcPort_ = builderForValue.build();
      } else {
        srcPortBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x10000000;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Source port.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value src_port = 30;</code>
     */
    public Builder mergeSrcPort(com.google.protobuf.UInt32Value value) {
      if (srcPortBuilder_ == null) {
        if (((bitField0_ & 0x10000000) != 0) &&
                srcPort_ != null &&
                srcPort_ != com.google.protobuf.UInt32Value.getDefaultInstance()) {
          getSrcPortBuilder().mergeFrom(value);
        } else {
          srcPort_ = value;
        }
      } else {
        srcPortBuilder_.mergeFrom(value);
      }
      if (srcPort_ != null) {
        bitField0_ |= 0x10000000;
        onChanged();
      }
      return this;
    }
    /**
     * <pre>
     * Source port.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value src_port = 30;</code>
     */
    public Builder clearSrcPort() {
      bitField0_ = (bitField0_ & ~0x10000000);
      srcPort_ = null;
      if (srcPortBuilder_ != null) {
        srcPortBuilder_.dispose();
        srcPortBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Source port.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value src_port = 30;</code>
     */
    public com.google.protobuf.UInt32Value.Builder getSrcPortBuilder() {
      bitField0_ |= 0x10000000;
      onChanged();
      return getSrcPortFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * Source port.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value src_port = 30;</code>
     */
    public com.google.protobuf.UInt32ValueOrBuilder getSrcPortOrBuilder() {
      if (srcPortBuilder_ != null) {
        return srcPortBuilder_.getMessageOrBuilder();
      } else {
        return srcPort_ == null ?
                com.google.protobuf.UInt32Value.getDefaultInstance() : srcPort_;
      }
    }
    /**
     * <pre>
     * Source port.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value src_port = 30;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.UInt32Value, com.google.protobuf.UInt32Value.Builder, com.google.protobuf.UInt32ValueOrBuilder>
    getSrcPortFieldBuilder() {
      if (srcPortBuilder_ == null) {
        srcPortBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
                com.google.protobuf.UInt32Value, com.google.protobuf.UInt32Value.Builder, com.google.protobuf.UInt32ValueOrBuilder>(
                getSrcPort(),
                getParentForChildren(),
                isClean());
        srcPort_ = null;
      }
      return srcPortBuilder_;
    }

    private com.google.protobuf.UInt32Value tcpFlags_;
    private com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.UInt32Value, com.google.protobuf.UInt32Value.Builder, com.google.protobuf.UInt32ValueOrBuilder> tcpFlagsBuilder_;
    /**
     * <pre>
     * TCP Flags.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value tcp_flags = 31;</code>
     * @return Whether the tcpFlags field is set.
     */
    public boolean hasTcpFlags() {
      return ((bitField0_ & 0x20000000) != 0);
    }
    /**
     * <pre>
     * TCP Flags.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value tcp_flags = 31;</code>
     * @return The tcpFlags.
     */
    public com.google.protobuf.UInt32Value getTcpFlags() {
      if (tcpFlagsBuilder_ == null) {
        return tcpFlags_ == null ? com.google.protobuf.UInt32Value.getDefaultInstance() : tcpFlags_;
      } else {
        return tcpFlagsBuilder_.getMessage();
      }
    }
    /**
     * <pre>
     * TCP Flags.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value tcp_flags = 31;</code>
     */
    public Builder setTcpFlags(com.google.protobuf.UInt32Value value) {
      if (tcpFlagsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        tcpFlags_ = value;
      } else {
        tcpFlagsBuilder_.setMessage(value);
      }
      bitField0_ |= 0x20000000;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * TCP Flags.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value tcp_flags = 31;</code>
     */
    public Builder setTcpFlags(
            com.google.protobuf.UInt32Value.Builder builderForValue) {
      if (tcpFlagsBuilder_ == null) {
        tcpFlags_ = builderForValue.build();
      } else {
        tcpFlagsBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x20000000;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * TCP Flags.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value tcp_flags = 31;</code>
     */
    public Builder mergeTcpFlags(com.google.protobuf.UInt32Value value) {
      if (tcpFlagsBuilder_ == null) {
        if (((bitField0_ & 0x20000000) != 0) &&
                tcpFlags_ != null &&
                tcpFlags_ != com.google.protobuf.UInt32Value.getDefaultInstance()) {
          getTcpFlagsBuilder().mergeFrom(value);
        } else {
          tcpFlags_ = value;
        }
      } else {
        tcpFlagsBuilder_.mergeFrom(value);
      }
      if (tcpFlags_ != null) {
        bitField0_ |= 0x20000000;
        onChanged();
      }
      return this;
    }
    /**
     * <pre>
     * TCP Flags.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value tcp_flags = 31;</code>
     */
    public Builder clearTcpFlags() {
      bitField0_ = (bitField0_ & ~0x20000000);
      tcpFlags_ = null;
      if (tcpFlagsBuilder_ != null) {
        tcpFlagsBuilder_.dispose();
        tcpFlagsBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <pre>
     * TCP Flags.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value tcp_flags = 31;</code>
     */
    public com.google.protobuf.UInt32Value.Builder getTcpFlagsBuilder() {
      bitField0_ |= 0x20000000;
      onChanged();
      return getTcpFlagsFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * TCP Flags.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value tcp_flags = 31;</code>
     */
    public com.google.protobuf.UInt32ValueOrBuilder getTcpFlagsOrBuilder() {
      if (tcpFlagsBuilder_ != null) {
        return tcpFlagsBuilder_.getMessageOrBuilder();
      } else {
        return tcpFlags_ == null ?
                com.google.protobuf.UInt32Value.getDefaultInstance() : tcpFlags_;
      }
    }
    /**
     * <pre>
     * TCP Flags.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value tcp_flags = 31;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.UInt32Value, com.google.protobuf.UInt32Value.Builder, com.google.protobuf.UInt32ValueOrBuilder>
    getTcpFlagsFieldBuilder() {
      if (tcpFlagsBuilder_ == null) {
        tcpFlagsBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
                com.google.protobuf.UInt32Value, com.google.protobuf.UInt32Value.Builder, com.google.protobuf.UInt32ValueOrBuilder>(
                getTcpFlags(),
                getParentForChildren(),
                isClean());
        tcpFlags_ = null;
      }
      return tcpFlagsBuilder_;
    }

    private com.google.protobuf.UInt32Value tos_;
    private com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.UInt32Value, com.google.protobuf.UInt32Value.Builder, com.google.protobuf.UInt32ValueOrBuilder> tosBuilder_;
    /**
     * <pre>
     * TOS
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value tos = 32;</code>
     * @return Whether the tos field is set.
     */
    public boolean hasTos() {
      return ((bitField0_ & 0x40000000) != 0);
    }
    /**
     * <pre>
     * TOS
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value tos = 32;</code>
     * @return The tos.
     */
    public com.google.protobuf.UInt32Value getTos() {
      if (tosBuilder_ == null) {
        return tos_ == null ? com.google.protobuf.UInt32Value.getDefaultInstance() : tos_;
      } else {
        return tosBuilder_.getMessage();
      }
    }
    /**
     * <pre>
     * TOS
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value tos = 32;</code>
     */
    public Builder setTos(com.google.protobuf.UInt32Value value) {
      if (tosBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        tos_ = value;
      } else {
        tosBuilder_.setMessage(value);
      }
      bitField0_ |= 0x40000000;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * TOS
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value tos = 32;</code>
     */
    public Builder setTos(
            com.google.protobuf.UInt32Value.Builder builderForValue) {
      if (tosBuilder_ == null) {
        tos_ = builderForValue.build();
      } else {
        tosBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x40000000;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * TOS
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value tos = 32;</code>
     */
    public Builder mergeTos(com.google.protobuf.UInt32Value value) {
      if (tosBuilder_ == null) {
        if (((bitField0_ & 0x40000000) != 0) &&
                tos_ != null &&
                tos_ != com.google.protobuf.UInt32Value.getDefaultInstance()) {
          getTosBuilder().mergeFrom(value);
        } else {
          tos_ = value;
        }
      } else {
        tosBuilder_.mergeFrom(value);
      }
      if (tos_ != null) {
        bitField0_ |= 0x40000000;
        onChanged();
      }
      return this;
    }
    /**
     * <pre>
     * TOS
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value tos = 32;</code>
     */
    public Builder clearTos() {
      bitField0_ = (bitField0_ & ~0x40000000);
      tos_ = null;
      if (tosBuilder_ != null) {
        tosBuilder_.dispose();
        tosBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <pre>
     * TOS
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value tos = 32;</code>
     */
    public com.google.protobuf.UInt32Value.Builder getTosBuilder() {
      bitField0_ |= 0x40000000;
      onChanged();
      return getTosFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * TOS
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value tos = 32;</code>
     */
    public com.google.protobuf.UInt32ValueOrBuilder getTosOrBuilder() {
      if (tosBuilder_ != null) {
        return tosBuilder_.getMessageOrBuilder();
      } else {
        return tos_ == null ?
                com.google.protobuf.UInt32Value.getDefaultInstance() : tos_;
      }
    }
    /**
     * <pre>
     * TOS
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value tos = 32;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.UInt32Value, com.google.protobuf.UInt32Value.Builder, com.google.protobuf.UInt32ValueOrBuilder>
    getTosFieldBuilder() {
      if (tosBuilder_ == null) {
        tosBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
                com.google.protobuf.UInt32Value, com.google.protobuf.UInt32Value.Builder, com.google.protobuf.UInt32ValueOrBuilder>(
                getTos(),
                getParentForChildren(),
                isClean());
        tos_ = null;
      }
      return tosBuilder_;
    }

    private int netflowVersion_ = 0;
    /**
     * <pre>
     * Netflow version
     * </pre>
     *
     * <code>.NetflowVersion netflow_version = 33;</code>
     * @return The enum numeric value on the wire for netflowVersion.
     */
    @java.lang.Override public int getNetflowVersionValue() {
      return netflowVersion_;
    }
    /**
     * <pre>
     * Netflow version
     * </pre>
     *
     * <code>.NetflowVersion netflow_version = 33;</code>
     * @param value The enum numeric value on the wire for netflowVersion to set.
     * @return This builder for chaining.
     */
    public Builder setNetflowVersionValue(int value) {
      netflowVersion_ = value;
      bitField0_ |= 0x80000000;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Netflow version
     * </pre>
     *
     * <code>.NetflowVersion netflow_version = 33;</code>
     * @return The netflowVersion.
     */
    @java.lang.Override
    public org.opennms.netmgt.telemetry.protocols.netflow.transport.NetflowVersion getNetflowVersion() {
      org.opennms.netmgt.telemetry.protocols.netflow.transport.NetflowVersion result = org.opennms.netmgt.telemetry.protocols.netflow.transport.NetflowVersion.forNumber(netflowVersion_);
      return result == null ? org.opennms.netmgt.telemetry.protocols.netflow.transport.NetflowVersion.UNRECOGNIZED : result;
    }
    /**
     * <pre>
     * Netflow version
     * </pre>
     *
     * <code>.NetflowVersion netflow_version = 33;</code>
     * @param value The netflowVersion to set.
     * @return This builder for chaining.
     */
    public Builder setNetflowVersion(org.opennms.netmgt.telemetry.protocols.netflow.transport.NetflowVersion value) {
      if (value == null) {
        throw new NullPointerException();
      }
      bitField0_ |= 0x80000000;
      netflowVersion_ = value.getNumber();
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Netflow version
     * </pre>
     *
     * <code>.NetflowVersion netflow_version = 33;</code>
     * @return This builder for chaining.
     */
    public Builder clearNetflowVersion() {
      bitField0_ = (bitField0_ & ~0x80000000);
      netflowVersion_ = 0;
      onChanged();
      return this;
    }

    private com.google.protobuf.UInt32Value vlan_;
    private com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.UInt32Value, com.google.protobuf.UInt32Value.Builder, com.google.protobuf.UInt32ValueOrBuilder> vlanBuilder_;
    /**
     * <pre>
     * VLAN ID.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value vlan = 34;</code>
     * @return Whether the vlan field is set.
     */
    public boolean hasVlan() {
      return ((bitField1_ & 0x00000001) != 0);
    }
    /**
     * <pre>
     * VLAN ID.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value vlan = 34;</code>
     * @return The vlan.
     */
    public com.google.protobuf.UInt32Value getVlan() {
      if (vlanBuilder_ == null) {
        return vlan_ == null ? com.google.protobuf.UInt32Value.getDefaultInstance() : vlan_;
      } else {
        return vlanBuilder_.getMessage();
      }
    }
    /**
     * <pre>
     * VLAN ID.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value vlan = 34;</code>
     */
    public Builder setVlan(com.google.protobuf.UInt32Value value) {
      if (vlanBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        vlan_ = value;
      } else {
        vlanBuilder_.setMessage(value);
      }
      bitField1_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * VLAN ID.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value vlan = 34;</code>
     */
    public Builder setVlan(
            com.google.protobuf.UInt32Value.Builder builderForValue) {
      if (vlanBuilder_ == null) {
        vlan_ = builderForValue.build();
      } else {
        vlanBuilder_.setMessage(builderForValue.build());
      }
      bitField1_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * VLAN ID.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value vlan = 34;</code>
     */
    public Builder mergeVlan(com.google.protobuf.UInt32Value value) {
      if (vlanBuilder_ == null) {
        if (((bitField1_ & 0x00000001) != 0) &&
                vlan_ != null &&
                vlan_ != com.google.protobuf.UInt32Value.getDefaultInstance()) {
          getVlanBuilder().mergeFrom(value);
        } else {
          vlan_ = value;
        }
      } else {
        vlanBuilder_.mergeFrom(value);
      }
      if (vlan_ != null) {
        bitField1_ |= 0x00000001;
        onChanged();
      }
      return this;
    }
    /**
     * <pre>
     * VLAN ID.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value vlan = 34;</code>
     */
    public Builder clearVlan() {
      bitField1_ = (bitField1_ & ~0x00000001);
      vlan_ = null;
      if (vlanBuilder_ != null) {
        vlanBuilder_.dispose();
        vlanBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <pre>
     * VLAN ID.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value vlan = 34;</code>
     */
    public com.google.protobuf.UInt32Value.Builder getVlanBuilder() {
      bitField1_ |= 0x00000001;
      onChanged();
      return getVlanFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * VLAN ID.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value vlan = 34;</code>
     */
    public com.google.protobuf.UInt32ValueOrBuilder getVlanOrBuilder() {
      if (vlanBuilder_ != null) {
        return vlanBuilder_.getMessageOrBuilder();
      } else {
        return vlan_ == null ?
                com.google.protobuf.UInt32Value.getDefaultInstance() : vlan_;
      }
    }
    /**
     * <pre>
     * VLAN ID.
     * </pre>
     *
     * <code>.google.protobuf.UInt32Value vlan = 34;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.UInt32Value, com.google.protobuf.UInt32Value.Builder, com.google.protobuf.UInt32ValueOrBuilder>
    getVlanFieldBuilder() {
      if (vlanBuilder_ == null) {
        vlanBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
                com.google.protobuf.UInt32Value, com.google.protobuf.UInt32Value.Builder, com.google.protobuf.UInt32ValueOrBuilder>(
                getVlan(),
                getParentForChildren(),
                isClean());
        vlan_ = null;
      }
      return vlanBuilder_;
    }

    private java.lang.Object nodeIdentifier_ = "";
    /**
     * <pre>
     * node lookup identifier.
     * </pre>
     *
     * <code>string node_identifier = 35;</code>
     * @return The nodeIdentifier.
     */
    public java.lang.String getNodeIdentifier() {
      java.lang.Object ref = nodeIdentifier_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
                (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        nodeIdentifier_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <pre>
     * node lookup identifier.
     * </pre>
     *
     * <code>string node_identifier = 35;</code>
     * @return The bytes for nodeIdentifier.
     */
    public com.google.protobuf.ByteString
    getNodeIdentifierBytes() {
      java.lang.Object ref = nodeIdentifier_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b =
                com.google.protobuf.ByteString.copyFromUtf8(
                        (java.lang.String) ref);
        nodeIdentifier_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <pre>
     * node lookup identifier.
     * </pre>
     *
     * <code>string node_identifier = 35;</code>
     * @param value The nodeIdentifier to set.
     * @return This builder for chaining.
     */
    public Builder setNodeIdentifier(
            java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      nodeIdentifier_ = value;
      bitField1_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * node lookup identifier.
     * </pre>
     *
     * <code>string node_identifier = 35;</code>
     * @return This builder for chaining.
     */
    public Builder clearNodeIdentifier() {
      nodeIdentifier_ = getDefaultInstance().getNodeIdentifier();
      bitField1_ = (bitField1_ & ~0x00000002);
      onChanged();
      return this;
    }
    /**
     * <pre>
     * node lookup identifier.
     * </pre>
     *
     * <code>string node_identifier = 35;</code>
     * @param value The bytes for nodeIdentifier to set.
     * @return This builder for chaining.
     */
    public Builder setNodeIdentifierBytes(
            com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      nodeIdentifier_ = value;
      bitField1_ |= 0x00000002;
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


    // @@protoc_insertion_point(builder_scope:FlowMessage)
  }

  // @@protoc_insertion_point(class_scope:FlowMessage)
  private static final org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage();
  }

  public static org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<FlowMessage>
          PARSER = new com.google.protobuf.AbstractParser<FlowMessage>() {
    @java.lang.Override
    public FlowMessage parsePartialFrom(
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

  public static com.google.protobuf.Parser<FlowMessage> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<FlowMessage> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

