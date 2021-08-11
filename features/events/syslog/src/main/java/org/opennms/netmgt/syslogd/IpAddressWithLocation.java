package org.opennms.netmgt.syslogd;

import java.net.InetAddress;
import java.util.Objects;

public class IpAddressWithLocation {

    private final InetAddress inetAddress;
    private final String location;

    public IpAddressWithLocation(InetAddress inetAddress, String location) {
        this.inetAddress = inetAddress;
        this.location = location;
    }

    public InetAddress getInetAddress() {
        return inetAddress;
    }

    public String getLocation() {
        return location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IpAddressWithLocation that = (IpAddressWithLocation) o;
        return Objects.equals(inetAddress, that.inetAddress) && Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inetAddress, location);
    }
}
