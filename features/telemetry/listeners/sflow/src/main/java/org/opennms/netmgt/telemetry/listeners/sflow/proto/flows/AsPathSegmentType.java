package org.opennms.netmgt.telemetry.listeners.sflow.proto.flows;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;

import org.opennms.netmgt.telemetry.listeners.api.utils.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.sflow.proto.*;

// enum as_path_segment_type {
//    AS_SET      = 1,            /* Unordered set of ASs */
//    AS_SEQUENCE = 2             /* Ordered set of ASs */
// };

public enum AsPathSegmentType  {
  AS_SET(1),
  AS_SEQUENCE(2);

  public final int value;

          AsPathSegmentType(final int value) {
      this.value = value;
          }

  public static AsPathSegmentType from(final ByteBuffer buffer) throws InvalidPacketException {
    final int value = (int) BufferUtils.uint32(buffer).intValue();
            switch (value) {
      case 1: return AS_SET;
      case 2: return AS_SEQUENCE;
      default:
        throw new InvalidPacketException(buffer, "Unknown value: {}", value);
    }
  }
}
