package org.opennms.netmgt.telemetry.listeners.sflow.proto.flows;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;

import org.opennms.netmgt.telemetry.listeners.api.utils.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.sflow.proto.*;

// struct ethernet_counters {
//    unsigned int dot3StatsAlignmentErrors;
//    unsigned int dot3StatsFCSErrors;
//    unsigned int dot3StatsSingleCollisionFrames;
//    unsigned int dot3StatsMultipleCollisionFrames;
//    unsigned int dot3StatsSQETestErrors;
//    unsigned int dot3StatsDeferredTransmissions;
//    unsigned int dot3StatsLateCollisions;
//    unsigned int dot3StatsExcessiveCollisions;
//    unsigned int dot3StatsInternalMacTransmitErrors;
//    unsigned int dot3StatsCarrierSenseErrors;
//    unsigned int dot3StatsFrameTooLongs;
//    unsigned int dot3StatsInternalMacReceiveErrors;
//    unsigned int dot3StatsSymbolErrors;
// };

public class EthernetCounters  {
  public final UnsignedInteger dot3StatsAlignmentErrors;
  public final UnsignedInteger dot3StatsFCSErrors;
  public final UnsignedInteger dot3StatsSingleCollisionFrames;
  public final UnsignedInteger dot3StatsMultipleCollisionFrames;
  public final UnsignedInteger dot3StatsSQETestErrors;
  public final UnsignedInteger dot3StatsDeferredTransmissions;
  public final UnsignedInteger dot3StatsLateCollisions;
  public final UnsignedInteger dot3StatsExcessiveCollisions;
  public final UnsignedInteger dot3StatsInternalMacTransmitErrors;
  public final UnsignedInteger dot3StatsCarrierSenseErrors;
  public final UnsignedInteger dot3StatsFrameTooLongs;
  public final UnsignedInteger dot3StatsInternalMacReceiveErrors;
  public final UnsignedInteger dot3StatsSymbolErrors;

  public EthernetCounters (final ByteBuffer buffer) throws InvalidPacketException {
    this.dot3StatsAlignmentErrors = BufferUtils.uint32(buffer);
    this.dot3StatsFCSErrors = BufferUtils.uint32(buffer);
    this.dot3StatsSingleCollisionFrames = BufferUtils.uint32(buffer);
    this.dot3StatsMultipleCollisionFrames = BufferUtils.uint32(buffer);
    this.dot3StatsSQETestErrors = BufferUtils.uint32(buffer);
    this.dot3StatsDeferredTransmissions = BufferUtils.uint32(buffer);
    this.dot3StatsLateCollisions = BufferUtils.uint32(buffer);
    this.dot3StatsExcessiveCollisions = BufferUtils.uint32(buffer);
    this.dot3StatsInternalMacTransmitErrors = BufferUtils.uint32(buffer);
    this.dot3StatsCarrierSenseErrors = BufferUtils.uint32(buffer);
    this.dot3StatsFrameTooLongs = BufferUtils.uint32(buffer);
    this.dot3StatsInternalMacReceiveErrors = BufferUtils.uint32(buffer);
    this.dot3StatsSymbolErrors = BufferUtils.uint32(buffer);
  }
}
