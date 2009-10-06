//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2007 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Jul 25: Move code here from IcmpSocket as it depends on 
//              OpenNMS specific code
// 2007 Jun 23: Use Java 5 generics, format code. - dj@opennms.org
// 2003 Mar 05: Cleaned up some ICMP related code.
// 2002 Nov 13: Added response time stats for ICMP requests.
// 
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8
//
// 
//

package org.opennms.netmgt.ping;

import java.io.IOException;
import java.net.DatagramPacket;

import org.opennms.protocols.icmp.ICMPEchoPacket;
import org.opennms.protocols.icmp.IcmpSocket;

public class Ping {

    public static class Stuff implements Runnable {
        private IcmpSocket m_socket;
    private short m_icmpId;
    
        public Stuff(IcmpSocket socket, short icmpId) {
            m_socket = socket;
            m_icmpId = icmpId;
        }
    
        public void run() {
            try {
                while (true) {
                    DatagramPacket pkt = m_socket.receive();
                    org.opennms.netmgt.ping.PingReply reply;
                    try {
                        reply = org.opennms.netmgt.ping.PingReply.create(pkt);
                    } catch (Throwable t) {
                        // do nothing but skip this packet
                        continue;
                    }
            
                    if (reply.isEchoReply()
                        && reply.getIdentity() == m_icmpId) {
                        float rtt = ((float) reply.getPacket().getPingRTT())
                                    / 1000;
                        System.out.println(ICMPEchoPacket.getNetworkSize()
                                           + " bytes from "
                                           + pkt.getAddress().getHostAddress()
                                           + ": icmp_seq="
                                           + reply.getPacket().getTID()
                                           + ". time="
                                           + rtt + " ms");
                    }
                }
            } catch (Throwable t) {
                System.err.println("An exception occured processing the "
                                   + "datagram, thread exiting.");
                t.printStackTrace();
                System.exit(1);
            }
        }
    }

    public static void main(String[] argv) {
    if (argv.length != 1) {
            System.err.println("incorrect number of command-line arguments.");
            System.err.println("usage: java -cp ... "
                               + IcmpSocket.class.getName() + " <host>");
            System.exit(1);
        }
    
        String host = argv[0];
    
        IcmpSocket m_socket = null;
    
        try {
            m_socket = new IcmpSocket();
    } catch (UnsatisfiedLinkError e) {
            System.err.println("UnsatisfiedLinkError while creating an "
                               + "IcmpSocket.  Most likely failed to load "
                               + "libjicmp.so.  Try setting the property "
                               + "'opennms.library.jicmp' to point at the "
                               + "full path name of the libjicmp.so shared "
                               + "library "
                               + "(e.g. 'java -Dopennms.library.jicmp=/some/path/libjicmp.so ...')");
            e.printStackTrace();
            System.exit(1);
    } catch (NoClassDefFoundError e) {
            System.err.println("NoClassDefFoundError while creating an "
                               + "IcmpSocket.  Most likely failed to load "
                               + "libjicmp.so.");
            e.printStackTrace();
            System.exit(1);
    } catch (IOException e) {
            System.err.println("IOException while creating an "
                               + "IcmpSocket.");
            e.printStackTrace();
            System.exit(1);
        }
    
    java.net.InetAddress addr = null;
        try {
        addr = java.net.InetAddress.getByName(host);
        } catch (java.net.UnknownHostException e) {
            System.err.println("UnknownHostException when looking up "
                               + host + ".");
            e.printStackTrace();
            System.exit(1);
        }
    
        System.out.println("PING " + host + " (" + addr.getHostAddress()
                           + "): 56 data bytes");
    
    short m_icmpId = 2;
    
        Ping.Stuff s = new Ping.Stuff(m_socket, m_icmpId);
        Thread t = new Thread(s);
        t.start();
    
        for (long m_fiberId = 0; true; m_fiberId++) {
    	    // build a packet
            org.opennms.protocols.icmp.ICMPEchoPacket pingPkt =
                new org.opennms.protocols.icmp.ICMPEchoPacket(m_fiberId);
            pingPkt.setIdentity(m_icmpId);
            pingPkt.computeChecksum();
    
            // convert it to a datagram to be sent
            byte[] buf = pingPkt.toBytes();
            DatagramPacket sendPkt =
                new DatagramPacket(buf, buf.length, addr, 0);
            buf = null;
            pingPkt = null;
    
            try {
                m_socket.send(sendPkt);
            } catch (IOException e) {
                System.err.println("IOException received when sending packet.");
                e.printStackTrace();
                System.exit(1);
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // do nothing
            }
        }
    }

}
