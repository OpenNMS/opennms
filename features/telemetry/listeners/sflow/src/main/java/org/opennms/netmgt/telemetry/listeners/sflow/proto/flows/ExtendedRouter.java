package org.opennms.netmgt.telemetry.listeners.sflow.proto.flows;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;

import org.opennms.netmgt.telemetry.listeners.api.utils.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.sflow.proto.*;

// struct extended_router {
//    next_hop nexthop;            /* IP address of next hop router */
//    unsigned int src_mask_len;   /* Source address prefix mask
//                                    (expressed as number of bits) */
//    unsigned int dst_mask_len;   /* Destination address prefix mask
//                                    (expressed as number of bits) */
// };

public class ExtendedRouter  {
  public final NextHop nexthop;
  public final UnsignedInteger src_mask_len;
  public final UnsignedInteger dst_mask_len;

  public ExtendedRouter (final ByteBuffer buffer) throws InvalidPacketException {
    this.nexthop = new NextHop(buffer);
    this.src_mask_len = BufferUtils.uint32(buffer);
    this.dst_mask_len = BufferUtils.uint32(buffer);
  }
}
