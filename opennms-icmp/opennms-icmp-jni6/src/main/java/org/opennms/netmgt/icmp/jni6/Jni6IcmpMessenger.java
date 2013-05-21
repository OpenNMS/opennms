/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.icmp.jni6;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Inet6Address;
import java.util.Queue;

import org.opennms.core.utils.LogUtils;
import org.opennms.protocols.icmp6.ICMPv6EchoReply;
import org.opennms.protocols.icmp6.ICMPv6Packet;
import org.opennms.protocols.icmp6.ICMPv6Packet.Type;
import org.opennms.protocols.icmp6.ICMPv6Socket;
import org.opennms.protocols.rt.Messenger;

/**
 * JniIcmpMessenger
 *
 * @author brozow
 * @version $Id: $
 */
public class Jni6IcmpMessenger implements Messenger<Jni6PingRequest, Jni6PingResponse> {
    
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
        m_socket = new ICMPv6Socket();
    }

    void processPackets(Queue<Jni6PingResponse> pendingReplies) {
        while (true) {
            try {
                DatagramPacket packet = m_socket.receive();
                
                Jni6PingResponse reply = Jni6IcmpMessenger.createPingResponse(packet);
                
                if (reply != null && reply.getIdentifier() == m_pingerId) {
                    pendingReplies.offer(reply);
                }

     
            } catch (IOException e) {
                LogUtils.errorf(this, e, "I/O Error occurred reading from ICMP Socket");
            } catch (IllegalArgumentException e) {
                // this is not an EchoReply so ignore it
            } catch (IndexOutOfBoundsException e) {
                // this packet is not a valid EchoReply ignore it
            } catch (Throwable e) {
                LogUtils.errorf(this, e, "Unexpected Exception processing reply packet!");
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
    public void start(final Queue<Jni6PingResponse> responseQueue) {
        Thread socketReader = new Thread("JNI-ICMP-"+m_pingerId+"-Socket-Reader") {

            @Override
            public void run() {
                try {
                    processPackets(responseQueue);
                } catch (Throwable t) {
                    LogUtils.errorf(this, t, "Unexpected exception on Thread %s!", this);
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
}
