package org.opennms.netmgt.telemetry.listeners.sflow.proto.flows;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;

import org.opennms.netmgt.telemetry.listeners.api.utils.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.sflow.proto.*;

// struct extended_mpls_tunnel {
//    string tunnel_lsp_name<>;   /* Tunnel name */
//    unsigned int tunnel_id;     /* Tunnel ID */
//    unsigned int tunnel_cos;    /* Tunnel COS value */
// };

public class ExtendedMplsTunnel  {
  public final AsciiString tunnel_lsp_name;
  public final UnsignedInteger tunnel_id;
  public final UnsignedInteger tunnel_cos;

  public ExtendedMplsTunnel (final ByteBuffer buffer) throws InvalidPacketException {
    this.tunnel_lsp_name = new AsciiString(buffer, Optional.empty());
    this.tunnel_id = BufferUtils.uint32(buffer);
    this.tunnel_cos = BufferUtils.uint32(buffer);
  }
}
