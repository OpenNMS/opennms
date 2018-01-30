package org.opennms.netmgt.telemetry.listeners.sflow.proto.flows;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;

import org.opennms.netmgt.telemetry.listeners.api.utils.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.sflow.proto.*;

// typedef opaque mac[6];

public class Mac  {
  public final Opaque<byte[]> mac;

  public Mac(final ByteBuffer buffer) throws InvalidPacketException {
  this.mac = new Opaque(buffer, Optional.of(6), Opaque::parseBytes);
  }
}
