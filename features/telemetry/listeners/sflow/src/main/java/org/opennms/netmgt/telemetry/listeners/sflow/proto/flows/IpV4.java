package org.opennms.netmgt.telemetry.listeners.sflow.proto.flows;

import java.nio.ByteBuffer;
import java.util.Optional;

import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.sflow.proto.Opaque;

// typedef opaque ip_v4[4];

public class IpV4 {
    public final Opaque<byte[]> ip_v4;

    public IpV4(final ByteBuffer buffer) throws InvalidPacketException {
        this.ip_v4 = new Opaque(buffer, Optional.of(4), Opaque::parseBytes);
    }
}
