package org.opennms.netmgt.telemetry.listeners.sflow.proto.flows;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;

import org.opennms.netmgt.telemetry.listeners.api.utils.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.sflow.proto.*;

// struct vg_counters {
//   unsigned int dot12InHighPriorityFrames;
//   unsigned hyper dot12InHighPriorityOctets;
//   unsigned int dot12InNormPriorityFrames;
//   unsigned hyper dot12InNormPriorityOctets;
//   unsigned int dot12InIPMErrors;
//   unsigned int dot12InOversizeFrameErrors;
//   unsigned int dot12InDataErrors;
//   unsigned int dot12InNullAddressedFrames;
//   unsigned int dot12OutHighPriorityFrames;
//   unsigned hyper dot12OutHighPriorityOctets;
//   unsigned int dot12TransitionIntoTrainings;
//   unsigned hyper dot12HCInHighPriorityOctets;
//   unsigned hyper dot12HCInNormPriorityOctets;
//   unsigned hyper dot12HCOutHighPriorityOctets;
// };

public class VgCounters  {
  public final UnsignedInteger dot12InHighPriorityFrames;
  public final UnsignedLong dot12InHighPriorityOctets;
  public final UnsignedInteger dot12InNormPriorityFrames;
  public final UnsignedLong dot12InNormPriorityOctets;
  public final UnsignedInteger dot12InIPMErrors;
  public final UnsignedInteger dot12InOversizeFrameErrors;
  public final UnsignedInteger dot12InDataErrors;
  public final UnsignedInteger dot12InNullAddressedFrames;
  public final UnsignedInteger dot12OutHighPriorityFrames;
  public final UnsignedLong dot12OutHighPriorityOctets;
  public final UnsignedInteger dot12TransitionIntoTrainings;
  public final UnsignedLong dot12HCInHighPriorityOctets;
  public final UnsignedLong dot12HCInNormPriorityOctets;
  public final UnsignedLong dot12HCOutHighPriorityOctets;

  public VgCounters (final ByteBuffer buffer) throws InvalidPacketException {
    this.dot12InHighPriorityFrames = BufferUtils.uint32(buffer);
    this.dot12InHighPriorityOctets = BufferUtils.uint64(buffer);
    this.dot12InNormPriorityFrames = BufferUtils.uint32(buffer);
    this.dot12InNormPriorityOctets = BufferUtils.uint64(buffer);
    this.dot12InIPMErrors = BufferUtils.uint32(buffer);
    this.dot12InOversizeFrameErrors = BufferUtils.uint32(buffer);
    this.dot12InDataErrors = BufferUtils.uint32(buffer);
    this.dot12InNullAddressedFrames = BufferUtils.uint32(buffer);
    this.dot12OutHighPriorityFrames = BufferUtils.uint32(buffer);
    this.dot12OutHighPriorityOctets = BufferUtils.uint64(buffer);
    this.dot12TransitionIntoTrainings = BufferUtils.uint32(buffer);
    this.dot12HCInHighPriorityOctets = BufferUtils.uint64(buffer);
    this.dot12HCInNormPriorityOctets = BufferUtils.uint64(buffer);
    this.dot12HCOutHighPriorityOctets = BufferUtils.uint64(buffer);
  }
}
