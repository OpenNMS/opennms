/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.ping;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Queue;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;
import org.opennms.protocols.icmp.IcmpSocket;
import org.opennms.protocols.rt.Messenger;

/**
 * IcmpMessenger
 *
 * @author brozow
 */
public class IcmpMessenger implements Messenger<PingRequest, PingReply> {
    
    IcmpSocket m_socket;
    
    public IcmpMessenger() throws IOException {
        m_socket = new IcmpSocket();
    }

    public IcmpSocket getIcmpSocket() {
        return m_socket;
    }
    
    private Category log() {
        return Logger.getLogger(getClass());
    }
    
    void debugf(String format, Object... args) {
        if (log().isDebugEnabled()) {
            log().debug(String.format(format, args));
        }
    }

    private void errorf(String format, Object... args) {
        log().error(String.format(format, args));
    }

    void errorf(Throwable t, String format, Object... args) {
        log().error(String.format(format, args), t);
    }

    void processPackets(Queue<PingReply> pendingReplies) {
        while (true) {
            try {
                DatagramPacket packet = getIcmpSocket().receive();
        
                PingReply reply = PingReply.create(packet);
                
                if (reply.isEchoReply() && reply.getIdentity() == PingRequest.FILTER_ID) {
                    debugf("Found an echo packet addr = %s, port = %d, length = %d, created reply %s", packet.getAddress(), packet.getPort(), packet.getLength(), reply);
                    pendingReplies.offer(reply);
                }
            } catch (IOException e) {
                errorf(e, "I/O Error occurred reading from ICMP Socket");
            } catch (IllegalArgumentException e) {
                // this is not an EchoReply so ignore it
            } catch (IndexOutOfBoundsException e) {
                // this packet is not a valid EchoReply ignore it
            } catch (Throwable t) {
                errorf(t, "Unexpect Exception processing reply packet!");
            }
            
        }
    }
    
    public void sendRequest(PingRequest request) {
        request.sendRequest(getIcmpSocket());
    }

    public void start(final Queue<PingReply> replyQueue) {
        Thread socketReader = new Thread("ICMP-Socket-Reader") {

            public void run() {
                try {
                    processPackets(replyQueue);
                } catch (Throwable t) {
                    errorf(t, "Unexpected exception on Thread %s!", this);
                }
            }
        };
        socketReader.start();
    }



}
