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

package org.opennms.netmgt.dhcpd;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Observable;

import org.opennms.core.fiber.Fiber;
import org.opennms.core.utils.InetAddressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.bucknell.net.JDHCP.DHCPMessage;

final class Client extends Observable implements Runnable, Fiber {
	
	private static final Logger LOG = LoggerFactory.getLogger(Client.class);

    private final static short DHCP_TARGET_PORT = 67;

    private static InetAddress NULL_ADDR;

    private DatagramSocket m_sender;

    private Socket m_client;

    private ObjectOutputStream m_objsOut;

    private String m_name;

    private int m_status;

    private Thread m_worker;

    private UnicastListener m_unicastListener;

    private boolean m_keepListening;

    static {
    	NULL_ADDR = InetAddressUtils.addr("0.0.0.0");
    }

    /**
     * The remote DHCP server we sent the request to has the option of either
     * unicasting the response directly back to us or broadcasting the response
     * to port 68 on the local subnet. The Receiver class handles the broadcast
     * scenario and this class will take care of the unicast scenario.
     */
    public final class UnicastListener extends Thread {
        /**
         * Udp connection over which unicast DHCP response will be received.
         */
        DatagramSocket m_incomingUdp;

        /**
         * Client where any received responses will be forwarded.
         */
        Client m_client;

        /**
         * Constructor
         * 
         * @param incoming
         *            UDP socket over which the DHCP request was sent
         * @param client
         *            The client to which any unicasted responses are to be
         *            forwarded
         */
        public UnicastListener(DatagramSocket incoming, Client client) {
            super("UnicastListener-UDP-" + incoming.getLocalPort());
            LOG.debug("constructing UnicastListener-UDP-{}", incoming.getLocalPort());
            m_incomingUdp = incoming;
            m_client = client;
        }

        /**
         * Does the work of the thread. Listens for unicasted responses from the
         * DHCP server. If a response is received it will be forwarded to the
         * client which requested that the DHCP request be generated.
         */
        @Override
        public void run() {

                LOG.debug("thread {} running...", this.getName());

            // set socket timeout to 1 second so the value of m_keepListening
            // can be checked periodically
            try {
                m_incomingUdp.setSoTimeout(1000);
            } catch (IOException ioE) {
                LOG.error("UnicastListener.run: unable to set socket timeout, reason: {}", ioE.getMessage());
                m_keepListening = true;
            }

            // According to RFC 2131 a DHCP client must be prepared to receive a
            // DHCP message up to 576 bytes. Although larger messages can
            // be negotiated between the DHCP client and server if desired.
            //
            // Allocating a 2k buffer which should be more than sufficient.
            // Any incoming packet larger than this size will cause an
            // arrayOutOfBoundsException to be generated, in that case an
            // error message will be logged and the packet will be discarded.
            //
            byte[] dgbuf = new byte[2048];

            // Wait for any incoming unicast responses.
            //
            while (m_keepListening) {
                try {
                    DatagramPacket pkt = new DatagramPacket(dgbuf, dgbuf.length);
                    m_incomingUdp.receive(pkt);
                    Message msg = new Message(pkt.getAddress(), new DHCPMessage(pkt.getData()));

                    try {
                        m_client.sendMessage(msg);
                    } catch (IOException ex) {
                        LOG.warn("Error sending unicast response to client {}", m_client.getName());
                    }
                } catch (InterruptedIOException ex) {
                    // Check exit flag
                    continue;
                } catch (ArrayIndexOutOfBoundsException oobE) {
                    // Packet was too large for buffer...log and discard
                    LOG.debug("UnicastListener.run: array out of bounds exception.", oobE);
                    LOG.warn("UnicastListener.run: malformed DHCP packet, packet too large for buffer (buffer sz={}), discarding packet.", dgbuf.length);
                } catch (IOException ioE) {
                    LOG.error("UnicastListener.run: io exception receiving response", ioE);
                    m_keepListening = false;
                } catch (Throwable E) {
                    LOG.error("UnicastListener.run: exception receiving response", E);
                    m_keepListening = false;
                }
            }

                LOG.debug("thread {} exiting...", super.getName());
        }
    }

