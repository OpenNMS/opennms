package org.opennms.netmgt.telemetry.listeners.sflow.proto.flows;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;

import org.opennms.netmgt.telemetry.listeners.api.utils.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.sflow.proto.*;

// struct extended_mpls_vc {
//    string vc_instance_name<>;  /* VC instance name */
//    unsigned int vll_vc_id;     /* VLL/VC instance ID */
//    unsigned int vc_label_cos;  /* VC Label COS value */
// };

public class ExtendedMplsVc  {
  public final AsciiString vc_instance_name;
  public final UnsignedInteger vll_vc_id;
  public final UnsignedInteger vc_label_cos;

  public ExtendedMplsVc (final ByteBuffer buffer) throws InvalidPacketException {
    this.vc_instance_name = new AsciiString(buffer, Optional.empty());
    this.vll_vc_id = BufferUtils.uint32(buffer);
    this.vc_label_cos = BufferUtils.uint32(buffer);
  }
}
