package org.opennms.netmgt.telemetry.listeners.sflow.proto.flows;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;

import org.opennms.netmgt.telemetry.listeners.api.utils.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.sflow.proto.*;

// struct extended_mpls_LDP_FEC {
//    unsigned int mplsFecAddrPrefixLength;
// };

public class ExtendedMplsLdpFec  {
  public final UnsignedInteger mplsFecAddrPrefixLength;

  public ExtendedMplsLdpFec (final ByteBuffer buffer) throws InvalidPacketException {
    this.mplsFecAddrPrefixLength = BufferUtils.uint32(buffer);
  }
}
