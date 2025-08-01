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
package org.opennms.netmgt.icmp.jni;

import java.io.IOException;
import java.net.DatagramPacket;

import org.opennms.core.logging.Logging;
import org.opennms.protocols.icmp.ICMPEchoPacket;
import org.opennms.protocols.icmp.IcmpSocket;
import org.opennms.core.tracker.Messenger;
import org.opennms.core.tracker.ReplyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JniIcmpMessenger
 *
 * @author brozow
 * @version $Id: $
 */
public class JniIcmpMessenger implements Messenger<JniPingRequest, JniPingResponse> {
    private static final Logger LOG = LoggerFactory.getLogger(JniIcmpMessenger.class);

    private int m_pingerId;
    private IcmpSocket m_socket;

    /**
     * <p>Constructor for JniIcmpMessenger.</p>
     * @param pingerId 
     *
     * @throws java.io.IOException if any.
     */
    public JniIcmpMessenger(int pingerId) throws IOException {
        m_pingerId = pingerId;
        m_socket = new IcmpSocket(Integer.valueOf(pingerId).shortValue());
    }

    void processPackets(ReplyHandler<JniPingResponse> callback) {
        final int pingerId = m_pingerId;
        while (true) {
            try {
                DatagramPacket packet = m_socket.receive();

                JniPingResponse reply = JniIcmpMessenger.createPingResponse(packet);

                if (reply.isEchoReply() && reply.getIdentifier() == pingerId) {
                    // Remove this so we don't send a lot of time in this method when we should be processing packets
                    // LogUtils.debugf(this, "Found an echo packet addr = %s, port = %d, length = %d, created reply %s", packet.getAddress(), packet.getPort(), packet.getLength(), reply);
                    callback.handleReply(reply);
                }
            } catch (IOException e) {
                LOG.error("I/O Error occurred reading from ICMP Socket", e);
            } catch (IllegalArgumentException e) {
                // this is not an EchoReply so ignore it
            } catch (IndexOutOfBoundsException e) {
                // this packet is not a valid EchoReply ignore it
            } catch (Throwable e) {
                LOG.error("Unexpected Exception processing reply packet!", e);
            }

        }
    }

    /**
     * <p>sendRequest</p>
     *
     * @param request a {@link org.opennms.netmgt.icmp.jni.JniPingRequest} object.
     */
    @Override
    public void sendRequest(JniPingRequest request) {
        request.send(m_socket);
    }

    /** {@inheritDoc} */
    @Override
    public void start(final ReplyHandler<JniPingResponse> callback) {
        final Thread socketReader = new Thread("JNI-ICMP-"+m_pingerId+"-Socket-Reader") {
            @Override
            public void run() {
                Logging.putPrefix("icmp");
                try {
                    processPackets(callback);
                } catch (Throwable t) {
                    LOG.error("Unexpected exception on Thread {}!", this, t);
                }
            }
        };
        socketReader.setDaemon(true);
        socketReader.start();
    }

    /**
     * <p>
     * Creates a new instance of the class using the passed datagram as the data
     * source. The address and ping packet are extracted from the datagram and
     * returned as a new instance of the class. In addition to extracting the
     * packet, the packet's received time is updated to the current time.
     * </p>
     *
     * <p>
     * If the received datagram is not an echo reply or an incorrect length then
     * an exception is generated to alert the caller.
     * </p>
     *
     * @param packet
     *            The packet with the ICMP datagram.
     * @throws java.lang.IllegalArgumentException
     *             Throw if the datagram is not the correct length or type.
     * @throws java.lang.IndexOutOfBoundsException
     *             Thrown if the datagram does not contain sufficient data.
     * @return a {@link org.opennms.netmgt.icmp.spi.PingReply} object.
     */
    public static JniPingResponse createPingResponse(DatagramPacket packet) {
        // Construct a new packet
        //
        ICMPEchoPacket pkt = new ICMPEchoPacket(packet.getData());
        if (pkt.getReceivedTime() == 0)
            pkt.setReceivedTime();

        // Construct and return the new reply
        //
        return new JniPingResponse(packet.getAddress(), pkt);
    }

    public void setTrafficClass(int tc) throws IOException {
        m_socket.setTrafficClass(tc);
    }

    public void setAllowFragmentation(final boolean allow) throws IOException {
        if (!allow) {
            m_socket.dontFragment();
        }
    }

}
