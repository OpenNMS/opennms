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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Iterator;
import java.util.List;

import org.opennms.core.fiber.Fiber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.bucknell.net.JDHCP.DHCPMessage;

final class Receiver implements Runnable, Fiber {
	
	private static final Logger LOG = LoggerFactory.getLogger(Receiver.class);

    private final static short DHCP_TARGET_PORT = 68;

    private DatagramSocket m_receiver;

    private String m_name;

    private int m_status;

    private Thread m_worker;

    private List<Client> m_clients;

    Receiver(List<Client> clients) throws IOException {
        m_name = "DHCPReceiver";
        m_worker = null;
        m_status = START_PENDING;
        m_receiver = new DatagramSocket(DHCP_TARGET_PORT);
        m_receiver.setSoTimeout(1000);
        m_clients = clients;
    }

    /**
     * <p>start</p>
     */
    @Override
    public synchronized void start() {
        if (m_worker != null)
            throw new IllegalStateException("The fiber has already been started");

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
        m_receiver.close();
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

        // set the state
        //
        synchronized (this) {
            m_status = RUNNING;
        }

        byte[] dgbuf = new byte[2048];

        // Roundy, Roundy, Round we go...
        //
        for (;;) {
            try {
                DatagramPacket pkt = new DatagramPacket(dgbuf, dgbuf.length);
                m_receiver.receive(pkt);
                LOG.debug("got a DHCP response.");
                Message msg = new Message(pkt.getAddress(), new DHCPMessage(pkt.getData()));

                synchronized (m_clients) {
                    Iterator<Client> iter = m_clients.iterator();
                    if(!iter.hasNext()) {
                        LOG.debug("No client waiting for response.");
                    }
                    while (iter.hasNext()) {
                        Client c = iter.next();
                        if (c.getStatus() == RUNNING) {
                            try {
                                LOG.debug("sending DHCP response pkt to client {}", c.getName());
                                c.sendMessage(msg);
                            } catch (IOException ex) {
                                LOG.warn("Error sending response to client {}", c.getName());
                            }
                        } else if (c.getStatus() == STOPPED) {
                            LOG.debug("Removing stale client {}", c.getName());
                            iter.remove();
                        }
                    }
                }

            } catch (InterruptedIOException ex) {
                // ignore
            } catch (ArrayIndexOutOfBoundsException ex) {
                LOG.warn("An error occurred when reading DHCP response. Ignoring exception: ", ex);
            } catch (IOException ex) {
                synchronized (this) {
                    if (m_status == RUNNING)
                        LOG.warn("Failed to read message, I/O error", ex);
                }
                break;
            } catch (Throwable t) {
                synchronized (this) {
                    if (m_status == RUNNING)
                        LOG.warn("Undeclared throwable caught", t);
                }
                break;
            }

            synchronized (this) {
                if (m_status != RUNNING)
                    break;
            }
        }

        synchronized (this) {
            m_status = STOP_PENDING;
        }

        // close the datagram socket
        //
        m_receiver.close();

        synchronized (this) {
            m_status = STOPPED;
        }

    } // end run() method

}
