package org.opennms.netmgt.telemetry.listeners.sflow.proto.flows;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;

import org.opennms.netmgt.telemetry.listeners.api.utils.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.sflow.proto.*;

// typedef opaque ip_v6[16];

public class IpV6  {
  public final Opaque<byte[]> ip_v6;

  public IpV6(final ByteBuffer buffer) throws InvalidPacketException {
  this.ip_v6 = new Opaque(buffer, Optional.of(16), Opaque::parseBytes);
  }
}
