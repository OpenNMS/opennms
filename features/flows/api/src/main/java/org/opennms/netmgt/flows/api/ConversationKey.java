/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.api;

import java.util.Objects;

/**
 * Contains all of the fields used to uniquely identify a conversation.
 */
public class ConversationKey {

    private final String location;
    private final int protocol;
    private final String srcIp;
    private final String dstIp;
    private final int srcPort;
    private final int dstPort;

    public ConversationKey(String location, int protocol, String srcIp, int srcPort, String dstIp, int dstPort) {
        this.location = location;
        this.protocol = protocol;
        this.srcIp = Objects.requireNonNull(srcIp);
        this.srcPort = srcPort;
        this.dstIp = Objects.requireNonNull(dstIp);
        this.dstPort = dstPort;
    }

    public String getLocation() {
        return location;
    }

    public int getProtocol() {
        return protocol;
    }

    public String getSrcIp() {
        return srcIp;
    }

    public String getDstIp() {
        return dstIp;
    }

    public int getSrcPort() {
        return srcPort;
    }

    public int getDstPort() {
        return dstPort;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConversationKey that = (ConversationKey) o;
        return protocol == that.protocol &&
                srcPort == that.srcPort &&
                dstPort == that.dstPort &&
                Objects.equals(location, that.location) &&
                Objects.equals(srcIp, that.srcIp) &&
                Objects.equals(dstIp, that.dstIp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, protocol, srcIp, dstIp, srcPort, dstPort);
    }
}
