//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc. All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2004 May 05: Switch from SocketChannel to Socket with connection timeout.
// 2003 Jul 21: Explicitly closed socket.
// 2003 Jul 18: Enabled retries for monitors.
// 2003 Jul 02: Fixed a ClassCastException.
// 2003 Jun 11: Added a "catch" for RRD update errors. Bug #748.
// 2003 Jan 31: Added the ability to imbed RRA information in poller packages.
// 2003 Jan 31: Cleaned up some unused imports.
// 2003 Jan 29: Added response times to certain monitors.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp. All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8
//

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

import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.utils.RelaxedX509TrustManager;

/**
 * This class is designed to be used by the service poller framework to test the
 * availability of the HTTPS service on remote interfaces. The class implements
 * the ServiceMonitor interface that allows it to be used along with other
 * plug-ins by the service poller framework.
 *
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <a href="mailto:david@opennms.org">David Hustace </a>
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="mailto:jason@opennms.org">Jason </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <a href="mailto:david@opennms.org">David Hustace </a>
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="mailto:jason@opennms.org">Jason </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <a href="mailto:david@opennms.org">David Hustace </a>
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="mailto:jason@opennms.org">Jason </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <a href="mailto:david@opennms.org">David Hustace </a>
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="mailto:jason@opennms.org">Jason </A>
 * @version $Id: $
 */
@Distributable
final public class HttpsMonitor extends HttpMonitor {

    /**
     * Default HTTPS ports.
     */
    private static final int[] DEFAULT_PORTS = { 443 };

    /** {@inheritDoc} */
    protected int[] determinePorts(Map<String, Object> parameters) {
        return ParameterMap.getKeyedIntegerArray(parameters, "port", DEFAULT_PORTS);
    }

    /** {@inheritDoc} */
    protected Socket wrapSocket(Socket socket) throws IOException {
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
        SSLSocketFactory socketFactory = sslContext.getSocketFactory();
        Socket wrappedSocket;
        InetAddress inetAddress = socket.getInetAddress();
        String hostAddress = inetAddress.getHostAddress();
        int port = socket.getPort();
        wrappedSocket = socketFactory.createSocket(socket, hostAddress, port, true);
        return wrappedSocket;
    }

}
