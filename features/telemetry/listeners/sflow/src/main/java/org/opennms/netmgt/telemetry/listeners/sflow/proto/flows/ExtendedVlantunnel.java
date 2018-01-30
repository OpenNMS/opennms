package org.opennms.netmgt.telemetry.listeners.sflow.proto.flows;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;

import org.opennms.netmgt.telemetry.listeners.api.utils.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.sflow.proto.*;

// struct extended_vlantunnel { 
//   unsigned int stack<>;  /* List of stripped 802.1Q TPID/TCI layers. Each 
//                             TPID,TCI pair is represented as a single 32 bit 
//                             integer. Layers listed from outermost to 
//                             innermost. */ 
// };

public class ExtendedVlantunnel  {
  public final Array<UnsignedInteger> stack;

  public ExtendedVlantunnel (final ByteBuffer buffer) throws InvalidPacketException {
    this.stack = new Array(buffer, Optional.empty(), BufferUtils::uint32);
  }
}
