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

import static org.dhcp4java.DHCPConstants.BOOTP_REPLY_PORT;
import static org.dhcp4java.DHCPConstants.BOOTP_REQUEST_PORT;
import static org.dhcp4java.DHCPConstants.BOOTREQUEST;
import static org.dhcp4java.DHCPConstants.DHCPDISCOVER;
import static org.dhcp4java.DHCPConstants.DHCPINFORM;
import static org.dhcp4java.DHCPConstants.DHCPREQUEST;
import static org.dhcp4java.DHCPConstants.DHO_DHCP_REQUESTED_ADDRESS;
import static org.dhcp4java.DHCPConstants.HTYPE_ETHER;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.dhcp4java.DHCPOption;
import org.dhcp4java.DHCPPacket;
import org.opennms.features.dhcpd.Dhcpd;
import org.opennms.features.dhcpd.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DhcpdImpl implements Dhcpd {
    private static final Logger LOG = LoggerFactory.getLogger(DhcpdImpl.class);
    private final Set<TransactionImpl> transactions = new HashSet<>();
    private int xid = new Random().nextInt();
    private boolean shutdown = false;
    private final Listener port67Listener = new Listener(this, BOOTP_REQUEST_PORT);
    private final Listener port68Listener = new Listener(this, BOOTP_REPLY_PORT);

    public DhcpdImpl() {
    }

    private synchronized int nextXid() {
        return xid++;
    }

    private DHCPPacket createPacket(int xid, TransactionImpl transaction, byte messageType) {
        final DHCPPacket dhcpPacket = new DHCPPacket();

        dhcpPacket.setOp(BOOTREQUEST);
        dhcpPacket.setDHCPMessageType(messageType);

        dhcpPacket.setHtype(HTYPE_ETHER);
        dhcpPacket.setHlen((byte) 6);
        dhcpPacket.setChaddr(transaction.getMacAddress());
        dhcpPacket.setXid(xid);
        dhcpPacket.setSecs((short) 0);

        if (transaction.isRelayMode()) {
            dhcpPacket.setGiaddr(transaction.getMyIpAddress());
            dhcpPacket.setHops((byte) 1);
        } else {
            dhcpPacket.setHops((byte) 0);
            dhcpPacket.setFlags((short) 0x8000);
        }

        switch (dhcpPacket.getDHCPMessageType()) {
            case DHCPREQUEST: {
                dhcpPacket.setOption(DHCPOption.newOptionAsInetAddress(DHO_DHCP_REQUESTED_ADDRESS, transaction.getRequestIpAddress()));
                dhcpPacket.setCiaddr(transaction.getRequestIpAddress());
                break;
            }
            case DHCPINFORM: {
                dhcpPacket.setOption(DHCPOption.newOptionAsInetAddress(DHO_DHCP_REQUESTED_ADDRESS, transaction.getMyIpAddress()));
                dhcpPacket.setCiaddr(transaction.getMyIpAddress());
                break;
            }
        }

        return dhcpPacket;
    }

    private void executeAndWait(final TransactionImpl transaction) throws IOException {
        if (shutdown || !port67Listener.start() || !port68Listener.start()) {
            return;
        }

        final int xid = nextXid();
        transaction.setXid(xid);

        synchronized (this.transactions) {
            this.transactions.add(transaction);
        }

        final List<DHCPPacket> dhcpPackets = new ArrayList<>();

        dhcpPackets.add(createPacket(xid, transaction, DHCPDISCOVER));

        if (transaction.isExtendedMode()) {
            dhcpPackets.add(createPacket(xid, transaction, DHCPINFORM));
            dhcpPackets.add(createPacket(xid, transaction, DHCPREQUEST));
        }

        for (final DHCPPacket dhcpPacket : dhcpPackets) {
            final byte[] buf = dhcpPacket.serialize();
            final DatagramPacket discoverPacket = new DatagramPacket(buf, buf.length);
            discoverPacket.setAddress(transaction.getHostAddress());

            transaction.updateStartTime();

            if (transaction.isRelayMode()) {
                discoverPacket.setPort(BOOTP_REQUEST_PORT);
                this.port67Listener.send(discoverPacket);
            } else {
                discoverPacket.setPort(BOOTP_REQUEST_PORT);
                this.port68Listener.send(discoverPacket);
            }

            for (int i = 0; i < Math.max(0, transaction.getTimeout() / 250); i++) {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    // ignore
                }

                if (transaction.isSuccess()) {
                    synchronized (this.transactions) {
                        this.transactions.remove(transaction);
                    }
                    return;
                }
            }
        }

        synchronized (this.transactions) {
            this.transactions.remove(transaction);
        }
    }

    @Override
    public Transaction executeTransaction(final String hostAddress, final String macAddress, final boolean relayMode, final String myIpAddress, final boolean extendedMode, final String requestIpAddress, final int timeout) throws IOException {
        final TransactionImpl transaction = new TransactionImpl(hostAddress, macAddress, relayMode, myIpAddress, extendedMode, requestIpAddress, timeout);
        executeAndWait(transaction);
        return transaction;
    }

    protected void checkTransactions(final Response response) {
        synchronized (this.transactions) {
            if (this.transactions.isEmpty()) {
                LOG.debug("No polling request is waiting for response.");
                return;
            }

            for (final TransactionImpl transaction : this.transactions) {
                if (transaction.check(response)) {
                    return;
                }
            }
        }
    }

    @Override
    public void shutdown() {
        this.shutdown = true;
        this.port67Listener.stop();
        this.port68Listener.stop();
        LOG.debug("Dhcpd terminated successfully.");
    }
}
