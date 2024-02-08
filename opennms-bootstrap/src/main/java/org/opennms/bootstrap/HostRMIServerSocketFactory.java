/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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

    private static final long serialVersionUID = 2980115395787642955L;

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
