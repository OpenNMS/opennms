package org.opennms.netmgt.telemetry.listeners.sflow.proto.flows;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;

import org.opennms.netmgt.telemetry.listeners.api.utils.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.sflow.proto.*;

// typedef int label_stack<>;

public class LabelStack  {
  public final Array<Integer> label_stack;

  public LabelStack(final ByteBuffer buffer) throws InvalidPacketException {
  this.label_stack = new Array(buffer, Optional.empty(), BufferUtils::sint32);
  }
}
