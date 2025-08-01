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
package org.opennms.netmgt.provision.detector.datagram.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.opennms.netmgt.provision.support.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>DatagramClient class.</p>
 *
 * @author brozow
 * @version $Id: $
 */
public class DatagramClient implements Client<DatagramPacket, DatagramPacket> {

    private static final Logger LOG = LoggerFactory.getLogger(DatagramClient.class);    
    private DatagramSocket m_socket;
    
    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.detector.Client#close()
     */
    /**
     * <p>close</p>
     */
    @Override
    public void close() {
        m_socket.close();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.detector.Client#connect(java.net.InetAddress, int, int)
     */
    /** {@inheritDoc} */
    @Override
    public void connect(final InetAddress address, final int port, final int timeout) throws IOException {
        LOG.debug("Address: {}, port: {}, timeout: {}", address, port, timeout);

        m_socket = new DatagramSocket();
        m_socket.setSoTimeout(timeout);
        m_socket.connect(address, port);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.detector.Client#receiveBanner()
     */
    /**
     * <p>receiveBanner</p>
     *
     * @return a {@link java.net.DatagramPacket} object.
     * @throws java.io.IOException if any.
     */
    @Override
    public DatagramPacket receiveBanner() throws IOException {
        throw new UnsupportedOperationException("Client<DatagramPacket,DatagramPacket>.receiveBanner is not yet implemented");
        
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.detector.Client#sendRequest(java.lang.Object)
     */
    /**
     * <p>sendRequest</p>
     *
     * @param request a {@link java.net.DatagramPacket} object.
     * @return a {@link java.net.DatagramPacket} object.
     * @throws java.io.IOException if any.
     */
    @Override
    public DatagramPacket sendRequest(final DatagramPacket request) throws IOException {

        m_socket.send(request);

        final byte[] data = new byte[512];
        DatagramPacket response = new DatagramPacket(data, data.length);
        m_socket.receive(response);

        return response;
    }
}