    Client(Socket clnt) throws IOException {
        m_name = "DHCPClient-TCP-" + clnt.getPort();
        m_client = clnt;
        m_worker = null;
        m_status = START_PENDING;
        m_sender = new DatagramSocket();

        // Construct UnicastListener thread which will receive the
        // DHCP response if the remote DHCP server unicasts the response
        // directly back over the outgoing Datagram Socket
        //
        LOG.debug("Client.ctor: outgoing udp socket port: {}", m_sender.getLocalPort());
        m_unicastListener = new UnicastListener(m_sender, this);
        m_keepListening = true;

        m_objsOut = new ObjectOutputStream(m_client.getOutputStream());
        m_objsOut.reset();
        m_objsOut.flush();
    }

    void sendMessage(Message msg) throws IOException {
        m_objsOut.writeObject(msg);
        m_objsOut.flush();
    }

    /**
     * <p>start</p>
     */
    @Override
    public synchronized void start() {
        if (m_worker != null)
            throw new IllegalStateException("The fiber has already been started");

        // Start UnicastListener thread.
        //
        m_unicastListener.start();

        m_worker = new Thread(this, getName());
        m_worker.setDaemon(true);
        m_worker.start();
        m_status = STARTING;
    }

    /**
     * <p>stop</p>
     */
    @Override
    public synchronized void stop() {
        m_status = STOP_PENDING;
        try {
            m_objsOut.close();
            m_client.close();
        } catch (IOException ex) {
        }

        m_sender.close();
        m_worker.interrupt();
    }

    /**
     * <p>getStatus</p>
     *
     * @return a int.
     */
    @Override
    public synchronized int getStatus() {
        return m_status;
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getName() {
        return m_name;
    }

    /**
     * <p>run</p>
     */
    @Override
    public void run() {
        boolean isOk = true;

        // get the input stream as a object stream
        //
        ObjectInputStream input = null;
        try {
            input = new ObjectInputStream(m_client.getInputStream());
        } catch (IOException ex) {
            LOG.warn("Failed to read client's input stream", ex);
            isOk = false;
        }

        // set the state
        //
        if (isOk) {
            synchronized (this) {
                m_status = RUNNING;
            }
        }

        // Roundy, Roundy, Round we go...
        //
        while (isOk && m_status == RUNNING) {
            try {
                Message msg = (Message) input.readObject();
                if (msg.getAddress().equals(NULL_ADDR)) {
                        LOG.debug("Got disconnect request from Poller corresponding to sending port {}", m_sender.getLocalPort());
                    isOk = false;
                } else {
                        LOG.debug("Got request... adress = {}", msg.getAddress());
                    byte[] dhcp = msg.getMessage().externalize();

                    DatagramPacket pkt = new DatagramPacket(dhcp, dhcp.length, msg.getAddress(), DHCP_TARGET_PORT);
                    try {
                           LOG.debug("sending request on port: {}", m_sender.getLocalPort());
                        m_sender.send(pkt);
                    } catch (IOException ex) {
                    } // discard
                }
            } catch (ClassNotFoundException ex) {
                LOG.warn("Failed to read message, no class found", ex);
                isOk = false;
            } catch (IOException ex) {
                LOG.warn("Failed to read message, I/O error", ex);
                isOk = false;
            } catch (ClassCastException ex) {
                LOG.warn("Failed to read an appropriate message", ex);
                isOk = false;
            } catch (Throwable t) {
                LOG.warn("Undeclared throwable caught", t);
                isOk = false;
            }
        }

        synchronized (this) {
            m_status = STOP_PENDING;
        }

        // stop the unicast listener thread and wait for it to exit
        //
        m_keepListening = false;
            LOG.debug("run: waiting for UnicastListener thread {} to die...", this.getName());
        try {
            m_unicastListener.join();
        } catch (InterruptedException e) {
            LOG.debug("run: interrupted while waiting for UnicastListener thread {} to die", this.getName(), e);
        }
            LOG.debug("run: UnicastListener thread {} is dead...", this.getName());

        // close the datagram socket
        //
        m_sender.close();

        // close the client's socket
        //
        try {
            input.close();
            m_client.close();
        } catch (IOException e) {
        }

        // Notify
        //
        notifyObservers();

        synchronized (this) {
            m_status = STOPPED;
        }

    } // end run() method

}
