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

package org.opennms.netmgt.flows.classification.persistence;

public enum ProtocolType {
    HOPOPT(0),
    ICMP(1),
    IGMP(2),
    GGP(3),
    IP(4),
    Stream(5),
    TCP(6),
    CBT(7),
    EGP(8),
    IGP(9),
    BBN_RCC_MON(10),
    NVP2(11),
    PUP(12),
    ARGUS(13),
    EMCON(14),
    XNET(15),
    CHAOS(16),
    UDP(17),
    Multiplexing(18),
    DCN_MEAS(19),
    HMP(20),
    PRM(21),
    XNS_IDP(22),
    TRUNK_1(23),
    TRUNK_2(24),
    LEAF_1(25),
    LEAF_2(26),
    RDP(27),
    IRTP(28),
    ISO_TP4(29),

    // ...
    RESERVED(255);

    private final int number;

    ProtocolType(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public static ProtocolType createFrom(int protocol) {
        for (ProtocolType p : values()) {
            if (p.getNumber() == protocol) {
                return p;
            }
        }
        throw new IllegalArgumentException("No protocol found for " + protocol);
    }
}
