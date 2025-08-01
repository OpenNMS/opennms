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
import java.net.InetAddress;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.protocols.icmp.ICMPEchoPacket;
import org.opennms.protocols.icmp.IcmpSocket;
import org.opennms.protocols.icmp6.ICMPv6Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Ping {
	
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
                        System.out.println(pkt.getData().length
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
        short icmpId = (short) new Random().nextInt(Short.MAX_VALUE);

        ICMPv6Socket m_socket = null;

        try {
            m_socket = new ICMPv6Socket(icmpId);
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

        Ping.Stuff s = new Ping.Stuff(m_socket, icmpId);
        Thread t = new Thread(s, Ping.class.getSimpleName());
        t.start();

        for (long m_fiberId = 0; true; m_fiberId++) {
            // build a packet
            ICMPEchoPacket pingPkt = new ICMPEchoPacket(m_fiberId);
            pingPkt.setIdentity(icmpId);
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
