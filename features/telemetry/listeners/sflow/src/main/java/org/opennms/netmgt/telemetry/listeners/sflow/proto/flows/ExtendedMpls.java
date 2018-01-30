package org.opennms.netmgt.telemetry.listeners.sflow.proto.flows;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;

import org.opennms.netmgt.telemetry.listeners.api.utils.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.sflow.proto.*;

// struct extended_mpls { 
//    next_hop nexthop;           /* Address of the next hop */ 
//    label_stack in_stack;       /* Label stack of received packet */ 
//    label_stack out_stack;      /* Label stack for transmitted packet */ 
// };

public class ExtendedMpls  {
  public final NextHop nexthop;
  public final LabelStack in_stack;
  public final LabelStack out_stack;

  public ExtendedMpls (final ByteBuffer buffer) throws InvalidPacketException {
    this.nexthop = new NextHop(buffer);
    this.in_stack = new LabelStack(buffer);
    this.out_stack = new LabelStack(buffer);
  }
}
