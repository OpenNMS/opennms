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

package org.opennms.netmgt.telemetry.adapters.netflow.v5;

import java.nio.ByteBuffer;

import org.opennms.netmgt.telemetry.adapters.netflow.v5.fields.InetAddressField;
import org.opennms.netmgt.telemetry.adapters.netflow.v5.fields.IntField;
import org.opennms.netmgt.telemetry.adapters.netflow.v5.fields.LongField;
import org.opennms.netmgt.telemetry.adapters.netflow.v5.fields.ShortField;

public class NetflowRecord {

    private final ByteBuffer data;
    private final int offset;

    public NetflowRecord(ByteBuffer data, int offset) {
        this.data = data;
        this.offset = offset;
    }

    // src_addr: Source IP ADdress
    public String getSrcAddr() {
        return new InetAddressField(0, 3, data, offset).getValue().toString();
    }

    // dst_addr: Destination IP address
    public String getDstAddr() {
        return new InetAddressField(4, 7, data, offset).getValue().toString();
    }

    // next_hop: IP address of next hop router
    public String getNextHop() {
        return new InetAddressField(8, 11, data, offset).getValue().toString();
    }

    // SNMP index of input interface
    public int getInput() {
        return new IntField(12, 13, data, offset).getValue();
    }

    // SNMP index of output interface
    public int getOutput() {
        return new IntField(14, 15, data, offset).getValue();
    }

    // Packets in the flow
    public long getDPkts() {
        return new LongField(16, 19, data, offset).getValue();
    }

    // Total number of Layer 3 bytes in the packets of the flow
    public long getDOctets() {
        return new LongField(20, 23, data, offset).getValue();
    }

    // SysUptime at start of flow
    public long getFirst() {
        return new LongField(24, 27, data, offset).getValue();
    }

    // SysUpdate at the time the last packet of the flow was received
    public long getLast() {
        return new LongField(28, 31, data, offset).getValue();
    }

    // TCP/UDP source port number or equivalent
    public int getSrcPort() {
        return new IntField(32, 33, data, offset).getValue();
    }

    // TCP/UDP destination port number or equivalent
    public int getDstPort() {
        return new IntField(34, 35, data, offset).getValue();
    }

    // Cumulative OR of TCP falgs
    public int getTcpFlags() {
        return new ShortField(37, 37, data, offset).getValue();
    }

    // IP protocol type (e.g. TCP = 6; UDP = 17)
    public int getProt() {
        return new ShortField(38, 38, data, offset).getValue();
    }

    // IP type of service (ToS)
    public int getTOS() {
        return new ShortField(39, 39, data, offset).getValue();
    }

    // Autonomous system number of the source, either origin or peer
    public int getSrcAs() {
        return new IntField(40, 41, data, offset).getValue();
    }

    // Autonomous system number of the destination, either origin or peer
    public int getDstAs() {
        return new IntField(42, 43, data, offset).getValue();
    }

    // Source address prefix mask bits
    public int getSrcMask() {
        return new ShortField(44, 44, data, offset).getValue();
    }


    // Destination address prefix mask bits
    public int getDstMask() {
        return new ShortField(45, 45, data, offset).getValue();
    }

}
