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
import org.opennms.netmgt.provision.support.ntp.NtpMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
/**
 * <p>NtpClient class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@Scope("prototype")
public class NtpClient implements Client<NtpMessage, DatagramPacket> {
    
    private static final Logger LOG = LoggerFactory.getLogger(NtpClient.class);
    private DatagramSocket m_socket;
    private int m_port;
    private InetAddress m_address;
    
    /**
     * <p>close</p>
     */
    @Override
    public void close() {
        m_socket.close();
    }

    /** {@inheritDoc} */
    @Override
    public void connect(final InetAddress address, final int port, final int timeout) throws IOException, Exception {
        LOG.debug("Address: {}, port: {}, timeout: {}", address, port, timeout);
        m_socket = new DatagramSocket();
        m_socket.setSoTimeout(timeout);
        setAddress(address);
        setPort(port);
    }

    /**
     * <p>receiveBanner</p>
     *
     * @return a {@link java.net.DatagramPacket} object.
     * @throws java.io.IOException if any.
     * @throws java.lang.Exception if any.
     */
    @Override
    public DatagramPacket receiveBanner() throws IOException, Exception {
        throw new UnsupportedOperationException("Client<NtpMessage,DatagramPacket>.receiveBanner is not yet implemented");
    }

    /**
     * <p>sendRequest</p>
     *
     * @param request a {@link org.opennms.netmgt.provision.support.ntp.NtpMessage} object.
     * @return a {@link java.net.DatagramPacket} object.
     * @throws java.io.IOException if any.
     * @throws java.lang.Exception if any.
     */
    @Override
    public DatagramPacket sendRequest(final NtpMessage request) throws IOException, Exception {
        
        final byte[] buf = new NtpMessage().toByteArray();
        m_socket.send(new DatagramPacket(buf, buf.length, getAddress(), getPort()));

        final byte[] data = new byte[512];
        final DatagramPacket packet = new DatagramPacket(data, data.length);
        m_socket.receive(packet);

        return packet;
    }

    /**
     * <p>setAddress</p>
     *
     * @param address a {@link java.net.InetAddress} object.
     */
    protected void setAddress(final InetAddress address) {
        m_address = address;
    }

    /**
     * <p>getAddress</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
    public InetAddress getAddress() {
        return m_address;
    }

    /**
     * <p>setPort</p>
     *
     * @param port a int.
     */
    protected void setPort(final int port) {
        m_port = port;
    }

    /**
     * <p>getPort</p>
     *
     * @return a int.
     */
    public int getPort() {
        return m_port;
    }
}
