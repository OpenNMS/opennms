/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision.detector.simple.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.provision.detector.simple.request.LineOrientedRequest;
import org.opennms.netmgt.provision.detector.simple.response.MultilineOrientedResponse;
import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.support.trustmanager.RelaxedX509TrustManager;

/**
 * <p>SSLClient class.</p>
 *
 * @author thedesloge
 * @version $Id: $
 */
public class SSLClient extends MultilineOrientedClient implements Client<LineOrientedRequest, MultilineOrientedResponse> {
    
    

    /** {@inheritDoc} */
    public void connect(final InetAddress address, final int port, final int timeout) throws IOException {
        m_socket = getWrappedSocket(address, port, timeout);
        setOutput(m_socket.getOutputStream());
        setInput(new BufferedReader(new InputStreamReader(m_socket.getInputStream())));
    }
    
    /**
     * <p>getWrappedSocket</p>
     *
     * @param address a {@link java.net.InetAddress} object.
     * @param port a int.
     * @param timeout a int.
     * @return a {@link java.net.Socket} object.
     * @throws java.io.IOException if any.
     */
    protected Socket getWrappedSocket(final InetAddress address, final int port, final int timeout) throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(address, port), timeout);
        socket.setSoTimeout(timeout);
        try {
            return wrapSocket(socket, address.getHostAddress(), port);
        } catch (final Exception e) {
            LogUtils.debugf(this, e, "Unable to wrap socket in SSL.");
            return null;
        }
    }
    
    /**
     * <p>wrapSocket</p>
     *
     * @param socket a {@link java.net.Socket} object.
     * @param hostAddress a {@link java.lang.String} object.
     * @param port a int.
     * @return a {@link java.net.Socket} object.
     * @throws java.lang.Exception if any.
     */
    protected Socket wrapSocket(final Socket socket, final String hostAddress, final int port) throws Exception {
        Socket sslSocket;

        // set up the certificate validation. USING THIS SCHEME WILL ACCEPT ALL CERTIFICATES
        SSLSocketFactory sslSF = null;
        final TrustManager[] tm = { new RelaxedX509TrustManager() };
        final SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, tm, new java.security.SecureRandom());
        sslSF = sslContext.getSocketFactory();
        LogUtils.debugf(this, "SSL port: %d", port);
        sslSocket = sslSF.createSocket(socket, hostAddress, port, true);
        return sslSocket;
    }

}
