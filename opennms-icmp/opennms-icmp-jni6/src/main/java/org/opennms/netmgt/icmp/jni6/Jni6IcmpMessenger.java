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
package org.opennms.netmgt.icmp.jni6;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Inet6Address;

import org.opennms.core.logging.Logging;
import org.opennms.protocols.icmp6.ICMPv6EchoReply;
import org.opennms.protocols.icmp6.ICMPv6Packet;
import org.opennms.protocols.icmp6.ICMPv6Packet.Type;
import org.opennms.protocols.icmp6.ICMPv6Socket;
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
public class Jni6IcmpMessenger implements Messenger<Jni6PingRequest, Jni6PingResponse> {
    private static final Logger LOG = LoggerFactory.getLogger(Jni6IcmpMessenger.class);
    
    private int m_pingerId;
    private ICMPv6Socket m_socket;
    
    /**
     * <p>Constructor for JniIcmpMessenger.</p>
     * @param pingerId 
     *
     * @throws java.io.IOException if any.
     */
    public Jni6IcmpMessenger(int pingerId) throws IOException {
        m_pingerId = pingerId;
        m_socket = new ICMPv6Socket(Integer.valueOf(pingerId).shortValue());
    }

    void processPackets(ReplyHandler<Jni6PingResponse> callback) {
        while (true) {
            try {
                DatagramPacket packet = m_socket.receive();
                
                Jni6PingResponse reply = Jni6IcmpMessenger.createPingResponse(packet);
                
                if (reply != null && reply.getIdentifier() == m_pingerId) {
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
     * @param request a {@link org.opennms.netmgt.icmp.jni6.Jni6PingRequest} object.
     */
    @Override
    public void sendRequest(Jni6PingRequest request) {
        request.send(m_socket);
    }

    /** {@inheritDoc} */
    @Override
    public void start(final ReplyHandler<Jni6PingResponse> callback) {
        Thread socketReader = new Thread("JNI-ICMP-"+m_pingerId+"-Socket-Reader") {

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
    public static Jni6PingResponse createPingResponse(DatagramPacket packet) {

        ICMPv6Packet icmpPacket = new ICMPv6Packet(packet.getData(), packet.getOffset(), packet.getLength());

        if (icmpPacket.getType() != Type.EchoReply) return null;

        ICMPv6EchoReply echoReply = new ICMPv6EchoReply(icmpPacket);

        if (!echoReply.isEchoReply() || !echoReply.isValid()) return null;

        Inet6Address address = (Inet6Address) packet.getAddress();

        return new Jni6PingResponse(address, echoReply);
    }


    public void setTrafficClass(int tc) throws IOException {
        try {
            m_socket.setTrafficClass(tc);
        } catch (final IOException e) {
            LOG.error("Failed to set traffic class {} on ICMPv6 socket.", tc, e);
        }
    }

    public void setAllowFragmentation(final boolean allow) throws IOException {
        if (!allow) {
            try {
                m_socket.dontFragment();
            } catch (final IOException e) {
                LOG.error("Failed to set 'Don't Fragment' bit on ICMPv6 socket.", e);
            }
        }
    }
}
