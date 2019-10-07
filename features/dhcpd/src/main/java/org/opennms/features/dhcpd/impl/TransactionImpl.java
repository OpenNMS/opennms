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

import java.net.InetAddress;
import java.util.Set;
import org.dhcp4java.DHCPConstants;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.features.dhcpd.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Sets;

public class TransactionImpl implements Transaction {
    private static final Logger LOG = LoggerFactory.getLogger(TransactionImpl.class);
    private static final Set<Byte> EXTENDED_MODE_ACCEPTED_RESPONSES = Sets.newHashSet(DHCPConstants.DHCPOFFER, DHCPConstants.DHCPACK, DHCPConstants.DHCPNAK);
    private static final int MAC_ADDRESS_LENGTH = 6;
    private static final byte[] DEFAULT_MAC_ADDRESS = {(byte) 0x00,
            (byte) 0x06, (byte) 0x0d, (byte) 0xbe, (byte) 0x9c, (byte) 0xb2,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};

    private final InetAddress hostAddress;
    private final byte[] macAddress;
    private final InetAddress requestIpAddress;
    private final boolean extendedMode;
    private final boolean relayMode;
    private final InetAddress myIpAddress;
    private final int timeout;

    private int xid;
    private boolean success = false;
    private Response response;
    private long startTime = -1;
    private long endTime = -1;

    public TransactionImpl(final String hostAddress, final String macAddress, final boolean relayMode, final String myIpAddress, final boolean extendedMode, final String requestIpAddress, final int timeout) {
        this.hostAddress = InetAddressUtils.addr(hostAddress);
        this.macAddress = parseMacAddress(macAddress);
        this.requestIpAddress = InetAddressUtils.addr(requestIpAddress);
        this.relayMode = relayMode;
        this.myIpAddress = InetAddressUtils.addr(myIpAddress != null ? myIpAddress : "0.0.0.0");
        this.extendedMode = extendedMode;
        this.timeout = timeout;
    }

    public int getTimeout() {
        return this.timeout;
    }

    public boolean check(Response response) {
        if (isExtendedMode()) {
            this.success = (response.getDhcpPacket().getXid() == this.xid) && (EXTENDED_MODE_ACCEPTED_RESPONSES.contains(response.getDhcpPacket().getDHCPMessageType()));
        } else {
            this.success = (response.getDhcpPacket().getXid() == this.xid) && (response.getDhcpPacket().getDHCPMessageType() == DHCPConstants.DHCPOFFER);
        }

        if (this.success) {
            this.response = response;
            this.endTime = System.currentTimeMillis();
        }

        return this.success;
    }

    public void updateStartTime() {
        if (!this.isSuccess()) {
            this.startTime = System.currentTimeMillis();
        }
    }

    @Override
    public long getResponseTime() {
        if (isSuccess()) {
            return this.endTime - this.startTime;
        } else {
            return -1;
        }
    }

    public Response getResponse() {
        return this.response;
    }

    @Override
    public boolean isSuccess() {
        return this.success;
    }

    public InetAddress getHostAddress() {
        return this.hostAddress;
    }

    public byte[] getMacAddress() {
        return this.macAddress;
    }

    public InetAddress getRequestIpAddress() {
        return this.requestIpAddress;
    }

    public boolean isExtendedMode() {
        return this.extendedMode;
    }

    public boolean isRelayMode() {
        return this.relayMode;
    }

    private byte[] parseMacAddress(final String macAddressString) {
        byte[] macAddressBytes = DEFAULT_MAC_ADDRESS;
        final String[] elements = macAddressString.split(":");

        if (elements.length != MAC_ADDRESS_LENGTH) {
            LOG.debug("Invalid format for MAC address {}", macAddressString);
        } else {
            for (int i = 0; i < MAC_ADDRESS_LENGTH; i++) {
                try {
                    macAddressBytes[i] = (byte) Integer.parseInt(elements[i], 16);
                } catch (NumberFormatException e) {
                    LOG.debug("Error parsing octet {} MAC address {}", i, e);
                }
            }
        }
        return macAddressBytes;
    }

    public int getXid() {
        return this.xid;
    }

    public void setXid(final int xid) {
        this.xid = xid;
    }

    public InetAddress getMyIpAddress() {
        return this.myIpAddress;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("hostAddress", this.hostAddress)
                .add("macAddress", this.macAddress)
                .add("requestIpAddress", this.requestIpAddress)
                .add("extendedMode", this.extendedMode)
                .add("relayMode", this.relayMode)
                .add("myIpAddress", this.myIpAddress)
                .add("timeout", this.timeout)
                .add("xid", this.xid)
                .add("success", this.success)
                .add("response", this.response)
                .toString();
    }
}
