package org.opennms.netmgt.telemetry.listeners.sflow.proto.flows;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;

import org.opennms.netmgt.telemetry.listeners.api.utils.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.sflow.proto.*;

// struct sampled_ethernet {
//      unsigned int length;   /* The length of the MAC packet received on the
//                                network, excluding lower layer encapsulations
//                                and framing bits but including FCS octets */
//      mac src_mac;           /* Source MAC address */
//      mac dst_mac;           /* Destination MAC address */
//      unsigned int type;     /* Ethernet packet type */
// };

public class SampledEthernet  {
  public final UnsignedInteger length;
  public final Mac src_mac;
  public final Mac dst_mac;
  public final UnsignedInteger type;

  public SampledEthernet (final ByteBuffer buffer) throws InvalidPacketException {
    this.length = BufferUtils.uint32(buffer);
    this.src_mac = new Mac(buffer);
    this.dst_mac = new Mac(buffer);
    this.type = BufferUtils.uint32(buffer);
  }
}
