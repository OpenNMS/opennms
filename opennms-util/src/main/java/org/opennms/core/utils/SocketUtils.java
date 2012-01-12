/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

/**
 * @author <a href="mailto:seth@opennms.org">Seth</a>
 * @author <a href="http://www.opennms.org">OpenNMS </a>
 */
public abstract class SocketUtils {

    public interface SocketWrapper {
        Socket wrapSocket(Socket socket) throws IOException;
    }

    public static class SslSocketWrapper implements SocketWrapper {
        private final String[] m_cipherSuites;

        public SslSocketWrapper() {
            this(null);
        }

        public SslSocketWrapper(String[] cipherSuites) {
            m_cipherSuites = cipherSuites;
        }
        @Override
        public Socket wrapSocket(Socket socket) throws IOException {
            return wrapSocketInSslContext(socket, m_cipherSuites);
        }
    }

    public static class DefaultSocketWrapper implements SocketWrapper {
        @Override
        public Socket wrapSocket(Socket socket) throws IOException {
            return socket;
        }
    }

    public static class TimeoutSocketFactory {

        private final int m_timeout;
        private final SocketWrapper m_socketWrapper;

        public TimeoutSocketFactory(final int timeout) {
            this(timeout, null);
        }

        /**
         * Oh noes, dyslexia!!!
         */
        public TimeoutSocketFactory(final int timeout, final SocketWrapper wocketSrapper) {
            m_timeout = timeout;
            m_socketWrapper = wocketSrapper;
        }

        public Socket createSocket(final String host, final int port) throws IOException, UnknownHostException {
            Socket socket = new Socket(host, port);
            socket.setSoTimeout(m_timeout);
            if (m_socketWrapper != null) {
                socket = m_socketWrapper.wrapSocket(socket);
            }
            return socket;
        }
    }

    public static Socket wrapSocketInSslContext(Socket socket) throws IOException {
        return wrapSocketInSslContext(socket, null);
    }

    public static Socket wrapSocketInSslContext(Socket socket, String[] cipherSuites) throws IOException {
        TrustManager[] tm = { new RelaxedX509TrustManager() };
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, tm, new java.security.SecureRandom());
        } catch (NoSuchAlgorithmException e) {
            LogUtils.errorf(SocketUtils.class, e, "wrapSocket: Error wrapping socket, throwing runtime exception: %s", e.getMessage());
            throw new IllegalStateException("No such algorithm in SSLSocketFactory: " + e);
        } catch (KeyManagementException e) {
            LogUtils.errorf(SocketUtils.class, e, "wrapSocket: Error wrapping socket, throwing runtime exception: %s", e.getMessage());
            throw new IllegalStateException("Key management exception in SSLSocketFactory: " + e);
        }
        SSLSocketFactory socketFactory = sslContext.getSocketFactory();
        InetAddress inetAddress = socket.getInetAddress();
        String hostAddress = InetAddressUtils.str(inetAddress);
        Socket wrappedSocket = socketFactory.createSocket(socket, hostAddress, socket.getPort(), true);
        if (cipherSuites != null && cipherSuites.length > 0) {
            final SSLSocket sslSocket = (SSLSocket) wrappedSocket;
            sslSocket.setEnabledCipherSuites(cipherSuites);
        }
        return wrappedSocket;
    }

}
