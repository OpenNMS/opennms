package org.opennms.netmgt.telemetry.listeners.sflow.proto.flows;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;

import org.opennms.netmgt.telemetry.listeners.api.utils.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.sflow.proto.*;

// struct extended_switch {
//    unsigned int src_vlan;     /* The 802.1Q VLAN id of incoming frame */
//    unsigned int src_priority; /* The 802.1p priority of incoming frame */
//    unsigned int dst_vlan;     /* The 802.1Q VLAN id of outgoing frame */
//    unsigned int dst_priority; /* The 802.1p priority of outgoing frame */
// };

public class ExtendedSwitch  {
  public final UnsignedInteger src_vlan;
  public final UnsignedInteger src_priority;
  public final UnsignedInteger dst_vlan;
  public final UnsignedInteger dst_priority;

  public ExtendedSwitch (final ByteBuffer buffer) throws InvalidPacketException {
    this.src_vlan = BufferUtils.uint32(buffer);
    this.src_priority = BufferUtils.uint32(buffer);
    this.dst_vlan = BufferUtils.uint32(buffer);
    this.dst_priority = BufferUtils.uint32(buffer);
  }
}
