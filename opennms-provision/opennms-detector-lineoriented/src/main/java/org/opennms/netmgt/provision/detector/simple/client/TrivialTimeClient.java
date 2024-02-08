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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import org.opennms.netmgt.provision.detector.simple.request.TrivialTimeRequest;
import org.opennms.netmgt.provision.detector.simple.response.TrivialTimeResponse;
import org.opennms.netmgt.provision.support.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrivialTimeClient implements Client<TrivialTimeRequest, TrivialTimeResponse> {
    
    private static final Logger LOG = LoggerFactory.getLogger(TrivialTimeClient.class);
    /**
     * Seconds to subtract from a 1970-01-01 00:00:00-based UNIX timestamp
     * to make it comparable to a 1900-01-01 00:00:00-based timestamp from
     * the trivial time service (actually adding a negative value)
     */
    private static final int EPOCH_ADJ_FACTOR = 2085978496;

    private String protocol;
    private int allowedSkew;
    private int retries;

    private Socket tcpSocket;
    private DatagramSocket udpSocket;
    private DatagramPacket udpPacket;

    public TrivialTimeClient(String protocol, int allowedSkew) {
        super();
        this.protocol = protocol;
        this.allowedSkew = allowedSkew;
    }

    @Override
    public void connect(InetAddress address, int port, int timeout) throws IOException, Exception {
        // Validate Protocol
        if (!isTcp() && !isUdp()) {
            throw new  IllegalArgumentException("Unsupported protocol, only TCP and UDP currently supported");
        } else if (isUdp()) {
            LOG.warn("UDP support is largely untested");
        }
        // Initialize Socket
        if (isTcp()) {
            tcpSocket = new Socket();
            tcpSocket.connect(new InetSocketAddress(address, port), timeout);
            tcpSocket.setSoTimeout(timeout);
        } else {
            udpSocket = new DatagramSocket();
            udpSocket.setSoTimeout(timeout);
            udpPacket = new DatagramPacket(new byte[]{}, 0, address, port); // Empty datagram per RFC868
        }
        LOG.debug("Connected to host: {} on {} port: {}", address, protocol.toUpperCase(), port);
    }

    @Override
    public void close() {
        try {
            if (isTcp() && tcpSocket != null) {
                tcpSocket.close();
            }
            if (isUdp() && udpSocket != null) {
                udpSocket.close();
            }
        } catch (Exception e) {
            LOG.error("Can't close detector sockets.", e);
        }
    }

    @Override
    public TrivialTimeResponse receiveBanner() throws IOException, Exception {
        return null;
    }

    @Override
    public TrivialTimeResponse sendRequest(TrivialTimeRequest request) throws IOException, Exception {
        boolean gotTime = false;
        int remoteTime = 0;
        int localTime = 0;
        for (int i = 0; i < retries && !gotTime; i++) {
            // Try to read from the socket
            byte[] timeBytes = new byte[4];
            ByteBuffer timeByteBuffer = ByteBuffer.wrap(timeBytes);
            int bytesRead = 0;

            if (isTcp()) {
                bytesRead = tcpSocket.getInputStream().read(timeBytes);
            }

            if (isUdp()) {
                // Send an empty datagram per RFC868
                udpSocket.send(udpPacket);
                DatagramPacket timePacket = new DatagramPacket(timeBytes, timeBytes.length);
                // Try to receive a response from the remote socket
                udpSocket.receive(timePacket);
                bytesRead = timePacket.getLength();
            }

            if (bytesRead != 4) {
                continue;
            }
            LOG.debug("sendRequest: {} bytes read = {}", protocol, bytesRead);

            try {
                remoteTime = timeByteBuffer.getInt();
            } catch (BufferUnderflowException bue) {
                LOG.error("Encountered buffer underflow while reading time from remote socket.");
                remoteTime = 0;
                continue; // to next iteration of for() loop
            }

            localTime = (int)(System.currentTimeMillis() / 1000) - EPOCH_ADJ_FACTOR;
            gotTime = true;
        }
        return gotTime ? new TrivialTimeResponse(remoteTime, localTime, allowedSkew) : new TrivialTimeResponse();
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    private boolean isTcp() {
        return protocol.equalsIgnoreCase("tcp");
    }

    private boolean isUdp() {
        return protocol.equalsIgnoreCase("udp");
    }

}
