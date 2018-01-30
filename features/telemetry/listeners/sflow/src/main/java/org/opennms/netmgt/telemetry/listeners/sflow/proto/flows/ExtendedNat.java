package org.opennms.netmgt.telemetry.listeners.sflow.proto.flows;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;

import org.opennms.netmgt.telemetry.listeners.api.utils.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.sflow.proto.*;

// struct extended_nat {
//      address src_address;            /* Source address */
//      address dst_address;            /* Destination address */
// };

public class ExtendedNat  {
  public final Address src_address;
  public final Address dst_address;

  public ExtendedNat (final ByteBuffer buffer) throws InvalidPacketException {
    this.src_address = new Address(buffer);
    this.dst_address = new Address(buffer);
  }
}
