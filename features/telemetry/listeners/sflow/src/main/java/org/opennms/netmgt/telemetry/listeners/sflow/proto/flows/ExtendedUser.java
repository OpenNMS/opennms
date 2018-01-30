package org.opennms.netmgt.telemetry.listeners.sflow.proto.flows;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;

import org.opennms.netmgt.telemetry.listeners.api.utils.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.sflow.proto.*;

// struct extended_user {
//    charset src_charset;        /* Character set for src_user */
//    opaque src_user<>;          /* User ID associated with packet source */
//    charset dst_charset;        /* Character set for dst_user */
//    opaque dst_user<>;          /* User ID associated with packet destination */
// };

public class ExtendedUser  {
  public final Charset src_charset;
  public final Opaque<byte[]> src_user;
  public final Charset dst_charset;
  public final Opaque<byte[]> dst_user;

  public ExtendedUser (final ByteBuffer buffer) throws InvalidPacketException {
    this.src_charset = new Charset(buffer);
    this.src_user = new Opaque(buffer, Optional.empty(), Opaque::parseBytes);
    this.dst_charset = new Charset(buffer);
    this.dst_user = new Opaque(buffer, Optional.empty(), Opaque::parseBytes);
  }
}
