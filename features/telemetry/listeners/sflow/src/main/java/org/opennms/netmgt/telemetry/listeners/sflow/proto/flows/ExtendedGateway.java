package org.opennms.netmgt.telemetry.listeners.sflow.proto.flows;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;

import org.opennms.netmgt.telemetry.listeners.api.utils.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.sflow.proto.*;

// struct extended_gateway {
//    next_hop nexthop;           /* Address of the border router that should
//                                   be used for the destination network */
//    unsigned int as;            /* Autonomous system number of router */
//    unsigned int src_as;        /* Autonomous system number of source */
//    unsigned int src_peer_as;   /* Autonomous system number of source peer */
//    as_path_type dst_as_path<>; /* Autonomous system path to the destination */
//    unsigned int communities<>; /* Communities associated with this route */
//    unsigned int localpref;     /* LocalPref associated with this route */
// };

public class ExtendedGateway  {
  public final NextHop nexthop;
  public final UnsignedInteger as;
  public final UnsignedInteger src_as;
  public final UnsignedInteger src_peer_as;
  public final Array<AsPathType> dst_as_path;
  public final Array<UnsignedInteger> communities;
  public final UnsignedInteger localpref;

  public ExtendedGateway (final ByteBuffer buffer) throws InvalidPacketException {
    this.nexthop = new NextHop(buffer);
    this.as = BufferUtils.uint32(buffer);
    this.src_as = BufferUtils.uint32(buffer);
    this.src_peer_as = BufferUtils.uint32(buffer);
    this.dst_as_path = new Array(buffer, Optional.empty(), AsPathType::new);
    this.communities = new Array(buffer, Optional.empty(), BufferUtils::uint32);
    this.localpref = BufferUtils.uint32(buffer);
  }
}
