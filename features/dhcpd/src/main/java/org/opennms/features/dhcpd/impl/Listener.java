/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.dhcpd.impl;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

import org.dhcp4java.DHCPPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Listener implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(Listener.class);
    private boolean shutdown = false;

    private final int port;
    private final DhcpdImpl dhcpd;
    private DatagramSocket datagramSocket;
    private Thread thread;

    public Listener(final DhcpdImpl dhcpd, int port) {
        this.dhcpd = dhcpd;
        this.port = port;
    }

    public void send(DatagramPacket message) throws IOException {
        this.datagramSocket.send(message);
    }

    public synchronized boolean start() {
        if (shutdown) {
            return false;
        }

        if (thread != null && thread.isAlive()) {
            return true;
        }

        try {
            this.datagramSocket = new DatagramSocket(this.port);
            this.datagramSocket.setBroadcast(true);
            this.datagramSocket.setSoTimeout(1000);
        } catch (Exception e) {
            LOG.error("Error opening datagram socket for port {}, DHCP Monitor will not work", this.port);
            return false;
        }

        this.thread = new Thread(this);
        this.thread.start();

        return true;
    }

    public synchronized void stop() {
        this.shutdown = true;

        if (this.thread != null && this.thread.isAlive()) {
            this.thread.interrupt();
        }
    }

    @Override
    public void run() {
        byte[] dgbuf = new byte[2048];

        while (!this.shutdown) {
            try {
                final DatagramPacket pkt = new DatagramPacket(dgbuf, dgbuf.length);
                this.datagramSocket.receive(pkt);
                final Response response = new Response(pkt.getAddress(), DHCPPacket.getPacket(pkt));

                LOG.debug("Received DHCP response on port {}, xid {}, type {}, address {}", this.port, response.getDhcpPacket().getXid(), response.getDhcpPacket().getDHCPMessageType(), response.getDhcpPacket().getAddress());

                dhcpd.checkTransactions(response);
            } catch (SocketTimeoutException e) {
                // ignore
            } catch (ArrayIndexOutOfBoundsException e) {
                LOG.warn("An error occurred when reading DHCP response", e);
            } catch (IOException e) {
                LOG.warn("Failed to read message, I/O error", e);
            } catch (Throwable t) {
                LOG.warn("Undeclared throwable caught", t);
            }
        }
    }
}
