package org.opennms.netmgt.telemetry.listeners.sflow.proto.flows;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;

import org.opennms.netmgt.telemetry.listeners.api.utils.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.sflow.proto.*;

// enum url_direction {
//    src    = 1,                 /* Source address is server */
//    dst    = 2                  /* Destination address is server */
// };

public enum UrlDirection  {
  src(1),
  dst(2);

  public final int value;

          UrlDirection(final int value) {
      this.value = value;
          }

  public static UrlDirection from(final ByteBuffer buffer) throws InvalidPacketException {
    final int value = (int) BufferUtils.uint32(buffer).intValue();
            switch (value) {
      case 1: return src;
      case 2: return dst;
      default:
        throw new InvalidPacketException(buffer, "Unknown value: {}", value);
    }
  }
}
