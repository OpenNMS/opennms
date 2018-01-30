package org.opennms.netmgt.telemetry.listeners.sflow.proto.flows;

import java.nio.ByteBuffer;
import java.util.Optional;

import org.opennms.netmgt.telemetry.listeners.api.utils.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.sflow.proto.Array;

import com.google.common.primitives.UnsignedInteger;

// union as_path_type switch (as_path_segment_type type) {
//    case AS_SET:
//       unsigned int as_set<>;
//    case AS_SEQUENCE:
//       unsigned int as_sequence<>;
// };

public class AsPathType {
    public final AsPathSegmentType type;
    public final Array<UnsignedInteger> asSet;
    public final Array<UnsignedInteger> asSequence;

    public AsPathType(final ByteBuffer buffer) throws InvalidPacketException {
        this.type = AsPathSegmentType.from(buffer);
        switch (this.type) {
            case AS_SET:
                this.asSet = new Array<>(buffer, Optional.empty(), BufferUtils::uint32);
                this.asSequence = null;
                break;
            case AS_SEQUENCE:
                this.asSet = null;
                this.asSequence = new Array<>(buffer, Optional.empty(), BufferUtils::uint32);
                break;
            default:
                throw new IllegalStateException();
        }
    }
}
