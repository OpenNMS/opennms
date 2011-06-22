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
 * by the Free Software Foundation, either version 2 of the License,
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

package org.opennms.netmgt.poller.monitors;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.utils.RelaxedX509TrustManager;

/**
 * This class is designed to be used by the service poller framework to test the
 * availability of the HTTPS service on remote interfaces. The class implements
 * the ServiceMonitor interface that allows it to be used along with other
 * plug-ins by the service poller framework.
 *
 * @author <a href="mailto:david@opennms.org">David Hustace </a>
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="mailto:jason@opennms.org">Jason </A>
 */
@Distributable
final public class HttpsMonitor extends HttpMonitor {

    /**
     * Default HTTPS ports.
     */
    private static final int[] DEFAULT_PORTS = { 443 };

    /** {@inheritDoc} */
    @Override
    protected int[] determinePorts(Map<String, Object> parameters) {
        return ParameterMap.getKeyedIntegerArray(parameters, "port", DEFAULT_PORTS);
    }

    /** {@inheritDoc} */
    @Override
    protected Socket wrapSocket(Socket socket) throws IOException {
        SSLSocketFactory sslSF = null;
        TrustManager[] tm = { new RelaxedX509TrustManager() };
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, tm, new java.security.SecureRandom());
        } catch (NoSuchAlgorithmException e) {
            log().error("wrapSocket: Error wrapping socket, throwing runtime exception..."+e);
            throw new IllegalStateException("No such algorith in SSLSocketFactory: "+e);
        } catch (KeyManagementException e) {
            log().error("wrapSocket: Error wrapping socket, throwing runtime exception..."+e);
            throw new IllegalStateException("Key management exception in SSLSocketFactory: "+e);
        }
        sslSF = sslContext.getSocketFactory();
        Socket wrappedSocket;
        InetAddress inetAddress = socket.getInetAddress();
        String hostAddress = InetAddressUtils.str(inetAddress);
        int port = socket.getPort();
        wrappedSocket = sslSF.createSocket(socket, hostAddress, port, true);
        return wrappedSocket;
    }

}
