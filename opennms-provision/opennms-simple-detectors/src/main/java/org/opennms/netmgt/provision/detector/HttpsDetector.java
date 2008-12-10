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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.opennms.netmgt.provision.ServiceDetector;
import org.opennms.netmgt.utils.RelaxedX509TrustManager;

public class HttpsDetector extends HttpDetector implements ServiceDetector {
    
    private String m_hostAddress;
    
    protected HttpsDetector() {
        super();
    }
    
    public void onInit() {
        setServiceName("Https");
        setPort(443);
        setTimeout(500);
        setRetries(1);
        
        sendHttpQuery(queryURLRequest(getUrl()));
        addHttpResponseHandler(contains("HTTP/"), null, getUrl(), isCheckRetCode(), getMaxRetCode());
    }
    
    protected Socket createSocketConnection(InetAddress host, int port, int timeout) throws Exception {
        setHostAddress(host.getHostAddress());
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), timeout);
        socket.setSoTimeout(timeout);
        return wrapSocket(socket);
    }
    
    protected Socket wrapSocket(Socket socket) throws Exception {
        Socket sslSocket;

        // set up the certificate validation. USING THIS SCHEME WILL ACCEPT ALL
        // CERTIFICATES
        SSLSocketFactory sslSF = null;

        TrustManager[] tm = { new RelaxedX509TrustManager() };
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, tm, new java.security.SecureRandom());
        sslSF = sslContext.getSocketFactory();
        System.out.println("port: " + getPort());
        sslSocket = sslSF.createSocket(socket, getHostAddress(), getPort(), true);
        return sslSocket;
    }

    public void setHostAddress(String hostAddress) {
        m_hostAddress = hostAddress;
    }

    public String getHostAddress() {
        return m_hostAddress;
    }

}
