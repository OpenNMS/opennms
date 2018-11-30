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

import org.opennms.netmgt.topologies.service.api.OnmsTopologyMessage.TopologyMessageStatus;

public class OnmsTopologyException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -6913989384724814658L;

    OnmsTopologyRef m_ref;
    String m_protocol;
    TopologyMessageStatus m_messageStatus;

    public OnmsTopologyException(String message) {
        super(message);
    }

    public OnmsTopologyException(String message,Throwable throwable) {
        super(message, throwable);
    }

    public OnmsTopologyException(String message, String protocol) {
        super(message);
        m_protocol =protocol;
    }

    public OnmsTopologyException(String message,String protocol, Throwable throwable) {
        super(message, throwable);
        m_protocol=protocol;
    }

    public OnmsTopologyException(String message, String protocol, TopologyMessageStatus status) {
        super(message);
        m_protocol =protocol;
        m_messageStatus=status;
    }

    public OnmsTopologyException(String message, OnmsTopologyRef ref, String protocol) {
        super(message);
        m_ref=ref;
        m_protocol =protocol;
    }

    public OnmsTopologyException(String message,OnmsTopologyRef ref, String protocol, Throwable throwable) {
        super(message, throwable);
        m_ref=ref;
        m_protocol=protocol;
    }

    public OnmsTopologyException(String message, OnmsTopologyRef ref, String protocol, TopologyMessageStatus status) {
        super(message);
        m_ref=ref;
        m_protocol =protocol;
        m_messageStatus=status;
    }

    public OnmsTopologyException(String message, OnmsTopologyRef ref, TopologyMessageStatus status) {
        super(message);
        m_ref=ref;
        m_messageStatus=status;
    }

    public OnmsTopologyException(String message,OnmsTopologyRef ref, String protocol, TopologyMessageStatus status,Throwable throwable) {
        super(message, throwable);
        m_ref=ref;
        m_protocol=protocol;
        m_messageStatus=status;
    }

    public OnmsTopologyException(String string,
            TopologyMessageStatus messagestatus) {
        m_messageStatus = messagestatus;
    }

    public String getId() {
        if (m_ref == null) {
            return "no ref associated to this";
        }
        return m_ref.getId();
    }
    
    public String getProtocol() {
        return m_protocol;
    }
    
    public TopologyMessageStatus getMessageStatus() {
        return m_messageStatus;
    }

}
