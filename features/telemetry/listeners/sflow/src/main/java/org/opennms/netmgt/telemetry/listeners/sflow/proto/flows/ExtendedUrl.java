package org.opennms.netmgt.telemetry.listeners.sflow.proto.flows;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;

import org.opennms.netmgt.telemetry.listeners.api.utils.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.sflow.proto.*;

// struct extended_url {
//    url_direction direction;    /* Direction of connection */
//    string url<>;               /* The HTTP request-line (see RFC 2616) */
//    string host<>;              /* The host field from the HTTP header */
// };

public class ExtendedUrl  {
  public final UrlDirection direction;
  public final AsciiString url;
  public final AsciiString host;

  public ExtendedUrl (final ByteBuffer buffer) throws InvalidPacketException {
    this.direction = UrlDirection.from(buffer);
    this.url = new AsciiString(buffer, Optional.empty());
    this.host = new AsciiString(buffer, Optional.empty());
  }
}
