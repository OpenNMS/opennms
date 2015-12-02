package org.opennms.bootstrap;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RMISocketFactory;

public class HostRMIServerSocketFactory extends RMISocketFactory implements RMIServerSocketFactory, RMIClientSocketFactory, Serializable {
    private String m_host;

    public HostRMIServerSocketFactory() {
    }

    public HostRMIServerSocketFactory(final String host) {
        m_host = host;
    }

    @Override
    public ServerSocket createServerSocket(final int port) throws IOException {
        final InetAddress address;
        if ("localhost".equals(m_host)) {
            address = InetAddress.getLoopbackAddress();
        } else {
            address = InetAddress.getByName(m_host);
        }
        return new ServerSocket(port, -1, address);
    }

    @Override
    public Socket createSocket(final String host, final int port) throws IOException {
        return RMISocketFactory.getDefaultSocketFactory().createSocket(host, port);
    }

    public String getHost() {
        return m_host;
    }
    public void setHost(final String host) {
        m_host = host;
    }
}
