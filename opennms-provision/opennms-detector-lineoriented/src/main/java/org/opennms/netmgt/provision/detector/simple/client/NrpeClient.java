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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.opennms.core.utils.SocketUtils;
import org.opennms.core.utils.SocketWrapper;
import org.opennms.netmgt.provision.detector.simple.request.NrpeRequest;
import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.support.nrpe.NrpePacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>NrpeClient class.</p>
 *
 * @author Donald Desloge
 * @version $Id: $
 */
public class NrpeClient implements Client<NrpeRequest, NrpePacket>, SocketWrapper {
    
    private static final Logger LOG = LoggerFactory.getLogger(NrpeClient.class);
    /** 
     * List of cipher suites to use when talking SSL to NRPE, which uses anonymous DH
     */
    private static final String[] ADH_CIPHER_SUITES = new String[] {"TLS_DH_anon_WITH_AES_128_CBC_SHA"};
    
    private Socket m_socket;
    private int m_padding = 2;
    private boolean m_useSsl = true;
    private OutputStream m_out;
    private InputStream m_in;
    
    /**
     * <p>close</p>
     */
    @Override
    public void close() {
        Socket socket = m_socket;
        m_socket = null;
        try {
            if(socket != null) {
                socket.close();
            }
            
        } catch (final IOException e) {
            LOG.debug("failed to close socket", e);
        }
        
    }

    /** {@inheritDoc} */
    @Override
    public void connect(final InetAddress address, final int port, final int timeout) throws IOException, Exception {
        m_socket = getWrappedSocket(address, port, timeout);
        setOutput(m_socket.getOutputStream());
        setInput(m_socket.getInputStream());
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
    protected Socket getWrappedSocket(InetAddress address, int port, int timeout) throws IOException {
        final Socket socket = new Socket();
        socket.connect(new InetSocketAddress(address, port), timeout);
        socket.setSoTimeout(timeout);
        try {
            return wrapSocket(socket);
        } catch (final IOException e) {
            LOG.debug("an error occurred while SSL-wrapping a socket ({}:{})", address, port, e);
            return null;
        }
    }
    
    /**
     * <p>wrapSocket</p>
     *
     * @param socket a {@link java.net.Socket} object.
     * @param hostAddress a {@link java.lang.String} object.
     * @param port a int.
     * @return a {@link java.net.Socket} object.
     * @throws java.lang.Exception if any.
     */
    @Override
    public Socket wrapSocket(final Socket socket) throws IOException {
        if (!isUseSsl()) {
            return socket;
        } else {
            // Set this socket to use anonymous Diffie-Hellman ciphers. This removes the authentication
            // benefits of SSL, but it's how NRPE rolls so we have to play along.
            return SocketUtils.wrapSocketInSslContext(socket, null, ADH_CIPHER_SUITES);
        }
    }

    /**
     * <p>receiveBanner</p>
     *
     * @return a {@link org.opennms.netmgt.provision.support.nrpe.NrpePacket} object.
     * @throws java.io.IOException if any.
     * @throws java.lang.Exception if any.
     */
    @Override
    public NrpePacket receiveBanner() throws IOException, Exception {
        return receiveResponse();
    }

    /**
     * <p>sendRequest</p>
     *
     * @param request a {@link org.opennms.netmgt.provision.detector.simple.request.NrpeRequest} object.
     * @return a {@link org.opennms.netmgt.provision.support.nrpe.NrpePacket} object.
     * @throws java.io.IOException if any.
     * @throws java.lang.Exception if any.
     */
    @Override
    public NrpePacket sendRequest(final NrpeRequest request) throws IOException, Exception {
        request.send(getOutput());
        return receiveResponse();
    }
    
    private NrpePacket receiveResponse() throws Exception {
        final NrpePacket response = NrpePacket.receivePacket(getInput(), getPadding());
        LOG.info("what is response: {}", response.getResultCode());
        return response;
    }

    /**
     * <p>setPadding</p>
     *
     * @param padding a int.
     */
    public void setPadding(final int padding) {
        m_padding = padding;
    }

    /**
     * <p>getPadding</p>
     *
     * @return a int.
     */
    public int getPadding() {
        return m_padding;
    }

    /**
     * <p>setUseSsl</p>
     *
     * @param useSsl a boolean.
     */
    public void setUseSsl(final boolean useSsl) {
        m_useSsl = useSsl;
    }

    /**
     * <p>isUseSsl</p>
     *
     * @return a boolean.
     */
    public boolean isUseSsl() {
        return m_useSsl;
    }

    /**
     * <p>setOutput</p>
     *
     * @param out a {@link java.io.OutputStream} object.
     */
    public void setOutput(final OutputStream out) {
        m_out = out;
    }

    /**
     * <p>getOutput</p>
     *
     * @return a {@link java.io.OutputStream} object.
     */
    public OutputStream getOutput() {
        return m_out;
    }

    /**
     * <p>setInput</p>
     *
     * @param in a {@link java.io.InputStream} object.
     */
    public void setInput(final InputStream in) {
        m_in = in;
    }

    /**
     * <p>getInput</p>
     *
     * @return a {@link java.io.InputStream} object.
     */
    public InputStream getInput() {
        return m_in;
    }

}
