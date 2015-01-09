package org.opennms.netmgt.provision.service;

import java.net.InetAddress;

public interface HostnameResolver {
    public String getHostname(final InetAddress addr);
}