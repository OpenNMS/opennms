/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.topologies.service.api.OnmsTopology;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyConsumer;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyException;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyMessage;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyProtocol;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyDao;

public class OnmsTopologyDaoInMemoryImpl implements OnmsTopologyDao {

    Logger LOG = LoggerFactory.getLogger(OnmsTopologyDaoInMemoryImpl.class);
    private Map<OnmsTopologyProtocol,OnmsTopologyUpdater> m_updatersMap = new HashMap<OnmsTopologyProtocol, OnmsTopologyUpdater>();
    Set<OnmsTopologyConsumer> m_consumers = new HashSet<OnmsTopologyConsumer>();

    @Override
    public OnmsTopology getTopology(String protocolSupported) throws OnmsTopologyException {
        OnmsTopologyProtocol protocol = OnmsTopologyProtocol.create(protocolSupported);
        if (m_updatersMap.containsKey(protocol)) {
            return m_updatersMap.get(protocol).getTopology();
        }
        throw new OnmsTopologyException(String.format("%s protocol not supported",protocolSupported));
    }

    @Override
    public void subscribe(OnmsTopologyConsumer consumer) {
        synchronized (m_consumers) {
            m_consumers.add(consumer);            
        }
    }

    @Override
    public void unsubscribe(OnmsTopologyConsumer consumer) {
        synchronized (m_consumers) {
            m_consumers.remove(consumer);
        }
    }

    @Override
    public void register(OnmsTopologyUpdater updater) throws OnmsTopologyException {
        synchronized (m_updatersMap) {
            if (m_updatersMap.containsKey(updater.getProtocol())) {
                throw new OnmsTopologyException("Protocol already registered", updater.getProtocol());
            }
            m_updatersMap.put(updater.getProtocol(), updater);
        }
    }

    @Override
    public void unregister(OnmsTopologyUpdater updater) throws OnmsTopologyException {
        synchronized (m_updatersMap) {
            OnmsTopologyUpdater subscribed =  m_updatersMap.get(updater.getProtocol());
            if (subscribed == null) {
                throw new OnmsTopologyException("updater is not registered", updater.getProtocol());
            }
            if (subscribed == updater) {
                m_updatersMap.remove(updater.getProtocol());
            } else {
                throw new OnmsTopologyException("updater is not registered", updater.getProtocol());                
            }
        }
    }

    @Override
    public Set<String> getSupportedProtocols() {
        final Set<String> protocols = new HashSet<String>();
        synchronized (m_updatersMap) {
            m_updatersMap.keySet().stream().forEach(p -> protocols.add(p.getId()));
        }
        return protocols;
    }

    @Override
    public void update(OnmsTopologyUpdater updater,
            OnmsTopologyMessage message) throws OnmsTopologyException {
        final OnmsTopologyProtocol protocol = updater.getProtocol();
        if (!m_updatersMap.containsKey(protocol)) {
            throw new OnmsTopologyException(String.format("cannot update message with id: %s. Protocol not supported",
                                                          message.getMessagebody().getId()),
                                            protocol,
                                            message.getMessagestatus());
        }
        if (m_updatersMap.get(protocol) != updater) {
            throw new OnmsTopologyException(String.format("cannot update message with id: %s. Updater not registered",
                                                          message.getMessagebody().getId()),
                                            protocol,
                                            message.getMessagestatus());
        }
        synchronized (m_consumers) {
            m_consumers
                .stream()
                .filter(consumer ->  consumer.getProtocols().contains(protocol))
                .forEach(consumer -> consumer.consume(message));            
        }
    }
}
