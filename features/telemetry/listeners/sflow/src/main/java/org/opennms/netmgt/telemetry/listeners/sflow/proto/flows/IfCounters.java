package org.opennms.netmgt.telemetry.listeners.sflow.proto.flows;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;

import org.opennms.netmgt.telemetry.listeners.api.utils.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.sflow.proto.*;

// struct if_counters {
//    unsigned int ifIndex;
//    unsigned int ifType;
//    unsigned hyper ifSpeed;
//    unsigned int ifDirection;    /* derived from MAU MIB (RFC 2668)
//                                    0 = unkown, 1=full-duplex, 2=half-duplex,
//                                    3 = in, 4=out */
//    unsigned int ifStatus;       /* bit field with the following bits assigned
//                                    bit 0 = ifAdminStatus (0 = down, 1 = up)
//                                    bit 1 = ifOperStatus (0 = down, 1 = up) */
//    unsigned hyper ifInOctets;
//    unsigned int ifInUcastPkts;
//    unsigned int ifInMulticastPkts;
//    unsigned int ifInBroadcastPkts;
//    unsigned int ifInDiscards;
//    unsigned int ifInErrors;
//    unsigned int ifInUnknownProtos;
//    unsigned hyper ifOutOctets;
//    unsigned int ifOutUcastPkts;
//    unsigned int ifOutMulticastPkts;
//    unsigned int ifOutBroadcastPkts;
//    unsigned int ifOutDiscards;
//    unsigned int ifOutErrors;
//    unsigned int ifPromiscuousMode;
// };

public class IfCounters  {
  public final UnsignedInteger ifIndex;
  public final UnsignedInteger ifType;
  public final UnsignedLong ifSpeed;
  public final UnsignedInteger ifDirection;
  public final UnsignedInteger ifStatus;
  public final UnsignedLong ifInOctets;
  public final UnsignedInteger ifInUcastPkts;
  public final UnsignedInteger ifInMulticastPkts;
  public final UnsignedInteger ifInBroadcastPkts;
  public final UnsignedInteger ifInDiscards;
  public final UnsignedInteger ifInErrors;
  public final UnsignedInteger ifInUnknownProtos;
  public final UnsignedLong ifOutOctets;
  public final UnsignedInteger ifOutUcastPkts;
  public final UnsignedInteger ifOutMulticastPkts;
  public final UnsignedInteger ifOutBroadcastPkts;
  public final UnsignedInteger ifOutDiscards;
  public final UnsignedInteger ifOutErrors;
  public final UnsignedInteger ifPromiscuousMode;

  public IfCounters (final ByteBuffer buffer) throws InvalidPacketException {
    this.ifIndex = BufferUtils.uint32(buffer);
    this.ifType = BufferUtils.uint32(buffer);
    this.ifSpeed = BufferUtils.uint64(buffer);
    this.ifDirection = BufferUtils.uint32(buffer);
    this.ifStatus = BufferUtils.uint32(buffer);
    this.ifInOctets = BufferUtils.uint64(buffer);
    this.ifInUcastPkts = BufferUtils.uint32(buffer);
    this.ifInMulticastPkts = BufferUtils.uint32(buffer);
    this.ifInBroadcastPkts = BufferUtils.uint32(buffer);
    this.ifInDiscards = BufferUtils.uint32(buffer);
    this.ifInErrors = BufferUtils.uint32(buffer);
    this.ifInUnknownProtos = BufferUtils.uint32(buffer);
    this.ifOutOctets = BufferUtils.uint64(buffer);
    this.ifOutUcastPkts = BufferUtils.uint32(buffer);
    this.ifOutMulticastPkts = BufferUtils.uint32(buffer);
    this.ifOutBroadcastPkts = BufferUtils.uint32(buffer);
    this.ifOutDiscards = BufferUtils.uint32(buffer);
    this.ifOutErrors = BufferUtils.uint32(buffer);
    this.ifPromiscuousMode = BufferUtils.uint32(buffer);
  }
}
