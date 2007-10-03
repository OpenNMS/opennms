package org.opennms.netmgt.protocols;

import java.net.InetAddress;

public interface Protocol {

    public boolean exists(InetAddress address, int port, int timeout);
    public double check(InetAddress address, int port, int timeout) throws FailedCheckException;
    
}