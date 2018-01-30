package org.opennms.netmgt.telemetry.listeners.sflow.proto.flows;

import java.nio.ByteBuffer;

import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;

// union address switch (address_type type) {
//    case UNKNOWN:
//      void;
//    case IP_V4:
//      ip_v4 ip;
//    case IP_V6:
//      ip_v6 ip;
// };

public class Address {
    public final AddressType type;
    public final IpV4 ipV4;
    public final IpV6 ipV6;

    public Address(final ByteBuffer buffer) throws InvalidPacketException {
        this.type = AddressType.from(buffer);
        switch (this.type) {
            case IP_V4:
                this.ipV4 = new IpV4(buffer);
                this.ipV6 = null;
                break;
            case IP_V6:
                this.ipV4 = null;
                this.ipV6 = new IpV6(buffer);
                break;
            default:
                throw new IllegalStateException();
        }
    }
}
