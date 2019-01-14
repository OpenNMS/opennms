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

package org.opennms.netmgt.topologies.service.impl;

import java.util.HashSet;
import java.util.Set;

import org.opennms.netmgt.topologies.service.api.OnmsTopologyConsumer;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyEdge;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyException;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyMessage;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyPort;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyProtocol;
import org.opennms.netmgt.topologies.service.api.OnmsTopologySegment;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyVertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnmsTopologyLogger implements OnmsTopologyConsumer {
    
    private final static Logger LOG = LoggerFactory.getLogger(OnmsTopologyLogger.class);

    private final OnmsTopologyProtocol m_protocol;
    
    public OnmsTopologyLogger(String protocol) throws OnmsTopologyException {
        m_protocol =OnmsTopologyProtocol.create(protocol);
    }

    @Override
    public String getName() {
        return m_protocol.getId()+":Consumer:Logger";
    }

    @Override
    public Set<OnmsTopologyProtocol> getProtocols() {
        Set<OnmsTopologyProtocol> protocols = new HashSet<>();
        protocols.add(m_protocol);
        return protocols;
    }

    @Override
    public void consume(OnmsTopologyMessage message) {
        LOG.debug("-------Start receiving message--------");
        LOG.debug("received message type: {}" ,  message.getMessagestatus());
        LOG.debug("ref: {}",message.getMessagebody().getId());
        LOG.debug("protocol: {}",message.getProtocol().getId());
        if (message.getMessagebody() instanceof OnmsTopologyVertex) {
            LOG.debug("vertex: {}", ((OnmsTopologyVertex)message.getMessagebody()).getLabel());
        }
        if (message.getMessagebody() instanceof OnmsTopologyEdge) {
            OnmsTopologyEdge edge = (OnmsTopologyEdge)message.getMessagebody();
            OnmsTopologyPort source = edge.getSource();
            OnmsTopologyPort target = edge.getTarget();
            LOG.debug("edge: vertex {} port {}", source.getVertex().getLabel(), source.getIfname());
            LOG.debug("edge: vertex {} port {}", target.getVertex().getLabel(), target.getIfname());
        } else if (message.getMessagebody() instanceof OnmsTopologySegment) {
           OnmsTopologySegment shared = (OnmsTopologySegment) message.getMessagebody(); 
           shared.getSources().stream().forEach( p -> {
               LOG.debug("edge: vertex {} port {}", p.getVertex().getLabel(), p.getIfname());
           });
        }
        LOG.debug("-------End receiving message--------");

    }

}
