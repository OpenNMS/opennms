package org.opennms.netmgt.telemetry.listeners.sflow.proto.flows;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;

import org.opennms.netmgt.telemetry.listeners.api.utils.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.sflow.proto.*;

// enum header_protocol {
//    ETHERNET_ISO88023    = 1,
//    ISO88024_TOKENBUS    = 2,
//    ISO88025_TOKENRING   = 3,
//    FDDI                 = 4,
//    FRAME_RELAY          = 5,
//    X25                  = 6,
//    PPP                  = 7,
//    SMDS                 = 8,                
//    AAL5                 = 9,
//    AAL5_IP              = 10, /* e.g. Cisco AAL5 mux */
//    IPv4                 = 11,
//    IPv6                 = 12,
//    MPLS                 = 13,
//    POS                  = 14  /* RFC 1662, 2615 */
// };

public enum HeaderProtocol  {
  ETHERNET_ISO88023(1),
  ISO88024_TOKENBUS(2),
  ISO88025_TOKENRING(3),
  FDDI(4),
  FRAME_RELAY(5),
  X25(6),
  PPP(7),
  SMDS(8),
  AAL5(9),
  AAL5_IP(10),
  IPv4(11),
  IPv6(12),
  MPLS(13),
  POS(14);

  public final int value;

          HeaderProtocol(final int value) {
      this.value = value;
          }

  public static HeaderProtocol from(final ByteBuffer buffer) throws InvalidPacketException {
    final int value = (int) BufferUtils.uint32(buffer).intValue();
            switch (value) {
      case 1: return ETHERNET_ISO88023;
      case 2: return ISO88024_TOKENBUS;
      case 3: return ISO88025_TOKENRING;
      case 4: return FDDI;
      case 5: return FRAME_RELAY;
      case 6: return X25;
      case 7: return PPP;
      case 8: return SMDS;
      case 9: return AAL5;
      case 10: return AAL5_IP;
      case 11: return IPv4;
      case 12: return IPv6;
      case 13: return MPLS;
      case 14: return POS;
      default:
        throw new InvalidPacketException(buffer, "Unknown value: {}", value);
    }
  }
}
