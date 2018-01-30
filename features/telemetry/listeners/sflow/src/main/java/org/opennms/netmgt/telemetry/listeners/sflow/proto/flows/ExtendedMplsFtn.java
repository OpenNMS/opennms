package org.opennms.netmgt.telemetry.listeners.sflow.proto.flows;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;

import org.opennms.netmgt.telemetry.listeners.api.utils.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.sflow.proto.*;

// struct extended_mpls_FTN {
//    string mplsFTNDescr<>;
//    unsigned int mplsFTNMask;
// };

public class ExtendedMplsFtn  {
  public final AsciiString mplsFTNDescr;
  public final UnsignedInteger mplsFTNMask;

  public ExtendedMplsFtn (final ByteBuffer buffer) throws InvalidPacketException {
    this.mplsFTNDescr = new AsciiString(buffer, Optional.empty());
    this.mplsFTNMask = BufferUtils.uint32(buffer);
  }
}
