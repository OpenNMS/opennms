package org.opennms.netmgt.telemetry.listeners.sflow.proto.flows;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;

import org.opennms.netmgt.telemetry.listeners.api.utils.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.sflow.proto.*;

// struct sampled_ipv4 {
//    unsigned int length;     /* The length of the IP packet excluding 
//                                lower layer encapsulations */
//    unsigned int protocol;   /* IP Protocol type
//                                (for example, TCP = 6, UDP = 17) */
//    ip_v4 src_ip;            /* Source IP Address */
//    ip_v4 dst_ip;            /* Destination IP Address */
//    unsigned int src_port;   /* TCP/UDP source port number or equivalent */
//    unsigned int dst_port;   /* TCP/UDP destination port number or equivalent */
//    unsigned int tcp_flags;  /* TCP flags */
//    unsigned int tos;        /* IP type of service */
// };

public class SampledIpv4  {
  public final UnsignedInteger length;
  public final UnsignedInteger protocol;
  public final IpV4 src_ip;
  public final IpV4 dst_ip;
  public final UnsignedInteger src_port;
  public final UnsignedInteger dst_port;
  public final UnsignedInteger tcp_flags;
  public final UnsignedInteger tos;

  public SampledIpv4 (final ByteBuffer buffer) throws InvalidPacketException {
    this.length = BufferUtils.uint32(buffer);
    this.protocol = BufferUtils.uint32(buffer);
    this.src_ip = new IpV4(buffer);
    this.dst_ip = new IpV4(buffer);
    this.src_port = BufferUtils.uint32(buffer);
    this.dst_port = BufferUtils.uint32(buffer);
    this.tcp_flags = BufferUtils.uint32(buffer);
    this.tos = BufferUtils.uint32(buffer);
  }
}
