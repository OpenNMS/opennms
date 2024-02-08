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
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.opennms.netmgt.provision.detector.simple.request.LineOrientedRequest;
import org.opennms.netmgt.provision.detector.simple.response.MultilineOrientedResponse;
import org.opennms.netmgt.provision.support.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>MultilineOrientedClient class.</p>
 *
 * @author Donald Desloge
 * @version $Id: $
 */
public class MultilineOrientedClient implements Client<LineOrientedRequest, MultilineOrientedResponse> {
    
    private static final Logger LOG = LoggerFactory.getLogger(MultilineOrientedClient.class);
    protected Socket m_socket;
    private OutputStream m_out;
    private BufferedReader m_in;
    
    /**
     * <p>close</p>
     */
    @Override
    public void close() {
        Socket socket = m_socket;
        m_socket = null;
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (final IOException e) {
            LOG.debug("Unable to close socket", e);
        }       
    }

    /** {@inheritDoc} */
    @Override
    public void connect(final InetAddress address, final int port, final int timeout) throws IOException {
        final Socket socket = new Socket();
        socket.connect(new InetSocketAddress(address, port), timeout);
        socket.setSoTimeout(timeout);
        setInput(new BufferedReader(new InputStreamReader(socket.getInputStream())));
        setOutput(socket.getOutputStream());
        m_socket = socket;
    }

    /**
     * <p>receiveBanner</p>
     *
     * @return a {@link org.opennms.netmgt.provision.detector.simple.response.MultilineOrientedResponse} object.
     * @throws java.io.IOException if any.
     */
    @Override
    public MultilineOrientedResponse receiveBanner() throws IOException {
        return receiveResponse();
    }

    /**
     * <p>sendRequest</p>
     *
     * @param request a {@link org.opennms.netmgt.provision.detector.simple.request.LineOrientedRequest} object.
     * @return a {@link org.opennms.netmgt.provision.detector.simple.response.MultilineOrientedResponse} object.
     * @throws java.io.IOException if any.
     */
    @Override
    public MultilineOrientedResponse sendRequest(final LineOrientedRequest request) throws IOException {
        request.send(getOutput());
        return receiveResponse();
    }
    
    /**
     * @return
     * @throws IOException
     */
    private MultilineOrientedResponse receiveResponse() throws IOException {
        final MultilineOrientedResponse response = new MultilineOrientedResponse();
        response.receive(getInput());
        return response;
    }

    /**
     * <p>setInput</p>
     *
     * @param in a {@link java.io.BufferedReader} object.
     */
    public void setInput(final BufferedReader in) {
        m_in = in;
    }

    /**
     * <p>getInput</p>
     *
     * @return a {@link java.io.BufferedReader} object.
     */
    public BufferedReader getInput() {
        return m_in;
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

}
