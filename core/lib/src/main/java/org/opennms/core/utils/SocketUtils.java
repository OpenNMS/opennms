/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:seth@opennms.org">Seth</a>
 * @author <a href="http://www.opennms.org">OpenNMS </a>
 */
public abstract class SocketUtils {
	
	private static final Logger LOG = LoggerFactory.getLogger(SocketUtils.class);

    public static Socket wrapSocketInSslContext(Socket socket) throws IOException {
        return wrapSocketInSslContext(socket, null, null);
    }

    public static SSLSocket wrapSocketInSslContext(Socket socket, String protocol) throws IOException {
        return wrapSocketInSslContext(socket, protocol, null);
    }

    public static SSLSocket wrapSocketInSslContext(Socket socket, String protocol, String[] cipherSuites) throws IOException {
        TrustManager[] tm = { new RelaxedX509ExtendedTrustManager() };
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance(protocol == null ? "SSL" : protocol);
            sslContext.init(null, tm, new java.security.SecureRandom());
        } catch (NoSuchAlgorithmException e) {
        	LOG.error("wrapSocket: Error wrapping socket, throwing runtime exception", e);
            throw new IllegalStateException("No such algorithm in SSLSocketFactory: " + e);
        } catch (KeyManagementException e) {
        	LOG.error("wrapSocket: Error wrapping socket, throwing runtime exception", e);
            throw new IllegalStateException("Key management exception in SSLSocketFactory: " + e);
        }
        SSLSocketFactory socketFactory = sslContext.getSocketFactory();
        InetAddress inetAddress = socket.getInetAddress();
        String hostAddress = InetAddressUtils.str(inetAddress);
        SSLSocket wrappedSocket = (SSLSocket) socketFactory.createSocket(socket, hostAddress, socket.getPort(), true);
        if (cipherSuites != null && cipherSuites.length > 0) {
            wrappedSocket.setEnabledCipherSuites(cipherSuites);
        }
        return wrappedSocket;
    }

}
