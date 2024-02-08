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
package org.opennms.netmgt.provision.detector.simple.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.opennms.core.utils.SocketUtils;
import org.opennms.netmgt.provision.detector.simple.request.LineOrientedRequest;
import org.opennms.netmgt.provision.detector.simple.response.MultilineOrientedResponse;
import org.opennms.netmgt.provision.support.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>SSLClient class.</p>
 *
 * @author thedesloge
 * @version $Id: $
 */
public class SSLClient extends MultilineOrientedClient implements Client<LineOrientedRequest, MultilineOrientedResponse> {
    
    private static final Logger LOG = LoggerFactory.getLogger(SSLClient.class);
    

    /** {@inheritDoc} */
    @Override
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
            return SocketUtils.wrapSocketInSslContext(socket);
        } catch (final Exception e) {
            LOG.debug("Unable to wrap socket in SSL.", e);
            return null;
        }
    }

}
