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
package org.opennms.netmgt.provision.detector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.utils.RelaxedX509TrustManager;

/**
 * @author thedesloge
 *
 */
public class SSLClient extends MultilineOrientedClient implements Client<LineOrientedRequest, MultilineOrientedResponse> {
    
    

    public void connect(InetAddress address, int port, int timeout) throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(address, port), timeout);
        socket.setSoTimeout(timeout);
        setInput(new BufferedReader(new InputStreamReader(socket.getInputStream())));
        setOutput(socket.getOutputStream());
        try {
            m_socket = wrapSocket(socket, address.getHostAddress(), port);
        } catch (Exception e) {
            //e.printStackTrace();
        }        
    }
    
    protected Socket wrapSocket(Socket socket, String hostAddress, int port) throws Exception {
        Socket sslSocket;

        // set up the certificate validation. USING THIS SCHEME WILL ACCEPT ALL
        // CERTIFICATES
        SSLSocketFactory sslSF = null;

        TrustManager[] tm = { new RelaxedX509TrustManager() };
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, tm, new java.security.SecureRandom());
        sslSF = sslContext.getSocketFactory();
        sslSocket = sslSF.createSocket(socket, hostAddress, port, true);
        return sslSocket;
    }

}
