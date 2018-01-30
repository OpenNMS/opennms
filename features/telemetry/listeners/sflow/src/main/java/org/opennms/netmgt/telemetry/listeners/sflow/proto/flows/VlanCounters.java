package org.opennms.netmgt.telemetry.listeners.sflow.proto.flows;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;

import org.opennms.netmgt.telemetry.listeners.api.utils.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.sflow.proto.*;

// struct vlan_counters {
//   unsigned int vlan_id;
//   unsigned hyper octets;
//   unsigned int ucastPkts;
//   unsigned int multicastPkts;
//   unsigned int broadcastPkts;
//   unsigned int discards;
// };

public class VlanCounters  {
  public final UnsignedInteger vlan_id;
  public final UnsignedLong octets;
  public final UnsignedInteger ucastPkts;
  public final UnsignedInteger multicastPkts;
  public final UnsignedInteger broadcastPkts;
  public final UnsignedInteger discards;

  public VlanCounters (final ByteBuffer buffer) throws InvalidPacketException {
    this.vlan_id = BufferUtils.uint32(buffer);
    this.octets = BufferUtils.uint64(buffer);
    this.ucastPkts = BufferUtils.uint32(buffer);
    this.multicastPkts = BufferUtils.uint32(buffer);
    this.broadcastPkts = BufferUtils.uint32(buffer);
    this.discards = BufferUtils.uint32(buffer);
  }
}
