//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Mar 05: Changes to support response times and more platforms.
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

package org.opennms.protocols.icmp;

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.DatagramPacket;

/**
 * This class provides a bridge between the host operating system so that ICMP
 * messages may be sent and received.
 * 
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 * 
 */
public final class IcmpSocket {
    /**
     * This instance is used by the native code to save and store file
     * descriptor information about the icmp socket. This needs to be
     * constructed prior to calling the init method, preferable in the
     * constructor.
     */
    private FileDescriptor m_rawFd;

    /**
     * This method is used to open the initial operating system icmp socket. The
     * descriptor for the socket is stored in the member m_rawFd.
     * 
     * @throws java.io.IOException
     *             This is thrown if an error occurs opening the ICMP socket.
     */
    private native void initSocket() throws IOException;

    /**
     * Constructs a new socket that is able to send and receive ICMP messages.
     * The newly constructed socket will receive all ICMP messages directed at
     * the local machine. The application must be prepared to handle any and
     * discard any non-interesting ICMP messages.
     * 
     * @exception java.io.IOException
     *                This exception is thrown if the socket fails to be opened
     *                correctly.
     */
    public IcmpSocket() throws IOException {
	String property = System.getProperty("opennms.library.jicmp");
	if (property != null) {
	    System.load(property);
	} else {
	    System.loadLibrary("jicmp");
	}

        m_rawFd = new FileDescriptor();
        initSocket();
    }

    /**
     * This method is used to receive the next ICMP datagram from the operating
     * system. The returned datagram packet's address is set to the sending
     * host's address. The port number is always set to Zero, and the buffer is
     * set to the contents of the raw ICMP message.
     * 
     * @exception java.io.IOException
     *                Thrown if an error occurs reading the next ICMP message.
     * 
     */
    public final native DatagramPacket receive() throws IOException;

    /**
     * This method is used to send the passed datagram using the ICMP transport.
     * The destination of the datagram packet is used as the send to destination
     * for the underlying ICMP socket. The port number of the datagram packet is
     * ignored completely.
     * 
     * @exception java.io.IOException
     *                Thrown if an error occurs sending the datagram to the
     *                remote host.
     * @exception java.net.NoRouteToHostException
     *                Thrown if the destination address is a broadcast address.
     */
    public final native void send(DatagramPacket packet) throws IOException;

    /**
     * This method is used to close and release the resources assocated with the
     * instance. The file descriptor is closed at the operating system level and
     * any subsequent calls to this instance should result in exceptions being
     * generated.
     */
    public final native void close();

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

        Stuff s = new Stuff(m_socket, m_icmpId);
        Thread t = new Thread(s);
        t.start();

        for (long m_fiberId = 0; true; m_fiberId++) {
    	    // build a packet
            org.opennms.netmgt.ping.Packet pingPkt =
                new org.opennms.netmgt.ping.Packet(m_fiberId);
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
                Thread.currentThread().sleep(1000);
            } catch (InterruptedException e) {
                // do nothing
            }
        }
    }

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
                    org.opennms.netmgt.ping.Reply reply;
                    try {
                        reply = org.opennms.netmgt.ping.Reply.create(pkt);
                    } catch (Throwable t) {
                        // do nothing but skip this packet
                        continue;
                    }
            
                    if (reply.isEchoReply()
                        && reply.getIdentity() == m_icmpId) {
                        float rtt = ((float) reply.getPacket().getPingRTT())
                                    / 1000;
                        System.out.println(reply.getPacket().getNetworkSize()
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
}
