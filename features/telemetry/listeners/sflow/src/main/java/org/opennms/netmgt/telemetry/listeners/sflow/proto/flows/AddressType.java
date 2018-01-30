package org.opennms.netmgt.telemetry.listeners.sflow.proto.flows;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;

import org.opennms.netmgt.telemetry.listeners.api.utils.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.sflow.proto.*;

// enum address_type {
//    UNKNOWN  = 0,
//    IP_V4    = 1,
//    IP_V6    = 2
// };

public enum AddressType  {
  UNKNOWN(0),
  IP_V4(1),
  IP_V6(2);

  public final int value;

          AddressType(final int value) {
      this.value = value;
          }

  public static AddressType from(final ByteBuffer buffer) throws InvalidPacketException {
    final int value = (int) BufferUtils.uint32(buffer).intValue();
            switch (value) {
      case 0: return UNKNOWN;
      case 1: return IP_V4;
      case 2: return IP_V6;
      default:
        throw new InvalidPacketException(buffer, "Unknown value: {}", value);
    }
  }
}
