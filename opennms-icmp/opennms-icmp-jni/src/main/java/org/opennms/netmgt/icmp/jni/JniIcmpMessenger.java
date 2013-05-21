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

package org.opennms.netmgt.icmp.jni;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Queue;

import org.opennms.core.utils.LogUtils;
import org.opennms.protocols.icmp.ICMPEchoPacket;
import org.opennms.protocols.icmp.IcmpSocket;
import org.opennms.protocols.rt.Messenger;

/**
 * JniIcmpMessenger
 *
 * @author brozow
 * @version $Id: $
 */
public class JniIcmpMessenger implements Messenger<JniPingRequest, JniPingResponse> {
    
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
        m_socket = new IcmpSocket();
    }

    void processPackets(Queue<JniPingResponse> pendingReplies) {
        final int pingerId = m_pingerId;
        while (true) {
            try {
                DatagramPacket packet = m_socket.receive();
        
                JniPingResponse reply = JniIcmpMessenger.createPingResponse(packet);
                
                if (reply.isEchoReply() && reply.getIdentifier() == pingerId) {
                    // Remove this so we don't send a lot of time in this method when we should be processing packets
                    // LogUtils.debugf(this, "Found an echo packet addr = %s, port = %d, length = %d, created reply %s", packet.getAddress(), packet.getPort(), packet.getLength(), reply);
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
     * @param request a {@link org.opennms.netmgt.icmp.jni.JniPingRequest} object.
     */
    @Override
    public void sendRequest(JniPingRequest request) {
        request.send(m_socket);
    }

    /** {@inheritDoc} */
    @Override
    public void start(final Queue<JniPingResponse> responseQueue) {
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
    public static JniPingResponse createPingResponse(DatagramPacket packet) {
        // Check the packet length
        //
        if (packet.getData().length != ICMPEchoPacket.getNetworkSize()) {
            throw new IllegalArgumentException("The packet is not the correct network size");
        }
    
        // Construct a new packet
        //
        ICMPEchoPacket pkt = new ICMPEchoPacket(packet.getData());
        if (pkt.getReceivedTime() == 0)
            pkt.setReceivedTime();
    
        // Construct and return the new reply
        //
        return new JniPingResponse(packet.getAddress(), pkt);
    }
}
