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
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.protocols.icmp.ICMPEchoPacket;
import org.opennms.protocols.icmp.IcmpSocket;
import org.opennms.protocols.icmp6.ICMPv6Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ping {
	
	private static final Logger LOG = LoggerFactory.getLogger(Ping.class);


    public static class Stuff implements Runnable {
        private ICMPv6Socket m_socket;
        private short m_icmpId;

        public Stuff(ICMPv6Socket socket, short icmpId) {
            m_socket = socket;
            m_icmpId = icmpId;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    DatagramPacket pkt = m_socket.receive();
                    Jni6PingResponse reply;
                    try {
                        reply = Jni6IcmpMessenger.createPingResponse(pkt);
                    } catch (Throwable t) {
                        // do nothing but skip this packet
                        continue;
                    }

                    if (reply.isEchoReply()
                            && reply.getThreadId() == m_icmpId) {
                        double rtt = reply.elapsedTime(TimeUnit.MILLISECONDS);
                        System.out.println(ICMPEchoPacket.getNetworkSize()
                                + " bytes from "
                                + InetAddressUtils.str(pkt.getAddress())
                                + ": icmp_seq="
                                + reply.getIdentifier()
                                + ". time="
                                + rtt + " ms");
                    }
                }
            } catch (final Throwable t) {
                LOG.error("An exception occured processing the datagram, thread exiting.", t);
                System.exit(1);
            }
        }
    }

    /**
     * <p>main</p>
     *
     * @param argv an array of {@link java.lang.String} objects.
     */
    public static void main(String[] argv) {
        if (argv.length != 1) {
            System.err.println("incorrect number of command-line arguments.");
            System.err.println("usage: java -cp ... "
                    + IcmpSocket.class.getName() + " <host>");
            System.exit(1);
        }

        String host = argv[0];

        ICMPv6Socket m_socket = null;

        try {
            m_socket = new ICMPv6Socket();
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
            addr = InetAddress.getByName(host);
        } catch (java.net.UnknownHostException e) {
            System.err.println("UnknownHostException when looking up "
                    + host + ".");
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("PING " + host + " (" + InetAddressUtils.str(addr) + "): 56 data bytes");

        short m_icmpId = 2;

        Ping.Stuff s = new Ping.Stuff(m_socket, m_icmpId);
        Thread t = new Thread(s, Ping.class.getSimpleName());
        t.start();

        for (long m_fiberId = 0; true; m_fiberId++) {
            // build a packet
            ICMPEchoPacket pingPkt = new ICMPEchoPacket(m_fiberId);
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
