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
    private final Integer protocol;
    private final String srcIp;
    private final String dstIp;
    private final Integer srcPort;
    private final Integer dstPort;

    public ConversationKey(String location, Integer protocol, String srcIp, Integer srcPort, String dstIp, Integer dstPort) {
        this.location = location;
        this.protocol = protocol;
        this.srcIp = srcIp;
        this.srcPort = srcPort;
        this.dstIp = dstIp;
        this.dstPort = dstPort;
    }

    public String getLocation() {
        return location;
    }

    public Integer getProtocol() {
        return protocol;
    }

    public String getSrcIp() {
        return srcIp;
    }

    public String getDstIp() {
        return dstIp;
    }

    public Integer getSrcPort() {
        return srcPort;
    }

    public Integer getDstPort() {
        return dstPort;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConversationKey that = (ConversationKey) o;
        return Objects.equals(protocol, that.protocol) &&
                Objects.equals(srcPort, that.srcPort) &&
                Objects.equals(dstPort, that.dstPort) &&
                Objects.equals(location, that.location) &&
                Objects.equals(srcIp, that.srcIp) &&
                Objects.equals(dstIp, that.dstIp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, protocol, srcIp, dstIp, srcPort, dstPort);
    }

    @Override
    public String toString() {
        return "ConversationKey{" +
                "location='" + location + '\'' +
                ", protocol=" + protocol +
                ", srcIp='" + srcIp + '\'' +
                ", dstIp='" + dstIp + '\'' +
                ", srcPort=" + srcPort +
                ", dstPort=" + dstPort +
                '}';
    }
}
