package org.opennms.netmgt.provision.service;

import java.net.InetAddress;

public final class DefaultHostnameResolver implements HostnameResolver {
    @Override public String getHostname(final InetAddress addr) {
        return addr.getCanonicalHostName();
    }
}