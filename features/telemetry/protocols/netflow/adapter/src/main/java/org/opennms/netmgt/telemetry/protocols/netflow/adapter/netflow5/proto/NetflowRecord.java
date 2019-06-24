/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.netflow.adapter.netflow5.proto;

import java.nio.ByteBuffer;

public class NetflowRecord {

    private final String srcAddr;
    private final String dstAddr;
    private final String nextHop;
    private final int input;
    private final int output;
    private final long dPkts;
    private final long dOctets;
    private final long first;
    private final long last;
    private final int srcPort;
    private final int dstPort;
    private final short tcpFlags;
    private final short prot;
    private final int srcAs;
    private final int dstAs;
    private final short toS;
    private final short srcMask;
    private final short dstMask;
    private final boolean egress;

    public NetflowRecord(ByteBuffer data, int offset) {
        // src_addr: Source IP ADdress
        this.srcAddr = Utils.getInetAddress(0, 3, data, offset);

        // dst_addr: Destination IP address
        this.dstAddr = Utils.getInetAddress(4, 7, data, offset);

        // next_hop: IP address of next hop router
        this.nextHop = Utils.getInetAddress(8, 11, data, offset);

        // SNMP index of input interface
        this.input = Utils.getInt(12, 13, data, offset);

        // SNMP index of output interface
        this.output = Utils.getInt(14, 15, data, offset);

        // Packets in the flow
        this.dPkts = Utils.getLong(16, 19, data, offset);

        // Total number of Layer 3 bytes in the packets of the flow
        this.dOctets = Utils.getLong(20, 23, data, offset);

        // SysUptime at start of flow
        this.first = Utils.getLong(24, 27, data, offset);

        // SysUptime at the time the last packet of the flow was received
        this.last = Utils.getLong(28, 31, data, offset);

        // TCP/UDP source port number or equivalent
        this.srcPort = Utils.getInt(32, 33, data, offset);

        // TCP/UDP destination port number or equivalent
        this.dstPort = Utils.getInt(34, 35, data, offset);

        // Cumulative OR of TCP falgs
        this.tcpFlags = Utils.getShort(37, 37, data, offset);

        // IP protocol type (e.g. TCP = 6; UDP = 17)
        this.prot = Utils.getShort(38, 38, data, offset);

        // IP type of service (ToS)
        this.toS = Utils.getShort(39, 39, data, offset);

        // Autonomous system number of the source, either origin or peer
        this.srcAs = Utils.getInt(40, 41, data, offset);

        // Autonomous system number of the destination, either origin or peer
        this.dstAs = Utils.getInt(42, 43, data, offset);

        // Source address prefix mask bits
        this.srcMask = Utils.getShort(44, 44, data, offset);

        // Destination address prefix mask bits
        this.dstMask = Utils.getShort(45, 45, data, offset);

        // 2nd bit of padding is set to 0x08 when this is an egress flow
        this.egress = Utils.getShort(47, 47, data, offset) == 0x08;
    }

    public String getSrcAddr() {
        return srcAddr;
    }

    public String getDstAddr() {
        return dstAddr;
    }

    public String getNextHop() {
        return nextHop;
    }

    public int getInput() {
        return input;
    }

    public int getOutput() {
        return output;
    }

    public long getDPkts() {
        return dPkts;
    }

    public long getDOctets() {
        return dOctets;
    }

    public long getFirst() {
        return first;
    }

    public long getLast() {
        return last;
    }

    public int getSrcPort() {
        return srcPort;
    }

    public int getDstPort() {
        return dstPort;
    }

    public int getTcpFlags() {
        return tcpFlags;
    }

    public int getProt() {
        return prot;
    }

    public int getSrcAs() {
        return srcAs;
    }

    public int getDstAs() {
        return dstAs;
    }

    public int getToS() {
        return toS;
    }

    public int getSrcMask() {
        return srcMask;
    }

    public int getDstMask() {
        return dstMask;
    }

    public boolean isEgress() {
        return egress;
    }
}
