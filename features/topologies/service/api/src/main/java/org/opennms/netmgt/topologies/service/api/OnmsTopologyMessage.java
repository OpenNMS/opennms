/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.topologies.service.api;

import java.util.Objects;

public class OnmsTopologyMessage {

    public static OnmsTopologyMessage update(OnmsTopologyRef messagebody, OnmsTopologyProtocol protocol) {
        Objects.requireNonNull(messagebody);
        Objects.requireNonNull(protocol);
        return new OnmsTopologyMessage(messagebody, protocol, TopologyMessageStatus.UPDATE);
    }

    public static OnmsTopologyMessage delete(OnmsTopologyRef messagebody, OnmsTopologyProtocol protocol) {
        Objects.requireNonNull(messagebody);
        Objects.requireNonNull(protocol);
        return new OnmsTopologyMessage(messagebody, protocol, TopologyMessageStatus.DELETE);
    }

    public enum TopologyMessageStatus {
        UPDATE,
        DELETE
    }

    private final OnmsTopologyRef m_messagebody;
    private final TopologyMessageStatus m_messagestatus;
    private final OnmsTopologyProtocol m_protocol;
    
    private <T extends OnmsTopologyRef>OnmsTopologyMessage(T messagebody, OnmsTopologyProtocol protocol, TopologyMessageStatus messagestatus) {
        m_messagebody=messagebody;
        m_messagestatus=messagestatus;
        m_protocol = protocol;
    }

    public OnmsTopologyRef getMessagebody() {
        return m_messagebody;
    }

    public TopologyMessageStatus getMessagestatus() {
        return m_messagestatus;
    }

    public OnmsTopologyProtocol getProtocol() {
        return m_protocol;
    }

}
