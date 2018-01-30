package org.opennms.netmgt.telemetry.listeners.sflow.proto.flows;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;

import org.opennms.netmgt.telemetry.listeners.api.utils.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.sflow.proto.*;

// struct processor {
//    percentage cpu_5s;           /* 5 second average CPU utilization */
//    percentage cpu_1m;           /* 1 minute average CPU utilization */
//    percentage cpu_5m;           /* 5 minute average CPU utilization */
//    unsigned hyper total_memory; /* total memory (in bytes) */
//    unsigned hyper free_memory;  /* free memory (in bytes) */
// };

public class Processor  {
  public final Percentage cpu_5s;
  public final Percentage cpu_1m;
  public final Percentage cpu_5m;
  public final UnsignedLong total_memory;
  public final UnsignedLong free_memory;

  public Processor (final ByteBuffer buffer) throws InvalidPacketException {
    this.cpu_5s = new Percentage(buffer);
    this.cpu_1m = new Percentage(buffer);
    this.cpu_5m = new Percentage(buffer);
    this.total_memory = BufferUtils.uint64(buffer);
    this.free_memory = BufferUtils.uint64(buffer);
  }
}
